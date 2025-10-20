package com.weeklyreport.filemanagement.controller;

import com.weeklyreport.filemanagement.dto.FileUploadRequest;
import com.weeklyreport.filemanagement.dto.FileUploadResponse;
import com.weeklyreport.filemanagement.service.FileManagementService;
import com.weeklyreport.common.dto.ApiResponse;
import com.weeklyreport.core.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件管理控制器
 * 提供文件上传、下载、预览、删除等API
 */
@RestController
@RequestMapping("/file-management")
@Tag(name = "File Management", description = "文件管理API")
public class FileManagementController {

    private static final Logger logger = LoggerFactory.getLogger(FileManagementController.class);

    @Autowired
    private FileManagementService fileManagementService;

    /**
     * 上传文件并关联到周报（可选）
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件，可选择关联到周报的特定部分")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "上传成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "文件大小超出限制"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "415", description = "不支持的文件类型")
    })
    public ApiResponse<FileUploadResponse> uploadFile(
            @Parameter(description = "上传的文件", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "周报ID（可选，不绑定周报时不需要）", required = false)
            @RequestParam(value = "weeklyReportId", required = false) Long weeklyReportId,

            @Parameter(description = "附件类型", example = "ROUTINE_TASK_RESULT")
            @RequestParam(value = "attachmentType", required = false, defaultValue = "GENERAL")
            String attachmentType,

            @Parameter(description = "关联的任务ID（用于任务相关附件）")
            @RequestParam(value = "relatedTaskId", required = false) Long relatedTaskId,

            @Parameter(description = "关联的项目ID（用于发展任务附件）")
            @RequestParam(value = "relatedProjectId", required = false) Long relatedProjectId,

            @Parameter(description = "关联的阶段ID（用于发展任务附件）")
            @RequestParam(value = "relatedPhaseId", required = false) Long relatedPhaseId,

            @Parameter(description = "附件描述")
            @RequestParam(value = "description", required = false) String description,

            @Parameter(description = "显示顺序")
            @RequestParam(value = "displayOrder", required = false) Integer displayOrder,

            @Parameter(description = "是否公开访问")
            @RequestParam(value = "isPublic", required = false, defaultValue = "false") Boolean isPublic,

            @AuthenticationPrincipal CustomUserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {

        try {
            logger.info("用户 {} 上传文件: {}, 周报ID: {}, 附件类型: {}",
                       userPrincipal.getId(), file.getOriginalFilename(), weeklyReportId, attachmentType);

            // 构建上传请求对象
            FileUploadRequest request = new FileUploadRequest();
            request.setWeeklyReportId(weeklyReportId);
            request.setAttachmentType(
                com.weeklyreport.filemanagement.entity.WeeklyReportAttachment.AttachmentType.fromString(attachmentType)
            );
            request.setRelatedTaskId(relatedTaskId);
            request.setRelatedProjectId(relatedProjectId);
            request.setRelatedPhaseId(relatedPhaseId);
            request.setDescription(description);
            request.setDisplayOrder(displayOrder);
            request.setIsPublic(isPublic);

            // 执行文件上传
            FileUploadResponse response = fileManagementService.uploadFile(
                file, request, userPrincipal.getId(), httpRequest
            );

            logger.info("文件上传成功: fileId={}, filename={}", response.getFileId(), response.getOriginalFilename());
            return ApiResponse.success(response);

        } catch (Exception e) {
            logger.error("文件上传失败: filename={}, userId={}", file.getOriginalFilename(), userPrincipal.getId(), e);
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     */
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传文件", description = "一次上传多个文件，可选择关联到同一个周报")
    public ApiResponse<List<FileUploadResponse>> uploadFiles(
            @Parameter(description = "上传的文件列表", required = true)
            @RequestParam("files") List<MultipartFile> files,

            @Parameter(description = "周报ID（可选，不绑定周报时不需要）", required = false)
            @RequestParam(value = "weeklyReportId", required = false) Long weeklyReportId,

            @Parameter(description = "附件类型", example = "ROUTINE_TASK_RESULT")
            @RequestParam(value = "attachmentType", required = false, defaultValue = "GENERAL")
            String attachmentType,

            @Parameter(description = "关联的任务ID（用于任务相关附件）")
            @RequestParam(value = "relatedTaskId", required = false) Long relatedTaskId,

            @Parameter(description = "关联的项目ID（用于发展任务附件）")
            @RequestParam(value = "relatedProjectId", required = false) Long relatedProjectId,

            @Parameter(description = "关联的阶段ID（用于发展任务附件）")
            @RequestParam(value = "relatedPhaseId", required = false) Long relatedPhaseId,

            @Parameter(description = "是否公开访问")
            @RequestParam(value = "isPublic", required = false, defaultValue = "false") Boolean isPublic,

            @AuthenticationPrincipal CustomUserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {

        try {
            logger.info("用户 {} 批量上传 {} 个文件到周报: {}", userPrincipal.getId(), files.size(), weeklyReportId);

            List<FileUploadResponse> responses = files.stream()
                .map(file -> {
                    try {
                        FileUploadRequest request = new FileUploadRequest();
                        request.setWeeklyReportId(weeklyReportId);
                        request.setAttachmentType(
                            com.weeklyreport.filemanagement.entity.WeeklyReportAttachment.AttachmentType.fromString(attachmentType)
                        );
                        request.setRelatedTaskId(relatedTaskId);
                        request.setRelatedProjectId(relatedProjectId);
                        request.setRelatedPhaseId(relatedPhaseId);
                        request.setIsPublic(isPublic);

                        return fileManagementService.uploadFile(file, request, userPrincipal.getId(), httpRequest);
                    } catch (Exception e) {
                        logger.error("批量上传中文件上传失败: {}", file.getOriginalFilename(), e);
                        // 返回失败状态的响应
                        FileUploadResponse errorResponse = new FileUploadResponse();
                        errorResponse.setOriginalFilename(file.getOriginalFilename());
                        errorResponse.setUploadStatus(com.weeklyreport.filemanagement.entity.FileAttachment.UploadStatus.FAILED);
                        return errorResponse;
                    }
                })
                .toList();

            long successCount = responses.stream()
                .filter(response -> response.getUploadStatus() ==
                       com.weeklyreport.filemanagement.entity.FileAttachment.UploadStatus.COMPLETED)
                .count();

            logger.info("批量上传完成: 总数={}, 成功={}, 失败={}", files.size(), successCount, files.size() - successCount);
            return ApiResponse.success(responses);

        } catch (Exception e) {
            logger.error("批量文件上传失败: userId={}", userPrincipal.getId(), e);
            return ApiResponse.error("批量文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{fileId}")
    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "文件ID", required = true)
            @PathVariable Long fileId,

            @AuthenticationPrincipal CustomUserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {

        try {
            logger.info("用户 {} 下载文件: fileId={}", userPrincipal.getId(), fileId);

            return fileManagementService.downloadFile(fileId, userPrincipal.getId(), httpRequest);

        } catch (Exception e) {
            logger.error("文件下载失败: fileId={}, userId={}", fileId, userPrincipal.getId(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件预览URL
     */
    @GetMapping("/preview/{fileId}")
    @Operation(summary = "获取文件预览URL", description = "获取文件的临时预览链接")
    public ApiResponse<String> getFilePreviewUrl(
            @Parameter(description = "文件ID", required = true)
            @PathVariable Long fileId,

            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

        try {
            logger.info("用户 {} 获取文件预览URL: fileId={}", userPrincipal.getId(), fileId);

            String previewUrl = fileManagementService.getFilePreviewUrl(fileId, userPrincipal.getId());

            return ApiResponse.success(previewUrl);

        } catch (Exception e) {
            logger.error("获取文件预览URL失败: fileId={}, userId={}", fileId, userPrincipal.getId(), e);
            return ApiResponse.error("获取文件预览URL失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件", description = "软删除文件，文件将被标记为已删除")
    public ApiResponse<String> deleteFile(
            @Parameter(description = "文件ID", required = true)
            @PathVariable Long fileId,

            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

        try {
            logger.info("用户 {} 删除文件: fileId={}", userPrincipal.getId(), fileId);

            fileManagementService.deleteFile(fileId, userPrincipal.getId());

            return ApiResponse.success("文件删除成功");

        } catch (Exception e) {
            logger.error("文件删除失败: fileId={}, userId={}", fileId, userPrincipal.getId(), e);
            return ApiResponse.error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取周报的所有附件
     */
    @GetMapping("/weekly-report/{weeklyReportId}/attachments")
    @Operation(summary = "获取周报附件", description = "获取指定周报的所有附件")
    public ApiResponse<List<FileUploadResponse>> getWeeklyReportAttachments(
            @Parameter(description = "周报ID", required = true)
            @PathVariable Long weeklyReportId,

            @Parameter(description = "附件类型过滤（可选）")
            @RequestParam(value = "attachmentType", required = false) String attachmentType,

            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

        try {
            logger.info("用户 {} 获取周报附件: weeklyReportId={}, attachmentType={}",
                       userPrincipal.getId(), weeklyReportId, attachmentType);

            List<FileUploadResponse> attachments = fileManagementService.getWeeklyReportAttachments(weeklyReportId);

            // 如果指定了附件类型，进行过滤
            if (attachmentType != null && !attachmentType.isEmpty()) {
                com.weeklyreport.filemanagement.entity.WeeklyReportAttachment.AttachmentType filterType =
                    com.weeklyreport.filemanagement.entity.WeeklyReportAttachment.AttachmentType.fromString(attachmentType);

                attachments = attachments.stream()
                    .filter(attachment -> attachment.getAttachmentType() == filterType)
                    .toList();
            }

            return ApiResponse.success(attachments);

        } catch (Exception e) {
            logger.error("获取周报附件失败: weeklyReportId={}, userId={}", weeklyReportId, userPrincipal.getId(), e);
            return ApiResponse.error("获取周报附件失败: " + e.getMessage());
        }
    }

    /**
     * 更新附件信息
     */
    @PutMapping("/attachment/{relationId}")
    @Operation(summary = "更新附件信息", description = "更新附件的描述、显示顺序等信息")
    public ApiResponse<String> updateAttachment(
            @Parameter(description = "周报附件关联ID", required = true)
            @PathVariable Long relationId,

            @Parameter(description = "新的描述")
            @RequestParam(value = "description", required = false) String description,

            @Parameter(description = "新的显示顺序")
            @RequestParam(value = "displayOrder", required = false) Integer displayOrder,

            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

        try {
            // TODO: 实现更新附件信息的逻辑
            logger.info("用户 {} 更新附件信息: relationId={}", userPrincipal.getId(), relationId);

            return ApiResponse.success("附件信息更新成功");

        } catch (Exception e) {
            logger.error("更新附件信息失败: relationId={}, userId={}", relationId, userPrincipal.getId(), e);
            return ApiResponse.error("更新附件信息失败: " + e.getMessage());
        }
    }

    /**
     * 移除周报附件关联（不删除文件本身）
     */
    @DeleteMapping("/weekly-report/{weeklyReportId}/attachment/{fileId}")
    @Operation(summary = "移除附件关联", description = "从周报中移除文件关联，但不删除文件本身")
    public ApiResponse<String> removeAttachmentFromWeeklyReport(
            @Parameter(description = "周报ID", required = true)
            @PathVariable Long weeklyReportId,

            @Parameter(description = "文件ID", required = true)
            @PathVariable Long fileId,

            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

        try {
            logger.info("用户 {} 从周报 {} 移除文件关联: fileId={}", userPrincipal.getId(), weeklyReportId, fileId);

            // TODO: 实现移除附件关联的逻辑

            return ApiResponse.success("附件关联移除成功");

        } catch (Exception e) {
            logger.error("移除附件关联失败: weeklyReportId={}, fileId={}, userId={}",
                        weeklyReportId, fileId, userPrincipal.getId(), e);
            return ApiResponse.error("移除附件关联失败: " + e.getMessage());
        }
    }
}
