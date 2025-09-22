package com.weeklyreport.dto.weeklyreport;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 周报V2创建和更新请求DTO
 * 基于error3.md中定义的结构化格式
 */
public class WeeklyReportV2Request {

    @NotBlank(message = "周报标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    @NotBlank(message = "周报周次不能为空")
    @JsonProperty("reportWeek")
    private String reportWeek; // 几月第几周（周几）

    @NotNull(message = "结构化内容不能为空")
    @Valid
    private WeeklyReportContentV2 content;

    @JsonProperty("additionalNotes")
    private String additionalNotes; // 其他备注

    @JsonProperty("developmentOpportunities")
    private String developmentOpportunities; // 可发展性清单

    // 构造函数
    public WeeklyReportV2Request() {}

    public WeeklyReportV2Request(String title, String reportWeek, WeeklyReportContentV2 content) {
        this.title = title;
        this.reportWeek = reportWeek;
        this.content = content;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReportWeek() {
        return reportWeek;
    }

    public void setReportWeek(String reportWeek) {
        this.reportWeek = reportWeek;
    }

    public WeeklyReportContentV2 getContent() {
        return content;
    }

    public void setContent(WeeklyReportContentV2 content) {
        this.content = content;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public String getDevelopmentOpportunities() {
        return developmentOpportunities;
    }

    public void setDevelopmentOpportunities(String developmentOpportunities) {
        this.developmentOpportunities = developmentOpportunities;
    }

    @Override
    public String toString() {
        return "WeeklyReportV2Request{" +
                "title='" + title + '\'' +
                ", reportWeek='" + reportWeek + '\'' +
                ", additionalNotes='" + additionalNotes + '\'' +
                ", developmentOpportunities='" + developmentOpportunities + '\'' +
                '}';
    }

    /**
     * 结构化内容DTO
     */
    public static class WeeklyReportContentV2 {
        
        @NotNull(message = "本周汇报不能为空")
        @Valid
        @JsonProperty("thisWeekReport")
        private ThisWeekReport thisWeekReport;

        @NotNull(message = "下周规划不能为空")
        @Valid
        @JsonProperty("nextWeekPlan")
        private NextWeekPlan nextWeekPlan;

        // 构造函数
        public WeeklyReportContentV2() {}

        public WeeklyReportContentV2(ThisWeekReport thisWeekReport, NextWeekPlan nextWeekPlan) {
            this.thisWeekReport = thisWeekReport;
            this.nextWeekPlan = nextWeekPlan;
        }

        // Getters and Setters
        public ThisWeekReport getThisWeekReport() {
            return thisWeekReport;
        }

        public void setThisWeekReport(ThisWeekReport thisWeekReport) {
            this.thisWeekReport = thisWeekReport;
        }

        public NextWeekPlan getNextWeekPlan() {
            return nextWeekPlan;
        }

        public void setNextWeekPlan(NextWeekPlan nextWeekPlan) {
            this.nextWeekPlan = nextWeekPlan;
        }
    }

    /**
     * 本周汇报DTO
     */
    public static class ThisWeekReport {
        
        @JsonProperty("routineTasks")
        private List<RoutineTaskReference> routineTasks;

        @JsonProperty("developmentTasks")
        private List<DevelopmentTaskReference> developmentTasks;

        // 构造函数
        public ThisWeekReport() {}

        public ThisWeekReport(List<RoutineTaskReference> routineTasks, 
                             List<DevelopmentTaskReference> developmentTasks) {
            this.routineTasks = routineTasks;
            this.developmentTasks = developmentTasks;
        }

        // Getters and Setters
        public List<RoutineTaskReference> getRoutineTasks() {
            return routineTasks;
        }

        public void setRoutineTasks(List<RoutineTaskReference> routineTasks) {
            this.routineTasks = routineTasks;
        }

        public List<DevelopmentTaskReference> getDevelopmentTasks() {
            return developmentTasks;
        }

        public void setDevelopmentTasks(List<DevelopmentTaskReference> developmentTasks) {
            this.developmentTasks = developmentTasks;
        }
    }

    /**
     * 下周规划DTO
     */
    public static class NextWeekPlan {
        
        @JsonProperty("routineTasks")
        private List<RoutineTaskPlan> routineTasks;

        @JsonProperty("developmentTasks")
        private List<DevelopmentTaskPlan> developmentTasks;

        // 构造函数
        public NextWeekPlan() {}

        public NextWeekPlan(List<RoutineTaskPlan> routineTasks, 
                           List<DevelopmentTaskPlan> developmentTasks) {
            this.routineTasks = routineTasks;
            this.developmentTasks = developmentTasks;
        }

        // Getters and Setters
        public List<RoutineTaskPlan> getRoutineTasks() {
            return routineTasks;
        }

        public void setRoutineTasks(List<RoutineTaskPlan> routineTasks) {
            this.routineTasks = routineTasks;
        }

        public List<DevelopmentTaskPlan> getDevelopmentTasks() {
            return developmentTasks;
        }

        public void setDevelopmentTasks(List<DevelopmentTaskPlan> developmentTasks) {
            this.developmentTasks = developmentTasks;
        }
    }

    /**
     * 日常任务引用DTO（本周汇报用）
     */
    public static class RoutineTaskReference {
        
        @NotNull(message = "任务ID不能为空")
        @JsonProperty("taskId")
        private Long taskId;

        @JsonProperty("actualResult")
        private String actualResult; // 实际结果

        @JsonProperty("analysisOfResultDifferences")
        private String analysisOfResultDifferences; // 结果差异分析

        // 构造函数
        public RoutineTaskReference() {}

        public RoutineTaskReference(Long taskId, String actualResult, String analysisOfResultDifferences) {
            this.taskId = taskId;
            this.actualResult = actualResult;
            this.analysisOfResultDifferences = analysisOfResultDifferences;
        }

        // Getters and Setters
        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public String getActualResult() {
            return actualResult;
        }

        public void setActualResult(String actualResult) {
            this.actualResult = actualResult;
        }

        public String getAnalysisOfResultDifferences() {
            return analysisOfResultDifferences;
        }

        public void setAnalysisOfResultDifferences(String analysisOfResultDifferences) {
            this.analysisOfResultDifferences = analysisOfResultDifferences;
        }
    }

    /**
     * 发展性任务引用DTO（本周汇报用）
     */
    public static class DevelopmentTaskReference {
        
        @NotNull(message = "项目ID不能为空")
        @JsonProperty("projectId")
        private Long projectId;

        @JsonProperty("phaseId")
        private Long phaseId; // 项目阶段ID，可选

        @JsonProperty("actualResult")
        private String actualResult; // 实际结果

        @JsonProperty("analysisOfResultDifferences")
        private String analysisOfResultDifferences; // 结果差异分析

        // 构造函数
        public DevelopmentTaskReference() {}

        public DevelopmentTaskReference(Long projectId, Long phaseId, 
                                      String actualResult, String analysisOfResultDifferences) {
            this.projectId = projectId;
            this.phaseId = phaseId;
            this.actualResult = actualResult;
            this.analysisOfResultDifferences = analysisOfResultDifferences;
        }

        // Getters and Setters
        public Long getProjectId() {
            return projectId;
        }

        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }

        public Long getPhaseId() {
            return phaseId;
        }

        public void setPhaseId(Long phaseId) {
            this.phaseId = phaseId;
        }

        public String getActualResult() {
            return actualResult;
        }

        public void setActualResult(String actualResult) {
            this.actualResult = actualResult;
        }

        public String getAnalysisOfResultDifferences() {
            return analysisOfResultDifferences;
        }

        public void setAnalysisOfResultDifferences(String analysisOfResultDifferences) {
            this.analysisOfResultDifferences = analysisOfResultDifferences;
        }
    }

    /**
     * 日常任务计划DTO（下周规划用）
     */
    public static class RoutineTaskPlan {
        
        @NotNull(message = "任务ID不能为空")
        @JsonProperty("taskId")
        private Long taskId;

        // 构造函数
        public RoutineTaskPlan() {}

        public RoutineTaskPlan(Long taskId) {
            this.taskId = taskId;
        }

        // Getters and Setters
        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }
    }

    /**
     * 发展性任务计划DTO（下周规划用）
     */
    public static class DevelopmentTaskPlan {
        
        @NotNull(message = "项目ID不能为空")
        @JsonProperty("projectId")
        private Long projectId;

        @JsonProperty("phaseId")
        private Long phaseId; // 项目阶段ID，可选

        // 构造函数
        public DevelopmentTaskPlan() {}

        public DevelopmentTaskPlan(Long projectId, Long phaseId) {
            this.projectId = projectId;
            this.phaseId = phaseId;
        }

        // Getters and Setters
        public Long getProjectId() {
            return projectId;
        }

        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }

        public Long getPhaseId() {
            return phaseId;
        }

        public void setPhaseId(Long phaseId) {
            this.phaseId = phaseId;
        }
    }
}