package com.weeklyreport.dto.ai;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for AI project insights analysis
 */
public class AIProjectInsightRequest {

    @NotNull(message = "Project ID is required")
    @Positive(message = "Project ID must be positive")
    private Long projectId;

    private LocalDate startDate; // Analysis period start
    
    private LocalDate endDate; // Analysis period end
    
    private List<String> insightTypes; // e.g., ["progress", "risks", "team_performance", "trends"]
    
    private Boolean includeComparisons = false; // Compare with previous periods
    
    private Boolean includePredictions = false; // Include future predictions
    
    private String granularity = "weekly"; // "daily", "weekly", "monthly"

    // Constructors
    public AIProjectInsightRequest() {}

    public AIProjectInsightRequest(Long projectId) {
        this.projectId = projectId;
    }

    public AIProjectInsightRequest(Long projectId, LocalDate startDate, LocalDate endDate) {
        this.projectId = projectId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<String> getInsightTypes() {
        return insightTypes;
    }

    public void setInsightTypes(List<String> insightTypes) {
        this.insightTypes = insightTypes;
    }

    public Boolean getIncludeComparisons() {
        return includeComparisons;
    }

    public void setIncludeComparisons(Boolean includeComparisons) {
        this.includeComparisons = includeComparisons;
    }

    public Boolean getIncludePredictions() {
        return includePredictions;
    }

    public void setIncludePredictions(Boolean includePredictions) {
        this.includePredictions = includePredictions;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    @Override
    public String toString() {
        return "AIProjectInsightRequest{" +
                "projectId=" + projectId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", insightTypes=" + insightTypes +
                ", granularity='" + granularity + '\'' +
                '}';
    }
}