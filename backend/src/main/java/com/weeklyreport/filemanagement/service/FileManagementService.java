package com.weeklyreport.filemanagement.service;

import com.weeklyreport.filemanagement.config.MinIOConfig;
import com.weeklyreport.filemanagement.dto.FileUploadRequest;
import com.weeklyreport.filemanagement.dto.FileUploadResponse;
import com.weeklyreport.filemanagement.entity.FileAccessLog;
import com.weeklyreport.filemanagement.entity.FileAttachment;
import com.weeklyreport.filemanagement.entity.WeeklyReportAttachment;
import com.weeklyreport.filemanagement.repository.FileAccessLogRepository;
import com.weeklyreport.filemanagement.repository.FileAttachmentRepository;
import com.weeklyreport.filemanagement.repository.WeeklyReportAttachmentRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 文件管理服务
 * 处理文件上传、下载、预览等核心功能
 */
@Service
@Transactional
public class FileManagementService {

    private static final Logger logger = LoggerFactory.getLogger(FileManagementService.class);

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinIOConfig.MinIOProperties minIOProperties;

    @Autowired
    private FileAttachmentRepository fileAttachmentRepository;

    @Autowired
    private WeeklyReportAttachmentRepository weeklyReportAttachmentRepository;

    @Autowired
    private FileAccessLogRepository fileAccessLogRepository;

    /**
     * 上传文件并关联到周报
     * 采用半异步模式：文件上传同步，后处理异步
     */
    public FileUploadResponse uploadFile(MultipartFile file, FileUploadRequest request, 
                                       Long userId, HttpServletRequest httpRequest) {
        try {
            // 1. 验证文件
            validateFile(file);

            // 2. 生成文件元数据
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String storedFilename = generateStoredFilename(fileExtension);
            String filePath = generateFilePath(storedFilename, request.getAttachmentType());
            String fileHash = calculateFileHash(file.getInputStream());

            // 3. 检查文件去重
            Optional<FileAttachment> existingFile = fileAttachmentRepository.findByFileHashAndDeletedAtIsNull(fileHash);
            if (existingFile.isPresent()) {
                logger.info("发现重复文件，复用现有文件: {}", existingFile.get().getOriginalFilename());
                FileAttachment existing = existingFile.get();
                if (request.getWeeklyReportId() != null) {
                    return createWeeklyReportAttachment(existing, request, userId);
                } else {
                    return FileUploadResponse.fromFileAttachment(existing);
                }
            }

            // 4. 创建文件记录（先标记为上传中）
            FileAttachment fileAttachment = new FileAttachment(
                originalFilename, storedFilename, filePath, 
                file.getSize(), file.getContentType(), fileExtension, userId
            );
            fileAttachment.setBucketName(minIOProperties.getBucketName());
            fileAttachment.setFileHash(fileHash);
            fileAttachment.setUploadStatus(FileAttachment.UploadStatus.UPLOADING);
            fileAttachment.setIsPublic(request.getIsPublic());
            
            fileAttachment = fileAttachmentRepository.save(fileAttachment);

            // 5. 上传文件到MinIO（同步）
            uploadToMinIO(file, filePath, fileAttachment);

            // 6. 更新上传状态为完成
            fileAttachment.setUploadStatus(FileAttachment.UploadStatus.COMPLETED);
            fileAttachment.setUploadProgress(100);
            fileAttachment = fileAttachmentRepository.save(fileAttachment);

            // 7. 记录访问日志
            logFileAccess(fileAttachment.getId(), userId, FileAccessLog.AccessAction.UPLOAD, httpRequest);

            // 8. 创建周报关联（仅当提供weeklyReportId时）
            FileUploadResponse response;
            if (request.getWeeklyReportId() != null) {
                response = createWeeklyReportAttachment(fileAttachment, request, userId);
            } else {
                // 普通文件上传，不关联周报
                response = FileUploadResponse.fromFileAttachment(fileAttachment);
            }

            // 9. 异步后处理（缩略图生成等）
            asyncPostProcessFile(fileAttachment);

            logger.info("文件上传成功: {} -> {}, 周报关联: {}", originalFilename, filePath,
                       request.getWeeklyReportId() != null ? "是" : "否");
            return response;

        } catch (Exception e) {
            logger.error("文件上传失败: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 下载文件
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadFile(Long fileId, Long userId, HttpServletRequest httpRequest) {
        try {
            // 1. 获取文件信息
            FileAttachment fileAttachment = fileAttachmentRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("文件不存在: " + fileId));

            if (fileAttachment.isDeleted()) {
                throw new RuntimeException("文件已被删除");
            }

            // 2. 权限检查（简化版：只检查是否为公开文件或文件所有者）
            if (!fileAttachment.getIsPublic() && !fileAttachment.getUploadedBy().equals(userId)) {
                throw new RuntimeException("没有权限访问该文件");
            }

            // 3. 从MinIO获取文件流
            InputStream fileStream = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                    .bucket(fileAttachment.getBucketName())
                    .object(fileAttachment.getFilePath())
                    .build()
            );

            // 4. 更新下载计数（异步）
            asyncUpdateDownloadCount(fileId);

            // 5. 记录访问日志（异步）
            asyncLogFileAccess(fileId, userId, FileAccessLog.AccessAction.DOWNLOAD, httpRequest);

            // 6. 构建响应
            InputStreamResource resource = new InputStreamResource(fileStream);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + fileAttachment.getOriginalFilename() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, fileAttachment.getMimeType())
                    .header(HttpHeaders.CONTENT_LENGTH, fileAttachment.getFileSize().toString())
                    .body(resource);

        } catch (Exception e) {
            logger.error("文件下载失败: fileId={}", fileId, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件预览URL
     */
    @Transactional(readOnly = true)
    public String getFilePreviewUrl(Long fileId, Long userId) {
        try {
            FileAttachment fileAttachment = fileAttachmentRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("文件不存在: " + fileId));

            if (fileAttachment.isDeleted()) {
                throw new RuntimeException("文件已被删除");
            }

            // 权限检查
            if (!fileAttachment.getIsPublic() && !fileAttachment.getUploadedBy().equals(userId)) {
                throw new RuntimeException("没有权限访问该文件");
            }

            // 生成预签名URL（临时访问链接）
            String presignedUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(fileAttachment.getBucketName())
                    .object(fileAttachment.getFilePath())
                    .expiry(minIOProperties.getUrlExpirySeconds())
                    .build()
            );

            return presignedUrl;

        } catch (Exception e) {
            logger.error("获取文件预览URL失败: fileId={}", fileId, e);
            throw new RuntimeException("获取文件预览URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文件（软删除）
     */
    public void deleteFile(Long fileId, Long userId) {
        try {
            FileAttachment fileAttachment = fileAttachmentRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("文件不存在: " + fileId));

            // 权限检查：只有文件所有者可以删除
            if (!fileAttachment.getUploadedBy().equals(userId)) {
                throw new RuntimeException("没有权限删除该文件");
            }

            // 软删除
            fileAttachment.markAsDeleted();
            fileAttachmentRepository.save(fileAttachment);

            logger.info("文件已标记删除: fileId={}, filename={}", fileId, fileAttachment.getOriginalFilename());

        } catch (Exception e) {
            logger.error("删除文件失败: fileId={}", fileId, e);
            throw new RuntimeException("删除文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取周报的所有附件
     */
    @Transactional(readOnly = true)
    public List<FileUploadResponse> getWeeklyReportAttachments(Long weeklyReportId) {
        List<WeeklyReportAttachment> attachments = weeklyReportAttachmentRepository
                .findByWeeklyReportIdWithFileAttachment(weeklyReportId);

        return attachments.stream()
                .map(attachment -> {
                    FileUploadResponse response = FileUploadResponse
                            .fromFileAttachmentWithRelation(attachment.getFileAttachment(), attachment);
                    
                    // 设置预览URL（如果支持预览）
                    if (attachment.getFileAttachment().isImage() || 
                        "application/pdf".equals(attachment.getFileAttachment().getMimeType())) {
                        try {
                            response.setPreviewUrl(getFilePreviewUrl(attachment.getFileAttachment().getId(), null));
                        } catch (Exception e) {
                            logger.warn("生成预览URL失败: fileId={}", attachment.getFileAttachment().getId());
                        }
                    }

                    return response;
                })
                .toList();
    }

    // ================== 私有辅助方法 ==================

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        if (file.getSize() > minIOProperties.getMaxFileSize()) {
            throw new RuntimeException("文件大小超过限制: " + (minIOProperties.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(minIOProperties.getAllowedContentTypes()).contains(contentType)) {
            throw new RuntimeException("不支持的文件类型: " + contentType);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex >= 0 ? filename.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    /**
     * 生成存储文件名
     */
    private String generateStoredFilename(String fileExtension) {
        return UUID.randomUUID().toString() + (fileExtension.isEmpty() ? "" : "." + fileExtension);
    }

    /**
     * 生成文件路径
     */
    private String generateFilePath(String storedFilename, WeeklyReportAttachment.AttachmentType attachmentType) {
        String typePath = attachmentType.name().toLowerCase().replace("_", "-");
        String datePath = LocalDateTime.now().toString().substring(0, 10); // YYYY-MM-DD
        return Paths.get("weekly-reports", typePath, datePath, storedFilename).toString();
    }

    /**
     * 计算文件哈希值（用于去重）
     */
    private String calculateFileHash(InputStream inputStream) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.warn("计算文件哈希值失败", e);
            return null;
        }
    }

    /**
     * 上传文件到MinIO
     */
    private void uploadToMinIO(MultipartFile file, String filePath, FileAttachment fileAttachment) {
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minIOProperties.getBucketName())
                    .object(filePath)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
        } catch (Exception e) {
            // 上传失败，更新状态
            fileAttachment.setUploadStatus(FileAttachment.UploadStatus.FAILED);
            fileAttachment.setErrorMessage(e.getMessage());
            fileAttachmentRepository.save(fileAttachment);
            throw new RuntimeException("上传到MinIO失败", e);
        }
    }

    /**
     * 创建周报附件关联
     */
    private FileUploadResponse createWeeklyReportAttachment(FileAttachment fileAttachment, 
                                                          FileUploadRequest request, Long userId) {
        // 获取最大显示顺序
        Integer maxOrder = weeklyReportAttachmentRepository
                .getMaxDisplayOrderByWeeklyReportId(request.getWeeklyReportId());
        
        WeeklyReportAttachment reportAttachment = new WeeklyReportAttachment(
            request.getWeeklyReportId(),
            fileAttachment.getId(),
            request.getAttachmentType(),
            request.getRelatedTaskId(),
            request.getRelatedProjectId(),
            request.getRelatedPhaseId()
        );
        
        reportAttachment.setDescription(request.getDescription());
        reportAttachment.setDisplayOrder(request.getDisplayOrder() != null ? 
                                       request.getDisplayOrder() : maxOrder + 1);

        reportAttachment = weeklyReportAttachmentRepository.save(reportAttachment);

        return FileUploadResponse.fromFileAttachmentWithRelation(fileAttachment, reportAttachment);
    }

    /**
     * 记录文件访问日志
     */
    private void logFileAccess(Long fileId, Long userId, FileAccessLog.AccessAction action, 
                             HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            FileAccessLog log = new FileAccessLog(fileId, userId, action, ipAddress, userAgent);
            fileAccessLogRepository.save(log);
        } catch (Exception e) {
            logger.warn("记录文件访问日志失败: fileId={}, action={}", fileId, action, e);
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }

    // ================== 异步方法 ==================

    /**
     * 异步后处理文件（缩略图生成等）
     */
    @Async
    public void asyncPostProcessFile(FileAttachment fileAttachment) {
        try {
            // TODO: 实现缩略图生成、图片压缩等功能
            logger.info("开始异步处理文件: {}", fileAttachment.getOriginalFilename());
            
            // 示例：如果是图片，可以生成缩略图
            if (fileAttachment.isImage()) {
                // generateThumbnail(fileAttachment);
            }
            
            logger.info("文件异步处理完成: {}", fileAttachment.getOriginalFilename());
        } catch (Exception e) {
            logger.error("文件异步处理失败: {}", fileAttachment.getOriginalFilename(), e);
        }
    }

    /**
     * 异步更新下载计数
     */
    @Async
    public void asyncUpdateDownloadCount(Long fileId) {
        try {
            Optional<FileAttachment> optionalFile = fileAttachmentRepository.findById(fileId);
            if (optionalFile.isPresent()) {
                FileAttachment file = optionalFile.get();
                file.incrementDownloadCount();
                fileAttachmentRepository.save(file);
            }
        } catch (Exception e) {
            logger.warn("更新下载计数失败: fileId={}", fileId, e);
        }
    }

    /**
     * 异步记录文件访问日志
     */
    @Async
    public void asyncLogFileAccess(Long fileId, Long userId, FileAccessLog.AccessAction action, 
                                 HttpServletRequest httpRequest) {
        logFileAccess(fileId, userId, action, httpRequest);
    }
}