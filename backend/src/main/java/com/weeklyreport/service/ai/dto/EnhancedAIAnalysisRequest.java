package com.weeklyreport.service.ai.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Enhanced AI analysis request with additional context
 */
public class EnhancedAIAnalysisRequest extends AIAnalysisRequest {
    
    @NotNull(message = "Manager ID cannot be null")
    private Long managerId;
    
    private ProjectData projectData;
    private WeeklyReportData weeklyReportData;
    private AnalysisContext analysisContext;
    
    public EnhancedAIAnalysisRequest() {
        super();
    }
    
    public EnhancedAIAnalysisRequest(String content, AnalysisType analysisType, Long managerId) {
        super(content, analysisType);
        this.managerId = managerId;
    }
    
    // Getters and setters
    public Long getManagerId() {
        return managerId;
    }
    
    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }
    
    public ProjectData getProjectData() {
        return projectData;
    }
    
    public void setProjectData(ProjectData projectData) {
        this.projectData = projectData;
    }
    
    public WeeklyReportData getWeeklyReportData() {
        return weeklyReportData;
    }
    
    public void setWeeklyReportData(WeeklyReportData weeklyReportData) {
        this.weeklyReportData = weeklyReportData;
    }
    
    public AnalysisContext getAnalysisContext() {
        return analysisContext;
    }
    
    public void setAnalysisContext(AnalysisContext analysisContext) {
        this.analysisContext = analysisContext;
    }
    
    /**
     * Analysis context enum
     */
    public enum AnalysisContext {
        PROJECT_FEASIBILITY,
        WEEKLY_REPORT_QUALITY,
        RISK_ASSESSMENT,
        COMPLIANCE_CHECK
    }
    
    /**
     * Project data structure for analysis
     */
    public static class ProjectData {
        private String projectName;
        private String projectContent;
        private String projectMembers;
        private String keyIndicators;
        private String expectedResults;
        private String timeline;
        private String stopLoss;
        private List<ProjectPhase> projectPhases;
        
        public ProjectData() {}
        
        // Getters and setters
        public String getProjectName() {
            return projectName;
        }
        
        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }
        
        public String getProjectContent() {
            return projectContent;
        }
        
        public void setProjectContent(String projectContent) {
            this.projectContent = projectContent;
        }
        
        public String getProjectMembers() {
            return projectMembers;
        }
        
        public void setProjectMembers(String projectMembers) {
            this.projectMembers = projectMembers;
        }
        
        public String getKeyIndicators() {
            return keyIndicators;
        }
        
        public void setKeyIndicators(String keyIndicators) {
            this.keyIndicators = keyIndicators;
        }
        
        public String getExpectedResults() {
            return expectedResults;
        }
        
        public void setExpectedResults(String expectedResults) {
            this.expectedResults = expectedResults;
        }
        
        public String getTimeline() {
            return timeline;
        }
        
        public void setTimeline(String timeline) {
            this.timeline = timeline;
        }
        
        public String getStopLoss() {
            return stopLoss;
        }
        
        public void setStopLoss(String stopLoss) {
            this.stopLoss = stopLoss;
        }

        public List<ProjectPhase> getProjectPhases() {
            return projectPhases;
        }

        public void setProjectPhases(List<ProjectPhase> projectPhases) {
            this.projectPhases = projectPhases;
        }
    }
    
    /**
     * Project phase data structure for AI analysis
     */
    public static class ProjectPhase {
        private String phaseName;
        private Integer phaseOrder;
        private String phaseDescription;
        private String assignedMembers;
        private String timeline;
        private String keyIndicators;
        private String estimatedResults;
        private String status;
        
        public ProjectPhase() {}
        
        // Getters and setters
        public String getPhaseName() {
            return phaseName;
        }
        
        public void setPhaseName(String phaseName) {
            this.phaseName = phaseName;
        }
        
        public Integer getPhaseOrder() {
            return phaseOrder;
        }
        
        public void setPhaseOrder(Integer phaseOrder) {
            this.phaseOrder = phaseOrder;
        }
        
        public String getPhaseDescription() {
            return phaseDescription;
        }
        
        public void setPhaseDescription(String phaseDescription) {
            this.phaseDescription = phaseDescription;
        }
        
        public String getAssignedMembers() {
            return assignedMembers;
        }
        
        public void setAssignedMembers(String assignedMembers) {
            this.assignedMembers = assignedMembers;
        }
        
        public String getTimeline() {
            return timeline;
        }
        
        public void setTimeline(String timeline) {
            this.timeline = timeline;
        }
        
        public String getKeyIndicators() {
            return keyIndicators;
        }
        
        public void setKeyIndicators(String keyIndicators) {
            this.keyIndicators = keyIndicators;
        }
        
        public String getEstimatedResults() {
            return estimatedResults;
        }
        
        public void setEstimatedResults(String estimatedResults) {
            this.estimatedResults = estimatedResults;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
    
    /**
     * Weekly report data structure for analysis
     */
    public static class WeeklyReportData {
        private String title;
        private String content;
        // 删除不匹配UI的字段: workSummary, achievements, challenges, nextWeekPlan
        // 这些数据现在从任务中生成，不再直接存储在周报中
        private String developmentOpportunities;
        private String additionalNotes;
        
        public WeeklyReportData() {}
        
        // Getters and setters
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public String getDevelopmentOpportunities() {
            return developmentOpportunities;
        }
        
        public void setDevelopmentOpportunities(String developmentOpportunities) {
            this.developmentOpportunities = developmentOpportunities;
        }
        
        public String getAdditionalNotes() {
            return additionalNotes;
        }
        
        public void setAdditionalNotes(String additionalNotes) {
            this.additionalNotes = additionalNotes;
        }
    }
}