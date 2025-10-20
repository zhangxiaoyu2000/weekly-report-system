package com.weeklyreport.comment.controller;

import com.weeklyreport.comment.dto.*;
import com.weeklyreport.comment.service.WeeklyReportCommentService;
import com.weeklyreport.common.dto.ApiResponse;
import com.weeklyreport.core.exception.AuthenticationException;
import com.weeklyreport.core.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 周报评论控制器
 */
@RestController
@RequestMapping("")
@Tag(name = "Weekly Report Comments", description = "周报评论管理API")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    private final WeeklyReportCommentService commentService;

    @Autowired
    public CommentController(WeeklyReportCommentService commentService) {
        this.commentService = commentService;
        logger.info("🏗️ CommentController successfully created and injected!");
    }

    /**
     * 获取周报的评论列表
     */
    @GetMapping("/weekly-reports/{weeklyReportId}/comments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "获取周报评论列表", description = "分页获取指定周报的所有评论和回复")
    public ResponseEntity<ApiResponse<CommentListResponse>> getComments(
            @Parameter(description = "周报ID") @PathVariable Long weeklyReportId,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        logger.info("🎯 getComments method called! WeeklyReportId: {}, page: {}, size: {}", weeklyReportId, page, size);

        Long currentUserId = getCurrentUserId(authentication);
        CommentListResponse response = commentService.getCommentsByWeeklyReportId(weeklyReportId, page, size, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 创建评论
     */
    @PostMapping("/weekly-reports/{weeklyReportId}/comments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "创建评论", description = "为指定周报创建评论")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Parameter(description = "周报ID") @PathVariable Long weeklyReportId,
            @Valid @RequestBody CommentCreateRequest request,
            Authentication authentication) {

        logger.info("🎯 createComment method called! WeeklyReportId: {}", weeklyReportId);

        // 确保请求中的周报ID与路径参数一致
        request.setWeeklyReportId(weeklyReportId);

        Long currentUserId = getCurrentUserId(authentication);
        CommentResponse response = commentService.createComment(request, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 获取当前用户ID的辅助方法
     */
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal) {
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
            return userPrincipal.getId();
        }
        throw new AuthenticationException.InvalidTokenException("用户未认证");
    }

    /**
     * 异常处理
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException e) {
        logger.error("Entity not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        logger.error("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException e) {
        logger.error("Authentication error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(e.getMessage()));
    }
}