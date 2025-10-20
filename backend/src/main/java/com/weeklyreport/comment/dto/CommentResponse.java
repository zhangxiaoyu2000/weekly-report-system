package com.weeklyreport.comment.dto;

import com.weeklyreport.comment.entity.WeeklyReportComment.CommentType;
import com.weeklyreport.comment.entity.WeeklyReportComment.CommentStatus;
import com.weeklyreport.user.entity.User.Role;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应DTO
 */
public class CommentResponse {

    private Long id;
    private Long weeklyReportId;
    private Long userId;
    private String username;
    private String fullName;
    private Role userRole;
    private Long parentCommentId;
    private String content;
    private CommentType commentType;
    private CommentStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private List<CommentResponse> replies; // 子回复
    private int replyCount; // 回复数量

    // 构造函数
    public CommentResponse() {}

    // 用户信息内部类
    public static class UserInfo {
        private Long id;
        private String username;
        private Role role;

        public UserInfo() {}

        public UserInfo(Long id, String username, Role role) {
            this.id = id;
            this.username = username;
            this.role = role;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
        }
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getUserRole() {
        return userRole;
    }

    public void setUserRole(Role userRole) {
        this.userRole = userRole;
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

    public List<CommentResponse> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentResponse> replies) {
        this.replies = replies;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    @Override
    public String toString() {
        return "CommentResponse{" +
                "id=" + id +
                ", weeklyReportId=" + weeklyReportId +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", commentType=" + commentType +
                ", replyCount=" + replyCount +
                ", createdAt=" + createdAt +
                '}';
    }
}