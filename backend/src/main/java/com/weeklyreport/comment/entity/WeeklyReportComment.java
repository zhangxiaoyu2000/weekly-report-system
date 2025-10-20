package com.weeklyreport.comment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.weeklyreport.user.entity.User;
import com.weeklyreport.weeklyreport.entity.WeeklyReport;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 周报评论实体类
 * 支持评论和回复功能，采用树形结构设计
 */
@Entity
@Table(name = "weekly_report_comments", indexes = {
    @Index(name = "idx_weekly_report_comments_report_id", columnList = "weekly_report_id"),
    @Index(name = "idx_weekly_report_comments_user_id", columnList = "user_id"),
    @Index(name = "idx_weekly_report_comments_parent_id", columnList = "parent_comment_id"),
    @Index(name = "idx_weekly_report_comments_status", columnList = "status"),
    @Index(name = "idx_weekly_report_comments_created_at", columnList = "created_at")
})
public class WeeklyReportComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Weekly report ID cannot be null")
    @Column(name = "weekly_report_id", nullable = false)
    private Long weeklyReportId;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @NotBlank(message = "Comment content cannot be blank")
    @Size(max = 5000, message = "Comment content must not exceed 5000 characters")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @NotNull(message = "Comment type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "comment_type", nullable = false)
    private CommentType commentType = CommentType.COMMENT;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CommentStatus status = CommentStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 关联映射
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_report_id", insertable = false, updatable = false)
    @JsonIgnore
    private WeeklyReport weeklyReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id", insertable = false, updatable = false)
    @JsonIgnore
    private WeeklyReportComment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<WeeklyReportComment> replies = new ArrayList<>();

    // 评论类型枚举
    public enum CommentType {
        COMMENT,    // 评论
        REPLY       // 回复
    }

    // 评论状态枚举
    public enum CommentStatus {
        ACTIVE,     // 活跃状态
        DELETED,    // 已删除
        HIDDEN      // 隐藏状态
    }

    // 构造函数
    public WeeklyReportComment() {}

    public WeeklyReportComment(Long weeklyReportId, Long userId, String content) {
        this.weeklyReportId = weeklyReportId;
        this.userId = userId;
        this.content = content;
        this.commentType = CommentType.COMMENT;
        this.status = CommentStatus.ACTIVE;
    }

    public WeeklyReportComment(Long weeklyReportId, Long userId, String content, Long parentCommentId) {
        this(weeklyReportId, userId, content);
        this.parentCommentId = parentCommentId;
        this.commentType = CommentType.REPLY;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CommentType getCommentType() {
        return commentType;
    }

    public void setCommentType(CommentType commentType) {
        this.commentType = commentType;
    }

    public CommentStatus getStatus() {
        return status;
    }

    public void setStatus(CommentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public WeeklyReport getWeeklyReport() {
        return weeklyReport;
    }

    public void setWeeklyReport(WeeklyReport weeklyReport) {
        this.weeklyReport = weeklyReport;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public WeeklyReportComment getParentComment() {
        return parentComment;
    }

    public void setParentComment(WeeklyReportComment parentComment) {
        this.parentComment = parentComment;
    }

    public List<WeeklyReportComment> getReplies() {
        return replies;
    }

    public void setReplies(List<WeeklyReportComment> replies) {
        this.replies = replies;
    }

    // 业务方法
    public boolean isComment() {
        return commentType == CommentType.COMMENT;
    }

    public boolean isReply() {
        return commentType == CommentType.REPLY;
    }

    public boolean isActive() {
        return status == CommentStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return status == CommentStatus.DELETED;
    }

    public void markAsDeleted() {
        this.status = CommentStatus.DELETED;
    }

    public void markAsHidden() {
        this.status = CommentStatus.HIDDEN;
    }

    public void activate() {
        this.status = CommentStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeeklyReportComment)) return false;
        WeeklyReportComment that = (WeeklyReportComment) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "WeeklyReportComment{" +
                "id=" + id +
                ", weeklyReportId=" + weeklyReportId +
                ", userId=" + userId +
                ", parentCommentId=" + parentCommentId +
                ", commentType=" + commentType +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}