package com.weeklyreport.filemanagement.dto;

import com.weeklyreport.filemanagement.entity.FileAttachment;
import com.weeklyreport.filemanagement.entity.WeeklyReportAttachment;

import java.time.LocalDateTime;

/**
 * 文件上传响应DTO
 */
public class FileUploadResponse {

    private Long fileId;
    private String originalFilename;
    private String storedFilename;
    private Long fileSize;
    private String formattedFileSize;
    private String mimeType;
    private String fileExtension;
    private FileAttachment.UploadStatus uploadStatus;
    private Integer uploadProgress;
    private String downloadUrl;
    private String previewUrl;
    private Boolean isImage;
    private Boolean isDocument;
    private LocalDateTime createdAt;

    // 关联信息
    private Long weeklyReportId;
    private WeeklyReportAttachment.AttachmentType attachmentType;
    private String attachmentTypeDescription;
    private Long relationId;  // 周报附件关联ID
    private String description;

    // 构造函数
    public FileUploadResponse() {}

    public FileUploadResponse(FileAttachment fileAttachment) {
        this.fileId = fileAttachment.getId();
        this.originalFilename = fileAttachment.getOriginalFilename();
        this.storedFilename = fileAttachment.getStoredFilename();
        this.fileSize = fileAttachment.getFileSize();
        this.formattedFileSize = fileAttachment.getFormattedFileSize();
        this.mimeType = fileAttachment.getMimeType();
        this.fileExtension = fileAttachment.getFileExtension();
        this.uploadStatus = fileAttachment.getUploadStatus();
        this.uploadProgress = fileAttachment.getUploadProgress();
        this.isImage = fileAttachment.isImage();
        this.isDocument = fileAttachment.isDocument();
        this.createdAt = fileAttachment.getCreatedAt();
    }

    public FileUploadResponse(FileAttachment fileAttachment, WeeklyReportAttachment reportAttachment) {
        this(fileAttachment);
        if (reportAttachment != null) {
            this.weeklyReportId = reportAttachment.getWeeklyReportId();
            this.attachmentType = reportAttachment.getAttachmentType();
            this.attachmentTypeDescription = reportAttachment.getAttachmentTypeDescription();
            this.relationId = reportAttachment.getId();
            this.description = reportAttachment.getDescription();
        }
    }

    // 静态工厂方法
    public static FileUploadResponse fromFileAttachment(FileAttachment fileAttachment) {
        return new FileUploadResponse(fileAttachment);
    }

    public static FileUploadResponse fromFileAttachmentWithRelation(FileAttachment fileAttachment, 
                                                                   WeeklyReportAttachment reportAttachment) {
        return new FileUploadResponse(fileAttachment, reportAttachment);
    }

    // 业务方法
    public boolean isUploadCompleted() {
        return uploadStatus == FileAttachment.UploadStatus.COMPLETED;
    }

    public boolean isUploadFailed() {
        return uploadStatus == FileAttachment.UploadStatus.FAILED;
    }

    public boolean canPreview() {
        return isImage || (isDocument && "application/pdf".equals(mimeType));
    }

    // Getters and Setters
    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFormattedFileSize() {
        return formattedFileSize;
    }

    public void setFormattedFileSize(String formattedFileSize) {
        this.formattedFileSize = formattedFileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public FileAttachment.UploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(FileAttachment.UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public Integer getUploadProgress() {
        return uploadProgress;
    }

    public void setUploadProgress(Integer uploadProgress) {
        this.uploadProgress = uploadProgress;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public Boolean getIsImage() {
        return isImage;
    }

    public void setIsImage(Boolean isImage) {
        this.isImage = isImage;
    }

    public Boolean getIsDocument() {
        return isDocument;
    }

    public void setIsDocument(Boolean isDocument) {
        this.isDocument = isDocument;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getWeeklyReportId() {
        return weeklyReportId;
    }

    public void setWeeklyReportId(Long weeklyReportId) {
        this.weeklyReportId = weeklyReportId;
    }

    public WeeklyReportAttachment.AttachmentType getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(WeeklyReportAttachment.AttachmentType attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getAttachmentTypeDescription() {
        return attachmentTypeDescription;
    }

    public void setAttachmentTypeDescription(String attachmentTypeDescription) {
        this.attachmentTypeDescription = attachmentTypeDescription;
    }

    public Long getRelationId() {
        return relationId;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "FileUploadResponse{" +
                "fileId=" + fileId +
                ", originalFilename='" + originalFilename + '\'' +
                ", fileSize=" + fileSize +
                ", mimeType='" + mimeType + '\'' +
                ", uploadStatus=" + uploadStatus +
                ", weeklyReportId=" + weeklyReportId +
                ", attachmentType=" + attachmentType +
                '}';
    }
}