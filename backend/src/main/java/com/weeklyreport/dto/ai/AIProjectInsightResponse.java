package com.weeklyreport.dto.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for AI project insights
 */
public class AIProjectInsightResponse {

    private Long projectId;
    
    private String projectName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate analysisStartDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate analysisEndDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedAt;
    
    private ProjectProgressInsight progressInsight;
    
    private TeamPerformanceInsight teamInsight;
    
    private List<RiskInsight> risks;
    
    private List<TrendInsight> trends;
    
    private Map<String, Object> predictions; // Future predictions
    
    private Map<String, Object> comparisons; // Historical comparisons

    /**
     * Project progress insights
     */
    public static class ProjectProgressInsight {
        private Double completionPercentage;
        private String progressStatus; // "on_track", "at_risk", "delayed"
        private Integer tasksCompleted;
        private Integer totalTasks;
        private String progressSummary;
        private List<String> keyAchievements;
        private List<String> blockers;

        // Getters and Setters
        public Double getCompletionPercentage() {
            return completionPercentage;
        }

        public void setCompletionPercentage(Double completionPercentage) {
            this.completionPercentage = completionPercentage;
        }

        public String getProgressStatus() {
            return progressStatus;
        }

        public void setProgressStatus(String progressStatus) {
            this.progressStatus = progressStatus;
        }

        public Integer getTasksCompleted() {
            return tasksCompleted;
        }

        public void setTasksCompleted(Integer tasksCompleted) {
            this.tasksCompleted = tasksCompleted;
        }

        public Integer getTotalTasks() {
            return totalTasks;
        }

        public void setTotalTasks(Integer totalTasks) {
            this.totalTasks = totalTasks;
        }

        public String getProgressSummary() {
            return progressSummary;
        }

        public void setProgressSummary(String progressSummary) {
            this.progressSummary = progressSummary;
        }

        public List<String> getKeyAchievements() {
            return keyAchievements;
        }

        public void setKeyAchievements(List<String> keyAchievements) {
            this.keyAchievements = keyAchievements;
        }

        public List<String> getBlockers() {
            return blockers;
        }

        public void setBlockers(List<String> blockers) {
            this.blockers = blockers;
        }
    }

    /**
     * Team performance insights
     */
    public static class TeamPerformanceInsight {
        private Double averageProductivity;
        private String teamMorale; // "high", "medium", "low"
        private Integer activeMembers;
        private Map<String, Double> memberContributions;
        private List<String> collaborationPatterns;
        private List<String> improvementAreas;

        // Getters and Setters
        public Double getAverageProductivity() {
            return averageProductivity;
        }

        public void setAverageProductivity(Double averageProductivity) {
            this.averageProductivity = averageProductivity;
        }

        public String getTeamMorale() {
            return teamMorale;
        }

        public void setTeamMorale(String teamMorale) {
            this.teamMorale = teamMorale;
        }

        public Integer getActiveMembers() {
            return activeMembers;
        }

        public void setActiveMembers(Integer activeMembers) {
            this.activeMembers = activeMembers;
        }

        public Map<String, Double> getMemberContributions() {
            return memberContributions;
        }

        public void setMemberContributions(Map<String, Double> memberContributions) {
            this.memberContributions = memberContributions;
        }

        public List<String> getCollaborationPatterns() {
            return collaborationPatterns;
        }

        public void setCollaborationPatterns(List<String> collaborationPatterns) {
            this.collaborationPatterns = collaborationPatterns;
        }

        public List<String> getImprovementAreas() {
            return improvementAreas;
        }

        public void setImprovementAreas(List<String> improvementAreas) {
            this.improvementAreas = improvementAreas;
        }
    }

    /**
     * Risk insights
     */
    public static class RiskInsight {
        private String riskType;
        private String severity; // "high", "medium", "low"
        private String description;
        private Double probability;
        private List<String> mitigation;
        private String impact;

        // Constructors
        public RiskInsight() {}

        public RiskInsight(String riskType, String severity, String description) {
            this.riskType = riskType;
            this.severity = severity;
            this.description = description;
        }

        // Getters and Setters
        public String getRiskType() {
            return riskType;
        }

        public void setRiskType(String riskType) {
            this.riskType = riskType;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Double getProbability() {
            return probability;
        }

        public void setProbability(Double probability) {
            this.probability = probability;
        }

        public List<String> getMitigation() {
            return mitigation;
        }

        public void setMitigation(List<String> mitigation) {
            this.mitigation = mitigation;
        }

        public String getImpact() {
            return impact;
        }

        public void setImpact(String impact) {
            this.impact = impact;
        }
    }

    /**
     * Trend insights
     */
    public static class TrendInsight {
        private String trendType;
        private String direction; // "increasing", "decreasing", "stable"
        private String description;
        private Map<String, Object> data;
        private String significance; // "high", "medium", "low"

        // Constructors
        public TrendInsight() {}

        public TrendInsight(String trendType, String direction, String description) {
            this.trendType = trendType;
            this.direction = direction;
            this.description = description;
        }

        // Getters and Setters
        public String getTrendType() {
            return trendType;
        }

        public void setTrendType(String trendType) {
            this.trendType = trendType;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }

        public String getSignificance() {
            return significance;
        }

        public void setSignificance(String significance) {
            this.significance = significance;
        }
    }

    // Constructors
    public AIProjectInsightResponse() {
        this.generatedAt = LocalDateTime.now();
    }

    public AIProjectInsightResponse(Long projectId, String projectName) {
        this();
        this.projectId = projectId;
        this.projectName = projectName;
    }

    // Getters and Setters
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public LocalDate getAnalysisStartDate() {
        return analysisStartDate;
    }

    public void setAnalysisStartDate(LocalDate analysisStartDate) {
        this.analysisStartDate = analysisStartDate;
    }

    public LocalDate getAnalysisEndDate() {
        return analysisEndDate;
    }

    public void setAnalysisEndDate(LocalDate analysisEndDate) {
        this.analysisEndDate = analysisEndDate;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public ProjectProgressInsight getProgressInsight() {
        return progressInsight;
    }

    public void setProgressInsight(ProjectProgressInsight progressInsight) {
        this.progressInsight = progressInsight;
    }

    public TeamPerformanceInsight getTeamInsight() {
        return teamInsight;
    }

    public void setTeamInsight(TeamPerformanceInsight teamInsight) {
        this.teamInsight = teamInsight;
    }

    public List<RiskInsight> getRisks() {
        return risks;
    }

    public void setRisks(List<RiskInsight> risks) {
        this.risks = risks;
    }

    public List<TrendInsight> getTrends() {
        return trends;
    }

    public void setTrends(List<TrendInsight> trends) {
        this.trends = trends;
    }

    public Map<String, Object> getPredictions() {
        return predictions;
    }

    public void setPredictions(Map<String, Object> predictions) {
        this.predictions = predictions;
    }

    public Map<String, Object> getComparisons() {
        return comparisons;
    }

    public void setComparisons(Map<String, Object> comparisons) {
        this.comparisons = comparisons;
    }

    @Override
    public String toString() {
        return "AIProjectInsightResponse{" +
                "projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", analysisStartDate=" + analysisStartDate +
                ", analysisEndDate=" + analysisEndDate +
                ", generatedAt=" + generatedAt +
                '}';
    }
}