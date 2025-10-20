package com.weeklyreport.comment.dto;

import jakarta.validation.constraints.*;

/**
 * 创建评论请求DTO
 */
public class CommentCreateRequest {

    @NotNull(message = "Weekly report ID cannot be null")
    private Long weeklyReportId;

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    private Long parentCommentId; // 可选，用于回复

    // 构造函数
    public CommentCreateRequest() {}

    public CommentCreateRequest(Long weeklyReportId, String content) {
        this.weeklyReportId = weeklyReportId;
        this.content = content;
    }

    public CommentCreateRequest(Long weeklyReportId, String content, Long parentCommentId) {
        this.weeklyReportId = weeklyReportId;
        this.content = content;
        this.parentCommentId = parentCommentId;
    }

    // Getters and Setters
    public Long getWeeklyReportId() {
        return weeklyReportId;
    }

    public void setWeeklyReportId(Long weeklyReportId) {
        this.weeklyReportId = weeklyReportId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    @Override
    public String toString() {
        return "CommentCreateRequest{" +
                "weeklyReportId=" + weeklyReportId +
                ", content='" + content + '\'' +
                ", parentCommentId=" + parentCommentId +
                '}';
    }
}