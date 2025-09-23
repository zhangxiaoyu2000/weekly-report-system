package com.weeklyreport.dto.weeklyreport;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * DTO for creating new weekly reports - 严格按照error3.md第31-67行数据结构设计
 * 
 * 对应error3.md前端提交的数据格式：
 * {
 *   "userid":"",
 *   "title":"22", 
 *   "reportWeek":"几月第几周（周几）",
 *   "content":{
 *     "Routine_tasks":[{"task_id":"", "actual_result":"", "AnalysisofResultDifferences":""}],
 *     "Developmental_tasks":[{"project_id":"", "phase_id":"", "actual_result":"", "AnalysisofResultDifferences":""}]
 *   },
 *   "nextWeekPlan":{
 *     "Routine_tasks":[{"task_id":""}],
 *     "Developmental_tasks":[{"project_id":"", "phase_id":""}]
 *   },
 *   "additionalNotes":"22222",
 *   "developmentOpportunities":"22222"
 * }
 */
public class WeeklyReportCreateRequest {

    // 用户ID - 可选字段，将从认证用户中获取
    private Long userId;                    // # 提交周报的用户id (严格按照WeeklyReport.java的userId字段)

    // 周报标题 - 可选字段，可以自动生成
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;                   // # 这是周报标题 (error3.md 第33行)

    // 周报日期 - 可选字段，可以从weekStart/weekEnd生成
    @Size(max = 50, message = "Report week must not exceed 50 characters")
    private String reportWeek;              // # 周报日期 几月第几周（周几） (error3.md 第34行)
    
    // 兼容测试接口的字段
    private String weekStart;               // 周开始日期 (YYYY-MM-DD)
    private String weekEnd;                 // 周结束日期 (YYYY-MM-DD)
    private Long projectId;                 // 关联项目ID

    @Valid
    private ContentDTO content;             // # 本周汇报内容 (error3.md 第35行) - 可选字段

    @Valid
    private NextWeekPlanDTO nextWeekPlan;   // # 下周规划 (error3.md 第52行)

    @Size(max = 2000, message = "Additional notes must not exceed 2000 characters")
    private String additionalNotes;         // # 其他备注 (error3.md 第65行)

    @Size(max = 2000, message = "Development opportunities must not exceed 2000 characters")
    private String developmentOpportunities; // # 可发展性清单 (error3.md 第66行)

    // Constructors
    public WeeklyReportCreateRequest() {}

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

    // 兼容字段的getter和setter
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

    // 兼容性方法 - 简化版本中不支持Template
    public Long getTemplateId() {
        return null; // 简化版本中不支持模板
    }
    
    public void setTemplateId(Long templateId) {
        // 简化版本中忽略
    }
    
    // 兼容性方法 - 简化版本中没有直接的项目ID
    public Long getProjectId() {
        return null; // 简化版本中项目ID通过content.Developmental_tasks获取
    }
    
    public void setProjectId(Long projectId) {
        // 简化版本中忽略
    }
    
    // 兼容性方法 - 简化版本中不支持优先级
    public Integer getPriority() {
        return 1; // 简化版本中返回默认优先级
    }
    
    public void setPriority(Integer priority) {
        // 简化版本中忽略
    }

    @Override
    public String toString() {
        return "WeeklyReportCreateRequest{" +
                "userId=" + userId +
                ", title='" + title + '\'' +
                ", reportWeek='" + reportWeek + '\'' +
                ", content=" + content +
                ", additionalNotes='" + additionalNotes + '\'' +
                ", developmentOpportunities='" + developmentOpportunities + '\'' +
                '}';
    }

    /**
     * 本周汇报内容 DTO - 对应error3.md第35-51行
     */
    public static class ContentDTO {
        
        @JsonProperty("routineTasks")
        @Valid
        private List<RoutineTaskDTO> routineTasks;      // # 日常性任务 (error3.md 第36行)
        
        @JsonProperty("developmentalTasks")
        @Valid  
        private List<DevelopmentalTaskDTO> developmentalTasks; // # 发展性任务 (error3.md 第43行)

        public ContentDTO() {}

        @Override
        public String toString() {
            return "ContentDTO{" +
                    "routineTasks=" + routineTasks +
                    ", developmentalTasks=" + developmentalTasks +
                    '}';
        }

        public List<RoutineTaskDTO> getRoutineTasks() {
            return routineTasks;
        }

        public void setRoutineTasks(List<RoutineTaskDTO> routineTasks) {
            this.routineTasks = routineTasks;
        }

        public List<DevelopmentalTaskDTO> getDevelopmentalTasks() {
            return developmentalTasks;
        }

        public void setDevelopmentalTasks(List<DevelopmentalTaskDTO> developmentalTasks) {
            this.developmentalTasks = developmentalTasks;
        }
        
        // 支持下划线格式的字段名（前端兼容性）
        @JsonProperty("Routine_tasks")
        public void setRoutine_tasks(List<RoutineTaskDTO> routineTasks) {
            this.routineTasks = routineTasks;
        }
        
        @JsonProperty("Developmental_tasks")
        public void setDevelopmental_tasks(List<DevelopmentalTaskDTO> developmentalTasks) {
            this.developmentalTasks = developmentalTasks;
        }
    }

    /**
     * 日常性任务 DTO - 对应error3.md第37-42行
     */
    public static class RoutineTaskDTO {
        
        @NotBlank(message = "Task ID cannot be blank")
        private String task_id;                         // # 对应任务表中的日常性任务的id 外键 (error3.md 第38行)
        
        @Size(max = 500, message = "Actual result must not exceed 500 characters")
        private String actual_result;                   // # 实际结果 (error3.md 第39行)
        
        @Size(max = 1000, message = "Analysis must not exceed 1000 characters")
        @JsonProperty("AnalysisofResultDifferences")
        private String analysisofResultDifferences;     // # 结果差异分析 (error3.md 第40行)

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
     * 发展性任务 DTO - 对应error3.md第44-50行
     */
    public static class DevelopmentalTaskDTO {
        
        @NotBlank(message = "Project ID cannot be blank")
        private String project_id;                      // # 对应项目表中的项目id。外键 (error3.md 第45行)
        
        @NotBlank(message = "Phase ID cannot be blank")
        private String phase_id;                        // # 对应该项目的某个阶段的id 外键 (error3.md 第46行)
        
        @Size(max = 500, message = "Actual result must not exceed 500 characters")
        private String actual_result;                   // # 实际结果 (error3.md 第47行)
        
        @Size(max = 1000, message = "Analysis must not exceed 1000 characters")
        @JsonProperty("AnalysisofResultDifferences")
        private String analysisofResultDifferences;     // # 结果差异分析 (error3.md 第48行)

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
     * 下周规划 DTO - 对应error3.md第52-64行
     */
    public static class NextWeekPlanDTO {
        
        @JsonProperty("routineTasks")
        private List<NextWeekRoutineTaskDTO> routineTasks;      // # 日常性任务 (error3.md 第53行)
        
        @JsonProperty("developmentalTasks")
        private List<NextWeekDevelopmentalTaskDTO> developmentalTasks; // # 发展性任务 (error3.md 第58行)

        public NextWeekPlanDTO() {}

        public List<NextWeekRoutineTaskDTO> getRoutineTasks() {
            return routineTasks;
        }

        public void setRoutineTasks(List<NextWeekRoutineTaskDTO> routineTasks) {
            this.routineTasks = routineTasks;
        }

        public List<NextWeekDevelopmentalTaskDTO> getDevelopmentalTasks() {
            return developmentalTasks;
        }

        public void setDevelopmentalTasks(List<NextWeekDevelopmentalTaskDTO> developmentalTasks) {
            this.developmentalTasks = developmentalTasks;
        }
        
        // 支持下划线格式的字段名（前端兼容性）
        @JsonProperty("Routine_tasks")
        public void setRoutine_tasks(List<NextWeekRoutineTaskDTO> routineTasks) {
            this.routineTasks = routineTasks;
        }
        
        @JsonProperty("Developmental_tasks")
        public void setDevelopmental_tasks(List<NextWeekDevelopmentalTaskDTO> developmentalTasks) {
            this.developmentalTasks = developmentalTasks;
        }
    }

    /**
     * 下周日常性任务 DTO - 对应error3.md第54-57行
     */
    public static class NextWeekRoutineTaskDTO {
        
        @NotBlank(message = "Task ID cannot be blank")
        private String task_id;     // # 对应任务表中的日常性任务的id 外键 (error3.md 第55行)

        public NextWeekRoutineTaskDTO() {}

        public String getTask_id() {
            return task_id;
        }

        public void setTask_id(String task_id) {
            this.task_id = task_id;
        }
    }

    /**
     * 下周发展性任务 DTO - 对应error3.md第59-63行
     */
    public static class NextWeekDevelopmentalTaskDTO {
        
        @NotBlank(message = "Project ID cannot be blank")
        private String project_id;  // # 对应项目表中的项目id 外键 (error3.md 第60行)
        
        @NotBlank(message = "Phase ID cannot be blank")
        private String phase_id;    // # 对应该项目的某个阶段的id 外键 (error3.md 第61行)

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