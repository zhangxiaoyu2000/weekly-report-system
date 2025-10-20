package com.weeklyreport.comment.service;

import com.weeklyreport.comment.dto.*;
import com.weeklyreport.comment.entity.WeeklyReportComment;
import com.weeklyreport.comment.entity.WeeklyReportComment.CommentType;
import com.weeklyreport.comment.repository.WeeklyReportCommentRepository;
import com.weeklyreport.core.exception.AuthenticationException;
import com.weeklyreport.user.entity.User;
import com.weeklyreport.user.repository.UserRepository;
import com.weeklyreport.weeklyreport.entity.WeeklyReport;
import com.weeklyreport.weeklyreport.repository.WeeklyReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 周报评论服务类
 */
@Service
@Transactional
public class WeeklyReportCommentService {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyReportCommentService.class);

    private final WeeklyReportCommentRepository commentRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final UserRepository userRepository;

    @Autowired
    public WeeklyReportCommentService(WeeklyReportCommentRepository commentRepository,
                                     WeeklyReportRepository weeklyReportRepository,
                                     UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.weeklyReportRepository = weeklyReportRepository;
        this.userRepository = userRepository;
    }

    /**
     * 创建评论
     */
    public CommentResponse createComment(CommentCreateRequest request, Long currentUserId) {
        logger.info("Creating comment for weekly report {} by user {}", request.getWeeklyReportId(), currentUserId);

        // 1. 验证权限：检查用户是否可以对该周报进行评论
        validateCommentPermission(request.getWeeklyReportId(), currentUserId);

        // 2. 验证周报状态：只有审核通过的周报才能评论
        WeeklyReport weeklyReport = weeklyReportRepository.findById(request.getWeeklyReportId())
            .orElseThrow(() -> new EntityNotFoundException("周报不存在"));

        // 只允许对APPROVED状态的周报评论
        if (weeklyReport.getStatus() != WeeklyReport.ReportStatus.APPROVED) {
            throw new IllegalStateException("只能对审核通过的周报进行评论");
        }

        // 3. 创建评论
        WeeklyReportComment comment = new WeeklyReportComment();
        comment.setWeeklyReportId(request.getWeeklyReportId());
        comment.setUserId(currentUserId);
        comment.setContent(request.getContent());

        // 4. 判断是评论还是回复
        if (request.getParentCommentId() != null) {
            WeeklyReportComment parentComment = commentRepository.findById(request.getParentCommentId())
                .orElseThrow(() -> new EntityNotFoundException("父评论不存在"));
            comment.setParentCommentId(parentComment.getId());
            comment.setCommentType(CommentType.REPLY);
        } else {
            comment.setCommentType(CommentType.COMMENT);
        }

        comment = commentRepository.save(comment);
        logger.info("Comment created successfully with ID: {}", comment.getId());
        
        return convertToResponse(comment);
    }

    /**
     * 获取周报的评论列表（分页）
     */
    @Transactional(readOnly = true)
    public CommentListResponse getCommentsByWeeklyReportId(Long weeklyReportId, int page, int size, Long currentUserId) {
        logger.info("Getting comments for weekly report {} by user {}", weeklyReportId, currentUserId);

        // 验证访问权限
        validateAccessPermission(weeklyReportId, currentUserId);

        Pageable pageable = PageRequest.of(page, size);
        Page<WeeklyReportComment> topLevelComments = commentRepository.findTopLevelCommentsWithUser(weeklyReportId, pageable);

        List<CommentResponse> commentResponses = topLevelComments.getContent().stream()
            .map(this::convertToResponseWithReplies)
            .collect(Collectors.toList());

        CommentListResponse response = new CommentListResponse();
        response.setComments(commentResponses);
        response.setTotalCount((int) topLevelComments.getTotalElements());
        response.setPageNum(page);
        response.setPageSize(size);
        response.setHasMore(topLevelComments.hasNext());

        logger.info("Retrieved {} comments for weekly report {}", commentResponses.size(), weeklyReportId);
        return response;
    }

    /**
     * 更新评论
     */
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest request, Long currentUserId) {
        logger.info("Updating comment {} by user {}", commentId, currentUserId);

        WeeklyReportComment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("评论不存在"));

        // 只有评论作者或超级管理员可以修改评论
        if (!comment.getUserId().equals(currentUserId) && !isCurrentUserSuperAdmin(currentUserId)) {
            throw new AccessDeniedException("无权限修改此评论");
        }

        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);

        logger.info("Comment {} updated successfully", commentId);
        return convertToResponse(comment);
    }

    /**
     * 删除评论（软删除）
     */
    public void deleteComment(Long commentId, Long currentUserId) {
        logger.info("Deleting comment {} by user {}", commentId, currentUserId);

        WeeklyReportComment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("评论不存在"));

        // 只有评论作者或超级管理员可以删除评论
        if (!comment.getUserId().equals(currentUserId) && !isCurrentUserSuperAdmin(currentUserId)) {
            throw new AccessDeniedException("无权限删除此评论");
        }

        int deletedCount = commentRepository.softDeleteComment(commentId);
        if (deletedCount == 0) {
            throw new EntityNotFoundException("评论不存在或已被删除");
        }

        logger.info("Comment {} deleted successfully", commentId);
    }

    /**
     * 获取评论的回复列表
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getRepliesByCommentId(Long commentId, Long currentUserId) {
        logger.info("Getting replies for comment {} by user {}", commentId, currentUserId);

        // 先验证评论是否存在
        WeeklyReportComment parentComment = commentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("评论不存在"));

        // 验证访问权限
        validateAccessPermission(parentComment.getWeeklyReportId(), currentUserId);

        List<WeeklyReportComment> replies = commentRepository.findRepliesWithUser(commentId);
        List<CommentResponse> replyResponses = replies.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());

        logger.info("Retrieved {} replies for comment {}", replyResponses.size(), commentId);
        return replyResponses;
    }

    /**
     * 权限验证：检查用户是否可以对该周报进行评论
     * 评论权限规则：
     * - SUPER_ADMIN: 可评论所有周报
     * - MANAGER: 只能评论自己提交的周报
     * - ADMIN: 无评论权限
     */
    private void validateCommentPermission(Long weeklyReportId, Long currentUserId) {
        // 验证用户存在
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        WeeklyReport weeklyReport = weeklyReportRepository.findById(weeklyReportId)
            .orElseThrow(() -> new EntityNotFoundException("周报不存在"));

        // 超级管理员可以评论任何周报
        if (currentUser.isSuperAdmin()) {
            logger.info("✅ SUPER_ADMIN {} 可评论周报 {}", currentUser.getUsername(), weeklyReportId);
            return;
        }

        // MANAGER只能评论自己提交的周报
        if (currentUser.getRole() == User.Role.MANAGER) {
            if (weeklyReport.getUserId().equals(currentUserId)) {
                logger.info("✅ MANAGER {} 可评论自己的周报 {}", currentUser.getUsername(), weeklyReportId);
                return;
            } else {
                throw new AccessDeniedException("只能评论自己提交的周报");
            }
        }

        // ADMIN和其他角色无评论权限
        throw new AccessDeniedException("无权限对此周报进行评论");
    }

    /**
     * 权限验证：检查用户是否可以查看评论
     */
    private void validateAccessPermission(Long weeklyReportId, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        WeeklyReport weeklyReport = weeklyReportRepository.findById(weeklyReportId)
            .orElseThrow(() -> new EntityNotFoundException("周报不存在"));

        // 超级管理员、管理员、周报作者可以查看
        if (currentUser.isSuperAdmin() || currentUser.isAdmin() || 
            weeklyReport.getUserId().equals(currentUserId)) {
            return;
        }

        // 其他管理员级别用户也可以查看
        if (currentUser.getRole() == User.Role.MANAGER) {
            return;
        }

        throw new AccessDeniedException("无权限查看此周报的评论");
    }

    /**
     * 检查当前用户是否为超级管理员
     */
    private boolean isCurrentUserSuperAdmin(Long userId) {
        return userRepository.findById(userId)
            .map(User::isSuperAdmin)
            .orElse(false);
    }

    /**
     * 转换实体为响应DTO
     */
    private CommentResponse convertToResponse(WeeklyReportComment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setWeeklyReportId(comment.getWeeklyReportId());
        response.setUserId(comment.getUserId());
        response.setParentCommentId(comment.getParentCommentId());
        response.setContent(comment.getContent());
        response.setCommentType(comment.getCommentType());
        response.setStatus(comment.getStatus());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());

        // 设置用户信息（如果已加载）
        if (comment.getUser() != null) {
            response.setUsername(comment.getUser().getUsername());
            response.setFullName(comment.getUser().getFullName());
            response.setUserRole(comment.getUser().getRole());
        } else {
            // 如果用户信息未加载，可以单独查询
            userRepository.findById(comment.getUserId()).ifPresent(user -> {
                response.setUsername(user.getUsername());
                response.setFullName(user.getFullName());
                response.setUserRole(user.getRole());
            });
        }

        return response;
    }

    /**
     * 转换实体为响应DTO（包含回复）
     */
    private CommentResponse convertToResponseWithReplies(WeeklyReportComment comment) {
        CommentResponse response = convertToResponse(comment);

        // 获取回复
        List<WeeklyReportComment> replies = commentRepository.findRepliesWithUser(comment.getId());
        List<CommentResponse> replyResponses = replies.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());

        response.setReplies(replyResponses);
        response.setReplyCount(replyResponses.size());

        return response;
    }
}