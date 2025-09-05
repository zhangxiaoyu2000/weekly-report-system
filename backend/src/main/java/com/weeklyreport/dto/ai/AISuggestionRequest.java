package com.weeklyreport.dto.ai;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for AI suggestion generation
 */
public class AISuggestionRequest {

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    private Long projectId; // Optional: for project-specific suggestions

    private Long reportId; // Optional: for report-specific suggestions

    @NotBlank(message = "Context is required")
    private String context; // "report_improvement", "project_planning", "team_management", etc.

    private String userInput; // Optional user input for context

    private List<String> focusAreas; // e.g., ["productivity", "communication", "deadlines"]

    private Integer maxSuggestions = 5; // Maximum number of suggestions to return

    private Map<String, Object> additionalContext; // Any additional context data

    // Constructors
    public AISuggestionRequest() {}

    public AISuggestionRequest(Long userId, String context) {
        this.userId = userId;
        this.context = context;
    }

    public AISuggestionRequest(Long userId, String context, String userInput) {
        this.userId = userId;
        this.context = context;
        this.userInput = userInput;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    public List<String> getFocusAreas() {
        return focusAreas;
    }

    public void setFocusAreas(List<String> focusAreas) {
        this.focusAreas = focusAreas;
    }

    public Integer getMaxSuggestions() {
        return maxSuggestions;
    }

    public void setMaxSuggestions(Integer maxSuggestions) {
        this.maxSuggestions = maxSuggestions;
    }

    public Map<String, Object> getAdditionalContext() {
        return additionalContext;
    }

    public void setAdditionalContext(Map<String, Object> additionalContext) {
        this.additionalContext = additionalContext;
    }

    @Override
    public String toString() {
        return "AISuggestionRequest{" +
                "userId=" + userId +
                ", projectId=" + projectId +
                ", reportId=" + reportId +
                ", context='" + context + '\'' +
                ", focusAreas=" + focusAreas +
                ", maxSuggestions=" + maxSuggestions +
                '}';
    }
}