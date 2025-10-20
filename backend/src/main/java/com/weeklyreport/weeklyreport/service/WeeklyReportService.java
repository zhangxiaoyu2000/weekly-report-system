package com.weeklyreport.weeklyreport.service;

import com.weeklyreport.weeklyreport.dto.*;
import com.weeklyreport.weeklyreport.entity.*;
import com.weeklyreport.weeklyreport.repository.*;
import com.weeklyreport.task.entity.*;
import com.weeklyreport.task.repository.*;
import com.weeklyreport.project.entity.*;
import com.weeklyreport.project.repository.*;
import com.weeklyreport.user.entity.User;
import com.weeklyreport.user.repository.UserRepository;
import com.weeklyreport.ai.entity.AIAnalysisResult;
import com.weeklyreport.ai.repository.AIAnalysisResultRepository;
import com.weeklyreport.ai.service.AIAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * WeeklyReportService - 严格按照error3.md数据结构重构的周报服务
 * 
 * 核心功能：
 * 1. 解析error3.md格式的前端数据
 * 2. 创建关联表记录(TaskReport, DevTaskReport)
 * 3. 更新任务的实际结果和差异分析
 * 4. 三级审批流程管理
 */
@Service
@Transactional
public class WeeklyReportService {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyReportService.class);

    @Value("${weekly-report.ai.confidence-threshold:0.7}")
    private double weeklyReportConfidenceThreshold;

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
    
    @Autowired
    private AIAnalysisResultRepository aiAnalysisResultRepository;
    
    @Autowired
    private AIAnalysisService aiAnalysisService;
    
    @Autowired
    private WeeklyReportNotificationService notificationService;

    /**
     * 创建周报 - 严格按照error3.md第31-67行数据格式处理
     */
    public WeeklyReport createWeeklyReport(WeeklyReportCreateRequest request) {
        // ======== 调试日志：详细记录接收到的请求数据 ========
        logger.info("🔍 开始创建周报，接收到的请求数据：");
        logger.info("🔍 Title: {}", request.getTitle());
        logger.info("🔍 ReportWeek: {}", request.getReportWeek());
        logger.info("🔍 UserId: {}", request.getUserId());
        logger.info("🔍 AdditionalNotes: {}", request.getAdditionalNotes());
        logger.info("🔍 DevelopmentOpportunities: {}", request.getDevelopmentOpportunities());
        
        // 关键：检查content字段
        if (request.getContent() != null) {
            logger.info("🔍 Content对象存在: {}", request.getContent());
            logger.info("🔍 Content类型: {}", request.getContent().getClass().getName());
            if (request.getContent().getRoutineTasks() != null) {
                logger.info("🔍 routineTasks数量: {}", request.getContent().getRoutineTasks().size());
                for (int i = 0; i < request.getContent().getRoutineTasks().size(); i++) {
                    WeeklyReportCreateRequest.RoutineTaskDTO task = request.getContent().getRoutineTasks().get(i);
                    logger.info("🔍 Routine_task[{}]: task_id={}, actual_result={}, analysis={}", 
                               i, task.getTask_id(), task.getActual_result(), task.getAnalysisofResultDifferences());
                }
            } else {
                logger.warn("🔍 Content存在但Routine_tasks为null");
            }
            if (request.getContent().getDevelopmentalTasks() != null) {
                logger.info("🔍 developmentalTasks数量: {}", request.getContent().getDevelopmentalTasks().size());
            } else {
                logger.warn("🔍 Content存在但Developmental_tasks为null");
            }
        } else {
            logger.warn("🔍 Content字段为null！");
        }
        
        // 检查nextWeekPlan字段
        if (request.getNextWeekPlan() != null) {
            logger.info("🔍 NextWeekPlan对象存在: {}", request.getNextWeekPlan());
            logger.info("🔍 NextWeekPlan.routineTasks: {}", request.getNextWeekPlan().getRoutineTasks());
            logger.info("🔍 NextWeekPlan.developmentalTasks: {}", request.getNextWeekPlan().getDevelopmentalTasks());
        } else {
            logger.warn("🔍 NextWeekPlan字段为null");
        }
        logger.info("🔍 ================================================");
        // ======== 调试日志结束 ========
        
        // 1. 验证用户ID
        Long userId = request.getUserId();
        userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        // 2. 创建WeeklyReport实体（草稿状态）
        WeeklyReport weeklyReport = new WeeklyReport();
        weeklyReport.setUserId(userId);
        weeklyReport.setTitle(request.getTitle());
        weeklyReport.setReportWeek(request.getReportWeek());
        weeklyReport.setAdditionalNotes(request.getAdditionalNotes());
        weeklyReport.setDevelopmentOpportunities(request.getDevelopmentOpportunities());

        // 设置初始状态为草稿
        weeklyReport.setStatus(WeeklyReport.ReportStatus.DRAFT);

        // 3. 保存周报实体
        weeklyReport = weeklyReportRepository.save(weeklyReport);

        // 4. 处理本周汇报内容
        if (request.getContent() != null) {
            logger.info("📋 Processing this week content for weekly report ID: {}", weeklyReport.getId());
            processThisWeekContent(weeklyReport, request.getContent());
        } else {
            logger.warn("⚠️ No content found in request for weekly report ID: {}", weeklyReport.getId());
        }

        // 5. 处理下周规划
        if (request.getNextWeekPlan() != null) {
            logger.info("📅 Processing next week plan for weekly report ID: {}", weeklyReport.getId());
            processNextWeekPlan(weeklyReport, request.getNextWeekPlan());
        } else {
            logger.warn("⚠️ No next week plan found in request for weekly report ID: {}", weeklyReport.getId());
        }

        // 6. 草稿创建完成（不触发AI分析）
        logger.info("✅ 周报草稿已创建，周报ID: {}, 用户ID: {}, 状态: {}",
            weeklyReport.getId(), userId, weeklyReport.getStatus());
        logger.info("💡 提示：请调用 submitForReview() 方法提交审核以触发AI分析");

        return weeklyReport;
    }

    /**
     * 创建周报并直接提交审核（跳过草稿状态）
     * 用于前端"提交周报"按钮的一键提交场景
     *
     * @param request 周报创建请求
     * @return 提交后的周报实体（状态为 AI_PROCESSING）
     */
    public WeeklyReport createAndSubmitDirectly(WeeklyReportCreateRequest request) {
        logger.info("🚀 开始创建并直接提交周报，用户ID: {}", request.getUserId());

        // 1. 验证用户ID
        Long userId = request.getUserId();
        userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        // 2. 创建WeeklyReport实体（直接设置为AI_PROCESSING状态）
        WeeklyReport weeklyReport = new WeeklyReport();
        weeklyReport.setUserId(userId);
        weeklyReport.setTitle(request.getTitle());
        weeklyReport.setReportWeek(request.getReportWeek());
        weeklyReport.setAdditionalNotes(request.getAdditionalNotes());
        weeklyReport.setDevelopmentOpportunities(request.getDevelopmentOpportunities());

        // 3. 直接设置为AI处理中状态（跳过DRAFT）
        weeklyReport.setStatus(WeeklyReport.ReportStatus.AI_PROCESSING);
        weeklyReport.setSubmittedAt(java.time.LocalDateTime.now());

        // 4. 保存周报实体
        weeklyReport = weeklyReportRepository.save(weeklyReport);
        logger.info("✅ 周报实体已创建，ID: {}, 状态: {}",
                   weeklyReport.getId(), weeklyReport.getStatus());

        // 5. 处理本周汇报内容
        if (request.getContent() != null) {
            logger.info("📋 处理本周汇报内容，周报ID: {}", weeklyReport.getId());
            processThisWeekContent(weeklyReport, request.getContent());
        } else {
            logger.warn("⚠️ 本周汇报内容为空，周报ID: {}", weeklyReport.getId());
        }

        // 6. 处理下周规划
        if (request.getNextWeekPlan() != null) {
            logger.info("📅 处理下周规划，周报ID: {}", weeklyReport.getId());
            processNextWeekPlan(weeklyReport, request.getNextWeekPlan());
        } else {
            logger.warn("⚠️ 下周规划为空，周报ID: {}", weeklyReport.getId());
        }

        // 7. 内容完整性校验（同 submitForReview 方法）
        try {
            validateReportCompleteness(weeklyReport);
        } catch (IllegalArgumentException e) {
            // 验证失败，删除已创建的周报
            logger.error("❌ 周报内容验证失败，删除周报ID: {}", weeklyReport.getId());
            weeklyReportRepository.delete(weeklyReport);
            throw e;
        }

        // 8. 触发AI分析
        try {
            logger.info("🤖 开始触发AI分析，周报ID: {}", weeklyReport.getId());
            triggerAIAnalysis(weeklyReport);
        } catch (Exception e) {
            logger.error("🤖 AI分析触发失败，周报ID: {}", weeklyReport.getId(), e);
            // AI分析失败时拒绝
            weeklyReport.aiReject("AI分析启动失败: " + e.getMessage());
            weeklyReportRepository.save(weeklyReport);
            throw new RuntimeException("AI分析启动失败: " + e.getMessage(), e);
        }

        // 9. 发送周报提交通知
        try {
            logger.info("📧 发送周报提交通知，周报ID: {}", weeklyReport.getId());
            notificationService.handleWeeklyReportSubmitted(
                weeklyReport.getId(),
                weeklyReport.getUserId()
            );
        } catch (Exception e) {
            logger.error("📧 发送周报提交通知失败，周报ID: {}", weeklyReport.getId(), e);
            // 通知失败不影响提交流程
        }

        logger.info("✅ 周报创建并提交成功，周报ID: {}, 状态: {}",
                   weeklyReport.getId(), weeklyReport.getStatus());
        return weeklyReport;
    }

    /**
     * 处理本周汇报内容 - 对应error3.md第35-51行
     */
    private void processThisWeekContent(WeeklyReport weeklyReport, WeeklyReportCreateRequest.ContentDTO content) {
        logger.info("📋 Starting to process this week content for weekly report ID: {}", weeklyReport.getId());
        
        // 处理日常性任务
        if (content.getRoutineTasks() != null) {
            logger.info("🔄 Processing {} routine tasks", content.getRoutineTasks().size());
            for (WeeklyReportCreateRequest.RoutineTaskDTO routineTask : content.getRoutineTasks()) {
                logger.info("📝 Processing routine task with ID: {}", routineTask.getTask_id());
                processRoutineTask(weeklyReport, routineTask);
            }
        } else {
            logger.warn("⚠️ No routine tasks found in content");
        }

        // 处理发展性任务
        if (content.getDevelopmentalTasks() != null) {
            logger.info("🚀 Processing {} developmental tasks", content.getDevelopmentalTasks().size());
            for (WeeklyReportCreateRequest.DevelopmentalTaskDTO devTask : content.getDevelopmentalTasks()) {
                logger.info("📝 Processing developmental task with project ID: {} and phase ID: {}", devTask.getProject_id(), devTask.getPhase_id());
                processDevelopmentalTask(weeklyReport, devTask);
            }
        } else {
            logger.warn("⚠️ No developmental tasks found in content");
        }
        
        logger.info("✅ Completed processing this week content for weekly report ID: {}", weeklyReport.getId());
    }

    /**
     * 处理下周规划 - 创建关联表记录，isWeek=false表示下周规划
     */
    private void processNextWeekPlan(WeeklyReport weeklyReport, WeeklyReportCreateRequest.NextWeekPlanDTO nextWeekPlan) {
        logger.info("📅 Starting to process next week plan for weekly report ID: {}", weeklyReport.getId());
        
        // 处理下周日常性任务
        if (nextWeekPlan.getRoutineTasks() != null) {
            logger.info("🔄 Processing {} next week routine tasks", nextWeekPlan.getRoutineTasks().size());
            for (WeeklyReportCreateRequest.NextWeekRoutineTaskDTO nextWeekTask : nextWeekPlan.getRoutineTasks()) {
                processNextWeekRoutineTask(weeklyReport, nextWeekTask);
            }
        } else {
            logger.warn("⚠️ No next week routine tasks found");
        }

        // 处理下周发展性任务
        if (nextWeekPlan.getDevelopmentalTasks() != null) {
            logger.info("🚀 Processing {} next week developmental tasks", nextWeekPlan.getDevelopmentalTasks().size());
            for (WeeklyReportCreateRequest.NextWeekDevelopmentalTaskDTO nextWeekDevTask : nextWeekPlan.getDevelopmentalTasks()) {
                processNextWeekDevelopmentalTask(weeklyReport, nextWeekDevTask);
            }
        } else {
            logger.warn("⚠️ No next week developmental tasks found");
        }
        
        logger.info("✅ Completed processing next week plan for weekly report ID: {}", weeklyReport.getId());
    }

    /**
     * 处理下周日常性任务 - 仅记录任务ID，不包含执行结果
     */
    private void processNextWeekRoutineTask(WeeklyReport weeklyReport, WeeklyReportCreateRequest.NextWeekRoutineTaskDTO nextWeekTaskDto) {
        logger.info("🔄 Processing next week routine task with ID: {}", nextWeekTaskDto.getTask_id());
        Long taskId = Long.parseLong(nextWeekTaskDto.getTask_id());
        
        // 1. 验证任务存在
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        logger.info("✅ Found task: {} (ID: {})", task.getTaskName(), taskId);

        // 2. 创建TaskReport关联记录，标记为下周规划（isWeek=false）
        TaskReport taskReport = new TaskReport(
            weeklyReport, 
            task, 
            null, // 下周规划没有实际结果
            null, // 下周规划没有差异分析
            false // 下周规划
        );
        
        logger.info("💾 Saving Next Week TaskReport: weeklyReportId={}, taskId={}", 
                   weeklyReport.getId(), taskId);
        TaskReport savedTaskReport = taskReportRepository.save(taskReport);
        logger.info("✅ Next Week TaskReport saved successfully with composite ID: {}", savedTaskReport.getId());
    }

    /**
     * 处理下周发展性任务 - 仅记录项目和阶段ID，不包含执行结果
     */
    private void processNextWeekDevelopmentalTask(WeeklyReport weeklyReport, WeeklyReportCreateRequest.NextWeekDevelopmentalTaskDTO nextWeekDevTaskDto) {
        Long projectId = Long.parseLong(nextWeekDevTaskDto.getProject_id());
        Long phaseId = Long.parseLong(nextWeekDevTaskDto.getPhase_id());

        // 1. 验证项目和阶段存在
        projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("项目不存在: " + projectId));
        
        projectPhaseRepository.findById(phaseId)
            .orElseThrow(() -> new RuntimeException("项目阶段不存在: " + phaseId));

        // 2. 创建DevTaskReport关联记录，标记为下周规划（isWeek=false）
        DevTaskReport devTaskReport = new DevTaskReport(
            weeklyReport.getId(),
            projectId,
            phaseId,
            null, // 下周规划没有实际结果
            null, // 下周规划没有差异分析
            false // 下周规划
        );
        
        logger.info("💾 Saving Next Week DevTaskReport: weeklyReportId={}, projectId={}, phaseId={}", 
                   weeklyReport.getId(), projectId, phaseId);
        devTaskReportRepository.save(devTaskReport);
        logger.info("✅ Next Week DevTaskReport saved successfully");
    }

    /**
     * 处理日常性任务 - 对应error3.md第37-42行
     * 修复：将实际结果存储在TaskReport关联表中，而不是Task定义表中
     */
    private void processRoutineTask(WeeklyReport weeklyReport, WeeklyReportCreateRequest.RoutineTaskDTO routineTaskDto) {
        logger.info("🔄 Processing routine task with ID: {}", routineTaskDto.getTask_id());
        
        try {
            Long taskId = Long.parseLong(routineTaskDto.getTask_id());
            
            // 1. 验证任务存在（但不修改任务实体）
            Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
            logger.info("✅ Found task: {} (ID: {})", task.getTaskName(), taskId);

            // 2. 验证周报ID已经生成
            if (weeklyReport.getId() == null) {
                throw new RuntimeException("WeeklyReport ID为空，无法创建TaskReport关联");
            }
            logger.info("✅ WeeklyReport ID confirmed: {}", weeklyReport.getId());

            // 3. 创建TaskReport关联记录，存储执行结果（本周汇报：isWeek=true）
            TaskReport taskReport = new TaskReport(
                weeklyReport, 
                task, 
                routineTaskDto.getActual_result(),
                routineTaskDto.getAnalysisofResultDifferences(),
                true  // 本周汇报
            );
            
            // 4. 验证复合主键生成
            if (taskReport.getId() == null) {
                logger.error("❌ TaskReport复合主键为null，手动设置");
                taskReport.setId(new TaskReport.TaskReportId(weeklyReport.getId(), task.getId()));
            }
            logger.info("✅ TaskReport复合主键: {}", taskReport.getId());
            
            logger.info("💾 Saving TaskReport: weeklyReportId={}, taskId={}, actualResult={}", 
                       weeklyReport.getId(), taskId, routineTaskDto.getActual_result());
            
            TaskReport savedTaskReport = taskReportRepository.save(taskReport);
            logger.info("✅ TaskReport saved successfully with composite ID: {}", savedTaskReport.getId());
            
            // 5. 验证保存结果
            if (savedTaskReport == null) {
                throw new RuntimeException("TaskReport保存失败，返回null");
            }
            
        } catch (Exception e) {
            logger.error("❌ 处理日常性任务失败: taskId={}, error={}", routineTaskDto.getTask_id(), e.getMessage(), e);
            throw new RuntimeException("处理日常性任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理发展性任务 - 对应error3.md第44-50行
     * 修复：将实际结果存储在DevTaskReport关联表中，而不是ProjectPhase定义表中
     */
    private void processDevelopmentalTask(WeeklyReport weeklyReport, WeeklyReportCreateRequest.DevelopmentalTaskDTO devTaskDto) {
        logger.info("🔄 Processing developmental task: projectId={}, phaseId={}", 
                   devTaskDto.getProject_id(), devTaskDto.getPhase_id());
        
        try {
            Long projectId = Long.parseLong(devTaskDto.getProject_id());
            Long phaseId = Long.parseLong(devTaskDto.getPhase_id());

            // 1. 验证项目和阶段存在（但不修改实体）
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在: " + projectId));
            logger.info("✅ Found project: {} (ID: {})", project.getName(), projectId);
            
            ProjectPhase projectPhase = projectPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new RuntimeException("项目阶段不存在: " + phaseId));
            logger.info("✅ Found project phase: {} (ID: {})", projectPhase.getPhaseName(), phaseId);

            // 2. 验证周报ID已经生成
            if (weeklyReport.getId() == null) {
                throw new RuntimeException("WeeklyReport ID为空，无法创建DevTaskReport关联");
            }
            logger.info("✅ WeeklyReport ID confirmed: {}", weeklyReport.getId());

            // 3. 创建DevTaskReport关联记录，存储执行结果（本周汇报：isWeek=true）
            DevTaskReport devTaskReport = new DevTaskReport(
                weeklyReport.getId(),
                projectId,
                phaseId,
                devTaskDto.getActual_result(),
                devTaskDto.getAnalysisofResultDifferences(),
                true  // 本周汇报
            );
            
            logger.info("💾 Saving DevTaskReport: weeklyReportId={}, projectId={}, phaseId={}, actualResult={}", 
                       weeklyReport.getId(), projectId, phaseId, devTaskDto.getActual_result());
            
            DevTaskReport savedDevTaskReport = devTaskReportRepository.save(devTaskReport);
            logger.info("✅ DevTaskReport saved successfully with ID: {}", savedDevTaskReport.getId());
            
            // 4. 验证保存结果
            if (savedDevTaskReport == null) {
                throw new RuntimeException("DevTaskReport保存失败，返回null");
            }
            
        } catch (Exception e) {
            logger.error("❌ 处理发展性任务失败: projectId={}, phaseId={}, error={}", 
                        devTaskDto.getProject_id(), devTaskDto.getPhase_id(), e.getMessage(), e);
            throw new RuntimeException("处理发展性任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新周报 - 包含结构化内容的处理
     */
    @Transactional
    public WeeklyReport updateWeeklyReport(Long reportId, WeeklyReportUpdateRequest request) {
        logger.info("🔧 开始更新周报，ID: {}", reportId);
        
        // ======== 调试日志：记录更新请求数据 ========
        logger.info("🔧 更新周报接收到的请求数据：");
        logger.info("🔧 Title: {}", request.getTitle());
        if (request.getContent() != null) {
            logger.info("🔧 Content存在，routine_tasks数量: {}, developmental_tasks数量: {}", 
                       request.getContent().getRoutineTasks() != null ? request.getContent().getRoutineTasks().size() : 0,
                       request.getContent().getDevelopmentalTasks() != null ? request.getContent().getDevelopmentalTasks().size() : 0);
        }
        if (request.getNextWeekPlan() != null) {
            logger.info("🔧 NextWeekPlan存在，routine_tasks数量: {}, developmental_tasks数量: {}", 
                       request.getNextWeekPlan().getRoutineTasks() != null ? request.getNextWeekPlan().getRoutineTasks().size() : 0,
                       request.getNextWeekPlan().getDevelopmentalTasks() != null ? request.getNextWeekPlan().getDevelopmentalTasks().size() : 0);
        } else {
            logger.warn("🔧 ⚠️ NextWeekPlan为null - 这是问题2的根源！");
        }
        // ======== 调试日志结束 ========
        
        // 1. 获取现有周报
        WeeklyReport report = weeklyReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("周报不存在"));
        
        // 2. 更新基本字段
        report.setTitle(request.getTitle());
        report.setReportWeek(request.getReportWeek());
        report.setAdditionalNotes(request.getAdditionalNotes());
        report.setDevelopmentOpportunities(request.getDevelopmentOpportunities());
        
        // 3. 保存基本字段更新
        WeeklyReport savedReport = weeklyReportRepository.save(report);
        
        // 4. 清除现有的所有关联数据（包括任务关联和AI分析结果）
        logger.info("🔧 清除现有任务关联和AI分析结果，周报ID: {}", reportId);
        
        // 4.1 清除任务报告关联（包括本周汇报和下周规划）
        taskReportRepository.deleteByIdWeeklyReportId(reportId);
        devTaskReportRepository.deleteByWeeklyReportId(reportId);
        
        // 4.2 清除旧的AI分析结果 - 修复重复数据问题
        // 首先清空周报的AI分析引用，避免外键约束错误
        report.setAiAnalysisId(null);
        weeklyReportRepository.save(report);
        
        int deletedAIResults = aiAnalysisResultRepository.deleteByReportId(reportId, AIAnalysisResult.EntityType.WEEKLY_REPORT);
        logger.info("🔧 清除了 {} 条旧的AI分析结果", deletedAIResults);
        
        // 5. 重新创建本周汇报内容（修复问题1：确保结果差异分析插入）
        if (request.getContent() != null) {
            logger.info("🔧 重新处理本周汇报内容");
            processThisWeekContentFromUpdateRequest(savedReport, request.getContent());
        } else {
            logger.warn("🔧 ⚠️ 更新时没有本周汇报内容");
        }
        
        // 6. 重新创建下周规划内容（修复问题2：处理下周规划数据）
        if (request.getNextWeekPlan() != null) {
            logger.info("🔧 重新处理下周规划内容");
            processNextWeekPlanFromUpdateRequest(savedReport, request.getNextWeekPlan());
        } else {
            logger.warn("🔧 ⚠️ 更新时没有下周规划内容 - 这会导致下周规划数据丢失");
        }
        
        // 7. 更新完成（草稿状态不变，允许继续编辑）
        // 注意：只有草稿或已拒绝状态的周报才允许更新
        WeeklyReport finalReport = weeklyReportRepository.save(savedReport);

        logger.info("🔧 ✅ 周报更新成功，ID: {}，状态: {}，可继续编辑或提交审核",
            reportId, finalReport.getStatus());
        return finalReport;
    }

    /**
     * 提交周报进入审批流程
     * @deprecated 使用 submitForReview(Long reportId) 替代
     */
    @Deprecated
    public void submitWeeklyReport(Long reportId) {
        logger.warn("⚠️ 调用了已废弃的submitWeeklyReport方法，请使用submitForReview()替代");
        // 委托给新方法
        submitForReview(reportId);
    }

    /**
     * AI分析通过 - 验证置信度
     */
    public void aiApproveWeeklyReport(Long reportId, Long aiAnalysisId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("周报不存在: " + reportId));

        // 检查状态：必须是AI处理中
        if (!report.isAIProcessing()) {
            throw new RuntimeException(
                String.format("只能对正在AI分析的周报进行AI审批，当前状态: %s",
                    report.getStatus())
            );
        }

        // 获取AI分析结果并验证置信度
        AIAnalysisResult analysisResult = aiAnalysisResultRepository.findById(aiAnalysisId)
            .orElseThrow(() -> new RuntimeException("AI分析结果不存在: " + aiAnalysisId));

        Double confidence = analysisResult.getConfidence();
        if (confidence == null || confidence < weeklyReportConfidenceThreshold) {
            throw new RuntimeException(String.format(
                "AI分析置信度不足: %.2f < %.2f，无法批准",
                confidence != null ? confidence : 0.0,
                weeklyReportConfidenceThreshold
            ));
        }

        report.setAiAnalysisId(aiAnalysisId);
        // 调用实体的aiApprove方法
        report.aiApprove();
        weeklyReportRepository.save(report);
    }

    /**
     * 管理员审核通过
     */
    public void adminApproveWeeklyReport(Long reportId, Long adminId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("周报不存在: " + reportId));

        // 检查状态：必须是待审核
        if (!report.isPendingReview()) {
            throw new RuntimeException(
                String.format("只能审核处于待审核状态的周报，当前状态: %s",
                    report.getStatus())
            );
        }

        // 调用实体的approve方法
        report.approve(adminId);
        weeklyReportRepository.save(report);

        // 触发管理员通过通知
        try {
            notificationService.handleAdminApproved(reportId, adminId);
            logger.info("📧 管理员通过通知已触发，周报ID: {}, 管理员ID: {}", reportId, adminId);
        } catch (Exception e) {
            logger.error("📧 ❌ 触发管理员通过通知失败，周报ID: {}", reportId, e);
        }
    }


    /**
     * 拒绝周报
     */
    public void rejectWeeklyReport(Long reportId, Long reviewerId, String reason, boolean isSuperAdmin) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("周报不存在: " + reportId));

        // 检查状态：必须是待审核
        if (!report.isPendingReview()) {
            throw new RuntimeException(
                String.format("只能拒绝处于待审核状态的周报，当前状态: %s",
                    report.getStatus())
            );
        }

        // 调用实体的reject方法
        report.reject(reviewerId, reason, isSuperAdmin);
        weeklyReportRepository.save(report);

        // 触发管理员拒绝通知
        try {
            notificationService.handleAdminRejected(reportId, reason, reviewerId);
            logger.info("📧 管理员拒绝通知已触发，周报ID: {}, 管理员ID: {}", reportId, reviewerId);
        } catch (Exception e) {
            logger.error("📧 ❌ 触发管理员拒绝通知失败，周报ID: {}", reportId, e);
        }
    }

    /**
     * 获取周报详情 - 包含完整的关联数据
     * 修复：使用正确的DTO响应格式，映射执行结果数据
     */
    @Transactional(readOnly = true)
    public WeeklyReportDetailResponse getWeeklyReportDetail(Long reportId) {
        // 修复ClassCastException: 分别查询周报和AI分析结果
        WeeklyReport report = weeklyReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("周报不存在: " + reportId));
        
        // 查询用户信息以获取用户名
        User reportUser = userRepository.findById(report.getUserId()).orElse(null);

        // 查询AI分析结果
        AIAnalysisResult aiAnalysis = null;
        try {
            List<AIAnalysisResult> aiResults = aiAnalysisResultRepository.findByReportIdAndEntityType(reportId, AIAnalysisResult.EntityType.WEEKLY_REPORT);
            if (!aiResults.isEmpty()) {
                aiAnalysis = aiResults.get(0); // 取第一个AI分析结果
            }
        } catch (Exception e) {
            logger.warn("查询AI分析结果失败: {}", e.getMessage());
        }

        // 查询日常任务关联
        List<TaskReport> taskReports = taskReportRepository.findByWeeklyReportId(reportId);
        
        // 查询发展任务关联
        List<DevTaskReport> devTaskReports = devTaskReportRepository.findByWeeklyReportId(reportId);

        // 构建响应对象
        WeeklyReportDetailResponse response = new WeeklyReportDetailResponse();
        
        // 设置基本信息
        response.setId(report.getId());
        response.setUserId(report.getUserId());
        response.setTitle(report.getTitle());
        response.setReportWeek(report.getReportWeek());
        response.setAdditionalNotes(report.getAdditionalNotes());
        response.setDevelopmentOpportunities(report.getDevelopmentOpportunities());
        response.setStatus(report.getStatus().toString());
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());
        if (reportUser != null) {
            response.setUsername(reportUser.getUsername());
        }
        
        // 设置AI分析信息
        logger.debug("🔍 getWeeklyReportDetail - 周报ID: {}, AI分析对象: {}", report.getId(), aiAnalysis);
        if (aiAnalysis != null) {
            logger.debug("🔍 getWeeklyReportDetail AI分析详情 - ID: {}, 状态: {}", 
                       aiAnalysis.getId(), aiAnalysis.getStatus());
            response.setAiAnalysisId(aiAnalysis.getId());
            response.setAiAnalysisResult(aiAnalysis.getResult());
            response.setAiConfidence(aiAnalysis.getConfidence());
            response.setAiAnalysisStatus(aiAnalysis.getStatus().name());
            response.setAiAnalysisCompletedAt(aiAnalysis.getCompletedAt());
        } else {
            logger.debug("🔍 getWeeklyReportDetail - AI分析为空，周报ID: {}", report.getId());
        }
        
        // 构建包含详细关联数据的内容结构 - 这是关键的修复！
        logger.info("🔧 修复问题 - 为周报ID {} 构建详细内容", reportId);
        buildContentWithDetails(response, reportId);

        return response;
    }

    /**
     * 根据用户查询周报列表
     */
    @Transactional(readOnly = true)
    public List<WeeklyReport> getWeeklyReportsByUserId(Long userId) {
        return weeklyReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 根据用户查询周报列表 - 分页版本
     */
    @Transactional(readOnly = true)
    public Page<WeeklyReportDetailResponse> getDetailedWeeklyReportsByUserId(Long userId, Pageable pageable) {
        try {
            logger.info("🔍🔍🔍 Service层 - getDetailedWeeklyReportsByUserId 被调用，用户ID: {}, 分页: {}", userId, pageable);
            
            // 0. 首先查询基础数据作为对比
            List<WeeklyReport> basicReports = weeklyReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
            logger.info("🔍 Service层对比 - 基础查询（无JOIN）获取到的周报数量: {}", basicReports.size());
            
            // 1. 获取用户的所有周报（包含AI分析结果）
            Page<Object[]> reportWithAIPage = weeklyReportRepository.findByUserIdWithAIAnalysis(userId, pageable);
            List<Object[]> reportWithAI = reportWithAIPage.getContent();
            logger.info("🔍 Service层 - 分页查询，当前页: {}, 单页大小: {}, 获取到的周报数量: {}", 
                       pageable.getPageNumber(), pageable.getPageSize(), reportWithAI.size());
            logger.info("🔍 Service层 - 从Repository获取到的周报数量（含AI）: {}", reportWithAI.size());
            
            // 2. 验证数据一致性
            if (basicReports.size() != reportWithAI.size()) {
                logger.error("❌❌❌ 数据不一致！基础查询: {} 条, 含AI查询: {} 条", 
                           basicReports.size(), reportWithAI.size());
                
                // 详细分析差异
                List<Long> basicIds = basicReports.stream().map(WeeklyReport::getId).toList();
                List<Long> aiQueryIds = reportWithAI.stream()
                    .map(arr -> ((WeeklyReport) arr[0]).getId()).toList();
                
                logger.error("❌ 基础查询ID列表: {}", basicIds);
                logger.error("❌ AI查询ID列表: {}", aiQueryIds);
                
                // 找出重复的ID
                List<Long> duplicateIds = aiQueryIds.stream()
                    .filter(id -> java.util.Collections.frequency(aiQueryIds, id) > 1)
                    .distinct().toList();
                
                if (!duplicateIds.isEmpty()) {
                    logger.error("❌ 发现重复的周报ID: {}", duplicateIds);
                }
            } else {
                logger.info("✅ 数据一致性检查通过：基础查询和AI查询返回相同数量的周报");
            }
            
            // 处理查询结果，将重复的周报合并，只保留最新的AI分析
            Map<Long, WeeklyReportDetailResponse> reportMap = new LinkedHashMap<>();
            
            for (Object[] result : reportWithAI) {
                WeeklyReport report = (WeeklyReport) result[0];
                AIAnalysisResult aiAnalysis = null;
                User reportUser = null;

                // 安全地处理AI分析结果
                if (result[1] != null) {
                    try {
                        aiAnalysis = (AIAnalysisResult) result[1];
                    } catch (ClassCastException e) {
                        logger.warn("🔍 AI分析结果类型转换失败 - 周报ID: {}, 对象类型: {}", 
                                   report.getId(), result[1].getClass().getSimpleName());
                    }
                }
                if (result.length > 2 && result[2] instanceof User user) {
                    reportUser = user;
                }
                
                Long reportId = report.getId();
                
                if (!reportMap.containsKey(reportId)) {
                    // 第一次遇到这个周报，创建响应对象
                    WeeklyReportDetailResponse response = new WeeklyReportDetailResponse();
                    
                    // 填充基本信息
                    response.setId(report.getId());
                    response.setTitle(report.getTitle());
                    response.setReportWeek(report.getReportWeek());
                    response.setStatus(report.getStatus().name());
                    response.setUserId(report.getUserId());
                    response.setCreatedAt(report.getCreatedAt());
                    response.setUpdatedAt(report.getUpdatedAt());
                    response.setAdditionalNotes(report.getAdditionalNotes());
                    response.setDevelopmentOpportunities(report.getDevelopmentOpportunities());
                    response.setRejectionReason(report.getRejectionReason());
                    response.setRejectedBy(report.getRejectedBy() != null ? report.getRejectedBy().name() : null);
                    if (reportUser != null) {
                        response.setUsername(reportUser.getUsername());
                        response.setCreatorName(reportUser.getFullName());
                        response.setCreatorUsername(reportUser.getUsername());
                    }

                    // 填充审核人信息
                    if (report.getAdminReviewerId() != null) {
                        userRepository.findById(report.getAdminReviewerId()).ifPresent(reviewer -> {
                            response.setReviewerName(reviewer.getFullName());
                            response.setReviewerUsername(reviewer.getUsername());
                        });
                    }
                    
                    // 填充AI分析信息
                    if (aiAnalysis != null) {
                        logger.info("🔍 AI分析详情 - ID: {}, 状态: {}, 置信度: {}", 
                                   aiAnalysis.getId(), aiAnalysis.getStatus(), aiAnalysis.getConfidence());
                        response.setAiAnalysisId(aiAnalysis.getId());
                        response.setAiAnalysisResult(aiAnalysis.getResult());
                        response.setAiConfidence(aiAnalysis.getConfidence());
                        response.setAiAnalysisStatus(aiAnalysis.getStatus().name());
                        response.setAiAnalysisCompletedAt(aiAnalysis.getCompletedAt());
                    } else {
                        logger.info("🔍 AI分析为空 - 周报ID: {}", report.getId());
                    }
                    
                    // 查询关联的任务报告数据
                    buildContentWithDetails(response, report.getId());
                    
                    reportMap.put(reportId, response);
                } else {
                    // 已存在此周报，检查是否有更新的AI分析
                    WeeklyReportDetailResponse existingResponse = reportMap.get(reportId);
                    if (aiAnalysis != null && 
                        (existingResponse.getAiAnalysisCompletedAt() == null ||
                         (aiAnalysis.getCompletedAt() != null && 
                          aiAnalysis.getCompletedAt().isAfter(existingResponse.getAiAnalysisCompletedAt())))) {
                        
                        logger.info("🔍 发现更新的AI分析 - 周报ID: {}, 新AI分析ID: {}", 
                                   reportId, aiAnalysis.getId());
                        existingResponse.setAiAnalysisId(aiAnalysis.getId());
                        existingResponse.setAiAnalysisResult(aiAnalysis.getResult());
                        existingResponse.setAiConfidence(aiAnalysis.getConfidence());
                        existingResponse.setAiAnalysisStatus(aiAnalysis.getStatus().name());
                        existingResponse.setAiAnalysisCompletedAt(aiAnalysis.getCompletedAt());
                    }
                    if (existingResponse.getUsername() == null && reportUser != null) {
                        existingResponse.setUsername(reportUser.getUsername());
                    }
                }
            }
            
            List<WeeklyReportDetailResponse> result = new ArrayList<>(reportMap.values());
            
            // 返回分页结果
            return new PageImpl<>(result, pageable, reportWithAIPage.getTotalElements());
            
        } catch (Exception e) {
            logger.error("获取详细周报列表失败", e);
            throw new RuntimeException("获取详细周报列表失败: " + e.getMessage());
        }
    }

    /**
     * 构建包含深度查询数据的内容结构
     * 使用 isWeek 字段区分本周汇报（isWeek=true）和下周规划（isWeek=false）
     */
    private void buildContentWithDetails(WeeklyReportDetailResponse response, Long weeklyReportId) {
        // 创建内容结构
        WeeklyReportDetailResponse.ContentDetailDTO content = new WeeklyReportDetailResponse.ContentDetailDTO();
        WeeklyReportDetailResponse.NextWeekPlanDetailDTO nextWeekPlan = new WeeklyReportDetailResponse.NextWeekPlanDetailDTO();
        
        // 查询所有日常性任务报告
        List<TaskReport> allTaskReports = taskReportRepository.findByWeeklyReportId(weeklyReportId);
        
        // 按 isWeek 字段分离本周汇报和下周规划
        List<WeeklyReportDetailResponse.RoutineTaskDetailDTO> thisWeekRoutineTasks = allTaskReports.stream()
            .filter(taskReport -> taskReport.getIsWeek() != null && taskReport.getIsWeek()) // 本周汇报 isWeek=true
            .map(this::mapToRoutineTaskDetailDTO)
            .collect(Collectors.toList());
        
        List<WeeklyReportDetailResponse.NextWeekRoutineTaskDetailDTO> nextWeekRoutineTasks = allTaskReports.stream()
            .filter(taskReport -> taskReport.getIsWeek() != null && !taskReport.getIsWeek()) // 下周规划 isWeek=false
            .map(this::mapToNextWeekRoutineTaskDetailDTO)
            .collect(Collectors.toList());
        
        // 查询所有发展性任务报告
        List<DevTaskReport> allDevTaskReports = devTaskReportRepository.findByWeeklyReportId(weeklyReportId);
        
        // 按 isWeek 字段分离本周汇报和下周规划
        List<WeeklyReportDetailResponse.DevelopmentalTaskDetailDTO> thisWeekDevelopmentalTasks = allDevTaskReports.stream()
            .filter(devTaskReport -> devTaskReport.getIsWeek() != null && devTaskReport.getIsWeek()) // 本周汇报 isWeek=true
            .map(this::mapToDevelopmentalTaskDetailDTO)
            .collect(Collectors.toList());
        
        List<WeeklyReportDetailResponse.NextWeekDevelopmentalTaskDetailDTO> nextWeekDevelopmentalTasks = allDevTaskReports.stream()
            .filter(devTaskReport -> devTaskReport.getIsWeek() != null && !devTaskReport.getIsWeek()) // 下周规划 isWeek=false
            .map(this::mapToNextWeekDevelopmentalTaskDetailDTO)
            .collect(Collectors.toList());
        
        // 设置本周汇报内容
        content.setRoutineTasks(thisWeekRoutineTasks);
        content.setDevelopmentalTasks(thisWeekDevelopmentalTasks);
        
        // 设置下周规划内容
        nextWeekPlan.setRoutineTasks(nextWeekRoutineTasks);
        nextWeekPlan.setDevelopmentalTasks(nextWeekDevelopmentalTasks);
        
        response.setContent(content);
        response.setNextWeekPlan(nextWeekPlan);
    }

    /**
     * 映射日常性任务报告到详情DTO
     */
    private WeeklyReportDetailResponse.RoutineTaskDetailDTO mapToRoutineTaskDetailDTO(TaskReport taskReport) {
        WeeklyReportDetailResponse.RoutineTaskDetailDTO dto = new WeeklyReportDetailResponse.RoutineTaskDetailDTO();
        dto.setTask_id(String.valueOf(taskReport.getTask().getId()));
        dto.setActual_result(taskReport.getActualResults()); // 修正方法名
        dto.setAnalysisofResultDifferences(taskReport.getResultDifferenceAnalysis());
        
        // 设置任务详细信息
        Task task = taskReport.getTask();
        WeeklyReportDetailResponse.TaskDetailInfo taskDetails = new WeeklyReportDetailResponse.TaskDetailInfo();
        taskDetails.setTaskName(task.getTaskName());
        taskDetails.setPersonnelAssignment(task.getPersonnelAssignment());
        taskDetails.setTimeline(task.getTimeline());
        taskDetails.setExpectedResults(task.getExpectedResults());
        dto.setTaskDetails(taskDetails);
        
        return dto;
    }

    /**
     * 映射发展性任务报告到详情DTO
     */
    private WeeklyReportDetailResponse.DevelopmentalTaskDetailDTO mapToDevelopmentalTaskDetailDTO(DevTaskReport devTaskReport) {
        WeeklyReportDetailResponse.DevelopmentalTaskDetailDTO dto = new WeeklyReportDetailResponse.DevelopmentalTaskDetailDTO();
        dto.setProject_id(String.valueOf(devTaskReport.getProject().getId()));
        dto.setPhase_id(devTaskReport.getProjectPhase() != null ? String.valueOf(devTaskReport.getProjectPhase().getId()) : ""); // 修正方法名
        dto.setActual_result(devTaskReport.getActualResults()); // 修正方法名
        dto.setAnalysisofResultDifferences(devTaskReport.getResultDifferenceAnalysis());
        
        // 设置项目详细信息
        Project project = devTaskReport.getProject();
        WeeklyReportDetailResponse.ProjectDetailInfo projectDetails = new WeeklyReportDetailResponse.ProjectDetailInfo();
        projectDetails.setProjectName(project.getName());
        projectDetails.setProjectContent(project.getDescription());
        projectDetails.setProjectMembers(project.getMembers());
        projectDetails.setExpectedResults(project.getExpectedResults());
        projectDetails.setTimeline(project.getTimeline());
        projectDetails.setStopLoss(project.getStopLoss());
        dto.setProjectDetails(projectDetails);
        
        // 设置阶段详细信息
        if (devTaskReport.getProjectPhase() != null) { // 修正方法名
            ProjectPhase phase = devTaskReport.getProjectPhase(); // 修正方法名
            WeeklyReportDetailResponse.PhaseDetailInfo phaseDetails = new WeeklyReportDetailResponse.PhaseDetailInfo();
            phaseDetails.setPhaseName(phase.getPhaseName());
            phaseDetails.setPhaseDescription(phase.getDescription());
            phaseDetails.setAssignedMembers(phase.getAssignedMembers());
            phaseDetails.setTimeline(phase.getSchedule());
            phaseDetails.setEstimatedResults(phase.getExpectedResults());
            dto.setPhaseDetails(phaseDetails);
        }
        
        return dto;
    }

    /**
     * 映射下周日常性任务到详情DTO
     */
    private WeeklyReportDetailResponse.NextWeekRoutineTaskDetailDTO mapToNextWeekRoutineTaskDetailDTO(TaskReport taskReport) {
        WeeklyReportDetailResponse.NextWeekRoutineTaskDetailDTO dto = new WeeklyReportDetailResponse.NextWeekRoutineTaskDetailDTO();
        dto.setTask_id(String.valueOf(taskReport.getTask().getId()));
        
        // 设置任务详细信息
        Task task = taskReport.getTask();
        WeeklyReportDetailResponse.TaskDetailInfo taskDetails = new WeeklyReportDetailResponse.TaskDetailInfo();
        taskDetails.setTaskName(task.getTaskName());
        taskDetails.setPersonnelAssignment(task.getPersonnelAssignment());
        taskDetails.setTimeline(task.getTimeline());
        taskDetails.setExpectedResults(task.getExpectedResults());
        dto.setTaskDetails(taskDetails);
        
        return dto;
    }

    /**
     * 映射下周发展性任务到详情DTO
     */
    private WeeklyReportDetailResponse.NextWeekDevelopmentalTaskDetailDTO mapToNextWeekDevelopmentalTaskDetailDTO(DevTaskReport devTaskReport) {
        WeeklyReportDetailResponse.NextWeekDevelopmentalTaskDetailDTO dto = new WeeklyReportDetailResponse.NextWeekDevelopmentalTaskDetailDTO();
        dto.setProject_id(String.valueOf(devTaskReport.getProject().getId()));
        dto.setPhase_id(devTaskReport.getProjectPhase() != null ? String.valueOf(devTaskReport.getProjectPhase().getId()) : ""); // 修正方法名
        
        // 设置项目详细信息
        Project project = devTaskReport.getProject();
        WeeklyReportDetailResponse.ProjectDetailInfo projectDetails = new WeeklyReportDetailResponse.ProjectDetailInfo();
        projectDetails.setProjectName(project.getName());
        projectDetails.setProjectContent(project.getDescription());
        projectDetails.setProjectMembers(project.getMembers());
        projectDetails.setExpectedResults(project.getExpectedResults());
        projectDetails.setTimeline(project.getTimeline());
        projectDetails.setStopLoss(project.getStopLoss());
        dto.setProjectDetails(projectDetails);
        
        // 设置阶段详细信息
        if (devTaskReport.getProjectPhase() != null) { // 修正方法名
            ProjectPhase phase = devTaskReport.getProjectPhase(); // 修正方法名
            WeeklyReportDetailResponse.PhaseDetailInfo phaseDetails = new WeeklyReportDetailResponse.PhaseDetailInfo();
            phaseDetails.setPhaseName(phase.getPhaseName());
            phaseDetails.setPhaseDescription(phase.getDescription());
            phaseDetails.setAssignedMembers(phase.getAssignedMembers());
            phaseDetails.setTimeline(phase.getSchedule());
            phaseDetails.setEstimatedResults(phase.getExpectedResults());
            dto.setPhaseDetails(phaseDetails);
        }
        
        return dto;
    }

    /**
     * 根据状态查询周报
     */
    @Transactional(readOnly = true)
    public List<WeeklyReport> getWeeklyReportsByStatus(WeeklyReport.ReportStatus status) {
        return weeklyReportRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    /**
     * 获取所有周报列表（包含AI分析结果）- 分页版本
     */
    @Transactional(readOnly = true)
    public Page<WeeklyReportDetailResponse> getAllWeeklyReportsWithAIAnalysis(Pageable pageable) {
        try {
            Page<Object[]> reportWithAIPage = weeklyReportRepository.findAllWithAIAnalysis(pageable);
            List<Object[]> reportWithAI = reportWithAIPage.getContent();
            
            // 处理查询结果，将重复的周报合并，只保留最新的AI分析
            Map<Long, WeeklyReportDetailResponse> reportMap = new LinkedHashMap<>();
            
            for (Object[] result : reportWithAI) {
                WeeklyReport report = (WeeklyReport) result[0];
                AIAnalysisResult aiAnalysis = null;
                User reportUser = null;

                // 安全地处理AI分析结果
                if (result[1] != null) {
                    try {
                        aiAnalysis = (AIAnalysisResult) result[1];
                    } catch (ClassCastException e) {
                        logger.warn("🔍 getAllWeeklyReports AI分析结果类型转换失败 - 周报ID: {}, 对象类型: {}", 
                                   report.getId(), result[1].getClass().getSimpleName());
                    }
                }
                if (result.length > 2 && result[2] instanceof User user) {
                    reportUser = user;
                }
                
                Long reportId = report.getId();
                
                if (!reportMap.containsKey(reportId)) {
                    // 第一次遇到这个周报，创建响应对象
                    WeeklyReportDetailResponse response = new WeeklyReportDetailResponse();
                    
                    // 填充基本信息
                    response.setId(report.getId());
                    response.setTitle(report.getTitle());
                    response.setReportWeek(report.getReportWeek());
                    response.setStatus(report.getStatus().name());
                    response.setUserId(report.getUserId());
                    response.setCreatedAt(report.getCreatedAt());
                    response.setUpdatedAt(report.getUpdatedAt());
                    response.setAdditionalNotes(report.getAdditionalNotes());
                    response.setDevelopmentOpportunities(report.getDevelopmentOpportunities());
                    response.setRejectionReason(report.getRejectionReason());
                    response.setRejectedBy(report.getRejectedBy() != null ? report.getRejectedBy().name() : null);
                    if (reportUser != null) {
                        response.setUsername(reportUser.getUsername());
                        response.setCreatorName(reportUser.getFullName());
                        response.setCreatorUsername(reportUser.getUsername());
                    }

                    // 填充审核人信息
                    if (report.getAdminReviewerId() != null) {
                        userRepository.findById(report.getAdminReviewerId()).ifPresent(reviewer -> {
                            response.setReviewerName(reviewer.getFullName());
                            response.setReviewerUsername(reviewer.getUsername());
                        });
                    }
                    
                    // 填充AI分析信息
                    if (aiAnalysis != null) {
                        response.setAiAnalysisId(aiAnalysis.getId());
                        response.setAiAnalysisResult(aiAnalysis.getResult());
                        response.setAiConfidence(aiAnalysis.getConfidence());
                        response.setAiAnalysisStatus(aiAnalysis.getStatus().name());
                        response.setAiAnalysisCompletedAt(aiAnalysis.getCompletedAt());
                    }
                    
                    // 查询关联的任务报告数据
                    buildContentWithDetails(response, report.getId());
                    
                    reportMap.put(reportId, response);
                } else {
                    // 已存在此周报，检查是否有更新的AI分析
                    WeeklyReportDetailResponse existingResponse = reportMap.get(reportId);
                    if (aiAnalysis != null && 
                        (existingResponse.getAiAnalysisCompletedAt() == null ||
                         (aiAnalysis.getCompletedAt() != null && 
                          aiAnalysis.getCompletedAt().isAfter(existingResponse.getAiAnalysisCompletedAt())))) {
                        
                        existingResponse.setAiAnalysisId(aiAnalysis.getId());
                        existingResponse.setAiAnalysisResult(aiAnalysis.getResult());
                        existingResponse.setAiConfidence(aiAnalysis.getConfidence());
                        existingResponse.setAiAnalysisStatus(aiAnalysis.getStatus().name());
                        existingResponse.setAiAnalysisCompletedAt(aiAnalysis.getCompletedAt());
                    }
                    if (existingResponse.getUsername() == null && reportUser != null) {
                        existingResponse.setUsername(reportUser.getUsername());
                    }
                }
            }
            
            List<WeeklyReportDetailResponse> result = new ArrayList<>(reportMap.values());
            
            return new PageImpl<>(result, pageable, reportWithAIPage.getTotalElements());
            
        } catch (Exception e) {
            logger.error("获取所有周报列表失败", e);
            throw new RuntimeException("获取所有周报列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态获取周报列表（包含AI分析结果）- 分页版本
     */
    @Transactional(readOnly = true)
    public Page<WeeklyReportDetailResponse> getWeeklyReportsByStatusWithAIAnalysis(WeeklyReport.ReportStatus status, Pageable pageable) {
        try {
            Page<Object[]> reportWithAIPage = weeklyReportRepository.findByStatusWithAIAnalysis(status, pageable);
            List<Object[]> reportWithAI = reportWithAIPage.getContent();
            
            // 处理查询结果，将重复的周报合并，只保留最新的AI分析
            Map<Long, WeeklyReportDetailResponse> reportMap = new LinkedHashMap<>();
            
            for (Object[] result : reportWithAI) {
                WeeklyReport report = (WeeklyReport) result[0];
                AIAnalysisResult aiAnalysis = null;
                User reportUser = null;

                // 安全地处理AI分析结果
                if (result[1] != null) {
                    try {
                        aiAnalysis = (AIAnalysisResult) result[1];
                    } catch (ClassCastException e) {
                        logger.warn("🔍 getWeeklyReportsByStatus AI分析结果类型转换失败 - 周报ID: {}, 对象类型: {}", 
                                   report.getId(), result[1].getClass().getSimpleName());
                    }
                }
                if (result.length > 2 && result[2] instanceof User user) {
                    reportUser = user;
                }
                
                Long reportId = report.getId();
                
                if (!reportMap.containsKey(reportId)) {
                    // 第一次遇到这个周报，创建响应对象
                    WeeklyReportDetailResponse response = new WeeklyReportDetailResponse();
                    
                    // 填充基本信息
                    response.setId(report.getId());
                    response.setTitle(report.getTitle());
                    response.setReportWeek(report.getReportWeek());
                    response.setStatus(report.getStatus().name());
                    response.setUserId(report.getUserId());
                    response.setCreatedAt(report.getCreatedAt());
                    response.setUpdatedAt(report.getUpdatedAt());
                    response.setAdditionalNotes(report.getAdditionalNotes());
                    response.setDevelopmentOpportunities(report.getDevelopmentOpportunities());
                    response.setRejectionReason(report.getRejectionReason());
                    response.setRejectedBy(report.getRejectedBy() != null ? report.getRejectedBy().name() : null);
                    if (reportUser != null) {
                        response.setUsername(reportUser.getUsername());
                        response.setCreatorName(reportUser.getFullName());
                        response.setCreatorUsername(reportUser.getUsername());
                    }

                    // 填充审核人信息
                    if (report.getAdminReviewerId() != null) {
                        userRepository.findById(report.getAdminReviewerId()).ifPresent(reviewer -> {
                            response.setReviewerName(reviewer.getFullName());
                            response.setReviewerUsername(reviewer.getUsername());
                        });
                    }
                    
                    // 填充AI分析信息
                    if (aiAnalysis != null) {
                        response.setAiAnalysisId(aiAnalysis.getId());
                        response.setAiAnalysisResult(aiAnalysis.getResult());
                        response.setAiConfidence(aiAnalysis.getConfidence());
                        response.setAiAnalysisStatus(aiAnalysis.getStatus().name());
                        response.setAiAnalysisCompletedAt(aiAnalysis.getCompletedAt());
                    }
                    
                    // 查询关联的任务报告数据
                    buildContentWithDetails(response, report.getId());
                    
                    reportMap.put(reportId, response);
                } else {
                    // 已存在此周报，检查是否有更新的AI分析
                    WeeklyReportDetailResponse existingResponse = reportMap.get(reportId);
                    if (aiAnalysis != null && 
                        (existingResponse.getAiAnalysisCompletedAt() == null ||
                         (aiAnalysis.getCompletedAt() != null && 
                          aiAnalysis.getCompletedAt().isAfter(existingResponse.getAiAnalysisCompletedAt())))) {
                        
                        existingResponse.setAiAnalysisId(aiAnalysis.getId());
                        existingResponse.setAiAnalysisResult(aiAnalysis.getResult());
                        existingResponse.setAiConfidence(aiAnalysis.getConfidence());
                        existingResponse.setAiAnalysisStatus(aiAnalysis.getStatus().name());
                        existingResponse.setAiAnalysisCompletedAt(aiAnalysis.getCompletedAt());
                    }
                    if (existingResponse.getUsername() == null && reportUser != null) {
                        existingResponse.setUsername(reportUser.getUsername());
                    }
                }
            }
            
            List<WeeklyReportDetailResponse> result = new ArrayList<>(reportMap.values());
            
            return new PageImpl<>(result, pageable, reportWithAIPage.getTotalElements());

        } catch (Exception e) {
            logger.error("根据状态获取周报列表失败", e);
            throw new RuntimeException("根据状态获取周报列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态和拒绝者获取周报列表（包含AI分析结果）- 分页版本
     */
    @Transactional(readOnly = true)
    public Page<WeeklyReportDetailResponse> getWeeklyReportsByStatusAndRejectedBy(
            WeeklyReport.ReportStatus status,
            WeeklyReport.RejectedBy rejectedBy,
            Pageable pageable) {
        try {
            Page<Object[]> reportWithAIPage = weeklyReportRepository.findByStatusAndRejectedByWithAIAnalysis(status, rejectedBy, pageable);
            List<Object[]> reportWithAI = reportWithAIPage.getContent();

            // 处理查询结果，将重复的周报合并，只保留最新的AI分析
            Map<Long, WeeklyReportDetailResponse> reportMap = new LinkedHashMap<>();

            for (Object[] result : reportWithAI) {
                WeeklyReport report = (WeeklyReport) result[0];
                AIAnalysisResult aiAnalysis = result[1] != null ? (AIAnalysisResult) result[1] : null;
                User reportUser = result.length > 2 && result[2] instanceof User ? (User) result[2] : null;

                Long reportId = report.getId();

                if (!reportMap.containsKey(reportId)) {
                    // 创建响应对象
                    WeeklyReportDetailResponse response = new WeeklyReportDetailResponse();

                    // 填充基本信息
                    response.setId(report.getId());
                    response.setTitle(report.getTitle());
                    response.setReportWeek(report.getReportWeek());
                    response.setStatus(report.getStatus().name());
                    response.setUserId(report.getUserId());
                    response.setCreatedAt(report.getCreatedAt());
                    response.setUpdatedAt(report.getUpdatedAt());
                    response.setAdditionalNotes(report.getAdditionalNotes());
                    response.setDevelopmentOpportunities(report.getDevelopmentOpportunities());
                    response.setRejectionReason(report.getRejectionReason());
                    response.setRejectedBy(report.getRejectedBy() != null ? report.getRejectedBy().name() : null);
                    if (reportUser != null) {
                        response.setUsername(reportUser.getUsername());
                        response.setCreatorName(reportUser.getFullName());
                        response.setCreatorUsername(reportUser.getUsername());
                    }

                    // 填充审核人信息
                    if (report.getAdminReviewerId() != null) {
                        userRepository.findById(report.getAdminReviewerId()).ifPresent(reviewer -> {
                            response.setReviewerName(reviewer.getFullName());
                            response.setReviewerUsername(reviewer.getUsername());
                        });
                    }

                    // 填充AI分析信息
                    if (aiAnalysis != null) {
                        response.setAiAnalysisId(aiAnalysis.getId());
                        response.setAiAnalysisResult(aiAnalysis.getResult());
                        response.setAiConfidence(aiAnalysis.getConfidence());
                        response.setAiAnalysisStatus(aiAnalysis.getStatus().name());
                        response.setAiAnalysisCompletedAt(aiAnalysis.getCompletedAt());
                    }

                    // 查询关联的任务报告数据
                    buildContentWithDetails(response, report.getId());

                    reportMap.put(reportId, response);
                }
            }

            List<WeeklyReportDetailResponse> result = new ArrayList<>(reportMap.values());

            return new PageImpl<>(result, pageable, reportWithAIPage.getTotalElements());

        } catch (Exception e) {
            logger.error("根据状态和拒绝者获取周报列表失败", e);
            throw new RuntimeException("根据状态和拒绝者获取周报列表失败: " + e.getMessage());
        }
    }

    /**
     * 映射TaskReport实体到TaskReportDTO
     */
    private TaskReportDTO mapToTaskReportDTO(TaskReport taskReport) {
        Task task = taskReport.getTask();
        TaskReportDTO dto = new TaskReportDTO();
        
        dto.setTaskId(task.getId());
        dto.setTaskName(task.getTaskName());
        dto.setPersonnelAssignment(task.getPersonnelAssignment());
        dto.setTimeline(task.getTimeline());
        dto.setExpectedResults(task.getExpectedResults());
        
        // 重要：从TaskReport关联表获取执行结果，而不是Task表
        dto.setActualResults(taskReport.getActualResults());
        dto.setResultDifferenceAnalysis(taskReport.getResultDifferenceAnalysis());
        
        return dto;
    }
    
    /**
     * 映射DevTaskReport实体到DevTaskReportDTO
     */
    private DevTaskReportDTO mapToDevTaskReportDTO(DevTaskReport devTaskReport) {
        Project project = devTaskReport.getProject();
        ProjectPhase phase = devTaskReport.getProjectPhase();
        DevTaskReportDTO dto = new DevTaskReportDTO();
        
        dto.setProjectId(project.getId());
        dto.setProjectName(project.getName());
        dto.setProjectDescription(project.getDescription());
        dto.setPhasesId(phase.getId());
        dto.setPhaseName(phase.getPhaseName());
        dto.setPhaseDescription(phase.getDescription());
        dto.setAssignedMembers(phase.getAssignedMembers());
        dto.setSchedule(phase.getSchedule());
        dto.setExpectedResults(phase.getExpectedResults());
        
        // 重要：从DevTaskReport关联表获取执行结果，而不是ProjectPhase表
        dto.setActualResults(devTaskReport.getActualResults());
        dto.setResultDifferenceAnalysis(devTaskReport.getResultDifferenceAnalysis());
        
        return dto;
    }
    
    /**
     * 提交周报进行审核
     * 用户明确提交后才触发AI分析
     */
    @Transactional
    public WeeklyReport submitForReview(Long reportId) {
        logger.info("📤 开始提交周报审核流程，周报ID: {}", reportId);

        // 1. 使用悲观锁加载周报
        WeeklyReport report = weeklyReportRepository.findByIdForUpdate(reportId)
            .orElseThrow(() -> new RuntimeException("周报不存在: " + reportId));

        // 2. 状态检查：只能提交草稿或已拒绝
        if (!report.isEditable()) {
            throw new IllegalStateException(
                String.format("只能提交草稿或已拒绝状态的周报，当前状态: %s", report.getStatus())
            );
        }

        // 3. 内容完整性检查
        validateReportCompleteness(report);

        // 4. 提交周报（状态转换）
        logger.info("📤 周报状态转换: {} → AI_PROCESSING, 周报ID: {}", report.getStatus(), reportId);
        report.submit(); // 调用实体的submit()方法
        WeeklyReport savedReport = weeklyReportRepository.save(report);

        logger.info("✅ 周报已提交审核，周报ID: {}, 状态: {}",
            reportId, savedReport.getStatus());

        // 5. 触发AI分析
        try {
            logger.info("🤖 开始触发AI分析，周报ID: {}", reportId);
            triggerAIAnalysis(savedReport);
        } catch (Exception e) {
            logger.error("🤖 AI分析触发失败，周报ID: {}", reportId, e);
            // AI分析失败时拒绝
            savedReport.aiReject("AI分析启动失败: " + e.getMessage());
            weeklyReportRepository.save(savedReport);
            throw new RuntimeException("AI分析启动失败: " + e.getMessage(), e);
        }

        // 6. 发送周报提交通知
        try {
            logger.info("📧 发送周报提交通知，周报ID: {}", reportId);
            notificationService.handleWeeklyReportSubmitted(reportId, savedReport.getUserId());
        } catch (Exception e) {
            logger.error("📧 发送周报提交通知失败，周报ID: {}", reportId, e);
            // 通知失败不影响提交流程
        }

        return savedReport;
    }

    /**
     * 内容完整性校验
     */
    private void validateReportCompleteness(WeeklyReport report) {
        List<String> errors = new ArrayList<>();

        if (report.getTitle() == null || report.getTitle().trim().isEmpty()) {
            errors.add("周报标题不能为空");
        }

        // 检查是否有任务报告
        List<TaskReport> taskReports = taskReportRepository.findByWeeklyReportId(report.getId());
        List<DevTaskReport> devTaskReports = devTaskReportRepository.findByWeeklyReportId(report.getId());

        if ((taskReports == null || taskReports.isEmpty()) &&
            (devTaskReports == null || devTaskReports.isEmpty())) {
            errors.add("至少需要添加一项任务报告（日常任务或发展任务）");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                "周报内容不完整，无法提交：" + String.join("; ", errors)
            );
        }

        logger.info("✅ 周报内容完整性检查通过，周报ID: {}", report.getId());
    }

    /**
     * 触发AI分析
     */
    private void triggerAIAnalysis(WeeklyReport report) {
        logger.info("🤖 ===============异步AI分析触发开始===============");
        logger.info("🤖 周报ID: {}", report.getId());
        logger.info("🤖 周报标题: {}", report.getTitle());
        logger.info("🤖 当前状态: {}", report.getStatus());
        logger.info("🤖 用户ID: {}", report.getUserId());
        logger.info("🤖 ============================================");

        try {
            // 调用AI分析服务进行异步周报分析
            logger.info("🤖 正在启动异步AI分析任务...");
            CompletableFuture<AIAnalysisResult> analysisResult = aiAnalysisService.analyzeWeeklyReportAsync(report);

            // 注册异步完成时的回调处理
            analysisResult.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    logger.error("🤖 ❌ 异步AI分析失败，周报ID: {}, 错误: {}",
                                report.getId(), throwable.getMessage());

                    // 尝试更新周报状态为AI拒绝
                    try {
                        WeeklyReport failedReport = weeklyReportRepository.findById(report.getId()).orElse(null);
                        if (failedReport != null && failedReport.isAIProcessing()) {
                            failedReport.aiReject("异步AI分析失败: " + throwable.getMessage());
                            weeklyReportRepository.save(failedReport);
                            logger.info("🤖 周报状态已更新为REJECTED (异步分析失败)");
                        }
                    } catch (Exception e) {
                        logger.error("🤖 ❌ 更新失败状态时出错", e);
                    }
                } else {
                    logger.info("🤖 ✅ 异步AI分析成功完成，周报ID: {}, 分析结果ID: {}",
                               report.getId(), result != null ? result.getId() : "null");

                    // 查询并记录最终状态
                    try {
                        WeeklyReport updatedReport = weeklyReportRepository.findById(report.getId()).orElse(null);
                        if (updatedReport != null) {
                            logger.info("🤖 异步分析后周报状态: {}", updatedReport.getStatus());
                        }
                    } catch (Exception e) {
                        logger.error("🤖 ❌ 查询最终状态时出错", e);
                    }
                }
            });

            logger.info("🤖 ✅ 异步AI分析任务已启动，周报ID: {}", report.getId());
            logger.info("🤖 ===============异步AI分析触发完成===============");
        } catch (Exception e) {
            logger.error("🤖 ❌ 异步AI分析启动失败，周报ID: {}, 错误类型: {}, 错误信息: {}",
                         report.getId(), e.getClass().getSimpleName(), e.getMessage());
            logger.error("🤖 完整错误堆栈: ", e);

            // 同步启动失败时，标记为AI拒绝
            try {
                if (report.isAIProcessing()) {
                    report.aiReject("异步任务启动失败: " + e.getMessage());
                    weeklyReportRepository.save(report);
                    logger.info("🤖 周报状态已更新为REJECTED (异步任务启动失败)");
                }
            } catch (Exception statusException) {
                logger.error("🤖 ❌ 更新失败状态时出错", statusException);
            }

            // 不抛出异常，避免影响周报提交流程
            // throw e; // 注释掉，让周报提交成功，只是AI分析失败
        }
    }

    /**
     * 处理来自更新请求的本周汇报内容
     */
    private void processThisWeekContentFromUpdateRequest(WeeklyReport weeklyReport, WeeklyReportUpdateRequest.ContentDTO content) {
        logger.info("🔧 Starting to process this week content from update request for weekly report ID: {}", weeklyReport.getId());
        
        // 处理日常性任务
        if (content.getRoutineTasks() != null) {
            logger.info("🔧 Processing {} routine tasks from update request", content.getRoutineTasks().size());
            for (WeeklyReportUpdateRequest.RoutineTaskDTO routineTask : content.getRoutineTasks()) {
                logger.info("🔧 Processing routine task with ID: {}", routineTask.getTask_id());
                processRoutineTaskFromUpdateRequest(weeklyReport, routineTask);
            }
        } else {
            logger.warn("🔧 ⚠️ No routine tasks found in update content");
        }

        // 处理发展性任务
        if (content.getDevelopmentalTasks() != null) {
            logger.info("🔧 Processing {} developmental tasks from update request", content.getDevelopmentalTasks().size());
            for (WeeklyReportUpdateRequest.DevelopmentalTaskDTO devTask : content.getDevelopmentalTasks()) {
                logger.info("🔧 Processing developmental task with project ID: {} and phase ID: {}", devTask.getProject_id(), devTask.getPhase_id());
                processDevelopmentalTaskFromUpdateRequest(weeklyReport, devTask);
            }
        } else {
            logger.warn("🔧 ⚠️ No developmental tasks found in update content");
        }
        
        logger.info("🔧 ✅ Completed processing this week content from update request for weekly report ID: {}", weeklyReport.getId());
    }

    /**
     * 处理来自更新请求的下周规划
     */
    private void processNextWeekPlanFromUpdateRequest(WeeklyReport weeklyReport, WeeklyReportUpdateRequest.NextWeekPlanDTO nextWeekPlan) {
        logger.info("🔧 Starting to process next week plan from update request for weekly report ID: {}", weeklyReport.getId());
        
        // 处理下周日常性任务
        if (nextWeekPlan.getRoutineTasks() != null) {
            logger.info("🔧 Processing {} next week routine tasks from update request", nextWeekPlan.getRoutineTasks().size());
            for (WeeklyReportUpdateRequest.NextWeekRoutineTaskDTO nextWeekTask : nextWeekPlan.getRoutineTasks()) {
                processNextWeekRoutineTaskFromUpdateRequest(weeklyReport, nextWeekTask);
            }
        } else {
            logger.warn("🔧 ⚠️ No next week routine tasks found in update request");
        }

        // 处理下周发展性任务
        if (nextWeekPlan.getDevelopmentalTasks() != null) {
            logger.info("🔧 Processing {} next week developmental tasks from update request", nextWeekPlan.getDevelopmentalTasks().size());
            for (WeeklyReportUpdateRequest.NextWeekDevelopmentalTaskDTO nextWeekDevTask : nextWeekPlan.getDevelopmentalTasks()) {
                processNextWeekDevelopmentalTaskFromUpdateRequest(weeklyReport, nextWeekDevTask);
            }
        } else {
            logger.warn("🔧 ⚠️ No next week developmental tasks found in update request");
        }
        
        logger.info("🔧 ✅ Completed processing next week plan from update request for weekly report ID: {}", weeklyReport.getId());
    }

    /**
     * 处理来自更新请求的日常性任务
     */
    private void processRoutineTaskFromUpdateRequest(WeeklyReport weeklyReport, WeeklyReportUpdateRequest.RoutineTaskDTO routineTaskDto) {
        logger.info("🔧 Processing routine task from update request with ID: {}", routineTaskDto.getTask_id());
        
        try {
            Long taskId = Long.parseLong(routineTaskDto.getTask_id());
            
            // 1. 验证任务存在
            Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
            logger.info("🔧 ✅ Found task: {} (ID: {})", task.getTaskName(), taskId);

            // 2. 创建TaskReport关联记录，存储执行结果（本周汇报：isWeek=true）
            TaskReport taskReport = new TaskReport(
                weeklyReport, 
                task, 
                routineTaskDto.getActual_result(),
                routineTaskDto.getAnalysisofResultDifferences(),
                true  // 本周汇报
            );
            
            logger.info("🔧 💾 Saving TaskReport from update request: weeklyReportId={}, taskId={}, actualResult={}", 
                       weeklyReport.getId(), taskId, routineTaskDto.getActual_result());
            
            TaskReport savedTaskReport = taskReportRepository.save(taskReport);
            logger.info("🔧 ✅ TaskReport saved successfully from update request with composite ID: {}", savedTaskReport.getId());
            
        } catch (Exception e) {
            logger.error("🔧 ❌ 处理更新请求的日常性任务失败: taskId={}, error={}", routineTaskDto.getTask_id(), e.getMessage(), e);
            throw new RuntimeException("处理更新请求的日常性任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理来自更新请求的发展性任务
     */
    private void processDevelopmentalTaskFromUpdateRequest(WeeklyReport weeklyReport, WeeklyReportUpdateRequest.DevelopmentalTaskDTO devTaskDto) {
        logger.info("🔧 Processing developmental task from update request: projectId={}, phaseId={}", 
                   devTaskDto.getProject_id(), devTaskDto.getPhase_id());
        
        try {
            Long projectId = Long.parseLong(devTaskDto.getProject_id());
            Long phaseId = Long.parseLong(devTaskDto.getPhase_id());

            // 1. 验证项目和阶段存在
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在: " + projectId));
            logger.info("🔧 ✅ Found project: {} (ID: {})", project.getName(), projectId);
            
            ProjectPhase projectPhase = projectPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new RuntimeException("项目阶段不存在: " + phaseId));
            logger.info("🔧 ✅ Found project phase: {} (ID: {})", projectPhase.getPhaseName(), phaseId);

            // 2. 创建DevTaskReport关联记录，存储执行结果（本周汇报：isWeek=true）
            DevTaskReport devTaskReport = new DevTaskReport(
                weeklyReport.getId(),
                projectId,
                phaseId,
                devTaskDto.getActual_result(),
                devTaskDto.getAnalysisofResultDifferences(),
                true  // 本周汇报
            );
            
            logger.info("🔧 💾 Saving DevTaskReport from update request: weeklyReportId={}, projectId={}, phaseId={}, actualResult={}", 
                       weeklyReport.getId(), projectId, phaseId, devTaskDto.getActual_result());
            
            DevTaskReport savedDevTaskReport = devTaskReportRepository.save(devTaskReport);
            logger.info("🔧 ✅ DevTaskReport saved successfully from update request with ID: {}", savedDevTaskReport.getId());
            
        } catch (Exception e) {
            logger.error("🔧 ❌ 处理更新请求的发展性任务失败: projectId={}, phaseId={}, error={}", 
                        devTaskDto.getProject_id(), devTaskDto.getPhase_id(), e.getMessage(), e);
            throw new RuntimeException("处理更新请求的发展性任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理来自更新请求的下周日常性任务
     */
    private void processNextWeekRoutineTaskFromUpdateRequest(WeeklyReport weeklyReport, WeeklyReportUpdateRequest.NextWeekRoutineTaskDTO nextWeekTaskDto) {
        logger.info("🔧 Processing next week routine task from update request with ID: {}", nextWeekTaskDto.getTask_id());
        Long taskId = Long.parseLong(nextWeekTaskDto.getTask_id());
        
        // 1. 验证任务存在
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        logger.info("🔧 ✅ Found task: {} (ID: {})", task.getTaskName(), taskId);

        // 2. 创建TaskReport关联记录，标记为下周规划（isWeek=false）
        TaskReport taskReport = new TaskReport(
            weeklyReport, 
            task, 
            null, // 下周规划没有实际结果
            null, // 下周规划没有差异分析
            false // 下周规划
        );
        
        logger.info("🔧 💾 Saving Next Week TaskReport from update request: weeklyReportId={}, taskId={}", 
                   weeklyReport.getId(), taskId);
        TaskReport savedTaskReport = taskReportRepository.save(taskReport);
        logger.info("🔧 ✅ Next Week TaskReport saved successfully from update request with composite ID: {}", savedTaskReport.getId());
    }

    /**
     * 处理来自更新请求的下周发展性任务
     */
    private void processNextWeekDevelopmentalTaskFromUpdateRequest(WeeklyReport weeklyReport, WeeklyReportUpdateRequest.NextWeekDevelopmentalTaskDTO nextWeekDevTaskDto) {
        Long projectId = Long.parseLong(nextWeekDevTaskDto.getProject_id());
        Long phaseId = Long.parseLong(nextWeekDevTaskDto.getPhase_id());

        // 1. 验证项目和阶段存在
        projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("项目不存在: " + projectId));
        
        projectPhaseRepository.findById(phaseId)
            .orElseThrow(() -> new RuntimeException("项目阶段不存在: " + phaseId));

        // 2. 创建DevTaskReport关联记录，标记为下周规划（isWeek=false）
        DevTaskReport devTaskReport = new DevTaskReport(
            weeklyReport.getId(),
            projectId,
            phaseId,
            null, // 下周规划没有实际结果
            null, // 下周规划没有差异分析
            false // 下周规划
        );
        
        logger.info("🔧 💾 Saving Next Week DevTaskReport from update request: weeklyReportId={}, projectId={}, phaseId={}", 
                   weeklyReport.getId(), projectId, phaseId);
        devTaskReportRepository.save(devTaskReport);
        logger.info("🔧 ✅ Next Week DevTaskReport saved successfully from update request");
    }
}
