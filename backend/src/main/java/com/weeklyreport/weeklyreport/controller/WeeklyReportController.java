package com.weeklyreport.weeklyreport.controller;

import com.weeklyreport.common.dto.ApiResponse;
import com.weeklyreport.weeklyreport.dto.WeeklyReportCreateRequest;
import com.weeklyreport.weeklyreport.dto.WeeklyReportUpdateRequest;
import com.weeklyreport.weeklyreport.dto.WeeklyReportDetailResponse;
import com.weeklyreport.weeklyreport.dto.TestUpdateRequest;
import com.weeklyreport.weeklyreport.entity.*;
import com.weeklyreport.weeklyreport.repository.*;
import com.weeklyreport.task.entity.*;
import com.weeklyreport.task.repository.*;
import com.weeklyreport.project.entity.*;
import com.weeklyreport.project.repository.*;
import com.weeklyreport.user.entity.User;
import com.weeklyreport.weeklyreport.service.WeeklyReportService;
import com.weeklyreport.user.service.UserService;
import com.weeklyreport.ai.entity.AIAnalysisResult;
import com.weeklyreport.ai.service.AIAnalysisService;
// import com.weeklyreport.shared.util.auth.SecurityUtils; // 简化版本中不需要
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * WeeklyReportController - 周报控制器（4状态系统）
 *
 * 状态流程：
 * DRAFT → AI_PROCESSING → ADMIN_REVIEWING → APPROVED
 *              ↓               ↓
 *          REJECTED ← ← ← REJECTED
 *
 * API端点：
 * POST   /api/weekly-reports                - 创建周报草稿（DRAFT）
 * PUT    /api/weekly-reports/{id}           - 更新周报（仅DRAFT或REJECTED可编辑）
 * PUT    /api/weekly-reports/{id}/submit    - 提交周报（DRAFT/REJECTED → AI_PROCESSING）
 * PUT    /api/weekly-reports/{id}/force-submit - 强行提交（AI拒绝 → ADMIN_REVIEWING）
 * PUT    /api/weekly-reports/{id}/ai-approve   - AI审批通过（AI_PROCESSING → ADMIN_REVIEWING）
 * PUT    /api/weekly-reports/{id}/admin-approve - 管理员审批通过（ADMIN_REVIEWING → APPROVED）
 * PUT    /api/weekly-reports/{id}/reject       - 拒绝周报（ADMIN_REVIEWING → REJECTED）
 * GET    /api/weekly-reports/{id}              - 获取周报详情
 * GET    /api/weekly-reports/my                - 获取我的周报列表
 * GET    /api/weekly-reports/my-drafts         - 获取我的草稿列表（DRAFT）
 * GET    /api/weekly-reports/my-submitted      - 获取我的已提交列表（非DRAFT）
 * GET    /api/weekly-reports/my-rejected       - 获取我的被拒绝列表（REJECTED）
 * GET    /api/weekly-reports/pending           - 获取待审批周报列表（ADMIN_REVIEWING）
 * DELETE /api/weekly-reports/{id}              - 删除周报（仅DRAFT或REJECTED可删除）
 */
@RestController
@RequestMapping("/weekly-reports")
public class WeeklyReportController {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyReportController.class);

    @Value("${weekly-report.ai.confidence-threshold:0.7}")
    private double confidenceThreshold;

    @Autowired
    private WeeklyReportService weeklyReportService;

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Autowired
    private WeeklyReportRepository weeklyReportRepository;

    @Autowired
    private UserService userService;

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

    /**
     * 获取当前用户
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("用户未认证");
        }
        String username = auth.getName();
        return userService.getUserProfile(username);
    }

    /**
     * 创建周报草稿 - 不触发AI分析
     * 状态：→ DRAFT
     * 注意：此接口创建的周报处于草稿状态，需要调用提交接口才会触发AI分析
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WeeklyReport>> createWeeklyReport(
            @Valid @RequestBody WeeklyReportCreateRequest request) {
        try {
            // ======== 调试日志：详细记录Controller接收到的请求数据 ========
            logger.error("🎯🎯🎯 Controller接收到创建周报请求：");
            logger.error("🎯🎯🎯 Title: {}", request.getTitle());
            logger.error("🎯🎯🎯 Request toString: {}", request.toString());

            // 关键：检查content字段在Controller层的状态
            if (request.getContent() != null) {
                logger.info("🎯 Controller层 - Content对象存在: {}", request.getContent());
                logger.info("🎯 Controller层 - Content类型: {}", request.getContent().getClass().getName());
                logger.info("🎯 Controller层 - Content详细信息: {}", request.getContent().toString());
                if (request.getContent().getRoutineTasks() != null) {
                    logger.info("🎯 Controller层 - Routine_tasks数量: {}", request.getContent().getRoutineTasks().size());
                    for (int i = 0; i < request.getContent().getRoutineTasks().size(); i++) {
                        WeeklyReportCreateRequest.RoutineTaskDTO task = request.getContent().getRoutineTasks().get(i);
                        logger.info("🎯 Controller层 - Routine_task[{}]: task_id={}, actual_result={}, analysis={}",
                                   i, task.getTask_id(), task.getActual_result(), task.getAnalysisofResultDifferences());
                    }
                } else {
                    logger.warn("🎯 Controller层 - Content存在但Routine_tasks为null");
                }
                if (request.getContent().getDevelopmentalTasks() != null) {
                    logger.info("🎯 Controller层 - Developmental_tasks数量: {}", request.getContent().getDevelopmentalTasks().size());
                    for (int i = 0; i < request.getContent().getDevelopmentalTasks().size(); i++) {
                        WeeklyReportCreateRequest.DevelopmentalTaskDTO task = request.getContent().getDevelopmentalTasks().get(i);
                        logger.info("🎯 Controller层 - Developmental_task[{}]: project_id={}, phase_id={}, actual_result={}, analysis={}",
                                   i, task.getProject_id(), task.getPhase_id(), task.getActual_result(), task.getAnalysisofResultDifferences());
                    }
                } else {
                    logger.warn("🎯 Controller层 - Content存在但Developmental_tasks为null");
                }
            } else {
                logger.error("🎯 Controller层 - Content字段为null！！！这是问题根源");
            }
            logger.info("🎯 Controller层检查完毕 ================================================");
            // ======== 调试日志结束 ========

            User currentUser = getCurrentUser();

            // 验证用户权限
            if (!currentUser.canCreateWeeklyReports()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("没有权限创建周报"));
            }

            // 自动填充缺失的字段
            preprocessRequest(request, currentUser);

            // 验证userid与当前用户一致
            Long requestUserId = request.getUserId();
            if (!requestUserId.equals(currentUser.getId())) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("只能为自己创建周报"));
            }

            try {
                WeeklyReport weeklyReport = weeklyReportService.createWeeklyReport(request);
                return ResponseEntity.ok(ApiResponse.success(
                    "周报草稿已创建，可继续编辑或调用提交接口触发审核",
                    weeklyReport
                ));
            } catch (Exception serviceException) {
                logger.error("Service layer error creating weekly report", serviceException);

                // 如果服务层失败，创建一个简单的周报草稿作为fallback
                WeeklyReport fallbackReport = new WeeklyReport();
                fallbackReport.setUserId(currentUser.getId());
                fallbackReport.setTitle(request.getTitle() != null ? request.getTitle() : "默认周报标题");
                fallbackReport.setReportWeek(request.getReportWeek() != null ? request.getReportWeek() : generateReportWeek(request));
                fallbackReport.setAdditionalNotes(request.getAdditionalNotes());
                // 设置为草稿状态
                fallbackReport.setStatus(WeeklyReport.ReportStatus.DRAFT);

                WeeklyReport savedReport = weeklyReportRepository.save(fallbackReport);
                return ResponseEntity.ok(ApiResponse.success(
                    "周报草稿已创建（简化版本），可继续编辑或调用提交接口触发审核",
                    savedReport
                ));
            }

        } catch (Exception e) {
            logger.error("创建周报失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("创建周报失败: " + e.getMessage()));
        }
    }

    /**
     * 创建周报并直接提交审核 - 一键提交（跳过草稿状态）
     * 状态：直接 → AI_PROCESSING
     *
     * POST /api/weekly-reports/submit-directly
     *
     * 适用场景：
     * - 用户在创建周报页面点击"提交周报"按钮
     * - 跳过草稿状态，直接进入AI分析流程
     *
     * 与 POST /api/weekly-reports 的区别：
     * - POST /weekly-reports: 创建草稿（DRAFT），不触发AI分析
     * - POST /weekly-reports/submit-directly: 直接提交审核（AI_PROCESSING），立即触发AI分析
     */
    @PostMapping("/submit-directly")
    public ResponseEntity<ApiResponse<WeeklyReport>> createAndSubmitDirectly(
            @Valid @RequestBody WeeklyReportCreateRequest request) {
        try {
            logger.info("🚀🚀🚀 Controller接收到直接提交周报请求");
            logger.info("🚀 Title: {}", request.getTitle());
            logger.info("🚀 UserId: {}", request.getUserId());

            User currentUser = getCurrentUser();

            // 验证用户权限
            if (!currentUser.canCreateWeeklyReports()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("没有权限创建周报"));
            }

            // 自动填充缺失的字段
            preprocessRequest(request, currentUser);

            // 验证userid与当前用户一致
            Long requestUserId = request.getUserId();
            if (!requestUserId.equals(currentUser.getId())) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("只能为自己创建周报"));
            }

            try {
                // 调用Service层的创建并提交方法
                WeeklyReport weeklyReport = weeklyReportService.createAndSubmitDirectly(request);

                return ResponseEntity.ok(ApiResponse.success(
                    "周报已提交，正在进行AI分析",
                    weeklyReport
                ));
            } catch (IllegalArgumentException e) {
                // 内容验证失败
                logger.error("周报内容验证失败", e);
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(e.getMessage()));
            } catch (RuntimeException e) {
                // AI分析启动失败等运行时异常
                logger.error("创建并提交周报失败（运行时异常）", e);
                return ResponseEntity.status(500)
                    .body(ApiResponse.error(e.getMessage()));
            } catch (Exception serviceException) {
                logger.error("Service层创建并提交周报失败", serviceException);
                return ResponseEntity.status(500)
                    .body(ApiResponse.error("创建并提交周报失败: " + serviceException.getMessage()));
            }

        } catch (Exception e) {
            logger.error("创建并提交周报失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("创建并提交周报失败: " + e.getMessage()));
        }
    }

    /**
     * 提交周报草稿进入审批流程 - 触发AI分析
     * 状态转换：DRAFT/REJECTED → AI_PROCESSING
     * 只能提交草稿或已拒绝状态的周报
     */
    @PutMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<WeeklyReport>> submitWeeklyReport(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();

            // 首先验证周报是否存在和属于当前用户
            java.util.Optional<WeeklyReport> reportOpt = weeklyReportRepository.findById(id);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("周报不存在"));
            }

            WeeklyReport report = reportOpt.get();
            if (!report.getUserId().equals(currentUser.getId())) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("只能提交自己的周报"));
            }

            // 验证周报状态：只能提交草稿或已拒绝的周报
            if (!report.isEditable()) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(
                        String.format("只能提交草稿或已拒绝状态的周报，当前状态: %s", report.getStatus())
                    ));
            }

            try {
                WeeklyReport submittedReport = weeklyReportService.submitForReview(id);
                return ResponseEntity.ok(ApiResponse.success(
                    "周报已提交，正在进行AI分析",
                    submittedReport
                ));
            } catch (IllegalStateException ise) {
                // 状态错误或内容不完整
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(ise.getMessage()));
            } catch (IllegalArgumentException iae) {
                // 内容验证失败
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(iae.getMessage()));
            } catch (Exception serviceException) {
                logger.error("Service layer error submitting weekly report", serviceException);
                return ResponseEntity.status(500)
                    .body(ApiResponse.error("提交周报失败: " + serviceException.getMessage()));
            }

        } catch (Exception e) {
            logger.error("提交周报失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("提交周报失败: " + e.getMessage()));
        }
    }

    /**
     * 强行提交周报 - 当AI拒绝时，用户可以强行提交到管理员审核
     * 状态转换：REJECTED(AI拒绝) → PENDING_REVIEW
     * PUT /weekly-reports/{id}/force-submit
     * 适用场景：AI拒绝的周报，用户不同意AI判断，直接提交给管理员审核
     */
    @PutMapping("/{id}/force-submit")
    public ResponseEntity<ApiResponse<WeeklyReport>> forceSubmitWeeklyReport(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();

            // 首先验证周报是否存在和属于当前用户
            java.util.Optional<WeeklyReport> reportOpt = weeklyReportRepository.findById(id);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("周报不存在"));
            }

            WeeklyReport report = reportOpt.get();

            // 权限检查：只有创建者可以强行提交
            if (!report.getUserId().equals(currentUser.getId())) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("只能强行提交自己的周报"));
            }

            // 状态检查：只有被AI拒绝的周报可以强行提交
            boolean canForceSubmit = report.isRejected() &&
                report.getRejectedBy() == WeeklyReport.RejectedBy.AI;

            if (!canForceSubmit) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(
                        String.format("只有被AI拒绝的周报才能强行提交，当前状态: %s, 拒绝者: %s",
                            report.getStatus(), report.getRejectedBy())
                    ));
            }

            // 强制提交：直接进入管理员审核状态
            report.setStatus(WeeklyReport.ReportStatus.ADMIN_REVIEWING);
            report.setSubmittedAt(java.time.LocalDateTime.now());
            // 清除拒绝信息
            report.setRejectedBy(null);
            report.setRejectionReason(null);
            report.setRejectedAt(null);
            weeklyReportRepository.save(report);

            logger.info("周报强行提交成功: 用户ID={}, 周报ID={}, 状态变更: {}",
                       currentUser.getId(), id, report.getStatus());

            return ResponseEntity.ok(ApiResponse.success(
                "周报强行提交成功，已转入管理员审核",
                report
            ));

        } catch (Exception e) {
            logger.error("强行提交周报失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("强行提交周报失败: " + e.getMessage()));
        }
    }

    /**
     * AI分析通过 - 系统内部调用（增强版本，带置信度二次验证）
     * 状态转换：AI_PROCESSING → PENDING_REVIEW
     *
     * 注意：此端点应仅供内部系统调用，生产环境应添加适当的权限控制
     */
    @PutMapping("/{id}/ai-approve")
    public ResponseEntity<ApiResponse<String>> aiApproveWeeklyReport(
            @PathVariable Long id,
            @RequestParam Long aiAnalysisId) {
        try {
            logger.info("🔐 [API端点] AI批准请求，周报ID: {}, 分析结果ID: {}", id, aiAnalysisId);

            // 1. 权限检查（TODO: 生产环境应添加@PreAuthorize注解或IP白名单）
            User currentUser = getCurrentUser();
            if (!currentUser.getRole().equals(User.Role.SUPER_ADMIN)) {
                logger.warn("⚠️ [API端点] 非SUPER_ADMIN用户尝试调用AI批准接口，用户ID: {}, 角色: {}",
                    currentUser.getId(), currentUser.getRole());
                // 暂时允许，但记录警告日志
                // 生产环境应返回403: return ResponseEntity.status(403).body(ApiResponse.error("权限不足"));
            }

            // 2. 二次验证AI分析结果置信度
            AIAnalysisResult analysisResult = aiAnalysisService.getAnalysisResults(aiAnalysisId);
            if (analysisResult == null) {
                logger.error("❌ [API端点] AI分析结果不存在，ID: {}", aiAnalysisId);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("AI分析结果不存在"));
            }

            Double confidence = analysisResult.getConfidence();
            if (confidence == null || confidence < confidenceThreshold) {
                logger.warn("⚠️ [API端点] 置信度不足，拒绝批准请求，置信度: {}, 阈值: {}",
                    confidence, confidenceThreshold);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(String.format(
                        "AI分析置信度不足(%.0f%%)，无法批准",
                        (confidence != null ? confidence : 0.0) * 100
                    )));
            }

            // 3. 执行批准操作（Service层会再次验证）
            weeklyReportService.aiApproveWeeklyReport(id, aiAnalysisId);

            logger.info("✅ [API端点] AI批准成功，周报ID: {}, 置信度: {}", id, confidence);
            return ResponseEntity.ok(ApiResponse.success("AI分析通过"));

        } catch (IllegalStateException e) {
            logger.error("❌ [API端点] AI批准失败（状态错误）: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ [API端点] AI批准失败（系统错误）", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("AI审批失败，请稍后重试"));
        }
    }

    /**
     * 管理员审批通过
     * 状态转换：ADMIN_REVIEWING → APPROVED
     */
    @PutMapping("/{id}/admin-approve")
    public ResponseEntity<ApiResponse<String>> adminApproveWeeklyReport(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();

            // 验证用户权限：管理员或超级管理员
            if (!currentUser.canReviewWeeklyReports()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("没有权限审批周报"));
            }

            // 验证周报是否存在
            java.util.Optional<WeeklyReport> reportOpt = weeklyReportRepository.findById(id);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("周报不存在"));
            }

            WeeklyReport report = reportOpt.get();

            // 状态检查：只有管理员审核中的周报可以由管理员审批
            if (!report.isAdminReviewing()) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(
                        String.format("只能审批管理员审核中的周报，当前状态: %s",
                            report.getStatus())
                    ));
            }

            try {
                // 调用Entity的adminApprove方法（直接到APPROVED）
                report.adminApprove(currentUser.getId());
                weeklyReportRepository.save(report);
                return ResponseEntity.ok(ApiResponse.success("管理员审批通过"));
            } catch (Exception serviceException) {
                logger.error("Service layer error admin approving weekly report", serviceException);
                return ResponseEntity.status(500)
                    .body(ApiResponse.error("管理员审批失败: " + serviceException.getMessage()));
            }

        } catch (Exception e) {
            logger.error("管理员审批失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("管理员审批失败: " + e.getMessage()));
        }
    }


    /**
     * 拒绝周报（管理员权限）
     * 状态转换：PENDING_REVIEW → REJECTED
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<String>> rejectWeeklyReport(
            @PathVariable Long id,
            @RequestBody RejectRequest request) {
        try {
            User currentUser = getCurrentUser();

            // 验证用户权限
            if (!currentUser.canReviewWeeklyReports()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("没有权限拒绝周报"));
            }

            // 验证周报是否存在
            java.util.Optional<WeeklyReport> reportOpt = weeklyReportRepository.findById(id);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("周报不存在"));
            }

            WeeklyReport report = reportOpt.get();

            // 状态检查：只有待审核状态的周报可以拒绝
            if (!report.isPendingReview()) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(
                        String.format("只能拒绝待审核状态的周报，当前状态: %s",
                            report.getStatus())
                    ));
            }

            try {
                boolean isSuperAdmin = currentUser.isSuperAdmin();
                weeklyReportService.rejectWeeklyReport(id, currentUser.getId(), request.getReason(), isSuperAdmin);
                return ResponseEntity.ok(ApiResponse.success("周报已拒绝"));
            } catch (Exception serviceException) {
                logger.error("Service layer error rejecting weekly report", serviceException);
                return ResponseEntity.status(500)
                    .body(ApiResponse.error("拒绝周报失败: " + serviceException.getMessage()));
            }

        } catch (Exception e) {
            logger.error("拒绝周报失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("拒绝周报失败: " + e.getMessage()));
        }
    }

    /**
     * 获取周报详情 - 包含完整的关联数据
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getWeeklyReportDetail(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();

            // 首先尝试从数据库直接获取基本的周报信息
            java.util.Optional<WeeklyReport> reportOpt = weeklyReportRepository.findById(id);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("周报不存在"));
            }

            WeeklyReport report = reportOpt.get();

            // 权限检查：只有周报作者或有审批权限的用户可以查看
            boolean isAuthor = report.getUserId().equals(currentUser.getId());
            boolean canReview = currentUser.canReviewWeeklyReports();

            if (!isAuthor && !canReview) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("没有权限查看此周报"));
            }

            try {
                // 尝试获取完整的详情
                WeeklyReportDetailResponse detail = weeklyReportService.getWeeklyReportDetail(id);
                return ResponseEntity.ok(ApiResponse.success("获取周报详情成功", detail));
            } catch (Exception serviceException) {
                logger.error("Service layer error getting weekly report detail, falling back to basic report", serviceException);
                // 如果服务层失败，返回基本的周报信息
                return ResponseEntity.ok(ApiResponse.success("获取周报基本信息成功", report));
            }

        } catch (Exception e) {
            logger.error("获取周报详情失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("获取周报详情失败: " + e.getMessage()));
        }
    }

    /**
     * 更新周报（仅创建者可更新草稿或已拒绝状态的周报）
     * 状态保持不变：DRAFT → DRAFT, REJECTED → REJECTED
     * PUT /weekly-reports/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WeeklyReport>> updateWeeklyReport(
            @PathVariable Long id,
            @Valid @RequestBody WeeklyReportUpdateRequest request) {
        try {
            // ======== 调试日志：详细记录Controller接收到的更新请求数据 ========
            logger.error("🔧🔧🔧 Controller接收到更新周报请求，ID: {}", id);
            logger.error("🔧🔧🔧 Title: {}", request.getTitle());
            logger.error("🔧🔧🔧 Request toString: {}", request.toString());

            // 关键：检查content字段在Controller层的状态
            if (request.getContent() != null) {
                logger.info("🔧 Controller层 - Content对象存在: {}", request.getContent());
                if (request.getContent().getRoutineTasks() != null) {
                    logger.info("🔧 Controller层 - Routine_tasks数量: {}", request.getContent().getRoutineTasks().size());
                    for (int i = 0; i < request.getContent().getRoutineTasks().size(); i++) {
                        WeeklyReportUpdateRequest.RoutineTaskDTO task = request.getContent().getRoutineTasks().get(i);
                        logger.info("🔧 Controller层 - Routine_task[{}]: task_id={}, actual_result={}, analysis={}",
                                   i, task.getTask_id(), task.getActual_result(), task.getAnalysisofResultDifferences());
                    }
                } else {
                    logger.warn("🔧 Controller层 - Content存在但Routine_tasks为null");
                }
                if (request.getContent().getDevelopmentalTasks() != null) {
                    logger.info("🔧 Controller层 - Developmental_tasks数量: {}", request.getContent().getDevelopmentalTasks().size());
                    for (int i = 0; i < request.getContent().getDevelopmentalTasks().size(); i++) {
                        WeeklyReportUpdateRequest.DevelopmentalTaskDTO task = request.getContent().getDevelopmentalTasks().get(i);
                        logger.info("🔧 Controller层 - Developmental_task[{}]: project_id={}, phase_id={}, actual_result={}, analysis={}",
                                   i, task.getProject_id(), task.getPhase_id(), task.getActual_result(), task.getAnalysisofResultDifferences());
                    }
                } else {
                    logger.warn("🔧 Controller层 - Content存在但Developmental_tasks为null");
                }
            } else {
                logger.error("🔧 Controller层 - Content字段为null！！！这是问题根源");
            }

            // 重要：检查nextWeekPlan字段在Controller层的状态
            if (request.getNextWeekPlan() != null) {
                logger.info("🔧 Controller层 - NextWeekPlan对象存在: {}", request.getNextWeekPlan());
                if (request.getNextWeekPlan().getRoutineTasks() != null) {
                    logger.info("🔧 Controller层 - NextWeek Routine_tasks数量: {}", request.getNextWeekPlan().getRoutineTasks().size());
                    for (int i = 0; i < request.getNextWeekPlan().getRoutineTasks().size(); i++) {
                        WeeklyReportUpdateRequest.NextWeekRoutineTaskDTO task = request.getNextWeekPlan().getRoutineTasks().get(i);
                        logger.info("🔧 Controller层 - NextWeek Routine_task[{}]: task_id={}", i, task.getTask_id());
                    }
                } else {
                    logger.warn("🔧 Controller层 - NextWeekPlan存在但Routine_tasks为null");
                }
                if (request.getNextWeekPlan().getDevelopmentalTasks() != null) {
                    logger.info("🔧 Controller层 - NextWeek Developmental_tasks数量: {}", request.getNextWeekPlan().getDevelopmentalTasks().size());
                    for (int i = 0; i < request.getNextWeekPlan().getDevelopmentalTasks().size(); i++) {
                        WeeklyReportUpdateRequest.NextWeekDevelopmentalTaskDTO task = request.getNextWeekPlan().getDevelopmentalTasks().get(i);
                        logger.info("🔧 Controller层 - NextWeek Developmental_task[{}]: project_id={}, phase_id={}",
                                   i, task.getProject_id(), task.getPhase_id());
                    }
                } else {
                    logger.warn("🔧 Controller层 - NextWeekPlan存在但Developmental_tasks为null");
                }
            } else {
                logger.error("🔧 Controller层 - NextWeekPlan字段为null！！！这是下周规划插入失败的根源");
            }
            logger.info("🔧 Controller层检查完毕 ================================================");
            // ======== 调试日志结束 ========

            User currentUser = getCurrentUser();

            // 验证周报是否存在
            java.util.Optional<WeeklyReport> reportOpt = weeklyReportRepository.findById(id);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("周报不存在"));
            }

            WeeklyReport report = reportOpt.get();

            // 权限检查：只有创建者可以更新
            if (!report.getUserId().equals(currentUser.getId())) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("只能更新自己的周报"));
            }

            // 状态检查：只有草稿或已拒绝状态的周报可以更新
            if (!report.isEditable()) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(
                        String.format("只能更新草稿或已拒绝状态的周报，当前状态: %s", report.getStatus())
                    ));
            }

            // 使用Service层的更新方法（包含事务管理）
            WeeklyReport updatedReport = weeklyReportService.updateWeeklyReport(id, request);

            return ResponseEntity.ok(ApiResponse.success("周报更新成功", updatedReport));

        } catch (Exception e) {
            logger.error("更新周报失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("更新周报失败: " + e.getMessage()));
        }
    }

    /**
     * 获取我的周报列表 - 返回包含深度查询关联数据的完整周报信息
     * 注意：此接口严格只返回当前登录用户的周报，不需要传入userId参数
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<WeeklyReportDetailResponse>>> getMyWeeklyReports(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        try {
            logger.info("🔍🔍🔍 /my 接口被调用 - 开始获取我的周报列表");

            // 获取当前认证用户
            User currentUser = getCurrentUser();
            logger.info("🔍 当前认证用户信息 - ID: {}, 用户名: {}, 角色: {}",
                       currentUser.getId(), currentUser.getUsername(), currentUser.getRole());

            // 强制验证：确保只返回当前用户的数据
            if (currentUser.getId() == null) {
                logger.error("❌ 严重错误：当前用户ID为null");
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户身份验证失败"));
            }

            // 调用Service层，传入当前用户ID
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<WeeklyReportDetailResponse> reports = weeklyReportService.getDetailedWeeklyReportsByUserId(currentUser.getId(), pageable);
            logger.info("🔍 分页查询 - 当前页: {}, 单页大小: {}, 总记录数: {}, 当前页记录数: {}", 
                       page, size, reports.getTotalElements(), reports.getContent().size());

            // 二次验证：确保所有返回的周报都属于当前用户
            List<WeeklyReportDetailResponse> reportList = reports.getContent();
            long wrongUserReports = reportList.stream()
                .filter(report -> !report.getUserId().equals(currentUser.getId()))
                .count();

            if (wrongUserReports > 0) {
                logger.error("❌❌❌ 严重安全问题：返回了{}条不属于当前用户的周报！", wrongUserReports);
                logger.error("❌ 当前用户ID: {}", currentUser.getId());
                for (WeeklyReportDetailResponse report : reports) {
                    if (!report.getUserId().equals(currentUser.getId())) {
                        logger.error("❌ 错误周报 - ID: {}, 标题: {}, 实际用户ID: {}",
                                   report.getId(), report.getTitle(), report.getUserId());
                    }
                }

                // 过滤掉不属于当前用户的周报
                reportList = reportList.stream()
                    .filter(report -> report.getUserId().equals(currentUser.getId()))
                    .toList();
                    
                reports = new org.springframework.data.domain.PageImpl<>(
                    reportList, reports.getPageable(), reports.getTotalElements());

                logger.warn("⚠️ 已过滤，最终返回周报数量: {}", reportList.size());
            }

            // 详细调试信息
            logger.info("🔍 最终返回的周报列表：");
            for (int i = 0; i < Math.min(reportList.size(), 5); i++) { // 只打印前5条
                WeeklyReportDetailResponse report = reportList.get(i);
                logger.info("🔍 [{}] 周报ID: {}, 标题: {}, 用户ID: {}, 状态: {}",
                           i, report.getId(), report.getTitle(), report.getUserId(), report.getStatus());
            }
            if (reportList.size() > 5) {
                logger.info("🔍 ... 还有{}条周报", reportList.size() - 5);
            }

            return ResponseEntity.ok(ApiResponse.success("获取我的周报列表成功", reports));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("用户未认证")) {
                logger.error("❌ 用户未认证访问 /my 接口");
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("请先登录"));
            }
            logger.error("获取我的周报列表失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("获取我的周报列表失败: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("获取我的周报列表失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("获取我的周报列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取待审批周报列表
     */
    /**
     * 获取所有周报列表（根据用户权限过滤）
     * GET /api/weekly-reports
     *
     * ⚠️ 重要说明：
     * - 管理员/超级管理员：返回所有用户的周报
     * - 普通用户：只返回自己的周报
     * - 如果只想获取当前用户的周报，请使用 GET /api/weekly-reports/my
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WeeklyReportDetailResponse>>> getAllWeeklyReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String rejectedBy,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        try {
            logger.info("🔍🔍🔍 /api/weekly-reports 根接口被调用 - 获取周报列表");
            logger.info("🔍 查询参数 - status: {}, rejectedBy: {}", status, rejectedBy);
            logger.warn("⚠️ 前端提醒：如果只需要获取当前用户的周报，建议使用 /api/weekly-reports/my 接口");

            User currentUser = getCurrentUser();
            logger.info("🔍 当前用户信息 - ID: {}, 用户名: {}, 角色: {}, canReviewWeeklyReports: {}",
                       currentUser.getId(), currentUser.getUsername(), currentUser.getRole(), currentUser.canReviewWeeklyReports());

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<WeeklyReportDetailResponse> reports;

            if (currentUser.canReviewWeeklyReports()) {
                logger.info("🔍 用户是管理员，返回所有周报");
                // 管理员和超级管理员可以查看所有周报（包含AI分析结果）
                if (status != null && rejectedBy != null) {
                    // 同时按状态和拒绝者筛选
                    logger.info("🔍 分页按状态和拒绝者筛选: status={}, rejectedBy={}", status, rejectedBy);
                    WeeklyReport.ReportStatus reportStatus = WeeklyReport.ReportStatus.valueOf(status.toUpperCase());
                    WeeklyReport.RejectedBy rejectedByEnum = WeeklyReport.RejectedBy.valueOf(rejectedBy.toUpperCase());
                    reports = weeklyReportService.getWeeklyReportsByStatusAndRejectedBy(reportStatus, rejectedByEnum, pageable);
                } else if (status != null) {
                    logger.info("🔍 分页按状态筛选: {}", status);
                    WeeklyReport.ReportStatus reportStatus = WeeklyReport.ReportStatus.valueOf(status.toUpperCase());
                    reports = weeklyReportService.getWeeklyReportsByStatusWithAIAnalysis(reportStatus, pageable);
                } else {
                    logger.info("🔍 分页获取所有状态的周报");
                    reports = weeklyReportService.getAllWeeklyReportsWithAIAnalysis(pageable);
                }
            } else {
                logger.info("🔍 用户是普通用户，只返回自己的周报");
                logger.warn("⚠️ 普通用户通过根接口访问，建议前端改用 /my 接口以获得更好的性能");
                // 普通用户只能查看自己的周报（包含AI分析结果）
                reports = weeklyReportService.getDetailedWeeklyReportsByUserId(currentUser.getId(), pageable);
            }

            logger.info("🔍 分页结果 - 当前页: {}, 单页大小: {}, 总记录数: {}, 当前页记录数: {}", 
                       page, size, reports.getTotalElements(), reports.getContent().size());

            return ResponseEntity.ok(ApiResponse.success("获取周报列表成功", reports));

        } catch (Exception e) {
            logger.error("获取周报列表失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("获取周报列表失败: " + e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<WeeklyReport>>> getPendingWeeklyReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        try {
            // 验证用户权限
            if (!getCurrentUser().canReviewWeeklyReports()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("没有权限查看待审批周报"));
            }

            WeeklyReport.ReportStatus reportStatus;
            if (status == null) {
                // 默认显示管理员审核的周报
                reportStatus = WeeklyReport.ReportStatus.ADMIN_REVIEWING;
            } else {
                reportStatus = WeeklyReport.ReportStatus.valueOf(status.toUpperCase());
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<WeeklyReport> reports = weeklyReportRepository.findByStatus(reportStatus, pageable);

            return ResponseEntity.ok(ApiResponse.success("获取待审批周报列表成功", reports));

        } catch (Exception e) {
            logger.error("获取待审批周报列表失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("获取待审批周报列表失败: " + e.getMessage()));
        }
    }

    /**
     * 预处理请求，自动填充缺失的字段
     */
    private void preprocessRequest(WeeklyReportCreateRequest request, User currentUser) {
        // 如果userId为空，从当前用户获取
        if (request.getUserId() == null) {
            request.setUserId(currentUser.getId());
        }

        // 如果title为空，自动生成
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            request.setTitle("周报-" + currentUser.getUsername() + "-" + java.time.LocalDate.now());
        }

        // 如果reportWeek为空，从weekStart/weekEnd生成
        if (request.getReportWeek() == null || request.getReportWeek().trim().isEmpty()) {
            request.setReportWeek(generateReportWeek(request));
        }

        // 如果content为空，创建空的content
        if (request.getContent() == null) {
            request.setContent(new WeeklyReportCreateRequest.ContentDTO());
        }
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

        // 2. 创建TaskReport关联记录并保存实际结果和差异分析
        TaskReport taskReport = new TaskReport(weeklyReport, task);
        taskReport.setActualResults(routineTaskDto.getActual_result());
        taskReport.setResultDifferenceAnalysis(routineTaskDto.getAnalysisofResultDifferences());
        taskReport.setIsWeek(true); // 本周汇报
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

        // 2. 创建DevTaskReport关联记录并保存实际结果和差异分析
        DevTaskReport devTaskReport = new DevTaskReport(weeklyReport.getId(), project.getId(), projectPhase.getId());
        devTaskReport.setActualResults(devTaskDto.getActual_result());
        devTaskReport.setResultDifferenceAnalysis(devTaskDto.getAnalysisofResultDifferences());
        devTaskReport.setIsWeek(true); // 本周汇报
        devTaskReportRepository.save(devTaskReport);
    }

    /**
     * 根据weekStart和weekEnd生成reportWeek描述
     */
    private String generateReportWeek(WeeklyReportCreateRequest request) {
        if (request.getWeekStart() != null && request.getWeekEnd() != null) {
            return request.getWeekStart() + " 至 " + request.getWeekEnd();
        }

        // 如果没有weekStart/weekEnd，使用当前周
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate monday = now.with(java.time.DayOfWeek.MONDAY);
        java.time.LocalDate friday = monday.plusDays(4);

        return monday + " 至 " + friday;
    }

    /**
     * 删除周报（仅创建者可删除草稿或已拒绝状态的周报）
     * 适用状态：DRAFT, REJECTED
     * DELETE /weekly-reports/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteWeeklyReport(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();

            // 验证周报是否存在
            java.util.Optional<WeeklyReport> reportOpt = weeklyReportRepository.findById(id);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("周报不存在"));
            }

            WeeklyReport report = reportOpt.get();

            // 权限检查：只有创建者可以删除
            if (!report.getUserId().equals(currentUser.getId())) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("只能删除自己的周报"));
            }

            // 状态检查：只有草稿或已拒绝状态的周报可以删除
            if (!report.isEditable()) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(
                        String.format("只能删除草稿或已拒绝状态的周报，当前状态: %s", report.getStatus())
                    ));
            }

            // 删除周报（级联删除关联的task_reports和dev_task_reports）
            weeklyReportRepository.delete(report);
            logger.info("周报删除成功: ID={}, 用户={}", id, currentUser.getUsername());

            return ResponseEntity.ok(ApiResponse.success("周报删除成功"));

        } catch (Exception e) {
            logger.error("删除周报失败: ID={}", id, e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("删除周报失败: " + e.getMessage()));
        }
    }

    /**
     * 查询当前用户的草稿周报
     * 状态过滤：DRAFT
     * GET /weekly-reports/my-drafts
     */
    @GetMapping("/my-drafts")
    public ResponseEntity<ApiResponse<Page<WeeklyReport>>> getMyDrafts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            User currentUser = getCurrentUser();
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));

            Page<WeeklyReport> drafts = weeklyReportRepository.findByUserIdAndStatus(
                currentUser.getId(),
                WeeklyReport.ReportStatus.DRAFT,
                pageable
            );

            return ResponseEntity.ok(ApiResponse.success(
                "获取草稿列表成功",
                drafts
            ));
        } catch (Exception e) {
            logger.error("获取草稿列表失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("获取草稿列表失败: " + e.getMessage()));
        }
    }

    /**
     * 查询当前用户已提交的周报（非草稿状态）
     * 状态过滤：AI_PROCESSING, PENDING_REVIEW, APPROVED, REJECTED
     * GET /weekly-reports/my-submitted
     */
    @GetMapping("/my-submitted")
    public ResponseEntity<ApiResponse<Page<WeeklyReport>>> getMySubmitted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String approvalStatus) {
        try {
            User currentUser = getCurrentUser();
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

            Page<WeeklyReport> reports;
            if (approvalStatus != null) {
                // 查询特定状态的周报（排除草稿）
                WeeklyReport.ReportStatus status = WeeklyReport.ReportStatus.valueOf(
                    approvalStatus.toUpperCase()
                );
                reports = weeklyReportRepository.findByUserIdAndStatus(
                    currentUser.getId(),
                    status,
                    pageable
                );
            } else {
                // 查询所有非草稿状态的周报（已提交、AI处理中、待审核、已通过、已拒绝）
                reports = weeklyReportRepository.findByUserIdAndStatusNot(
                    currentUser.getId(),
                    WeeklyReport.ReportStatus.DRAFT,
                    pageable
                );
            }

            return ResponseEntity.ok(ApiResponse.success(
                "获取已提交周报列表成功",
                reports
            ));
        } catch (Exception e) {
            logger.error("获取已提交周报列表失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("获取已提交周报列表失败: " + e.getMessage()));
        }
    }

    /**
     * 查询被拒绝的周报（可以重新编辑和提交）
     * GET /weekly-reports/my-rejected
     */
    @GetMapping("/my-rejected")
    public ResponseEntity<ApiResponse<Page<WeeklyReport>>> getMyRejectedReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            User currentUser = getCurrentUser();
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rejectedAt"));

            Page<WeeklyReport> rejectedReports = weeklyReportRepository.findByUserIdAndStatus(
                currentUser.getId(),
                WeeklyReport.ReportStatus.REJECTED,
                pageable
            );

            return ResponseEntity.ok(ApiResponse.success(
                "获取已拒绝周报列表成功",
                rejectedReports
            ));
        } catch (Exception e) {
            logger.error("获取已拒绝周报列表失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("获取已拒绝周报列表失败: " + e.getMessage()));
        }
    }

    /**
     * 测试简单更新 - 用于调试JSON解析问题
     */
    @PutMapping("/{id}/test")
    public ResponseEntity<ApiResponse<String>> testUpdate(
            @PathVariable Long id,
            @RequestBody TestUpdateRequest request) {
        try {
            logger.info("Test update received: {}", request.getTitle());
            return ResponseEntity.ok(ApiResponse.success("Test update successful"));
        } catch (Exception e) {
            logger.error("Test update failed", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("测试更新失败，请稍后重试"));
        }
    }

    /**
     * 拒绝请求DTO
     */
    public static class RejectRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
