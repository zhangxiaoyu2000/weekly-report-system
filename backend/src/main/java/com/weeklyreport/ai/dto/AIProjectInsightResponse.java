package com.weeklyreport.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI项目洞察响应DTO
 */
public class AIProjectInsightResponse {
    
    @NotNull
    private Long projectId;
    
    private String projectName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime analysisTime;
    
    private ProjectProgressInsight progressInsight;
    
    private List<RiskInsight> riskInsights;
    
    private TeamPerformanceInsight teamPerformance;
    
    private List<TrendInsight> trendInsights;
    
    private Map<String, Object> additionalMetrics;
    
    // 构造函数
    public AIProjectInsightResponse() {}
    
    public AIProjectInsightResponse(Long projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.analysisTime = LocalDateTime.now();
    }
    
    // Getter和Setter方法
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
    
    public LocalDateTime getAnalysisTime() {
        return analysisTime;
    }
    
    public void setAnalysisTime(LocalDateTime analysisTime) {
        this.analysisTime = analysisTime;
    }
    
    public ProjectProgressInsight getProgressInsight() {
        return progressInsight;
    }
    
    public void setProgressInsight(ProjectProgressInsight progressInsight) {
        this.progressInsight = progressInsight;
    }
    
    public List<RiskInsight> getRiskInsights() {
        return riskInsights;
    }
    
    public void setRiskInsights(List<RiskInsight> riskInsights) {
        this.riskInsights = riskInsights;
    }
    
    public TeamPerformanceInsight getTeamPerformance() {
        return teamPerformance;
    }
    
    public void setTeamPerformance(TeamPerformanceInsight teamPerformance) {
        this.teamPerformance = teamPerformance;
    }
    
    public List<TrendInsight> getTrendInsights() {
        return trendInsights;
    }
    
    public void setTrendInsights(List<TrendInsight> trendInsights) {
        this.trendInsights = trendInsights;
    }
    
    public Map<String, Object> getAdditionalMetrics() {
        return additionalMetrics;
    }
    
    public void setAdditionalMetrics(Map<String, Object> additionalMetrics) {
        this.additionalMetrics = additionalMetrics;
    }
    
    /**
     * 项目进度洞察内嵌类
     */
    public static class ProjectProgressInsight {
        private Double completionRate;
        private Integer tasksCompleted;
        private Integer totalTasks;
        private String progressStatus;
        private String progressSummary;
        private LocalDateTime estimatedCompletion;
        
        // 构造函数
        public ProjectProgressInsight() {}
        
        public ProjectProgressInsight(Double completionRate, Integer tasksCompleted, Integer totalTasks) {
            this.completionRate = completionRate;
            this.tasksCompleted = tasksCompleted;
            this.totalTasks = totalTasks;
        }
        
        // Getter和Setter方法
        public Double getCompletionRate() {
            return completionRate;
        }
        
        public void setCompletionRate(Double completionRate) {
            this.completionRate = completionRate;
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
        
        public String getProgressStatus() {
            return progressStatus;
        }
        
        public void setProgressStatus(String progressStatus) {
            this.progressStatus = progressStatus;
        }
        
        public LocalDateTime getEstimatedCompletion() {
            return estimatedCompletion;
        }
        
        public void setEstimatedCompletion(LocalDateTime estimatedCompletion) {
            this.estimatedCompletion = estimatedCompletion;
        }
        
        public String getProgressSummary() {
            return progressSummary;
        }
        
        public void setProgressSummary(String progressSummary) {
            this.progressSummary = progressSummary;
        }
        
        // Alias methods for compatibility
        public void setCompletionPercentage(double completionPercentage) {
            this.completionRate = completionPercentage / 100.0;
        }
        
        public double getCompletionPercentage() {
            return completionRate != null ? completionRate * 100.0 : 0.0;
        }
    }
    
    /**
     * 风险洞察内嵌类
     */
    public static class RiskInsight {
        private String riskType;
        private String severity; // LOW, MEDIUM, HIGH, CRITICAL
        private String description;
        private Double probability;
        private List<String> mitigation;
        
        // 构造函数
        public RiskInsight() {}
        
        public RiskInsight(String riskType, String severity, String description) {
            this.riskType = riskType;
            this.severity = severity;
            this.description = description;
        }
        
        // Getter和Setter方法
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
    }
    
    /**
     * 团队绩效洞察内嵌类
     */
    public static class TeamPerformanceInsight {
        private Double averageProductivity;
        private Integer activeMembers;
        private Map<String, Double> memberProductivity;
        private List<String> strengths;
        private List<String> improvements;
        
        // 构造函数
        public TeamPerformanceInsight() {}
        
        // Getter和Setter方法
        public Double getAverageProductivity() {
            return averageProductivity;
        }
        
        public void setAverageProductivity(Double averageProductivity) {
            this.averageProductivity = averageProductivity;
        }
        
        public Integer getActiveMembers() {
            return activeMembers;
        }
        
        public void setActiveMembers(Integer activeMembers) {
            this.activeMembers = activeMembers;
        }
        
        public Map<String, Double> getMemberProductivity() {
            return memberProductivity;
        }
        
        public void setMemberProductivity(Map<String, Double> memberProductivity) {
            this.memberProductivity = memberProductivity;
        }
        
        public List<String> getStrengths() {
            return strengths;
        }
        
        public void setStrengths(List<String> strengths) {
            this.strengths = strengths;
        }
        
        public List<String> getImprovements() {
            return improvements;
        }
        
        public void setImprovements(List<String> improvements) {
            this.improvements = improvements;
        }
    }
    
    /**
     * 趋势洞察内嵌类
     */
    public static class TrendInsight {
        private String trendType;
        private String direction; // UP, DOWN, STABLE
        private String description;
        private Double magnitude;
        private List<String> factors;
        
        // 构造函数
        public TrendInsight() {}
        
        public TrendInsight(String trendType, String direction, String description) {
            this.trendType = trendType;
            this.direction = direction;
            this.description = description;
        }
        
        // Getter和Setter方法
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
        
        public Double getMagnitude() {
            return magnitude;
        }
        
        public void setMagnitude(Double magnitude) {
            this.magnitude = magnitude;
        }
        
        public List<String> getFactors() {
            return factors;
        }
        
        public void setFactors(List<String> factors) {
            this.factors = factors;
        }
    }
}