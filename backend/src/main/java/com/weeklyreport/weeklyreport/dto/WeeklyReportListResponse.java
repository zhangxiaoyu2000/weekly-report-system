package com.weeklyreport.weeklyreport.dto;

import com.weeklyreport.weeklyreport.entity.WeeklyReport;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO for paginated weekly report list responses
 */
public class WeeklyReportListResponse {

    private List<WeeklyReportResponse> reports;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;

    // Statistics
    private Long totalDrafts;
    private Long totalSubmitted;
    private Long totalUnderReview;
    private Long totalApproved;
    private Long totalRejected;

    // Constructors
    public WeeklyReportListResponse() {}

    public WeeklyReportListResponse(List<WeeklyReportResponse> reports, Page<?> page) {
        this.reports = reports;
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
        this.isFirst = page.isFirst();
        this.isLast = page.isLast();
    }

    public WeeklyReportListResponse(List<WeeklyReportResponse> reports, long totalElements, 
                                   int totalPages, int currentPage, int pageSize) {
        this.reports = reports;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
        this.isFirst = currentPage == 0;
        this.isLast = currentPage == totalPages - 1;
    }

    // Getters and Setters
    public List<WeeklyReportResponse> getReports() {
        return reports;
    }

    public void setReports(List<WeeklyReportResponse> reports) {
        this.reports = reports;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public Long getTotalDrafts() {
        return totalDrafts;
    }

    public void setTotalDrafts(Long totalDrafts) {
        this.totalDrafts = totalDrafts;
    }

    public Long getTotalSubmitted() {
        return totalSubmitted;
    }

    public void setTotalSubmitted(Long totalSubmitted) {
        this.totalSubmitted = totalSubmitted;
    }

    public Long getTotalUnderReview() {
        return totalUnderReview;
    }

    public void setTotalUnderReview(Long totalUnderReview) {
        this.totalUnderReview = totalUnderReview;
    }

    public Long getTotalApproved() {
        return totalApproved;
    }

    public void setTotalApproved(Long totalApproved) {
        this.totalApproved = totalApproved;
    }

    public Long getTotalRejected() {
        return totalRejected;
    }

    public void setTotalRejected(Long totalRejected) {
        this.totalRejected = totalRejected;
    }

    // Helper method to calculate statistics from reports - 基于5状态系统
    public void calculateStatistics() {
        if (reports == null) {
            return;
        }

        long drafts = 0, submitted = 0, underReview = 0, approved = 0, rejected = 0;

        for (WeeklyReportResponse report : reports) {
            WeeklyReport.ReportStatus status = report.getStatus();

            if (status == null) {
                continue;
            }

            // 草稿状态
            if (status == WeeklyReport.ReportStatus.DRAFT) {
                drafts++;
            }

            // 已提交状态（AI_PROCESSING）
            if (status == WeeklyReport.ReportStatus.AI_PROCESSING) {
                submitted++;
            }

            // 审核中状态（AI_PROCESSING 或 ADMIN_REVIEWING）
            if (status == WeeklyReport.ReportStatus.AI_PROCESSING ||
                status == WeeklyReport.ReportStatus.ADMIN_REVIEWING) {
                underReview++;
            }

            // 已批准
            if (status == WeeklyReport.ReportStatus.APPROVED) {
                approved++;
            }

            // 已拒绝
            if (status == WeeklyReport.ReportStatus.REJECTED) {
                rejected++;
            }
        }

        this.totalDrafts = drafts;
        this.totalSubmitted = submitted;
        this.totalUnderReview = underReview;
        this.totalApproved = approved;
        this.totalRejected = rejected;
    }

    @Override
    public String toString() {
        return "WeeklyReportListResponse{" +
                "totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", reportCount=" + (reports != null ? reports.size() : 0) +
                '}';
    }
}