package com.weeklyreport.filemanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 文件访问日志实体
 * 记录文件的访问历史，用于审计和统计
 */
@Entity
@Table(name = "file_access_logs")
public class FileAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_attachment_id", nullable = false)
    private Long fileAttachmentId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AccessAction action;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "access_time", nullable = false)
    private LocalDateTime accessTime;

    /**
     * 访问操作类型枚举
     */
    public enum AccessAction {
        UPLOAD("上传"),
        DOWNLOAD("下载"),
        PREVIEW("预览"),
        DELETE("删除");

        private final String description;

        AccessAction(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.accessTime == null) {
            this.accessTime = LocalDateTime.now();
        }
    }

    // 构造函数
    public FileAccessLog() {}

    public FileAccessLog(Long fileAttachmentId, Long userId, AccessAction action) {
        this.fileAttachmentId = fileAttachmentId;
        this.userId = userId;
        this.action = action;
        this.accessTime = LocalDateTime.now();
    }

    public FileAccessLog(Long fileAttachmentId, Long userId, AccessAction action, 
                        String ipAddress, String userAgent) {
        this.fileAttachmentId = fileAttachmentId;
        this.userId = userId;
        this.action = action;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.accessTime = LocalDateTime.now();
    }

    // 业务方法
    public String getActionDescription() {
        return action.getDescription();
    }

    public boolean isDownloadAction() {
        return action == AccessAction.DOWNLOAD;
    }

    public boolean isUploadAction() {
        return action == AccessAction.UPLOAD;
    }

    public boolean isPreviewAction() {
        return action == AccessAction.PREVIEW;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileAttachmentId() {
        return fileAttachmentId;
    }

    public void setFileAttachmentId(Long fileAttachmentId) {
        this.fileAttachmentId = fileAttachmentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AccessAction getAction() {
        return action;
    }

    public void setAction(AccessAction action) {
        this.action = action;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }

    @Override
    public String toString() {
        return "FileAccessLog{" +
                "id=" + id +
                ", fileAttachmentId=" + fileAttachmentId +
                ", userId=" + userId +
                ", action=" + action +
                ", ipAddress='" + ipAddress + '\'' +
                ", accessTime=" + accessTime +
                '}';
    }
}