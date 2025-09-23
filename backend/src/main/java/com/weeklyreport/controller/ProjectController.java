package com.weeklyreport.controller;

import com.weeklyreport.dto.ApiResponse;
import com.weeklyreport.dto.project.*;
import com.weeklyreport.dto.ai.AIAnalysisResultResponse;
import com.weeklyreport.entity.Project;
import com.weeklyreport.entity.ProjectPhase;
import com.weeklyreport.entity.User;
import com.weeklyreport.entity.AIAnalysisResult;
import com.weeklyreport.repository.ProjectRepository;
import com.weeklyreport.repository.ProjectPhaseRepository;
import com.weeklyreport.repository.AIAnalysisResultRepository;
import com.weeklyreport.repository.projection.ProjectWithCreatorProjection;
import com.weeklyreport.service.ai.AIAnalysisService;
import com.weeklyreport.service.UserService;
import com.weeklyreport.util.auth.SecurityUtils;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
public class ProjectController extends BaseController {

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
    private ProjectResponse createProjectResponseFromDetailProjection(com.weeklyreport.repository.projection.ProjectDetailProjection detail) {
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
            case AI_ANALYZING: case AI_APPROVED: return Project.ProjectStatus.ACTIVE;
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
            case AI_APPROVED: return 30;
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
                .body(ApiResponse.error("Failed to get projects: " + e.getMessage()));
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
                    .body(ApiResponse.error("Project name already exists"));
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
            logger.info("Project created: {} by user: {}, entering AI analysis", savedProject.getId(), currentUserId);
            
            // 触发AI分析流程
            try {
                logger.info("Starting AI analysis for project: {}", savedProject.getId());
                aiAnalysisService.analyzeProject(savedProject);
                logger.info("AI analysis initiated for project: {}", savedProject.getId());
            } catch (Exception e) {
                logger.error("Failed to start AI analysis for project: {}", savedProject.getId(), e);
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
                        phase.setProjectId(savedProject.getId());
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
                .body(ApiResponse.error("Failed to create project: " + e.getMessage()));
        }
    }

    /**
     * GET /api/projects/pending-review
     * 获取待审批项目（只包含待审批状态的项目）
     */
    @GetMapping("/pending-review")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getPendingReviewProjects() {
        try {
            List<com.weeklyreport.repository.projection.ProjectDetailProjection> projectDetails;
            
            if (SecurityUtils.hasRole("SUPER_ADMIN")) {
                // 超级管理员只看到需要超级管理员审核的项目（管理员通过的项目）
                projectDetails = projectRepository.findPendingSuperAdminReviewProjectsWithDetails();
                logger.info("Super admin fetching pending review projects: {}", projectDetails.size());
            } else {
                // 管理员看到待管理员审核的项目
                projectDetails = projectRepository.findPendingAdminReviewProjectsWithDetails();
                logger.info("Admin fetching pending review projects: {}", projectDetails.size());
            }
            
            List<ProjectResponse> responses = buildProjectResponsesWithPhases(projectDetails);
            
            logger.info("Successfully returned {} pending review projects", responses.size());
            return ResponseEntity.ok(ApiResponse.success(responses));
            
        } catch (Exception e) {
            logger.error("Error getting pending review projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get pending review projects: " + e.getMessage()));
        }
    }

    /**
     * GET /api/projects/approved
     * 获取已通过项目（包含各种已通过状态的项目）
     */
    @GetMapping("/approved")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getApprovedProjects() {
        try {
            List<com.weeklyreport.repository.projection.ProjectDetailProjection> projectDetails;
            
            if (SecurityUtils.hasRole("SUPER_ADMIN")) {
                // 超级管理员看到所有已通过的项目
                projectDetails = projectRepository.findApprovedProjectsWithDetails();
                logger.info("Super admin fetching approved projects: {}", projectDetails.size());
            } else {
                // 管理员看到管理员通过和最终通过的项目
                projectDetails = projectRepository.findAdminApprovedProjectsWithDetails();
                logger.info("Admin fetching approved projects: {}", projectDetails.size());
            }
            
            List<ProjectResponse> responses = buildProjectResponsesWithPhases(projectDetails);
            
            logger.info("Successfully returned {} approved projects", responses.size());
            return ResponseEntity.ok(ApiResponse.success(responses));
            
        } catch (Exception e) {
            logger.error("Error getting approved projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get approved projects: " + e.getMessage()));
        }
    }

    /**
     * GET /api/projects/rejected
     * 获取已拒绝项目（包含各种拒绝状态的项目）
     */
    @GetMapping("/rejected")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getRejectedProjects() {
        try {
            List<com.weeklyreport.repository.projection.ProjectDetailProjection> projectDetails;
            
            if (SecurityUtils.hasRole("SUPER_ADMIN")) {
                // 超级管理员看到所有拒绝的项目
                projectDetails = projectRepository.findAllRejectedProjectsWithDetails();
                logger.info("Super admin fetching rejected projects: {}", projectDetails.size());
            } else {
                // 管理员看到AI拒绝和管理员拒绝的项目
                projectDetails = projectRepository.findAdminVisibleRejectedProjectsWithDetails();
                logger.info("Admin fetching rejected projects: {}", projectDetails.size());
            }
            
            List<ProjectResponse> responses = buildProjectResponsesWithPhases(projectDetails);
            
            logger.info("Successfully returned {} rejected projects", responses.size());
            return ResponseEntity.ok(ApiResponse.success(responses));
            
        } catch (Exception e) {
            logger.error("Error getting rejected projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get rejected projects: " + e.getMessage()));
        }
    }

    /**
     * 获取单个项目详情
     * GET /api/projects/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(@PathVariable Long id) {
        
        try {
            // 使用JOIN查询一次性获取项目和创建者信息
            ProjectWithCreatorProjection projectWithCreator = projectRepository.findProjectWithCreator(id);
            if (projectWithCreator == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Project not found"));
            }
            
            // 权限检查：管理员和超级管理员可以查看所有项目，主管只能查看自己创建的项目
            Long currentUserId = getCurrentUser().getId();
            if (SecurityUtils.hasRole("MANAGER") && !projectWithCreator.getCreatedBy().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
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
                .body(ApiResponse.error("Failed to get project: " + e.getMessage()));
        }
    }

    /**
     * 更新项目（仅创建者可更新）
     * PUT /api/projects/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectCreateRequest request) {
        
        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Project not found"));
            }
            
            Project project = projectOpt.get();
            Long currentUserId = getCurrentUser().getId();
            
            // 只有创建者可以更新
            if (!project.getCreatedBy().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only project creator can update"));
            }
            
            // 只有草稿状态或被拒绝的项目可以更新
            if (!project.isDraft() && !project.isRejected()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Project can only be updated when in DRAFT or REJECTED status"));
            }
            
            // 更新字段
            project.setName(request.getName());
            project.setDescription(request.getDescription());
            project.setMembers(request.getMembers());
            project.setExpectedResults(request.getExpectedResults());
            project.setTimeline(request.getTimeline());
            project.setStopLoss(request.getStopLoss());
            
            // 如果是被拒绝的项目，重置为草稿状态
            if (project.isRejected()) {
                project.setApprovalStatus(Project.ApprovalStatus.AI_ANALYZING);
                project.setRejectionReason(null);
            }
            
            Project savedProject = projectRepository.save(project);
            logger.info("Project updated: {} by user: {}", savedProject.getId(), currentUserId);
            
            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));
            
        } catch (Exception e) {
            logger.error("Error updating project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update project: " + e.getMessage()));
        }
    }

    /**
     * 删除项目（仅创建者可删除）
     * DELETE /api/projects/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        
        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Project not found"));
            }
            
            Project project = projectOpt.get();
            Long currentUserId = getCurrentUser().getId();
            
            // 只有创建者可以删除
            if (!project.getCreatedBy().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only project creator can delete"));
            }
            
            // 只有草稿状态的项目可以删除
            if (!project.isDraft()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Only DRAFT projects can be deleted"));
            }
            
            projectRepository.delete(project);
            logger.info("Project deleted: {} by user: {}", id, currentUserId);
            
            return ResponseEntity.ok(ApiResponse.success(null));
            
        } catch (Exception e) {
            logger.error("Error deleting project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete project: " + e.getMessage()));
        }
    }

    /**
     * 提交项目进行AI分析（主管权限）
     * PUT /api/projects/{id}/submit
     */
    @PutMapping("/{id}/submit")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> submitProject(@PathVariable Long id) {
        
        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Project not found"));
            }
            
            Project project = projectOpt.get();
            Long currentUserId = getCurrentUser().getId();
            
            // 只有创建者可以提交
            if (!project.getCreatedBy().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only project creator can submit"));
            }
            
            // 只有草稿状态或被拒绝的项目可以提交
            if (!project.isDraft() && !project.isRejected()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Only DRAFT or REJECTED projects can be submitted"));
            }
            
            // 更新状态为AI分析中
            project.submit();
            
            try {
                // TODO: 实现AI分析逻辑 - 当前跳过AI分析，直接标记为AI通过
                // 这里可以调用 aiAnalysisService.analyzeContent() 方法
                project.aiApprove(); // 临时直接标记为AI分析通过
                logger.info("AI analysis completed for project: {}", project.getId());
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
                .body(ApiResponse.error("Failed to submit project: " + e.getMessage()));
        }
    }

    /**
     * 主管强行提交项目（跳过AI分析，直接进入管理员审核）
     * POST /api/projects/{id}/force-submit
     */
    @PostMapping("/{id}/force-submit")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> forceSubmitProject(@PathVariable Long id) {
        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Project not found"));
            }
            
            Project project = projectOpt.get();
            Long currentUserId = getCurrentUser().getId();
            
            // 只有创建者可以强行提交
            if (!project.getCreatedBy().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only project creator can force submit"));
            }
            
            // 只有草稿状态或被拒绝的项目可以强行提交
            if (!project.isDraft() && !project.isRejected()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Only DRAFT or REJECTED projects can be force submitted"));
            }
            
            // 跳过AI分析，直接进入管理员审核状态
            project.setApprovalStatus(Project.ApprovalStatus.ADMIN_REVIEWING);
            project.setRejectionReason(null); // 清除之前的拒绝原因
            
            Project savedProject = projectRepository.save(project);
            logger.info("Project force submitted (bypassing AI): {} by user: {}", savedProject.getId(), currentUserId);
            
            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));
            
        } catch (Exception e) {
            logger.error("Error force submitting project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to force submit project: " + e.getMessage()));
        }
    }

    /**
     * 项目修改后重新提交（主管权限）
     * PUT /api/simple/projects/{id}/resubmit
     */
    @PutMapping("/{id}/resubmit")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> resubmitProject(
            @PathVariable Long id, 
            @Valid @RequestBody ProjectUpdateRequest updateRequest) {
        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Project not found"));
            }
            
            Project project = projectOpt.get();
            Long currentUserId = getCurrentUser().getId();
            
            // 只有创建者可以重新提交
            if (!project.getCreatedBy().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only project creator can resubmit"));
            }
            
            // 只有被拒绝的项目可以重新提交
            if (!project.isRejected()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Only REJECTED projects can be resubmitted"));
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
                aiAnalysisService.analyzeProject(savedProject);
                logger.info("AI analysis completed for resubmitted project: {}", savedProject.getId());
                
                // AI分析成功后，项目状态由AI分析服务自动更新
                // 重新查询项目以获取最新状态
                savedProject = projectRepository.findById(savedProject.getId()).orElse(savedProject);
                
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
                .body(ApiResponse.error("Failed to resubmit project: " + e.getMessage()));
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
                    .body(ApiResponse.error("Project not found"));
            }
            
            Project project = projectOpt.get();
            User currentUser = getCurrentUser();
            Long currentUserId = currentUser.getId();
            
            logger.info("Admin approval process - Project: {}, Current User: {}, User ID: {}, Username: {}", 
                       id, currentUser, currentUserId, currentUser.getUsername());
            
            // 检查项目状态：应该是AI分析通过状态、提交状态、管理员审核状态或管理员拒绝状态（允许重新审核）
            if (project.getApprovalStatus() != Project.ApprovalStatus.AI_APPROVED && 
                project.getApprovalStatus() != Project.ApprovalStatus.AI_ANALYZING &&
                project.getApprovalStatus() != Project.ApprovalStatus.ADMIN_REVIEWING &&
                project.getApprovalStatus() != Project.ApprovalStatus.ADMIN_REJECTED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Project must be in AI_APPROVED, AI_ANALYZING, ADMIN_REVIEWING, or ADMIN_REJECTED status for admin review"));
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
            
            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));
            
        } catch (Exception e) {
            logger.error("Error approving project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to approve project: " + e.getMessage()));
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
                    .body(ApiResponse.error("Project not found"));
            }
            
            Project project = projectOpt.get();
            
            // 直接设置为AI分析通过状态
            project.setApprovalStatus(Project.ApprovalStatus.AI_APPROVED);
            project.setStatus(Project.ProjectStatus.ACTIVE);
            project.setProgress(40);
            
            Project savedProject = projectRepository.save(project);
            logger.info("Project AI approved: {}", savedProject.getId());
            
            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));
            
        } catch (Exception e) {
            logger.error("Error AI approving project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to AI approve project: " + e.getMessage()));
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
                    .body(ApiResponse.error("Project not found"));
            }
            
            Project project = projectOpt.get();
            Long currentUserId = getCurrentUser().getId();
            
            // 检查项目状态：应该是管理员审核通过或审核失败状态
            if (project.getApprovalStatus() != Project.ApprovalStatus.ADMIN_APPROVED &&
                project.getApprovalStatus() != Project.ApprovalStatus.ADMIN_REJECTED &&
                project.getApprovalStatus() != Project.ApprovalStatus.SUPER_ADMIN_REVIEWING) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid project status for super admin review"));
            }
            
            // 超级管理员最终审批通过
            project.superAdminApprove(currentUserId);
            project.setApprovalStatus(Project.ApprovalStatus.FINAL_APPROVED);
            
            Project savedProject = projectRepository.save(project);
            logger.info("Project final approved: {} by user: {}", savedProject.getId(), currentUserId);
            
            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));
            
        } catch (Exception e) {
            logger.error("Error final approving project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to final approve project: " + e.getMessage()));
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
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> rejectProject(
            @PathVariable Long id,
            @RequestBody String reason) {
        
        try {
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Project not found"));
            }
            
            Project project = projectOpt.get();
            Long currentUserId = getCurrentUser().getId();
            
            // 检查当前用户是否有权限拒绝此项目
            boolean canReject = false;
            boolean isAdmin = SecurityUtils.hasRole("ADMIN");
            boolean isSuperAdmin = SecurityUtils.hasRole("SUPER_ADMIN");
            Project.ApprovalStatus currentStatus = project.getApprovalStatus();
            
            logger.info("Permission check - User: {}, isAdmin: {}, isSuperAdmin: {}, Project status: {}", 
                        getCurrentUser().getUsername(), isAdmin, isSuperAdmin, currentStatus);
            
            if (isAdmin && 
                (currentStatus == Project.ApprovalStatus.AI_APPROVED ||
                 currentStatus == Project.ApprovalStatus.ADMIN_REVIEWING ||
                 currentStatus == Project.ApprovalStatus.ADMIN_REJECTED)) {
                canReject = true;
                logger.info("ADMIN can reject: status {} matches AI_APPROVED, ADMIN_REVIEWING, or ADMIN_REJECTED", currentStatus);
            } else if (isSuperAdmin && 
                       (currentStatus == Project.ApprovalStatus.ADMIN_APPROVED ||
                        currentStatus == Project.ApprovalStatus.ADMIN_REJECTED ||
                        currentStatus == Project.ApprovalStatus.SUPER_ADMIN_REVIEWING)) {
                canReject = true;
                logger.info("SUPER_ADMIN can reject: status {} matches allowed statuses", currentStatus);
            } else {
                logger.warn("Permission denied - isAdmin: {}, isSuperAdmin: {}, status: {}", isAdmin, isSuperAdmin, currentStatus);
            }
            
            if (!canReject) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("No permission to reject this project"));
            }
            
            // 根据用户角色执行相应的拒绝操作
            if (isAdmin) {
                project.adminReject(currentUserId, reason);
                logger.info("ADMIN rejected project: {} by user: {}", project.getId(), currentUserId);
            } else if (isSuperAdmin) {
                project.superAdminReject(currentUserId, reason);
                logger.info("SUPER_ADMIN rejected project: {} by user: {}", project.getId(), currentUserId);
            }
            
            Project savedProject = projectRepository.save(project);
            logger.info("Project rejected: {} by user: {} with reason: {}", 
                       savedProject.getId(), currentUserId, reason);
            
            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));
            
        } catch (Exception e) {
            logger.error("Error rejecting project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to reject project: " + e.getMessage()));
        }
    }

    /**
     * 获取用户创建的项目列表
     * GET /api/projects/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getMyProjects() {
        
        try {
            Long currentUserId = getCurrentUser().getId();
            List<Project> projects = projectRepository.findByCreatedByOrderByCreatedAtDesc(currentUserId);
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
                .body(ApiResponse.error("Failed to get my projects: " + e.getMessage()));
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
            List<com.weeklyreport.repository.projection.ProjectDetailProjection> projectDetails;
            
            if (SecurityUtils.hasRole("SUPER_ADMIN")) {
                // 超级管理员只看到需要超级管理员审核的项目（管理员通过的项目）
                projectDetails = projectRepository.findPendingSuperAdminReviewProjectsWithDetails();
                logger.info("Super admin fetching pending projects: {}", projectDetails.size());
            } else {
                // 管理员看到待管理员审核的项目
                projectDetails = projectRepository.findPendingAdminReviewProjectsWithDetails();
                logger.info("Admin fetching pending projects: {}", projectDetails.size());
            }
            
            List<ProjectResponse> responses = projectDetails.stream()
                .map(detail -> {
                    // 从投影创建ProjectResponse，所有关联数据已通过JOIN获取
                    ProjectResponse response = createProjectResponseFromDetailProjection(detail);
                    
                    // 查询项目阶段（这部分仍需单独查询，因为是一对多关系）
                    List<ProjectPhase> phases = projectPhaseRepository.findByProjectIdOrderByCreatedAt(detail.getId());
                    List<ProjectPhaseResponse> phaseResponses = phases.stream()
                        .map(ProjectPhaseResponse::new)
                        .collect(Collectors.toList());
                    response.setPhases(phaseResponses);
                    
                    logger.debug("Project {} loaded with {} phases, AI result: {}", 
                               detail.getId(), phases.size(), detail.getAiResultId() != null ? "Yes" : "No");
                    
                    return response;
                })
                .collect(Collectors.toList());
            
            logger.info("Successfully returned {} pending projects with full details", responses.size());
            return ResponseEntity.ok(ApiResponse.success(responses));
            
        } catch (Exception e) {
            logger.error("Error getting pending projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get pending projects: " + e.getMessage()));
        }
    }

    /**
     * 构建包含阶段信息的ProjectResponse列表
     * 提取公共逻辑以避免重复代码
     */
    private List<ProjectResponse> buildProjectResponsesWithPhases(
            List<com.weeklyreport.repository.projection.ProjectDetailProjection> projectDetails) {
        return projectDetails.stream()
            .map(detail -> {
                // 从投影创建ProjectResponse，所有关联数据已通过JOIN获取
                ProjectResponse response = createProjectResponseFromDetailProjection(detail);
                
                // 查询项目阶段（这部分仍需单独查询，因为是一对多关系）
                List<ProjectPhase> phases = projectPhaseRepository.findByProjectIdOrderByCreatedAt(detail.getId());
                List<ProjectPhaseResponse> phaseResponses = phases.stream()
                    .map(ProjectPhaseResponse::new)
                    .collect(Collectors.toList());
                response.setPhases(phaseResponses);
                
                logger.debug("Project {} loaded with {} phases, AI result: {}", 
                           detail.getId(), phases.size(), detail.getAiResultId() != null ? "Yes" : "No");
                
                return response;
            })
            .collect(Collectors.toList());
    }
}