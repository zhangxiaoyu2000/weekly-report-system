package com.weeklyreport.weeklyreport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * DTO for updating existing weekly reports - 与WeeklyReportCreateRequest保持一致的结构
 */
public class WeeklyReportUpdateRequest {

    // 用户ID - 可选字段，将从认证用户中获取
    private Long userId;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    // 周报日期 - 可选字段，可以从weekStart/weekEnd生成
    @Size(max = 50, message = "Report week must not exceed 50 characters")
    private String reportWeek;

    // 兼容测试接口的字段
    private String weekStart;
    private String weekEnd;
    private Long projectId;

    @Valid
    private ContentDTO content;

    @Valid
    private NextWeekPlanDTO nextWeekPlan;

    @Size(max = 2000, message = "Additional notes must not exceed 2000 characters")
    private String additionalNotes;

    @Size(max = 2000, message = "Development opportunities must not exceed 2000 characters")
    private String developmentOpportunities;

    @Min(value = 1, message = "Priority must be between 1 and 10")
    @Max(value = 10, message = "Priority must be between 1 and 10")
    private Integer priority;

    private Long templateId;

    // Constructors
    public WeeklyReportUpdateRequest() {}

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    public ContentDTO getContent() {
        return content;
    }

    public void setContent(ContentDTO content) {
        this.content = content;
    }

    public NextWeekPlanDTO getNextWeekPlan() {
        return nextWeekPlan;
    }

    public void setNextWeekPlan(NextWeekPlanDTO nextWeekPlan) {
        this.nextWeekPlan = nextWeekPlan;
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

    public String getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(String weekStart) {
        this.weekStart = weekStart;
    }

    public String getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(String weekEnd) {
        this.weekEnd = weekEnd;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        return "WeeklyReportUpdateRequest{" +
                "userId=" + userId +
                ", title='" + title + '\'' +
                ", reportWeek='" + reportWeek + '\'' +
                ", content=" + content +
                ", nextWeekPlan=" + nextWeekPlan +
                ", additionalNotes='" + additionalNotes + '\'' +
                ", developmentOpportunities='" + developmentOpportunities + '\'' +
                '}';
    }

    /**
     * 本周汇报内容 DTO - 重用WeeklyReportCreateRequest中的结构
     */
    public static class ContentDTO {
        
        @JsonProperty("routineTasks")
        @Valid
        private List<RoutineTaskDTO> routineTasks;
        
        @JsonProperty("developmentalTasks")
        @Valid  
        private List<DevelopmentalTaskDTO> developmentalTasks;

        public ContentDTO() {}

        public List<RoutineTaskDTO> getRoutineTasks() {
            return routineTasks;
        }

        public void setRoutineTasks(List<RoutineTaskDTO> routine_tasks) {
            routineTasks = routine_tasks;
        }

        public List<DevelopmentalTaskDTO> getDevelopmentalTasks() {
            return developmentalTasks;
        }

        public void setDevelopmentalTasks(List<DevelopmentalTaskDTO> developmental_tasks) {
            developmentalTasks = developmental_tasks;
        }

        @Override
        public String toString() {
            return "ContentDTO{" +
                    "routineTasks=" + routineTasks +
                    ", developmentalTasks=" + developmentalTasks +
                    '}';
        }
    }

    /**
     * 日常性任务 DTO
     */
    public static class RoutineTaskDTO {
        
        @NotBlank(message = "Task ID cannot be blank")
        private String task_id;
        
        @Size(max = 500, message = "Actual result must not exceed 500 characters")
        private String actual_result;
        
        @Size(max = 1000, message = "Analysis must not exceed 1000 characters")
        @JsonProperty("AnalysisofResultDifferences")
        private String analysisofResultDifferences;

        public RoutineTaskDTO() {}

        public String getTask_id() {
            return task_id;
        }

        public void setTask_id(String task_id) {
            this.task_id = task_id;
        }

        public String getActual_result() {
            return actual_result;
        }

        public void setActual_result(String actual_result) {
            this.actual_result = actual_result;
        }

        public String getAnalysisofResultDifferences() {
            return analysisofResultDifferences;
        }

        public void setAnalysisofResultDifferences(String analysisofResultDifferences) {
            this.analysisofResultDifferences = analysisofResultDifferences;
        }
    }

    /**
     * 发展性任务 DTO
     */
    public static class DevelopmentalTaskDTO {
        
        @NotBlank(message = "Project ID cannot be blank")
        private String project_id;
        
        @NotBlank(message = "Phase ID cannot be blank")
        private String phase_id;
        
        @Size(max = 500, message = "Actual result must not exceed 500 characters")
        private String actual_result;
        
        @Size(max = 1000, message = "Analysis must not exceed 1000 characters")
        @JsonProperty("AnalysisofResultDifferences")
        private String analysisofResultDifferences;

        public DevelopmentalTaskDTO() {}

        public String getProject_id() {
            return project_id;
        }

        public void setProject_id(String project_id) {
            this.project_id = project_id;
        }

        public String getPhase_id() {
            return phase_id;
        }

        public void setPhase_id(String phase_id) {
            this.phase_id = phase_id;
        }

        public String getActual_result() {
            return actual_result;
        }

        public void setActual_result(String actual_result) {
            this.actual_result = actual_result;
        }

        public String getAnalysisofResultDifferences() {
            return analysisofResultDifferences;
        }

        public void setAnalysisofResultDifferences(String analysisofResultDifferences) {
            this.analysisofResultDifferences = analysisofResultDifferences;
        }
    }

    /**
     * 下周规划 DTO
     */
    public static class NextWeekPlanDTO {
        
        @JsonProperty("routineTasks")
        private List<NextWeekRoutineTaskDTO> routineTasks;
        
        @JsonProperty("developmentalTasks")
        private List<NextWeekDevelopmentalTaskDTO> developmentalTasks;

        // 兼容小写字段名的附加字段
        @JsonProperty("routine_tasks")
        private List<NextWeekRoutineTaskDTO> routine_tasks;
        
        @JsonProperty("developmental_tasks")
        private List<NextWeekDevelopmentalTaskDTO> developmental_tasks;

        public NextWeekPlanDTO() {}

        public List<NextWeekRoutineTaskDTO> getRoutineTasks() {
            // 优先返回有数据的字段，兼容大小写字段名
            if (routineTasks != null && !routineTasks.isEmpty()) {
                return routineTasks;
            }
            if (routine_tasks != null && !routine_tasks.isEmpty()) {
                return routine_tasks;
            }
            return routineTasks != null ? routineTasks : routine_tasks;
        }

        public void setRoutineTasks(List<NextWeekRoutineTaskDTO> routine_tasks) {
            this.routineTasks = routine_tasks;
            this.routine_tasks = routine_tasks; // 同时设置两个字段保证兼容性
        }

        public List<NextWeekDevelopmentalTaskDTO> getDevelopmentalTasks() {
            // 优先返回有数据的字段，兼容大小写字段名
            if (developmentalTasks != null && !developmentalTasks.isEmpty()) {
                return developmentalTasks;
            }
            if (developmental_tasks != null && !developmental_tasks.isEmpty()) {
                return developmental_tasks;
            }
            return developmentalTasks != null ? developmentalTasks : developmental_tasks;
        }

        public void setDevelopmentalTasks(List<NextWeekDevelopmentalTaskDTO> developmental_tasks) {
            this.developmentalTasks = developmental_tasks;
            this.developmental_tasks = developmental_tasks; // 同时设置两个字段保证兼容性
        }
    }

    /**
     * 下周日常性任务 DTO
     */
    public static class NextWeekRoutineTaskDTO {
        
        @NotBlank(message = "Task ID cannot be blank")
        private String task_id;

        public NextWeekRoutineTaskDTO() {}

        public String getTask_id() {
            return task_id;
        }

        public void setTask_id(String task_id) {
            this.task_id = task_id;
        }
    }

    /**
     * 下周发展性任务 DTO
     */
    public static class NextWeekDevelopmentalTaskDTO {
        
        @NotBlank(message = "Project ID cannot be blank")
        private String project_id;
        
        @NotBlank(message = "Phase ID cannot be blank")
        private String phase_id;

        public NextWeekDevelopmentalTaskDTO() {}

        public String getProject_id() {
            return project_id;
        }

        public void setProject_id(String project_id) {
            this.project_id = project_id;
        }

        public String getPhase_id() {
            return phase_id;
        }

        public void setPhase_id(String phase_id) {
            this.phase_id = phase_id;
        }
    }
}