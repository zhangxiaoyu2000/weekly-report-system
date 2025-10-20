package com.weeklyreport.weeklyreport.dto;

import com.weeklyreport.weeklyreport.entity.WeeklyReport;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDate;

/**
 * DTO for weekly report filtering and search parameters
 */
public class WeeklyReportFilterRequest {

    private String title;
    private WeeklyReport.ReportStatus status;
    private Long authorId;
    private Long reviewerId;
    private Long projectId;
    private Long templateId;
    private Integer year;
    private Integer weekNumber;
    private LocalDate reportWeekFrom;
    private LocalDate reportWeekTo;
    private LocalDate submittedFrom;
    private LocalDate submittedTo;
    private Integer priorityMin;
    private Integer priorityMax;
    private Boolean isLate;
    private Boolean needsReview;
    private String searchTerm; // Search in title, content, or notes

    // Pagination parameters
    @Min(value = 0, message = "Page number must be non-negative")
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer size = 20;

    // Sorting parameters
    private String sortBy = "weekStart";
    private String sortDirection = "DESC";

    // Constructors
    public WeeklyReportFilterRequest() {}

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public WeeklyReport.ReportStatus getStatus() {
        return status;
    }

    public void setStatus(WeeklyReport.ReportStatus status) {
        this.status = status;
    }


    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public LocalDate getReportWeekFrom() {
        return reportWeekFrom;
    }

    public void setReportWeekFrom(LocalDate reportWeekFrom) {
        this.reportWeekFrom = reportWeekFrom;
    }

    public LocalDate getReportWeekTo() {
        return reportWeekTo;
    }

    public void setReportWeekTo(LocalDate reportWeekTo) {
        this.reportWeekTo = reportWeekTo;
    }

    public LocalDate getSubmittedFrom() {
        return submittedFrom;
    }

    public void setSubmittedFrom(LocalDate submittedFrom) {
        this.submittedFrom = submittedFrom;
    }

    public LocalDate getSubmittedTo() {
        return submittedTo;
    }

    public void setSubmittedTo(LocalDate submittedTo) {
        this.submittedTo = submittedTo;
    }

    public Integer getPriorityMin() {
        return priorityMin;
    }

    public void setPriorityMin(Integer priorityMin) {
        this.priorityMin = priorityMin;
    }

    public Integer getPriorityMax() {
        return priorityMax;
    }

    public void setPriorityMax(Integer priorityMax) {
        this.priorityMax = priorityMax;
    }

    public Boolean getIsLate() {
        return isLate;
    }

    public void setIsLate(Boolean isLate) {
        this.isLate = isLate;
    }

    public Boolean getNeedsReview() {
        return needsReview;
    }

    public void setNeedsReview(Boolean needsReview) {
        this.needsReview = needsReview;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    // Helper methods
    public boolean hasFilters() {
        return title != null || status != null || authorId != null || reviewerId != null ||
               projectId != null || templateId != null || year != null || weekNumber != null ||
               reportWeekFrom != null || reportWeekTo != null || submittedFrom != null ||
               submittedTo != null || priorityMin != null || priorityMax != null ||
               isLate != null || needsReview != null || searchTerm != null;
    }

    public boolean isValidSortDirection() {
        return "ASC".equalsIgnoreCase(sortDirection) || "DESC".equalsIgnoreCase(sortDirection);
    }

    @Override
    public String toString() {
        return "WeeklyReportFilterRequest{" +
                "status=" + status +
                ", authorId=" + authorId +
                ", year=" + year +
                ", weekNumber=" + weekNumber +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}