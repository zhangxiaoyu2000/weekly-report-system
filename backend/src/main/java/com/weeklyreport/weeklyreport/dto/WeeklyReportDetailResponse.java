package com.weeklyreport.weeklyreport.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.weeklyreport.weeklyreport.entity.WeeklyReport;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 完整周报详情响应DTO - 包含深度查询的关联数据
 * 与WeeklyReportCreateRequest结构保持一致，并添加详细的关联信息
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeeklyReportDetailResponse {

    private Long id;                                // 周报ID
    private String title;                           // 周报标题
    private String reportWeek;                      // 周报日期
    private String status;                  // 审批状态
    private Long userId;                            // 用户ID
    private String username;                        // 用户名
    private String creatorName;                     // 创建者真实姓名
    private String creatorUsername;                 // 创建者用户名
    private LocalDateTime createdAt;                // 创建时间
    private LocalDateTime updatedAt;                // 更新时间
    private LocalDateTime submittedAt;              // 提交时间

    @Valid
    private ContentDetailDTO content;               // 本周汇报内容

    @Valid  
    private NextWeekPlanDetailDTO nextWeekPlan;     // 下周规划

    private String additionalNotes;                 // 其他备注
    private String developmentOpportunities;        // 可发展性清单
    
    // AI分析相关字段
    private Long aiAnalysisId;                      // AI分析ID
    private String aiAnalysisResult;                // AI分析结果
    private Double aiConfidence;                    // AI分析置信度
    private String aiAnalysisStatus;                // AI分析状态
    private LocalDateTime aiAnalysisCompletedAt;    // AI分析完成时间
    
    // 审批相关字段
    private String rejectionReason;                 // 拒绝理由
    private String rejectedBy;                      // 拒绝者 (AI/ADMIN)
    private String reviewerName;                    // 审核人真实姓名
    private String reviewerUsername;                // 审核人用户名

    // Constructors
    public WeeklyReportDetailResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getReportWeek() { return reportWeek; }
    public void setReportWeek(String reportWeek) { this.reportWeek = reportWeek; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public ContentDetailDTO getContent() { return content; }
    public void setContent(ContentDetailDTO content) { this.content = content; }

    public NextWeekPlanDetailDTO getNextWeekPlan() { return nextWeekPlan; }
    public void setNextWeekPlan(NextWeekPlanDetailDTO nextWeekPlan) { this.nextWeekPlan = nextWeekPlan; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public String getDevelopmentOpportunities() { return developmentOpportunities; }
    public void setDevelopmentOpportunities(String developmentOpportunities) { this.developmentOpportunities = developmentOpportunities; }

    public Long getAiAnalysisId() { return aiAnalysisId; }
    public void setAiAnalysisId(Long aiAnalysisId) { this.aiAnalysisId = aiAnalysisId; }

    public String getAiAnalysisResult() { return aiAnalysisResult; }
    public void setAiAnalysisResult(String aiAnalysisResult) { this.aiAnalysisResult = aiAnalysisResult; }

    public Double getAiConfidence() { return aiConfidence; }
    public void setAiConfidence(Double aiConfidence) { this.aiConfidence = aiConfidence; }

    public String getAiAnalysisStatus() { return aiAnalysisStatus; }
    public void setAiAnalysisStatus(String aiAnalysisStatus) { this.aiAnalysisStatus = aiAnalysisStatus; }

    public LocalDateTime getAiAnalysisCompletedAt() { return aiAnalysisCompletedAt; }
    public void setAiAnalysisCompletedAt(LocalDateTime aiAnalysisCompletedAt) { this.aiAnalysisCompletedAt = aiAnalysisCompletedAt; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getReviewerUsername() { return reviewerUsername; }
    public void setReviewerUsername(String reviewerUsername) { this.reviewerUsername = reviewerUsername; }

    /**
     * 本周汇报内容详情DTO - 包含完整的关联数据
     */
    public static class ContentDetailDTO {
        private List<RoutineTaskDetailDTO> routineTasks;
        private List<DevelopmentalTaskDetailDTO> developmentalTasks;

        public ContentDetailDTO() {}

        public List<RoutineTaskDetailDTO> getRoutineTasks() { return routineTasks; }
        public void setRoutineTasks(List<RoutineTaskDetailDTO> routineTasks) { this.routineTasks = routineTasks; }

        public List<DevelopmentalTaskDetailDTO> getDevelopmentalTasks() { return developmentalTasks; }
        public void setDevelopmentalTasks(List<DevelopmentalTaskDetailDTO> developmentalTasks) { this.developmentalTasks = developmentalTasks; }
    }

    /**
     * 日常性任务详情DTO - 包含完整的任务信息
     */
    public static class RoutineTaskDetailDTO {
        private String task_id;
        private String actual_result;
        @JsonProperty("AnalysisofResultDifferences")
        private String AnalysisofResultDifferences;
        private TaskDetailInfo taskDetails;

        public RoutineTaskDetailDTO() {}

        public String getTask_id() { return task_id; }
        public void setTask_id(String task_id) { this.task_id = task_id; }

        public String getActual_result() { return actual_result; }
        public void setActual_result(String actual_result) { this.actual_result = actual_result; }

        public String getAnalysisofResultDifferences() { return AnalysisofResultDifferences; }
        public void setAnalysisofResultDifferences(String analysisofResultDifferences) { AnalysisofResultDifferences = analysisofResultDifferences; }

        public TaskDetailInfo getTaskDetails() { return taskDetails; }
        public void setTaskDetails(TaskDetailInfo taskDetails) { this.taskDetails = taskDetails; }
    }

    /**
     * 发展性任务详情DTO - 包含完整的项目和阶段信息
     */
    public static class DevelopmentalTaskDetailDTO {
        private String project_id;
        private String phase_id;
        private String actual_result;
        @JsonProperty("AnalysisofResultDifferences")
        private String AnalysisofResultDifferences;
        private ProjectDetailInfo projectDetails;
        private PhaseDetailInfo phaseDetails;

        public DevelopmentalTaskDetailDTO() {}

        public String getProject_id() { return project_id; }
        public void setProject_id(String project_id) { this.project_id = project_id; }

        public String getPhase_id() { return phase_id; }
        public void setPhase_id(String phase_id) { this.phase_id = phase_id; }

        public String getActual_result() { return actual_result; }
        public void setActual_result(String actual_result) { this.actual_result = actual_result; }

        public String getAnalysisofResultDifferences() { return AnalysisofResultDifferences; }
        public void setAnalysisofResultDifferences(String analysisofResultDifferences) { AnalysisofResultDifferences = analysisofResultDifferences; }

        public ProjectDetailInfo getProjectDetails() { return projectDetails; }
        public void setProjectDetails(ProjectDetailInfo projectDetails) { this.projectDetails = projectDetails; }

        public PhaseDetailInfo getPhaseDetails() { return phaseDetails; }
        public void setPhaseDetails(PhaseDetailInfo phaseDetails) { this.phaseDetails = phaseDetails; }
    }

    /**
     * 下周规划详情DTO
     */
    public static class NextWeekPlanDetailDTO {
        private List<NextWeekRoutineTaskDetailDTO> routineTasks;
        private List<NextWeekDevelopmentalTaskDetailDTO> developmentalTasks;

        public NextWeekPlanDetailDTO() {}

        public List<NextWeekRoutineTaskDetailDTO> getRoutineTasks() { return routineTasks; }
        public void setRoutineTasks(List<NextWeekRoutineTaskDetailDTO> routineTasks) { this.routineTasks = routineTasks; }

        public List<NextWeekDevelopmentalTaskDetailDTO> getDevelopmentalTasks() { return developmentalTasks; }
        public void setDevelopmentalTasks(List<NextWeekDevelopmentalTaskDetailDTO> developmentalTasks) { this.developmentalTasks = developmentalTasks; }
    }

    /**
     * 下周日常性任务详情DTO
     */
    public static class NextWeekRoutineTaskDetailDTO {
        private String task_id;
        private TaskDetailInfo taskDetails;

        public NextWeekRoutineTaskDetailDTO() {}

        public String getTask_id() { return task_id; }
        public void setTask_id(String task_id) { this.task_id = task_id; }

        public TaskDetailInfo getTaskDetails() { return taskDetails; }
        public void setTaskDetails(TaskDetailInfo taskDetails) { this.taskDetails = taskDetails; }
    }

    /**
     * 下周发展性任务详情DTO
     */
    public static class NextWeekDevelopmentalTaskDetailDTO {
        private String project_id;
        private String phase_id;
        private ProjectDetailInfo projectDetails;
        private PhaseDetailInfo phaseDetails;

        public NextWeekDevelopmentalTaskDetailDTO() {}

        public String getProject_id() { return project_id; }
        public void setProject_id(String project_id) { this.project_id = project_id; }

        public String getPhase_id() { return phase_id; }
        public void setPhase_id(String phase_id) { this.phase_id = phase_id; }

        public ProjectDetailInfo getProjectDetails() { return projectDetails; }
        public void setProjectDetails(ProjectDetailInfo projectDetails) { this.projectDetails = projectDetails; }

        public PhaseDetailInfo getPhaseDetails() { return phaseDetails; }
        public void setPhaseDetails(PhaseDetailInfo phaseDetails) { this.phaseDetails = phaseDetails; }
    }

    /**
     * 任务详细信息
     */
    public static class TaskDetailInfo {
        private String taskName;
        private String personnelAssignment;
        private String timeline;
        private String quantitativeMetrics;
        private String expectedResults;

        public TaskDetailInfo() {}

        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }

        public String getPersonnelAssignment() { return personnelAssignment; }
        public void setPersonnelAssignment(String personnelAssignment) { this.personnelAssignment = personnelAssignment; }

        public String getTimeline() { return timeline; }
        public void setTimeline(String timeline) { this.timeline = timeline; }

        public String getQuantitativeMetrics() { return quantitativeMetrics; }
        public void setQuantitativeMetrics(String quantitativeMetrics) { this.quantitativeMetrics = quantitativeMetrics; }

        public String getExpectedResults() { return expectedResults; }
        public void setExpectedResults(String expectedResults) { this.expectedResults = expectedResults; }
    }

    /**
     * 项目详细信息
     */
    public static class ProjectDetailInfo {
        private String projectName;
        private String projectContent;
        private String projectMembers;
        private String expectedResults;
        private String timeline;
        private String stopLoss;

        public ProjectDetailInfo() {}

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }

        public String getProjectContent() { return projectContent; }
        public void setProjectContent(String projectContent) { this.projectContent = projectContent; }

        public String getProjectMembers() { return projectMembers; }
        public void setProjectMembers(String projectMembers) { this.projectMembers = projectMembers; }

        public String getExpectedResults() { return expectedResults; }
        public void setExpectedResults(String expectedResults) { this.expectedResults = expectedResults; }

        public String getTimeline() { return timeline; }
        public void setTimeline(String timeline) { this.timeline = timeline; }

        public String getStopLoss() { return stopLoss; }
        public void setStopLoss(String stopLoss) { this.stopLoss = stopLoss; }
    }

    /**
     * 阶段详细信息
     */
    public static class PhaseDetailInfo {
        private String phaseName;
        private String phaseDescription;
        private String assignedMembers;
        private String timeline;
        private String estimatedResults;

        public PhaseDetailInfo() {}

        public String getPhaseName() { return phaseName; }
        public void setPhaseName(String phaseName) { this.phaseName = phaseName; }

        public String getPhaseDescription() { return phaseDescription; }
        public void setPhaseDescription(String phaseDescription) { this.phaseDescription = phaseDescription; }

        public String getAssignedMembers() { return assignedMembers; }
        public void setAssignedMembers(String assignedMembers) { this.assignedMembers = assignedMembers; }

        public String getTimeline() { return timeline; }
        public void setTimeline(String timeline) { this.timeline = timeline; }

        public String getEstimatedResults() { return estimatedResults; }
        public void setEstimatedResults(String estimatedResults) { this.estimatedResults = estimatedResults; }
    }
}
