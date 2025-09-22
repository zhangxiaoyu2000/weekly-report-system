package com.weeklyreport.dto.weeklyreport;

import jakarta.validation.constraints.NotNull;

/**
 * Weekly Report Task Reference DTOs
 * Based on error3.md requirements for structured task references
 */
public class WeeklyReportTaskReference {
    
    /**
     * Routine Task Reference for weekly reports
     */
    public static class RoutineTaskReference {
        @NotNull
        private Long taskId; // 对应任务表中的日常性任务的id 外键
        private String actualResult; // 实际结果 (仅本周汇报)
        private String analysisOfResultDifferences; // 结果差异分析 (仅本周汇报)
        
        // Constructors
        public RoutineTaskReference() {}
        
        public RoutineTaskReference(Long taskId) {
            this.taskId = taskId;
        }
        
        public RoutineTaskReference(Long taskId, String actualResult, String analysisOfResultDifferences) {
            this.taskId = taskId;
            this.actualResult = actualResult;
            this.analysisOfResultDifferences = analysisOfResultDifferences;
        }
        
        // Getters and Setters
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        
        public String getActualResult() { return actualResult; }
        public void setActualResult(String actualResult) { this.actualResult = actualResult; }
        
        public String getAnalysisOfResultDifferences() { return analysisOfResultDifferences; }
        public void setAnalysisOfResultDifferences(String analysisOfResultDifferences) { 
            this.analysisOfResultDifferences = analysisOfResultDifferences; 
        }
    }
    
    /**
     * Development Task Reference for weekly reports
     */
    public static class DevelopmentTaskReference {
        @NotNull
        private Long projectId; // 对应项目表中的项目id 外键
        private Long phaseId; // 对应该项目的某个阶段的id 外键 (可选)
        private String actualResult; // 实际结果 (仅本周汇报)
        private String analysisOfResultDifferences; // 结果差异分析 (仅本周汇报)
        
        // Constructors
        public DevelopmentTaskReference() {}
        
        public DevelopmentTaskReference(Long projectId) {
            this.projectId = projectId;
        }
        
        public DevelopmentTaskReference(Long projectId, Long phaseId) {
            this.projectId = projectId;
            this.phaseId = phaseId;
        }
        
        public DevelopmentTaskReference(Long projectId, Long phaseId, String actualResult, String analysisOfResultDifferences) {
            this.projectId = projectId;
            this.phaseId = phaseId;
            this.actualResult = actualResult;
            this.analysisOfResultDifferences = analysisOfResultDifferences;
        }
        
        // Getters and Setters
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        
        public Long getPhaseId() { return phaseId; }
        public void setPhaseId(Long phaseId) { this.phaseId = phaseId; }
        
        public String getActualResult() { return actualResult; }
        public void setActualResult(String actualResult) { this.actualResult = actualResult; }
        
        public String getAnalysisOfResultDifferences() { return analysisOfResultDifferences; }
        public void setAnalysisOfResultDifferences(String analysisOfResultDifferences) { 
            this.analysisOfResultDifferences = analysisOfResultDifferences; 
        }
    }
    
    /**
     * Report Section containing tasks
     */
    public static class ReportSection {
        private java.util.List<RoutineTaskReference> routineTasks = new java.util.ArrayList<>();
        private java.util.List<DevelopmentTaskReference> developmentTasks = new java.util.ArrayList<>();
        
        // Constructors
        public ReportSection() {}
        
        // Getters and Setters
        public java.util.List<RoutineTaskReference> getRoutineTasks() { return routineTasks; }
        public void setRoutineTasks(java.util.List<RoutineTaskReference> routineTasks) { 
            this.routineTasks = routineTasks != null ? routineTasks : new java.util.ArrayList<>(); 
        }
        
        public java.util.List<DevelopmentTaskReference> getDevelopmentTasks() { return developmentTasks; }
        public void setDevelopmentTasks(java.util.List<DevelopmentTaskReference> developmentTasks) { 
            this.developmentTasks = developmentTasks != null ? developmentTasks : new java.util.ArrayList<>(); 
        }
    }
    
    /**
     * Complete Weekly Report Content Structure
     */
    public static class WeeklyReportContent {
        private ReportSection thisWeekReport = new ReportSection(); // 本周汇报
        private ReportSection nextWeekPlan = new ReportSection(); // 下周规划
        
        // Constructors
        public WeeklyReportContent() {}
        
        // Getters and Setters
        public ReportSection getThisWeekReport() { return thisWeekReport; }
        public void setThisWeekReport(ReportSection thisWeekReport) { 
            this.thisWeekReport = thisWeekReport != null ? thisWeekReport : new ReportSection(); 
        }
        
        public ReportSection getNextWeekPlan() { return nextWeekPlan; }
        public void setNextWeekPlan(ReportSection nextWeekPlan) { 
            this.nextWeekPlan = nextWeekPlan != null ? nextWeekPlan : new ReportSection(); 
        }
    }
}