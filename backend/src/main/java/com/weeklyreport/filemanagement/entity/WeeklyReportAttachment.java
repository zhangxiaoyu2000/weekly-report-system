package com.weeklyreport.filemanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 周报文件关联实体
 * 建立周报与文件附件的多对多关联关系
 */
@Entity
@Table(name = "weekly_report_attachments")
public class WeeklyReportAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weekly_report_id", nullable = false)
    private Long weeklyReportId;

    @Column(name = "file_attachment_id", nullable = false)
    private Long fileAttachmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", nullable = false)
    private AttachmentType attachmentType = AttachmentType.GENERAL;

    @Column(name = "related_task_id")
    private Long relatedTaskId;

    @Column(name = "related_project_id")
    private Long relatedProjectId;

    @Column(name = "related_phase_id")
    private Long relatedPhaseId;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 关联对象（延迟加载）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_attachment_id", insertable = false, updatable = false)
    private FileAttachment fileAttachment;

    /**
     * 附件类型枚举
     */
    public enum AttachmentType {
        ROUTINE_TASK_RESULT("日常任务实际结果附件"),
        ROUTINE_TASK_ANALYSIS("日常任务差异分析附件"),
        DEV_TASK_RESULT("发展任务实际结果附件"),
        DEV_TASK_ANALYSIS("发展任务差异分析附件"),
        ADDITIONAL_NOTES("其他备注附件"),
        DEVELOPMENT_OPPORTUNITIES("可发展性清单附件"),
        GENERAL("通用附件");

        private final String description;

        AttachmentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static AttachmentType fromString(String text) {
            for (AttachmentType type : AttachmentType.values()) {
                if (type.name().equalsIgnoreCase(text)) {
                    return type;
                }
            }
            return GENERAL;
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 构造函数
    public WeeklyReportAttachment() {}

    public WeeklyReportAttachment(Long weeklyReportId, Long fileAttachmentId, AttachmentType attachmentType) {
        this.weeklyReportId = weeklyReportId;
        this.fileAttachmentId = fileAttachmentId;
        this.attachmentType = attachmentType;
    }

    public WeeklyReportAttachment(Long weeklyReportId, Long fileAttachmentId, AttachmentType attachmentType,
                                 Long relatedTaskId, Long relatedProjectId, Long relatedPhaseId) {
        this.weeklyReportId = weeklyReportId;
        this.fileAttachmentId = fileAttachmentId;
        this.attachmentType = attachmentType;
        this.relatedTaskId = relatedTaskId;
        this.relatedProjectId = relatedProjectId;
        this.relatedPhaseId = relatedPhaseId;
    }

    // 业务方法
    public boolean isTaskRelated() {
        return relatedTaskId != null;
    }

    public boolean isProjectRelated() {
        return relatedProjectId != null || relatedPhaseId != null;
    }

    public boolean isRoutineTaskAttachment() {
        return attachmentType == AttachmentType.ROUTINE_TASK_RESULT || 
               attachmentType == AttachmentType.ROUTINE_TASK_ANALYSIS;
    }

    public boolean isDevelopmentTaskAttachment() {
        return attachmentType == AttachmentType.DEV_TASK_RESULT || 
               attachmentType == AttachmentType.DEV_TASK_ANALYSIS;
    }

    public String getAttachmentTypeDescription() {
        return attachmentType.getDescription();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWeeklyReportId() {
        return weeklyReportId;
    }

    public void setWeeklyReportId(Long weeklyReportId) {
        this.weeklyReportId = weeklyReportId;
    }

    public Long getFileAttachmentId() {
        return fileAttachmentId;
    }

    public void setFileAttachmentId(Long fileAttachmentId) {
        this.fileAttachmentId = fileAttachmentId;
    }

    public AttachmentType getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(AttachmentType attachmentType) {
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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public FileAttachment getFileAttachment() {
        return fileAttachment;
    }

    public void setFileAttachment(FileAttachment fileAttachment) {
        this.fileAttachment = fileAttachment;
    }

    @Override
    public String toString() {
        return "WeeklyReportAttachment{" +
                "id=" + id +
                ", weeklyReportId=" + weeklyReportId +
                ", fileAttachmentId=" + fileAttachmentId +
                ", attachmentType=" + attachmentType +
                ", relatedTaskId=" + relatedTaskId +
                ", relatedProjectId=" + relatedProjectId +
                ", relatedPhaseId=" + relatedPhaseId +
                ", createdAt=" + createdAt +
                '}';
    }
}