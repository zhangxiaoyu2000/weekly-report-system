package com.weeklyreport.comment.dto;

import java.util.List;

/**
 * 评论列表响应DTO
 */
public class CommentListResponse {

    private List<CommentResponse> comments;
    private int totalCount;
    private int pageNum;
    private int pageSize;
    private boolean hasMore;

    // 构造函数
    public CommentListResponse() {}

    public CommentListResponse(List<CommentResponse> comments, int totalCount, int pageNum, int pageSize) {
        this.comments = comments;
        this.totalCount = totalCount;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.hasMore = (pageNum + 1) * pageSize < totalCount;
    }

    // Getters and Setters
    public List<CommentResponse> getComments() {
        return comments;
    }

    public void setComments(List<CommentResponse> comments) {
        this.comments = comments;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    @Override
    public String toString() {
        return "CommentListResponse{" +
                "totalCount=" + totalCount +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", hasMore=" + hasMore +
                ", commentsSize=" + (comments != null ? comments.size() : 0) +
                '}';
    }
}