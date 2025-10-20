package com.weeklyreport.comment.dto;

import jakarta.validation.constraints.*;

/**
 * 更新评论请求DTO
 */
public class CommentUpdateRequest {

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    // 构造函数
    public CommentUpdateRequest() {}

    public CommentUpdateRequest(String content) {
        this.content = content;
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "CommentUpdateRequest{" +
                "content='" + content + '\'' +
                '}';
    }
}