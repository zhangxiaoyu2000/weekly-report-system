package com.weeklyreport.controller;

import com.weeklyreport.dto.ApiResponse;
import com.weeklyreport.dto.project.ProjectResponse;
import com.weeklyreport.entity.Project;
import com.weeklyreport.entity.User;
import com.weeklyreport.repository.ProjectRepository;
import com.weeklyreport.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;

import java.util.Optional;

/**
 * Simple API endpoints for frontend compatibility
 */
@RestController
@RequestMapping("/api/simple")
@CrossOrigin(origins = "*")
public class SimpleController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SimpleController.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserService userService;

    @PostConstruct
    public void init() {
        logger.info("SimpleController initialized successfully!");
    }

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
     * 主管强行提交项目（跳过AI分析，直接进入管理员审核）
     * POST /api/simple/projects/{id}/force-submit
     */
    @PostMapping("/projects/{id}/force-submit")
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
            logger.info("Project force submitted via simple API (bypassing AI): {} by user: {}", savedProject.getId(), currentUserId);
            
            return ResponseEntity.ok(ApiResponse.success(new ProjectResponse(savedProject)));
            
        } catch (Exception e) {
            logger.error("Error force submitting project via simple API: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to force submit project: " + e.getMessage()));
        }
    }
}