package com.weeklyreport.service;

import com.weeklyreport.dto.weeklyreport.WeeklyReportCreateRequest;
import com.weeklyreport.entity.*;
import com.weeklyreport.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * WeeklyReportServiceV3 - 严格按照error3.md数据结构重构的周报服务
 * 
 * 核心功能：
 * 1. 解析error3.md格式的前端数据
 * 2. 创建关联表记录(TaskReport, DevTaskReport)
 * 3. 更新任务的实际结果和差异分析
 * 4. 三级审批流程管理
 */
@Service
@Transactional
public class WeeklyReportServiceV3 {

    @Autowired
    private WeeklyReportRepository weeklyReportRepository;
    
    @Autowired
    private TaskReportRepository taskReportRepository;
    
    @Autowired
    private DevTaskReportRepository devTaskReportRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ProjectPhaseRepository projectPhaseRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 创建周报 - 严格按照error3.md第31-67行数据格式处理
     */
    public WeeklyReport createWeeklyReport(WeeklyReportCreateRequest request) {
        // 1. 验证用户ID
        Long userId = request.getUserId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        // 2. 创建WeeklyReport实体
        WeeklyReport weeklyReport = new WeeklyReport();
        weeklyReport.setUserId(userId);
        weeklyReport.setTitle(request.getTitle());
        weeklyReport.setReportWeek(request.getReportWeek());
        weeklyReport.setAdditionalNotes(request.getAdditionalNotes());
        weeklyReport.setDevelopmentOpportunities(request.getDevelopmentOpportunities());
        weeklyReport.setApprovalStatus(WeeklyReport.ApprovalStatus.AI_ANALYZING);

        // 3. 保存周报实体
        weeklyReport = weeklyReportRepository.save(weeklyReport);

        // 4. 处理本周汇报内容
        if (request.getContent() != null) {
            processThisWeekContent(weeklyReport, request.getContent());
        }

        // 5. 处理下周规划（暂时不存储，用于前端显示）
        // nextWeekPlan 数据不存储到数据库，仅用于前端显示和下次周报的任务选择

        return weeklyReport;
    }

    /**
     * 处理本周汇报内容 - 对应error3.md第35-51行
     */
    private void processThisWeekContent(WeeklyReport weeklyReport, WeeklyReportCreateRequest.ContentDTO content) {
        // 处理日常性任务
        if (content.getRoutineTasks() != null) {
            for (WeeklyReportCreateRequest.RoutineTaskDTO routineTask : content.getRoutineTasks()) {
                processRoutineTask(weeklyReport, routineTask);
            }
        }

        // 处理发展性任务
        if (content.getDevelopmentalTasks() != null) {
            for (WeeklyReportCreateRequest.DevelopmentalTaskDTO devTask : content.getDevelopmentalTasks()) {
                processDevelopmentalTask(weeklyReport, devTask);
            }
        }
    }

    /**
     * 处理日常性任务 - 对应error3.md第37-42行
     */
    private void processRoutineTask(WeeklyReport weeklyReport, WeeklyReportCreateRequest.RoutineTaskDTO routineTaskDto) {
        Long taskId = Long.parseLong(routineTaskDto.getTask_id());
        
        // 1. 查找任务
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));

        // 2. 更新任务的实际结果和差异分析
        // actualResults和resultDifferenceAnalysis已移至DevTaskReport表中存储
        // TODO: 需要通过关联的DevTaskReport实体来存储这些字段
        taskRepository.save(task);

        // 3. 创建TaskReport关联记录
        TaskReport taskReport = new TaskReport(weeklyReport, task);
        taskReportRepository.save(taskReport);
    }

    /**
     * 处理发展性任务 - 对应error3.md第44-50行
     */
    private void processDevelopmentalTask(WeeklyReport weeklyReport, WeeklyReportCreateRequest.DevelopmentalTaskDTO devTaskDto) {
        Long projectId = Long.parseLong(devTaskDto.getProject_id());
        Long phaseId = Long.parseLong(devTaskDto.getPhase_id());

        // 1. 查找项目和阶段
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("项目不存在: " + projectId));
        
        ProjectPhase projectPhase = projectPhaseRepository.findById(phaseId)
            .orElseThrow(() -> new RuntimeException("项目阶段不存在: " + phaseId));

        // 2. 更新项目阶段的实际结果和差异分析
        // actualResults和resultDifferenceAnalysis已移至DevTaskReport表中存储
        // TODO: 需要通过关联的DevTaskReport实体来存储这些字段
        projectPhaseRepository.save(projectPhase);

        // 3. 创建DevTaskReport关联记录
        DevTaskReport devTaskReport = new DevTaskReport(weeklyReport.getId(), project.getId(), projectPhase.getId());
        devTaskReportRepository.save(devTaskReport);
    }

    /**
     * 提交周报进入审批流程
     */
    public void submitWeeklyReport(Long reportId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("周报不存在: " + reportId));
        
        if (!report.isDraft()) {
            throw new RuntimeException("只能提交草稿状态的周报");
        }
        
        report.submit();
        weeklyReportRepository.save(report);
        
        // TODO: 触发AI分析
    }

    /**
     * AI分析通过
     */
    public void aiApproveWeeklyReport(Long reportId, Long aiAnalysisId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("周报不存在: " + reportId));
        
        if (!report.isSubmitted()) {
            throw new RuntimeException("只能对已提交的周报进行AI分析");
        }
        
        report.setAiAnalysisId(aiAnalysisId);
        report.aiApprove();
        weeklyReportRepository.save(report);
    }

    /**
     * 管理员审核通过
     */
    public void adminApproveWeeklyReport(Long reportId, Long adminId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("周报不存在: " + reportId));
        
        if (report.getApprovalStatus() != WeeklyReport.ApprovalStatus.ADMIN_REVIEWING) {
            throw new RuntimeException("只能审核等待管理员审核的周报");
        }
        
        report.adminApprove(adminId);
        weeklyReportRepository.save(report);
    }


    /**
     * 拒绝周报
     */
    public void rejectWeeklyReport(Long reportId, Long reviewerId, String reason) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("周报不存在: " + reportId));
        
        report.reject(reviewerId, reason);
        weeklyReportRepository.save(report);
    }

    /**
     * 获取周报详情 - 包含完整的关联数据
     */
    @Transactional(readOnly = true)
    public WeeklyReportDetailVO getWeeklyReportDetail(Long reportId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("周报不存在: " + reportId));

        // 查询日常任务关联
        List<TaskReport> taskReports = taskReportRepository.findByWeeklyReportId(reportId);
        
        // 查询发展任务关联
        List<DevTaskReport> devTaskReports = devTaskReportRepository.findByWeeklyReportId(reportId);

        // 构建返回对象
        WeeklyReportDetailVO detailVO = new WeeklyReportDetailVO();
        detailVO.setWeeklyReport(report);
        detailVO.setTaskReports(taskReports);
        detailVO.setDevTaskReports(devTaskReports);

        return detailVO;
    }

    /**
     * 根据用户查询周报列表
     */
    @Transactional(readOnly = true)
    public List<WeeklyReport> getWeeklyReportsByUserId(Long userId) {
        return weeklyReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 根据审批状态查询周报
     */
    @Transactional(readOnly = true)
    public List<WeeklyReport> getWeeklyReportsByStatus(WeeklyReport.ApprovalStatus status) {
        return weeklyReportRepository.findByApprovalStatusOrderByCreatedAtDesc(status);
    }

    /**
     * 周报详情VO - 包含完整关联数据
     */
    public static class WeeklyReportDetailVO {
        private WeeklyReport weeklyReport;
        private List<TaskReport> taskReports;
        private List<DevTaskReport> devTaskReports;

        // Getters and Setters
        public WeeklyReport getWeeklyReport() {
            return weeklyReport;
        }

        public void setWeeklyReport(WeeklyReport weeklyReport) {
            this.weeklyReport = weeklyReport;
        }

        public List<TaskReport> getTaskReports() {
            return taskReports;
        }

        public void setTaskReports(List<TaskReport> taskReports) {
            this.taskReports = taskReports;
        }

        public List<DevTaskReport> getDevTaskReports() {
            return devTaskReports;
        }

        public void setDevTaskReports(List<DevTaskReport> devTaskReports) {
            this.devTaskReports = devTaskReports;
        }
    }
}