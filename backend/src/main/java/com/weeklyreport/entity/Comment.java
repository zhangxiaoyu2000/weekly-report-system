package com.weeklyreport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Comment entity representing comments and feedback on weekly reports
 */
@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_comment_report", columnList = "weekly_report_id"),
    @Index(name = "idx_comment_author", columnList = "author_id"),
    @Index(name = "idx_comment_parent", columnList = "parent_id"),
    @Index(name = "idx_comment_created", columnList = "created_at")
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Comment content cannot be blank")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @NotNull(message = "Comment type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private CommentType type = CommentType.GENERAL;

    @NotNull(message = "Comment status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CommentStatus status = CommentStatus.ACTIVE;

    @Min(value = 1, message = "Priority must be between 1 and 5")
    @Max(value = 5, message = "Priority must be between 1 and 5")
    @Column(name = "priority")
    private Integer priority = 3; // 1=Low, 2=Normal, 3=Medium, 4=High, 5=Critical

    @Column(name = "is_resolved")
    private Boolean isResolved = false;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Size(max = 200, message = "Tags must not exceed 200 characters")
    @Column(name = "tags", length = 200)
    private String tags; // 标签，以逗号分隔

    @Column(name = "likes_count")
    private Integer likesCount = 0;

    @Column(name = "is_private")
    private Boolean isPrivate = false; // 是否为私人备注

    @Column(name = "attachments", columnDefinition = "JSON")
    private String attachments; // 附件信息，JSON格式存储

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Many-to-One relationship with WeeklyReport
    @NotNull(message = "Weekly report cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_report_id", nullable = false)
    private WeeklyReport weeklyReport;

    // Many-to-One relationship with User (Author)
    @NotNull(message = "Author cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // Self-referencing Many-to-One relationship (Parent Comment for replies)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // Self-referencing One-to-Many relationship (Reply Comments)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private Set<Comment> replies = new HashSet<>();

    // Many-to-One relationship with User (Resolver)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    // Comment type enum
    public enum CommentType {
        GENERAL,        // 一般评论
        SUGGESTION,     // 建议
        QUESTION,       // 问题
        APPROVAL,       // 审批意见
        REJECTION,      // 拒绝原因
        REVISION,       // 修改建议
        PRAISE,         // 表扬
        CONCERN,        // 关注点
        REMINDER        // 提醒
    }

    // Comment status enum
    public enum CommentStatus {
        ACTIVE,         // 活跃状态
        HIDDEN,         // 隐藏状态
        DELETED,        // 已删除（软删除）
        FLAGGED         // 已标记（需要审核）
    }

    // Constructors
    public Comment() {}

    public Comment(String content, WeeklyReport weeklyReport, User author) {
        this.content = content;
        this.weeklyReport = weeklyReport;
        this.author = author;
    }

    public Comment(String content, WeeklyReport weeklyReport, User author, CommentType type) {
        this.content = content;
        this.weeklyReport = weeklyReport;
        this.author = author;
        this.type = type;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CommentType getType() {
        return type;
    }

    public void setType(CommentType type) {
        this.type = type;
    }

    public CommentStatus getStatus() {
        return status;
    }

    public void setStatus(CommentStatus status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getIsResolved() {
        return isResolved;
    }

    public void setIsResolved(Boolean isResolved) {
        this.isResolved = isResolved;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Comment getParent() {
        return parent;
    }

    public void setParent(Comment parent) {
        this.parent = parent;
    }

    public Set<Comment> getReplies() {
        return replies;
    }

    public void setReplies(Set<Comment> replies) {
        this.replies = replies;
    }

    public User getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(User resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    // Utility methods for managing relationships
    public void addReply(Comment reply) {
        replies.add(reply);
        reply.setParent(this);
    }

    public void removeReply(Comment reply) {
        replies.remove(reply);
        reply.setParent(null);
    }

    // Business logic methods
    public void resolve(User resolver) {
        this.isResolved = true;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolver;
    }

    public void unresolve() {
        this.isResolved = false;
        this.resolvedAt = null;
        this.resolvedBy = null;
    }

    public void incrementLikes() {
        if (this.likesCount == null) {
            this.likesCount = 1;
        } else {
            this.likesCount++;
        }
    }

    public void decrementLikes() {
        if (this.likesCount != null && this.likesCount > 0) {
            this.likesCount--;
        }
    }

    public void hide() {
        this.status = CommentStatus.HIDDEN;
    }

    public void show() {
        this.status = CommentStatus.ACTIVE;
    }

    public void softDelete() {
        this.status = CommentStatus.DELETED;
    }

    public void flag() {
        this.status = CommentStatus.FLAGGED;
    }

    // Check if this is a root comment (not a reply)
    public boolean isRootComment() {
        return parent == null;
    }

    // Check if this comment has replies
    public boolean hasReplies() {
        return !replies.isEmpty();
    }

    // Get the total number of replies (including nested replies)
    public int getTotalRepliesCount() {
        int count = replies.size();
        for (Comment reply : replies) {
            count += reply.getTotalRepliesCount();
        }
        return count;
    }

    // Get all descendant replies
    public Set<Comment> getAllReplies() {
        Set<Comment> allReplies = new HashSet<>();
        for (Comment reply : replies) {
            allReplies.add(reply);
            allReplies.addAll(reply.getAllReplies());
        }
        return allReplies;
    }

    // Get the thread root comment
    public Comment getThreadRoot() {
        Comment root = this;
        while (root.getParent() != null) {
            root = root.getParent();
        }
        return root;
    }

    // Get comment depth in the thread
    public int getDepth() {
        int depth = 0;
        Comment current = this.parent;
        while (current != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    // Check if comment is editable by author
    public boolean isEditableBy(User user) {
        if (!user.equals(this.author)) {
            return false;
        }
        // Allow editing within 15 minutes of creation
        return createdAt.plusMinutes(15).isAfter(LocalDateTime.now());
    }

    // Get priority description
    public String getPriorityDescription() {
        if (priority == null) return "Normal";
        switch (priority) {
            case 1: return "Low";
            case 2: return "Normal";
            case 3: return "Medium";
            case 4: return "High";
            case 5: return "Critical";
            default: return "Unknown";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        Comment comment = (Comment) o;
        return id != null && id.equals(comment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", type=" + type +
                ", status=" + status +
                ", isResolved=" + isResolved +
                ", author=" + (author != null ? author.getFullName() : "null") +
                ", createdAt=" + createdAt +
                '}';
    }
}