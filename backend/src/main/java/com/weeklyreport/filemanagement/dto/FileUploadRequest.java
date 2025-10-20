package com.weeklyreport.filemanagement.dto;

import com.weeklyreport.filemanagement.entity.WeeklyReportAttachment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 文件上传请求DTO
 */
public class FileUploadRequest {

    @NotNull(message = "Weekly report ID cannot be null")
    private Long weeklyReportId;

    private WeeklyReportAttachment.AttachmentType attachmentType = WeeklyReportAttachment.AttachmentType.GENERAL;

    private Long relatedTaskId;

    private Long relatedProjectId;

    private Long relatedPhaseId;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private Integer displayOrder = 0;

    private Boolean isPublic = false;

    // 构造函数
    public FileUploadRequest() {}

    public FileUploadRequest(Long weeklyReportId, WeeklyReportAttachment.AttachmentType attachmentType) {
        this.weeklyReportId = weeklyReportId;
        this.attachmentType = attachmentType;
    }

    // 业务方法
    public boolean isTaskRelated() {
        return relatedTaskId != null;
    }

    public boolean isProjectRelated() {
        return relatedProjectId != null || relatedPhaseId != null;
    }

    public boolean isRoutineTaskAttachment() {
        return attachmentType == WeeklyReportAttachment.AttachmentType.ROUTINE_TASK_RESULT || 
               attachmentType == WeeklyReportAttachment.AttachmentType.ROUTINE_TASK_ANALYSIS;
    }

    public boolean isDevelopmentTaskAttachment() {
        return attachmentType == WeeklyReportAttachment.AttachmentType.DEV_TASK_RESULT || 
               attachmentType == WeeklyReportAttachment.AttachmentType.DEV_TASK_ANALYSIS;
    }

    // Getters and Setters
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

    public Long getRelatedTaskId() {
        return relatedTaskId;
    }

    public void setRelatedTaskId(Long relatedTaskId) {
        this.relatedTaskId = relatedTaskId;
    }

    public Long getRelatedProjectId() {
        return relatedProjectId;
    }

    public void setRelatedProjectId(Long relatedProjectId) {
        this.relatedProjectId = relatedProjectId;
    }

    public Long getRelatedPhaseId() {
        return relatedPhaseId;
    }

    public void setRelatedPhaseId(Long relatedPhaseId) {
        this.relatedPhaseId = relatedPhaseId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public String toString() {
        return "FileUploadRequest{" +
                "weeklyReportId=" + weeklyReportId +
                ", attachmentType=" + attachmentType +
                ", relatedTaskId=" + relatedTaskId +
                ", relatedProjectId=" + relatedProjectId +
                ", relatedPhaseId=" + relatedPhaseId +
                ", description='" + description + '\'' +
                ", displayOrder=" + displayOrder +
                ", isPublic=" + isPublic +
                '}';
    }
}