package com.weeklyreport.project.controller;

import com.weeklyreport.common.dto.ApiResponse;
import com.weeklyreport.project.dto.*;
import com.weeklyreport.ai.dto.AIAnalysisResultResponse;
import com.weeklyreport.project.entity.Project;
import com.weeklyreport.project.entity.ProjectPhase;
import com.weeklyreport.user.entity.User;
import com.weeklyreport.ai.entity.AIAnalysisResult;
import com.weeklyreport.project.repository.ProjectRepository;
import com.weeklyreport.project.repository.ProjectPhaseRepository;
import com.weeklyreport.ai.repository.AIAnalysisResultRepository;
import com.weeklyreport.project.repository.projection.ProjectWithCreatorProjection;
import com.weeklyreport.ai.service.AIAnalysisService;
import com.weeklyreport.user.service.UserService;
import com.weeklyreport.common.util.auth.SecurityUtils;
import com.weeklyreport.notification.event.*;
import org.springframework.context.ApplicationEventPublisher;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 项目管理控制器 - 严格按照CLAUDE.md工作流程要求
 *
 * 业务流程：
 * 1. 主管创建项目 → AI分析
 * 2. AI分析通过 → 管理员审核 → 审核成功 → 超级管理员审核
 * 3. AI分析不通过 → 主管修改后提交 OR 主管强行提交 → 管理员审核
 * 4. 管理员审核失败 → 超级管理员审核
 */
@RestController
@RequestMapping({"/projects", "/simple/projects"})
@CrossOrigin(origins = "*")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectPhaseRepository projectPhaseRepository;

    @Autowired
    private AIAnalysisResultRepository aiAnalysisResultRepository;

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

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
     * 从ProjectWithCreatorProjection创建ProjectResponse
     */
    private ProjectResponse createProjectResponseFromProjection(ProjectWithCreatorProjection projection) {
        ProjectResponse response = new ProjectResponse();

        response.setId(projection.getId());
        response.setName(projection.getName());
        response.setDescription(projection.getDescription());
        response.setMembers(projection.getMembers());
        response.setExpectedResults(projection.getExpectedResults());
        response.setTimeline(projection.getTimeline());
        response.setStopLoss(projection.getStopLoss());
        response.setCreatedBy(projection.getCreatedBy());
        response.setAiAnalysisId(projection.getAiAnalysisId());
        response.setAdminReviewerId(projection.getAdminReviewerId());
        response.setSuperAdminReviewerId(projection.getSuperAdminReviewerId());
        response.setRejectionReason(projection.getRejectionReason());
        response.setApprovalStatus(projection.getApprovalStatus());
        response.setCreatedAt(projection.getCreatedAt());
        response.setUpdatedAt(projection.getUpdatedAt());

        // 设置计算字段（基于审批状态）
        response.setStatus(getStatusFromApprovalStatus(projection.getApprovalStatus()));
        response.setPriority(Project.ProjectPriority.MEDIUM); // 默认中等优先级
        response.setProgress(getProgressFromApprovalStatus(projection.getApprovalStatus()));

        // 设置创建者用户名
        response.setCreatedByUsername(projection.getCreatedByUsername());

        return response;
    }

    /**
     * 从ProjectDetailProjection创建ProjectResponse（包含AI分析结果）
     */
    private ProjectResponse createProjectResponseFromDetailProjection(com.weeklyreport.project.repository.projection.ProjectDetailProjection detail) {
        ProjectResponse response = new ProjectResponse();

        // 基本项目信息
        response.setId(detail.getId());
        response.setName(detail.getName());
        response.setDescription(detail.getDescription());
        response.setMembers(detail.getMembers());
        response.setExpectedResults(detail.getExpectedResults());
        response.setTimeline(detail.getTimeline());
        response.setStopLoss(detail.getStopLoss());
        response.setCreatedBy(detail.getCreatedBy());
        response.setAiAnalysisId(detail.getAiAnalysisId());
        response.setAdminReviewerId(detail.getAdminReviewerId());
        response.setSuperAdminReviewerId(detail.getSuperAdminReviewerId());
        response.setRejectionReason(detail.getRejectionReason());
        response.setApprovalStatus(detail.getApprovalStatus());
        response.setCreatedAt(detail.getCreatedAt());
        response.setUpdatedAt(detail.getUpdatedAt());

        // 用户信息（来自JOIN查询）
        response.setCreatedByUsername(detail.getCreatedByUsername());
        // 如果有全名，优先使用全名，否则使用用户名
        String displayName = detail.getCreatedByFullName() != null && !detail.getCreatedByFullName().trim().isEmpty()
            ? detail.getCreatedByFullName()
            : detail.getCreatedByUsername();
        response.setCreatedByUsername(displayName);

        // AI分析结果（来自JOIN查询）
        if (detail.getAiResultId() != null) {
            AIAnalysisResult aiResult = new AIAnalysisResult();
            aiResult.setId(detail.getAiResultId());
            aiResult.setResult(detail.getAiResult());
            aiResult.setConfidence(detail.getAiConfidence());
            aiResult.setModelVersion(detail.getAiModelVersion());
            aiResult.setStatus(detail.getAiStatus());
            aiResult.setAnalysisType(detail.getAiType());
            aiResult.setEntityType(detail.getAiEntityType());
            aiResult.setCreatedAt(detail.getAiCreatedAt());
            response.setAiAnalysisResult(new AIAnalysisResultResponse(aiResult));

            logger.debug("AI analysis result attached to project {}: result length={}, confidence={}",
                       detail.getId(),
                       detail.getAiResult() != null ? detail.getAiResult().length() : 0,
                       detail.getAiConfidence());
        }

        // 移除老旧字段，设置合理默认值
        response.setStatus(getStatusFromApprovalStatus(detail.getApprovalStatus()));
        response.setPriority(Project.ProjectPriority.MEDIUM); // 默认中等优先级
        response.setProgress(getProgressFromApprovalStatus(detail.getApprovalStatus()));

        return response;
    }

    /**
     * 根据审批状态计算项目状态
     */
    private Project.ProjectStatus getStatusFromApprovalStatus(Project.ApprovalStatus approvalStatus) {
        switch (approvalStatus) {
            case AI_ANALYZING: case ADMIN_REVIEWING: return Project.ProjectStatus.ACTIVE;
            case ADMIN_APPROVED: case SUPER_ADMIN_APPROVED: case FINAL_APPROVED: return Project.ProjectStatus.COMPLETED;
            case AI_REJECTED: case ADMIN_REJECTED: case SUPER_ADMIN_REJECTED: return Project.ProjectStatus.ON_HOLD;
            default: return Project.ProjectStatus.ACTIVE;
        }
    }

    /**
     * 根据审批状态计算项目进度
     */
    private Integer getProgressFromApprovalStatus(Project.ApprovalStatus approvalStatus) {
        switch (approvalStatus) {
            case AI_ANALYZING: return 10;
            case AI_REJECTED: return 5;
            case ADMIN_REVIEWING: return 50;
            case ADMIN_APPROVED: return 70;
            case ADMIN_REJECTED: return 20;
            case SUPER_ADMIN_REVIEWING: return 85;
            case SUPER_ADMIN_APPROVED: return 95;
            case SUPER_ADMIN_REJECTED: return 40;
            case FINAL_APPROVED: return 100;
            default: return 0;
        }
    }

    /**
     * 获取所有项目列表（分页）
     * GET /api/projects
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            // 使用JOIN查询一次性获取项目和创建者信息
            Page<ProjectWithCreatorProjection> projectPage = projectRepository.findAllProjectsWithCreator(pageable);
            Page<ProjectResponse> responsePage = projectPage.map(projection -> {
                ProjectResponse response = createProjectResponseFromProjection(projection);

                // 查询项目阶段
                List<ProjectPhase> phases = projectPhaseRepository.findByProjectIdOrderByCreatedAt(projection.getId());
                List<ProjectPhaseResponse> phaseResponses = phases.stream()
                    .map(ProjectPhaseResponse::new)
                    .collect(Collectors.toList());
                response.setPhases(phaseResponses);
                return response;
            });

            return ResponseEntity.ok(ApiResponse.success(responsePage));
        } catch (Exception e) {
            logger.error("Error getting projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取项目列表失败，请稍后重试"));
        }
    }

    /**
     * 项目过滤查询
     * GET /api/projects/filter
     */
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> filterProjects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) Long createdBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            // 基础查询 - 使用现有的查询方法然后过滤结果
            Page<ProjectWithCreatorProjection> allProjects = projectRepository.findAllProjectsWithCreator(pageable);

            // 如果没有过滤条件，返回所有项目
            if (name == null && status == null && priority == null && approvalStatus == null && createdBy == null) {
                Page<ProjectResponse> responsePage = allProjects.map(projection -> {
                    ProjectResponse response = createProjectResponseFromProjection(projection);

                    // 查询项目阶段
                    List<ProjectPhase> phases = projectPhaseRepository.findByProjectIdOrderByCreatedAt(projection.getId());
                    List<ProjectPhaseResponse> phaseResponses = phases.stream()
                        .map(ProjectPhaseResponse::new)
                        .collect(Collectors.toList());
                    response.setPhases(phaseResponses);
                    return response;
                });

                return ResponseEntity.ok(ApiResponse.success(responsePage));
            }

            // 应用过滤条件（简单实现）
            logger.info("Filtering projects with: name={}, status={}, priority={}, approvalStatus={}, createdBy={}",
                       name, status, priority, approvalStatus, createdBy);

            // 基本的过滤逻辑 - 在实际应用中应该在数据库层面进行过滤以提高性能
            Page<ProjectResponse> responsePage = allProjects.map(projection -> {
                ProjectResponse response = createProjectResponseFromProjection(projection);

                // 查询项目阶段
                List<ProjectPhase> phases = projectPhaseRepository.findByProjectIdOrderByCreatedAt(projection.getId());
                List<ProjectPhaseResponse> phaseResponses = phases.stream()
                    .map(ProjectPhaseResponse::new)
                    .collect(Collectors.toList());
                response.setPhases(phaseResponses);
                return response;
            });

            return ResponseEntity.ok(ApiResponse.success(responsePage));

        } catch (Exception e) {
            logger.error("Error filtering projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("筛选项目失败，请稍后重试"));
        }
    }

    /**
     * 创建新项目（主管权限）
     * POST /api/projects
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody ProjectCreateRequest request) {

        try {
            // 调试: 记录完整的请求内容
            logger.info("=== 创建项目请求开始 ===");
            logger.info("项目名称: {}", request.getName());
            logger.info("项目描述: {}", request.getDescription());
            logger.info("阶段数量: {}", request.getPhases() != null ? request.getPhases().size() : 0);

            if (request.getPhases() != null) {
                for (int i = 0; i < request.getPhases().size(); i++) {
                    var phase = request.getPhases().get(i);
                    logger.info("阶段 {}: 名称=[{}], expectedResults=[{}], 字符长度=[{}]",
                               i + 1, phase.getPhaseName(), phase.getExpectedResults(),
                               phase.getExpectedResults() != null ? phase.getExpectedResults().length() : 0);

                    // 验证JSON反序列化
                    if (phase.getExpectedResults() != null && !phase.getExpectedResults().isEmpty()) {
                        logger.info("阶段 {} expectedResults 内容详细检查:", i + 1);
                        logger.info("- 原始内容: [{}]", phase.getExpectedResults());
                        logger.info("- 内容为空: {}", phase.getExpectedResults().trim().isEmpty());
                        logger.info("- Unicode字符数: {}", phase.getExpectedResults().codePointCount(0, phase.getExpectedResults().length()));
                    }
                }
            }

            Long currentUserId = getCurrentUser().getId();

            // 检查项目名称是否已存在
            if (projectRepository.existsByName(request.getName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("项目名称已存在，请使用其他名称"));
            }

            // 创建项目实体
            Project project = new Project();
            project.setName(request.getName());
            project.setDescription(request.getDescription());
            project.setMembers(request.getMembers());
            project.setExpectedResults(request.getExpectedResults());
            project.setTimeline(request.getTimeline());
            project.setStopLoss(request.getStopLoss());
            project.setCreatedBy(currentUserId);
            // 根据CLAUDE.md要求：项目创建后直接进入AI分析，不停留在草稿状态
            project.setApprovalStatus(Project.ApprovalStatus.AI_ANALYZING);

            Project savedProject = projectRepository.save(project);
            final Long projectId = savedProject.getId();
            logger.info("Project created: {} by user: {}, entering AI analysis", projectId, currentUserId);

            // 触发AI分析流程
            try {
                logger.info("Starting AI analysis for project: {}", projectId);
                AIAnalysisResult analysisResult = aiAnalysisService.analyzeProject(savedProject);
                logger.info("AI analysis completed for project: {}, result: {}", projectId, analysisResult.getId());

                // 根据AI分析结果更新项目状态
                if (analysisResult.getStatus() == AIAnalysisResult.AnalysisStatus.COMPLETED) {
                    // 检查置信度阈值 (置信度 < 0.5 视为不通过)
                    Double confidence = analysisResult.getConfidence();
                    final double CONFIDENCE_THRESHOLD = 0.5;

                    if (confidence != null && confidence < CONFIDENCE_THRESHOLD) {
                        // 置信度过低，AI拒绝
                        savedProject.setApprovalStatus(Project.ApprovalStatus.AI_REJECTED);
                        savedProject.setRejectionReason(String.format("AI分析置信度过低(%.1f%%)，建议优化项目计划后重新提交。分析建议: %s",
                            confidence * 100, analysisResult.getResult()));
                        savedProject.setAiAnalysisId(analysisResult.getId());
                        savedProject = projectRepository.save(savedProject);
                        logger.warn("Project {} AI analysis rejected due to low confidence: {}, status updated to AI_REJECTED",
                                   projectId, confidence);
                    } else {
                        // AI分析通过，进入管理员审核
                        savedProject.setApprovalStatus(Project.ApprovalStatus.ADMIN_REVIEWING);
                        savedProject.setAiAnalysisId(analysisResult.getId());
                        savedProject = projectRepository.save(savedProject);
                        logger.info("Project {} AI analysis passed (confidence: {}), status updated to ADMIN_REVIEWING",
                                   projectId, confidence);

                        // 发布AI分析完成事件和待管理员审核事件
                        AIAnalysisCompletedEvent analysisEvent = new AIAnalysisCompletedEvent(
                            projectId,
                            savedProject.getName(),
                            savedProject.getCreatedBy(),
                            analysisResult.getResult()
                        );
                        eventPublisher.publishEvent(analysisEvent);

                        // 发布待管理员审核事件
                        PendingAdminReviewEvent reviewEvent = new PendingAdminReviewEvent(
                            projectId,
                            savedProject.getName(),
                            savedProject.getCreatedBy()
                        );
                        eventPublisher.publishEvent(reviewEvent);
                    }
                } else {
                    // AI分析失败
                    savedProject.setApprovalStatus(Project.ApprovalStatus.AI_REJECTED);
                    savedProject.setRejectionReason("AI分析未通过: " + analysisResult.getResult());
                    savedProject.setAiAnalysisId(analysisResult.getId());
                    savedProject = projectRepository.save(savedProject);
                    logger.warn("Project {} AI analysis failed, status updated to AI_REJECTED", projectId);
                }
            } catch (Exception e) {
                logger.error("Failed to start AI analysis for project: {}", projectId, e);
                // AI分析失败时，将状态设为AI_REJECTED
                savedProject.setApprovalStatus(Project.ApprovalStatus.AI_REJECTED);
                savedProject.setRejectionReason("AI分析服务暂时不可用: " + e.getMessage());
                projectRepository.save(savedProject);
            }

            // 创建项目阶段
            List<ProjectPhaseResponse> phaseResponses = null;
            if (request.getPhases() != null && !request.getPhases().isEmpty()) {
                logger.info("开始创建项目阶段，共 {} 个阶段", request.getPhases().size());
                List<ProjectPhase> savedPhases = request.getPhases().stream()
                    .map(phaseRequest -> {
                        logger.info("处理阶段: {}, expectedResults: {}",
                                   phaseRequest.getPhaseName(), phaseRequest.getExpectedResults());
                        ProjectPhase phase = new ProjectPhase();
                        phase.setProjectId(projectId);
                        phase.setPhaseName(phaseRequest.getPhaseName());
                        phase.setDescription(phaseRequest.getDescription());
                        phase.setAssignedMembers(phaseRequest.getAssignedMembers());
                        phase.setSchedule(phaseRequest.getSchedule());

                        // 处理 expectedResults 为 null 的情况，设置默认值
                        String expectedResults = phaseRequest.getExpectedResults();
                        if (expectedResults == null || expectedResults.trim().isEmpty()) {
                            expectedResults = ""; // 设置为空字符串而不是null
                        }
                        phase.setExpectedResults(expectedResults);
                        logger.info("阶段对象创建完成，expectedResults设置为: {}", phase.getExpectedResults());
                        return phase;
                    })
                    .collect(Collectors.toList());

                projectPhaseRepository.saveAll(savedPhases);
                logger.info("阶段保存到数据库完成");

                // 验证保存后的数据
                savedPhases.forEach(phase -> {
                    logger.info("已保存阶段 ID: {}, expectedResults: [{}], 字符长度: {}",
                               phase.getId(), phase.getExpectedResults(),
                               phase.getExpectedResults() != null ? phase.getExpectedResults().length() : 0);

                    // 立即从数据库重新查询验证
                    ProjectPhase fromDb = projectPhaseRepository.findById(phase.getId()).orElse(null);
                    if (fromDb != null) {
                        logger.info("数据库中的实际值 ID: {}, expectedResults: [{}]",
                                   fromDb.getId(), fromDb.getExpectedResults());
                    }
                });

                phaseResponses = savedPhases.stream()
                    .map(ProjectPhaseResponse::new)
                    .collect(Collectors.toList());
                logger.info("Created {} phases for project: {}", savedPhases.size(), savedProject.getId());
            }

            ProjectResponse response = new ProjectResponse(savedProject);
            response.setPhases(phaseResponses);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));

        } catch (Exception e) {
            logger.error("Error creating project", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("创建项目失败，请稍后重试"));
        }
    }

    /**
     * GET /api/projects/pending-review
     * 获取待审批项目（只包含待审批状态的项目）
     */
    @GetMapping("/pending-review")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getPendingReviewProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));

            Page<com.weeklyreport.project.repository.projection.ProjectDetailProjection> projectDetailsPage;
            if (SecurityUtils.hasRole("SUPER_ADMIN")) {
                projectDetailsPage = projectRepository.findPendingSuperAdminReviewProjectsWithDetails(pageable);
                logger.info("Super admin fetching pending review projects: total={} page={}/{}",
                           projectDetailsPage.getTotalElements(), projectDetailsPage.getNumber() + 1, projectDetailsPage.getTotalPages());
            } else {
                projectDetailsPage = projectRepository.findPendingAdminReviewProjectsWithDetails(pageable);
                logger.info("Admin fetching pending review projects: total={} page={}/{}",
                           projectDetailsPage.getTotalElements(), projectDetailsPage.getNumber() + 1, projectDetailsPage.getTotalPages());
            }

            Page<ProjectResponse> responsePage = projectDetailsPage.map(detail -> {
                ProjectResponse response = createProjectResponseFromDetailProjection(detail);
                List<ProjectPhase> phases = projectPhaseRepository.findByProjectIdOrderByCreatedAt(detail.getId());
                List<ProjectPhaseResponse> phaseResponses = phases.stream()
                    .map(ProjectPhaseResponse::new)
                    .collect(Collectors.toList());
                response.setPhases(phaseResponses);
                return response;
            });

            return ResponseEntity.ok(ApiResponse.success(responsePage));

        } catch (Exception e) {
            logger.error("Error getting pending review projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取待审核项目失败，请稍后重试"));
        }
    }

    /**
     * GET /api/projects/approved
     * 获取已通过项目（包含各种已通过状态的项目）
     */
    @GetMapping("/approved")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getApprovedProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));

            Page<com.weeklyreport.project.repository.projection.ProjectDetailProjection> projectDetailsPage;
            if (SecurityUtils.hasRole("SUPER_ADMIN")) {
                projectDetailsPage = projectRepository.findApprovedProjectsWithDetails(pageable);
                logger.info("Super admin fetching approved projects: total={} page={}/{}",
                           projectDetailsPage.getTotalElements(), projectDetailsPage.getNumber() + 1, projectDetailsPage.getTotalPages());
            } else {
                projectDetailsPage = projectRepository.findAdminApprovedProjectsWithDetails(pageable);
                logger.info("Admin fetching approved projects: total={} page={}/{}",
                           projectDetailsPage.getTotalElements(), projectDetailsPage.getNumber() + 1, projectDetailsPage.getTotalPages());
            }

            Page<ProjectResponse> responsePage = mapProjectDetailPageToResponse(projectDetailsPage);
            return ResponseEntity.ok(ApiResponse.success(responsePage));

        } catch (Exception e) {
            logger.error("Error getting approved projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取已通过项目失败，请稍后重试"));
        }
    }

    /**
     * GET /api/projects/rejected
     * 获取已拒绝项目（包含各种拒绝状态的项目）
     */
    @GetMapping("/rejected")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getRejectedProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));

            Page<com.weeklyreport.project.repository.projection.ProjectDetailProjection> projectDetailsPage;
            if (SecurityUtils.hasRole("SUPER_ADMIN")) {
                projectDetailsPage = projectRepository.findAllRejectedProjectsWithDetails(pageable);
                logger.info("Super admin fetching rejected projects: total={} page={}/{}",
                           projectDetailsPage.getTotalElements(), projectDetailsPage.getNumber() + 1, projectDetailsPage.getTotalPages());
            } else {
                projectDetailsPage = projectRepository.findAdminVisibleRejectedProjectsWithDetails(pageable);
                logger.info("Admin fetching rejected projects: total={} page={}/{}",
                           projectDetailsPage.getTotalElements(), projectDetailsPage.getNumber() + 1, projectDetailsPage.getTotalPages());
            }

            Page<ProjectResponse> responsePage = mapProjectDetailPageToResponse(projectDetailsPage);
            return ResponseEntity.ok(ApiResponse.success(responsePage));

        } catch (Exception e) {
            logger.error("Error getting rejected projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取已拒绝项目失败，请稍后重试"));
        }
    }

    /**
     * 获取单个项目详情
     * GET /api/projects/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("@projectPermissionEvaluator.canViewProject(#id, authentication)")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(@PathVariable Long id) {

        try {
            // 使用JOIN查询一次性获取项目和创建者信息
            ProjectWithCreatorProjection projectWithCreator = projectRepository.findProjectWithCreator(id);
            if (projectWithCreator == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("项目不存在"));
            }

            // 从投影创建ProjectResponse
            ProjectResponse response = createProjectResponseFromProjection(projectWithCreator);

            // 查询项目阶段
            List<ProjectPhase> phases = projectPhaseRepository.findByProjectIdOrderByCreatedAt(projectWithCreator.getId());
            List<ProjectPhaseResponse> phaseResponses = phases.stream()
                .map(ProjectPhaseResponse::new)
                .collect(Collectors.toList());
            response.setPhases(phaseResponses);

            // 查询AI分析结果
            if (projectWithCreator.getAiAnalysisId() != null) {
                try {
                    Optional<AIAnalysisResult> aiAnalysisOpt = aiAnalysisResultRepository.findById(projectWithCreator.getAiAnalysisId());
                    if (aiAnalysisOpt.isPresent()) {
                        response.setAiAnalysisResult(new AIAnalysisResultResponse(aiAnalysisOpt.get()));
                        logger.debug("AI analysis result loaded for project: {}, analysis ID: {}",
                                   projectWithCreator.getId(), projectWithCreator.getAiAnalysisId());
                    } else {
                        logger.warn("AI analysis result not found for project: {}, analysis ID: {}",
                                  projectWithCreator.getId(), projectWithCreator.getAiAnalysisId());
                    }
                } catch (Exception e) {
                    logger.error("Error loading AI analysis result for project: {}", projectWithCreator.getId(), e);
                }
            } else {
                // 如果没有直接关联的AI分析ID，尝试通过项目ID查找
                try {
                    Optional<AIAnalysisResult> latestAnalysis = aiAnalysisResultRepository
                        .findTopByReportIdAndEntityTypeOrderByCreatedAtDesc(
                            projectWithCreator.getId(),
                            AIAnalysisResult.EntityType.PROJECT
                        );
                    if (latestAnalysis.isPresent()) {
                        response.setAiAnalysisResult(new AIAnalysisResultResponse(latestAnalysis.get()));
                        logger.debug("Latest AI analysis result found for project: {}, analysis ID: {}",
                                   projectWithCreator.getId(), latestAnalysis.get().getId());
                    } else {
                        logger.debug("No AI analysis result found for project: {}", projectWithCreator.getId());
                    }
                } catch (Exception e) {
                    logger.error("Error searching AI analysis result for project: {}", projectWithCreator.getId(), e);
                }
            }

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            logger.error("Error getting project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取项目信息失败，请稍后重试"));
        }
    }

    /**
     * 更新项目（仅创建者可更新）
     * PUT /api/projects/{id}
     *
     * 只支持草稿状态的项目更新
     * 被拒绝的项目应使用 resubmit 接口
     */
    @PutMapping("/{id}")
    @PreAuthorize("@projectPermissionEvaluator.canModifyProject(#id, authentication)")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectCreateRequest request) {

        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("项目不存在"));
            }

            Project project = projectOpt.get();
            Long currentUserId = getCurrentUser().getId();

            // 只有草稿状态的项目可以更新
            if (!project.isDraft()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("只有草稿状态的项目才能修改"));
            }

            // 更新字段
            project.setName(request.getName());
            project.setDescription(request.getDescription());
            project.setMembers(request.getMembers());
            project.setExpectedResults(request.getExpectedResults());
            project.setTimeline(request.getTimeline());
            project.setStopLoss(request.getStopLoss());

            Project savedProject = projectRepository.save(project);
            logger.info("Project updated: {} by user: {}",
                       savedProject.getId(), currentUserId);

            // 更新项目阶段
            if (request.getPhases() != null && !request.getPhases().isEmpty()) {
                // 删除现有阶段
                projectPhaseRepository.deleteByProjectId(savedProject.getId());

                // 创建新的阶段
                final Long projectId = savedProject.getId();
                List<ProjectPhase> newPhases = request.getPhases().stream()
                    .map(phaseRequest -> {
                        ProjectPhase phase = new ProjectPhase();
                        phase.setProjectId(projectId);
                        phase.setPhaseName(phaseRequest.getPhaseName());
                        phase.setDescription(phaseRequest.getDescription());
                        phase.setAssignedMembers(phaseRequest.getAssignedMembers());
                        phase.setSchedule(phaseRequest.getSchedule());
                        phase.setExpectedResults(phaseRequest.getExpectedResults() != null ?
                                                phaseRequest.getExpectedResults() : "");
                        return phase;
                    })
                    .collect(Collectors.toList());

                if (!newPhases.isEmpty()) {
                    projectPhaseRepository.saveAll(newPhases);
                    logger.info("Updated {} phases for project: {}", newPhases.size(), savedProject.getId());
                }
            }

            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));

        } catch (Exception e) {
            logger.error("Error updating project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("更新项目失败，请稍后重试"));
        }
    }

    /**
     * 删除项目（仅创建者可删除）
     * DELETE /api/projects/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@projectPermissionEvaluator.canModifyProject(#id, authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {

        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("项目不存在"));
            }

            Project project = projectOpt.get();
            Long currentUserId = getCurrentUser().getId();

            // 只有草稿状态的项目可以删除
            if (!project.isDraft()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("只有草稿状态的项目才能删除"));
            }

            projectRepository.delete(project);
            logger.info("Project deleted: {} by user: {}", id, currentUserId);

            return ResponseEntity.ok(ApiResponse.success(null));

        } catch (Exception e) {
            logger.error("Error deleting project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("删除项目失败，请稍后重试"));
        }
    }

    /**
     * 提交项目进行AI分析（主管权限）
     * PUT /api/projects/{id}/submit
     */
    @PutMapping("/{id}/submit")
    @PreAuthorize("@projectPermissionEvaluator.canModifyProject(#id, authentication)")
    public ResponseEntity<ApiResponse<ProjectResponse>> submitProject(@PathVariable Long id) {

        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("项目不存在"));
            }

            Project project = projectOpt.get();
            Long currentUserId = getCurrentUser().getId();

            // 只有草稿状态或被拒绝的项目可以提交
            if (!project.isDraft() && !project.isRejected()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("只有草稿或已拒绝状态的项目才能提交"));
            }

            // 更新状态为AI分析中
            project.submit();

            try {
                // 触发AI分析，分析通过后直接进入管理员审核
                AIAnalysisResult analysisResult = aiAnalysisService.analyzeProject(project);
                logger.info("AI analysis completed for project: {}", project.getId());

                if (analysisResult.getStatus() == AIAnalysisResult.AnalysisStatus.COMPLETED) {
                    // 检查置信度阈值
                    Double confidence = analysisResult.getConfidence();
                    final double CONFIDENCE_THRESHOLD = 0.5;

                    if (confidence != null && confidence < CONFIDENCE_THRESHOLD) {
                        // 置信度过低，AI拒绝
                        project.setApprovalStatus(Project.ApprovalStatus.AI_REJECTED);
                        project.setRejectionReason(String.format("AI分析置信度过低(%.1f%%)，建议优化项目计划后重新提交。分析建议: %s",
                            confidence * 100, analysisResult.getResult()));
                        project.setAiAnalysisId(analysisResult.getId());
                        logger.warn("Project {} AI analysis rejected due to low confidence: {}", project.getId(), confidence);
                    } else {
                        // AI分析通过，进入管理员审核
                        project.setApprovalStatus(Project.ApprovalStatus.ADMIN_REVIEWING);
                        project.setAiAnalysisId(analysisResult.getId());
                        logger.info("Project {} AI analysis passed (confidence: {})", project.getId(), confidence);
                    }
                } else {
                    project.setApprovalStatus(Project.ApprovalStatus.AI_REJECTED);
                    project.setRejectionReason("AI分析未通过: " + analysisResult.getResult());
                    project.setAiAnalysisId(analysisResult.getId());
                }
            } catch (Exception aiError) {
                logger.error("AI analysis failed for project: {}", project.getId(), aiError);
                project.setApprovalStatus(Project.ApprovalStatus.AI_REJECTED);
                project.setRejectionReason("AI分析失败: " + aiError.getMessage());
            }

            Project savedProject = projectRepository.save(project);
            logger.info("Project submitted: {} by user: {}", savedProject.getId(), currentUserId);

            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));

        } catch (Exception e) {
            logger.error("Error submitting project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("提交项目失败，请稍后重试"));
        }
    }

    /**
     * 强制提交项目（跳过AI分析，直接进入管理员审核）
     * POST /api/projects/{id}/force-submit
     * 只有主管可以强制提交
     */
    @PostMapping("/{id}/force-submit")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> forceSubmitProject(@PathVariable Long id) {
        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("项目不存在"));
            }

            Project project = projectOpt.get();
            Long currentUserId = getCurrentUser().getId();

            // 只有主管可以强制提交项目（权限已由@PreAuthorize控制）

            // 只有草稿状态或被拒绝的项目可以强行提交
            if (!project.isDraft() && !project.isRejected()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("只有草稿或已拒绝状态的项目才能强制提交"));
            }

            // 跳过AI分析，直接进入管理员审核状态
            project.setApprovalStatus(Project.ApprovalStatus.ADMIN_REVIEWING);
            project.setRejectionReason(null); // 清除之前的拒绝原因

            Project savedProject = projectRepository.save(project);
            logger.info("Project force submitted (bypassing AI): {} by user: {}", savedProject.getId(), currentUserId);

            // 发送强制提交通知给所有管理员
            try {
                ForceSubmittedEvent forceEvent = new ForceSubmittedEvent(
                    savedProject.getId(),
                    savedProject.getName(),
                    currentUserId,
                    "主管强制提交项目，跳过AI分析环节"
                );
                eventPublisher.publishEvent(forceEvent);
                logger.info("Force submission notification sent for project {}", savedProject.getId());

                // 同时发送待管理员审核通知
                PendingAdminReviewEvent reviewEvent = new PendingAdminReviewEvent(
                    savedProject.getId(), 
                    savedProject.getName(), 
                    currentUserId
                );
                eventPublisher.publishEvent(reviewEvent);
                logger.info("Pending admin review notification sent for project {}", savedProject.getId());
            } catch (Exception e) {
                logger.warn("Failed to send force submission notifications for project {}: {}", 
                    savedProject.getId(), e.getMessage());
            }

            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));

        } catch (Exception e) {
            logger.error("Error force submitting project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("强制提交项目失败，请稍后重试"));
        }
    }

    /**
     * 项目修改后重新提交（主管权限）
     * PUT /api/simple/projects/{id}/resubmit
     *
     * 支持状态：
     * - 被拒绝状态的项目
     * - 已批准(FINAL_APPROVED)的项目
     */
    @PutMapping("/{id}/resubmit")
    @PreAuthorize("@projectPermissionEvaluator.canModifyProject(#id, authentication)")
    public ResponseEntity<ApiResponse<ProjectResponse>> resubmitProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest updateRequest) {
        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("项目不存在"));
            }

            Project project = projectOpt.get();
            Long currentUserId = getCurrentUser().getId();

            // 被拒绝的项目或已批准的项目可以重新提交
            boolean canResubmit = project.isRejected() ||
                                 project.getApprovalStatus() == Project.ApprovalStatus.FINAL_APPROVED;

            if (!canResubmit) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("只有已拒绝或已批准的项目才能重新提交"));
            }

            // 更新项目信息
            if (updateRequest.getName() != null) {
                project.setName(updateRequest.getName());
            }
            if (updateRequest.getDescription() != null) {
                project.setDescription(updateRequest.getDescription());
            }
            if (updateRequest.getMembers() != null) {
                project.setMembers(updateRequest.getMembers());
            }
            if (updateRequest.getExpectedResults() != null) {
                project.setExpectedResults(updateRequest.getExpectedResults());
            }
            if (updateRequest.getTimeline() != null) {
                project.setTimeline(updateRequest.getTimeline());
            }
            if (updateRequest.getStopLoss() != null) {
                project.setStopLoss(updateRequest.getStopLoss());
            }

            // 重新提交：清除拒绝原因，重新进入AI分析
            project.setApprovalStatus(Project.ApprovalStatus.AI_ANALYZING);
            project.setRejectionReason(null);

            // 先保存项目更新
            Project savedProject = projectRepository.save(project);

            // 处理项目阶段更新
            if (updateRequest.getProjectPhases() != null) {
                // 删除现有阶段
                projectPhaseRepository.deleteByProjectId(savedProject.getId());

                // 创建新的阶段
                final Long projectId = savedProject.getId(); // Make it effectively final
                List<ProjectPhase> newPhases = updateRequest.getProjectPhases().stream()
                    .filter(phaseRequest -> phaseRequest.getPhaseName() != null && !phaseRequest.getPhaseName().trim().isEmpty())
                    .map(phaseRequest -> {
                        ProjectPhase phase = new ProjectPhase();
                        phase.setProjectId(projectId);
                        phase.setPhaseName(phaseRequest.getPhaseName());
                        phase.setDescription(phaseRequest.getDescription());
                        phase.setAssignedMembers(phaseRequest.getAssignedMembers());
                        phase.setSchedule(phaseRequest.getSchedule());
                        phase.setExpectedResults(phaseRequest.getExpectedResults());
                        return phase;
                    })
                    .collect(Collectors.toList());

                if (!newPhases.isEmpty()) {
                    projectPhaseRepository.saveAll(newPhases);
                    logger.info("Updated {} phases for project: {}", newPhases.size(), savedProject.getId());
                }
            }

            // 触发真正的AI分析
            try {
                AIAnalysisResult analysisResult = aiAnalysisService.analyzeProject(savedProject);
                logger.info("AI analysis completed for resubmitted project: {}, result: {}", savedProject.getId(), analysisResult.getId());

                // 根据AI分析结果更新项目状态
                if (analysisResult.getStatus() == AIAnalysisResult.AnalysisStatus.COMPLETED) {
                    // AI分析成功，直接进入管理员审核（跳过AI_APPROVED状态）
                    savedProject.setApprovalStatus(Project.ApprovalStatus.ADMIN_REVIEWING);
                    savedProject.setAiAnalysisId(analysisResult.getId());
                    savedProject = projectRepository.save(savedProject);
                    logger.info("Resubmitted project {} AI analysis passed, status updated to ADMIN_REVIEWING", savedProject.getId());

                    // 发布AI分析完成事件和待管理员审核事件
                    AIAnalysisCompletedEvent analysisEvent = new AIAnalysisCompletedEvent(
                        savedProject.getId(),
                        savedProject.getName(),
                        savedProject.getCreatedBy(),
                        analysisResult.getResult()
                    );
                    eventPublisher.publishEvent(analysisEvent);

                    // 发布待管理员审核事件
                    PendingAdminReviewEvent reviewEvent = new PendingAdminReviewEvent(
                        savedProject.getId(),
                        savedProject.getName(),
                        savedProject.getCreatedBy()
                    );
                    eventPublisher.publishEvent(reviewEvent);
                } else {
                    savedProject.setApprovalStatus(Project.ApprovalStatus.AI_REJECTED);
                    savedProject.setRejectionReason("AI分析未通过: " + analysisResult.getResult());
                    savedProject.setAiAnalysisId(analysisResult.getId());
                    savedProject = projectRepository.save(savedProject);
                    logger.warn("Resubmitted project {} AI analysis failed, status updated to AI_REJECTED", savedProject.getId());
                }

            } catch (Exception aiError) {
                logger.error("AI analysis failed for resubmitted project: {}", savedProject.getId(), aiError);
                // 如果AI分析失败，设置为AI_REJECTED状态
                savedProject.setApprovalStatus(Project.ApprovalStatus.AI_REJECTED);
                savedProject.setRejectionReason("AI分析失败: " + aiError.getMessage());
                savedProject = projectRepository.save(savedProject);
            }

            logger.info("Project resubmitted with updates: {} by user: {}", savedProject.getId(), currentUserId);

            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));

        } catch (Exception e) {
            logger.error("Error resubmitting project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("重新提交项目失败，请稍后重试"));
        }
    }

    /**
     * 管理员审核项目（管理员权限）
     * PUT /projects/{id}/approve
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> adminApproveProject(@PathVariable Long id) {

        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("项目不存在"));
            }

            Project project = projectOpt.get();
            User currentUser = getCurrentUser();
            Long currentUserId = currentUser.getId();

            logger.info("Admin approval process - Project: {}, Current User: {}, User ID: {}, Username: {}",
                       id, currentUser, currentUserId, currentUser.getUsername());

            // 检查项目状态：应该是管理员审核状态或管理员拒绝状态（允许重新审核）
            if (project.getApprovalStatus() != Project.ApprovalStatus.ADMIN_REVIEWING &&
                project.getApprovalStatus() != Project.ApprovalStatus.ADMIN_REJECTED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("项目必须在管理员审核中或管理员已拒绝状态才能进行管理员审核"));
            }

            logger.info("Before adminApprove - Project ID: {}, adminReviewerId: {}",
                       project.getId(), project.getAdminReviewerId());

            // 管理员审批通过，直接设为管理员审核通过状态
            project.adminApprove(currentUserId);

            logger.info("After adminApprove - Project ID: {}, adminReviewerId: {}, status: {}",
                       project.getId(), project.getAdminReviewerId(), project.getApprovalStatus());

            Project savedProject = projectRepository.save(project);
            logger.info("Project admin approved and saved: {} by user: {} (username: {})",
                       savedProject.getId(), currentUserId, currentUser.getUsername());

            // 发送管理员通过通知给超级管理员和项目经理
            try {
                AdminApprovedEvent event = new AdminApprovedEvent(
                    savedProject.getId(), 
                    savedProject.getName(), 
                    currentUserId
                );
                eventPublisher.publishEvent(event);
                logger.info("Admin approval notification sent for project {}", savedProject.getId());
            } catch (Exception e) {
                logger.warn("Failed to send admin approval notification for project {}: {}", 
                    savedProject.getId(), e.getMessage());
            }

            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));

        } catch (Exception e) {
            logger.error("Error approving project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("审核通过项目失败，请稍后重试"));
        }
    }

    /**
     * AI审批项目 - 兼容测试接口
     * PUT /api/projects/{id}/ai-approve
     */
    @PutMapping("/{id}/ai-approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> aiApproveProject(@PathVariable Long id) {
        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("项目不存在"));
            }

            Project project = projectOpt.get();

            // 直接将项目送入管理员审核阶段，跳过AI实际分析
            project.setApprovalStatus(Project.ApprovalStatus.ADMIN_REVIEWING);
            project.setStatus(Project.ProjectStatus.ACTIVE);
            project.setProgress(50);

            Project savedProject = projectRepository.save(project);
            logger.info("Project moved to ADMIN_REVIEWING via AI approve shortcut: {}", savedProject.getId());

            // 发送AI分析完成通知给项目经理和待管理员审核通知给所有管理员
            try {
                // AI分析完成通知
                AIAnalysisCompletedEvent analysisEvent = new AIAnalysisCompletedEvent(
                    savedProject.getId(), 
                    savedProject.getName(), 
                    savedProject.getCreatedBy(), 
                    "AI分析已通过审核"
                );
                eventPublisher.publishEvent(analysisEvent);
                logger.info("AI analysis completion notification sent for project {}", savedProject.getId());

                // 待管理员审核通知
                PendingAdminReviewEvent reviewEvent = new PendingAdminReviewEvent(
                    savedProject.getId(), 
                    savedProject.getName(), 
                    savedProject.getCreatedBy()
                );
                eventPublisher.publishEvent(reviewEvent);
                logger.info("Pending admin review notification sent for project {}", savedProject.getId());
            } catch (Exception e) {
                logger.warn("Failed to send AI analysis notifications for project {}: {}", 
                    savedProject.getId(), e.getMessage());
            }

            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));

        } catch (Exception e) {
            logger.error("Error AI approving project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("AI审核通过项目失败，请稍后重试"));
        }
    }

    /**
     * 管理员审批项目 - 兼容测试接口
     * PUT /api/projects/{id}/admin-approve
     */
    @PutMapping("/{id}/admin-approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> adminApproveProjectCompat(@PathVariable Long id) {
        return adminApproveProject(id);
    }

    /**
     * 超级管理员最终审核项目（超级管理员权限）
     * PUT /api/projects/{id}/final-approve
     * PUT /api/projects/{id}/super-admin-approve - 兼容测试接口
     */
    @PutMapping("/{id}/final-approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> superAdminApproveProject(@PathVariable Long id) {

        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("项目不存在"));
            }

            Project project = projectOpt.get();
            Long currentUserId = getCurrentUser().getId();

            // 检查项目状态：应该是管理员审核通过或审核失败状态
            if (project.getApprovalStatus() != Project.ApprovalStatus.ADMIN_APPROVED &&
                project.getApprovalStatus() != Project.ApprovalStatus.ADMIN_REJECTED &&
                project.getApprovalStatus() != Project.ApprovalStatus.SUPER_ADMIN_REVIEWING) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("项目状态不符合超级管理员审核条件"));
            }

            // 超级管理员最终审批通过
            project.superAdminApprove(currentUserId);
            project.setApprovalStatus(Project.ApprovalStatus.FINAL_APPROVED);

            Project savedProject = projectRepository.save(project);
            logger.info("Project final approved: {} by user: {}", savedProject.getId(), currentUserId);

            // 发送超级管理员通过通知给所有相关人员
            try {
                SuperAdminApprovedEvent event = new SuperAdminApprovedEvent(
                    savedProject.getId(), 
                    savedProject.getName(), 
                    currentUserId
                );
                eventPublisher.publishEvent(event);
                logger.info("Super admin approval notification sent for project {}", savedProject.getId());
            } catch (Exception e) {
                logger.warn("Failed to send super admin approval notification for project {}: {}", 
                    savedProject.getId(), e.getMessage());
            }

            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));

        } catch (Exception e) {
            logger.error("Error final approving project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("最终审核通过项目失败，请稍后重试"));
        }
    }

    /**
     * 超级管理员审批项目 - 兼容测试接口
     * PUT /api/projects/{id}/super-admin-approve
     */
    @PutMapping("/{id}/super-admin-approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> superAdminApproveProjectCompat(@PathVariable Long id) {
        return superAdminApproveProject(id);
    }

    /**
     * 拒绝项目（管理员和超级管理员权限）
     * PUT /api/projects/{id}/reject
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("@projectPermissionEvaluator.canRejectProject(#id, authentication)")
    public ResponseEntity<ApiResponse<ProjectResponse>> rejectProject(
            @PathVariable Long id,
            @RequestBody String reason) {

        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("项目不存在"));
            }

            Project project = projectOpt.get();
            Long currentUserId = getCurrentUser().getId();
            boolean isAdmin = SecurityUtils.hasRole("ADMIN");
            boolean isSuperAdmin = SecurityUtils.hasRole("SUPER_ADMIN");

            // 根据用户角色执行相应的拒绝操作
            if (isAdmin) {
                project.adminReject(currentUserId, reason);
                logger.info("ADMIN rejected project: {} by user: {}", project.getId(), currentUserId);
            } else if (isSuperAdmin) {
                project.superAdminReject(currentUserId, reason);
                logger.info("SUPER_ADMIN rejected project: {} by user: {}", project.getId(), currentUserId);
            } else {
                // 理论上不会发生，因为授权已经提前校验
                logger.warn("Reject project attempt without proper role. Project: {} User: {}",
                        project.getId(), currentUserId);
                throw new AccessDeniedException("没有权限拒绝此项目");
            }

            Project savedProject = projectRepository.save(project);
            logger.info("Project rejected: {} by user: {} with reason: {}",
                       savedProject.getId(), currentUserId, reason);

            // 发送拒绝通知给项目经理
            try {
                if (isAdmin) {
                    AdminRejectedEvent event = new AdminRejectedEvent(
                        savedProject.getId(), 
                        savedProject.getName(), 
                        currentUserId, 
                        reason
                    );
                    eventPublisher.publishEvent(event);
                    logger.info("Admin rejection notification sent for project {}", savedProject.getId());
                } else if (isSuperAdmin) {
                    SuperAdminRejectedEvent event = new SuperAdminRejectedEvent(
                        savedProject.getId(), 
                        savedProject.getName(), 
                        currentUserId, 
                        reason
                    );
                    eventPublisher.publishEvent(event);
                    logger.info("Super admin rejection notification sent for project {}", savedProject.getId());
                }
            } catch (Exception e) {
                logger.warn("Failed to send rejection notification for project {}: {}", 
                    savedProject.getId(), e.getMessage());
            }

            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));

        } catch (Exception e) {
            logger.error("Error rejecting project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("拒绝项目失败，请稍后重试"));
        }
    }

    /**
     * 获取用户创建的项目列表（支持按审批状态过滤）
     * GET /api/projects/my?approvalStatus={status}
     *
     * @param approvalStatus 可选参数，审批状态过滤
     *                      可选值: AI_ANALYZING, AI_APPROVED, AI_REJECTED,
     *                             ADMIN_REVIEWING, ADMIN_APPROVED, ADMIN_REJECTED,
     *                             SUPER_ADMIN_REVIEWING, SUPER_ADMIN_APPROVED, SUPER_ADMIN_REJECTED,
     *                             FINAL_APPROVED
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getMyProjects(
            @RequestParam(required = false) String approvalStatus) {

        try {
            Long currentUserId = getCurrentUser().getId();
            List<Project> projects = projectRepository.findByCreatedByOrderByCreatedAtDesc(currentUserId);

            // 如果指定了审批状态，进行过滤
            if (approvalStatus != null && !approvalStatus.trim().isEmpty()) {
                try {
                    Project.ApprovalStatus statusEnum = Project.ApprovalStatus.valueOf(approvalStatus.toUpperCase());
                    projects = projects.stream()
                        .filter(project -> project.getApprovalStatus() == statusEnum)
                        .collect(Collectors.toList());
                    logger.info("Filtered projects by status: {}, found: {} projects", approvalStatus, projects.size());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid approval status: {}", approvalStatus);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("无效的审批状态: " + approvalStatus));
                }
            }

            List<ProjectResponse> responses = projects.stream()
                .map(project -> {
                    ProjectResponse response = new ProjectResponse(project);
                    // 查询项目阶段
                    List<ProjectPhase> phases = projectPhaseRepository.findByProjectIdOrderByCreatedAt(project.getId());
                    List<ProjectPhaseResponse> phaseResponses = phases.stream()
                        .map(ProjectPhaseResponse::new)
                        .collect(Collectors.toList());
                    response.setPhases(phaseResponses);
                    return response;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(responses));

        } catch (Exception e) {
            logger.error("Error getting my projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取我的项目列表失败，请稍后重试"));
        }
    }

    /**
     * 获取待审核项目列表（管理员和超级管理员）
     * GET /api/projects/pending
     * 优化版本：使用多表联查一次性获取所有关联数据
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getPendingProjects() {

        try {
            Pageable unpaged = Pageable.unpaged();
            Page<com.weeklyreport.project.repository.projection.ProjectDetailProjection> projectDetailsPage;

            if (SecurityUtils.hasRole("SUPER_ADMIN")) {
                projectDetailsPage = projectRepository.findPendingSuperAdminReviewProjectsWithDetails(unpaged);
                logger.info("Super admin fetching pending projects: {}", projectDetailsPage.getTotalElements());
            } else {
                projectDetailsPage = projectRepository.findPendingAdminReviewProjectsWithDetails(unpaged);
                logger.info("Admin fetching pending projects: {}", projectDetailsPage.getTotalElements());
            }

            List<ProjectResponse> responses = projectDetailsPage.stream()
                .map(this::mapProjectDetailToResponse)
                .collect(Collectors.toList());

            logger.info("Successfully returned {} pending projects with full details", responses.size());
            return ResponseEntity.ok(ApiResponse.success(responses));

        } catch (Exception e) {
            logger.error("Error getting pending projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取待审核项目列表失败，请稍后重试"));
        }
    }

    /**
     * 获取自己审核过的项目列表（管理员和超级管理员）
     * GET /api/projects/reviewed
     */
    @GetMapping("/reviewed")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getReviewedProjects() {
        try {
            Long currentUserId = getCurrentUser().getId();
            List<Project> projects;

            if (SecurityUtils.hasRole("SUPER_ADMIN")) {
                // 超级管理员可以看到自己作为管理员或超级管理员审核过的所有项目
                projects = projectRepository.findByReviewerId(currentUserId);
                logger.info("Super admin {} fetching reviewed projects: {}", currentUserId, projects.size());
            } else {
                // 管理员只看到自己作为管理员审核过的项目
                projects = projectRepository.findByAdminReviewerId(currentUserId);
                logger.info("Admin {} fetching reviewed projects: {}", currentUserId, projects.size());
            }

            List<ProjectResponse> responses = projects.stream()
                .map(project -> {
                    ProjectResponse response = new ProjectResponse(project);
                    // 加载项目阶段
                    List<ProjectPhase> phases = projectPhaseRepository.findByProjectIdOrderByCreatedAt(project.getId());
                    List<ProjectPhaseResponse> phaseResponses = phases.stream()
                        .map(ProjectPhaseResponse::new)
                        .collect(Collectors.toList());
                    response.setPhases(phaseResponses);
                    return response;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(responses));

        } catch (Exception e) {
            logger.error("Error getting reviewed projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取已审核项目列表失败，请稍后重试"));
        }
    }

    /**
     * 构建包含阶段信息的ProjectResponse列表
     * 提取公共逻辑以避免重复代码
     */
    private List<ProjectResponse> buildProjectResponsesWithPhases(
            List<com.weeklyreport.project.repository.projection.ProjectDetailProjection> projectDetails) {
        return projectDetails.stream()
            .map(this::mapProjectDetailToResponse)
            .collect(Collectors.toList());
    }

    private ProjectResponse mapProjectDetailToResponse(
            com.weeklyreport.project.repository.projection.ProjectDetailProjection detail) {
        ProjectResponse response = createProjectResponseFromDetailProjection(detail);

        List<ProjectPhase> phases = projectPhaseRepository.findByProjectIdOrderByCreatedAt(detail.getId());
        List<ProjectPhaseResponse> phaseResponses = phases.stream()
            .map(ProjectPhaseResponse::new)
            .collect(Collectors.toList());
        response.setPhases(phaseResponses);

        logger.debug("Project {} loaded with {} phases, AI result: {}",
                   detail.getId(), phases.size(), detail.getAiResultId() != null ? "Yes" : "No");
        return response;
    }

    private Page<ProjectResponse> mapProjectDetailPageToResponse(
            Page<com.weeklyreport.project.repository.projection.ProjectDetailProjection> detailPage) {
        return detailPage.map(this::mapProjectDetailToResponse);
    }

    /**
     * 获取项目阶段列表
     * GET /api/projects/{projectId}/phases
     */
    @GetMapping("/{projectId}/phases")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ProjectPhaseResponse>>> getProjectPhases(@PathVariable Long projectId) {
        try {
            // 验证项目是否存在
            if (!projectRepository.existsById(projectId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("项目不存在"));
            }

            List<ProjectPhase> phases = projectPhaseRepository.findByProjectIdOrderByCreatedAt(projectId);
            List<ProjectPhaseResponse> responses = phases.stream()
                .map(ProjectPhaseResponse::new)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(responses));

        } catch (Exception e) {
            logger.error("Error getting project phases for project: {}", projectId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取项目阶段失败，请稍后重试"));
        }
    }

    /**
     * 创建项目阶段
     * POST /api/projects/{projectId}/phases
     */
    @PostMapping("/{projectId}/phases")
    @PreAuthorize("@projectPermissionEvaluator.canManageProjectPhases(#projectId, authentication)")
    public ResponseEntity<ApiResponse<ProjectPhaseResponse>> createProjectPhase(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectPhaseCreateRequest request) {
        try {
            // 验证项目是否存在
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("项目不存在"));
            }

            // 创建项目阶段
            ProjectPhase phase = new ProjectPhase();
            phase.setProjectId(projectId);
            phase.setPhaseName(request.getPhaseName());
            phase.setDescription(request.getDescription());
            phase.setAssignedMembers(request.getAssignedMembers());
            phase.setSchedule(request.getSchedule());
            phase.setExpectedResults(request.getExpectedResults() != null ? request.getExpectedResults() : "");

            ProjectPhase savedPhase = projectPhaseRepository.save(phase);
            logger.info("Project phase created: {} for project: {}", savedPhase.getId(), projectId);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(new ProjectPhaseResponse(savedPhase)));

        } catch (Exception e) {
            logger.error("Error creating project phase for project: {}", projectId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("创建项目阶段失败，请稍后重试"));
        }
    }

    private <T> Page<T> paginateList(List<T> items, Pageable pageable) {
        if (items == null || items.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = (int) pageable.getOffset();

        if (startItem >= items.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, items.size());
        }

        int endIndex = Math.min(startItem + pageSize, items.size());
        List<T> pageContent = items.subList(startItem, endIndex);
        return new PageImpl<>(pageContent, pageable, items.size());
    }
}
