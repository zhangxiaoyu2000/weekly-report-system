package com.weeklyreport.comment.repository;

import com.weeklyreport.comment.entity.WeeklyReportComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 周报评论数据访问层
 */
@Repository
public interface WeeklyReportCommentRepository extends JpaRepository<WeeklyReportComment, Long> {

    // 根据周报ID查询所有评论（按时间排序）
    @Query("SELECT c FROM WeeklyReportComment c WHERE c.weeklyReportId = :weeklyReportId AND c.status = 'ACTIVE' ORDER BY c.createdAt ASC")
    List<WeeklyReportComment> findByWeeklyReportIdAndStatusActive(@Param("weeklyReportId") Long weeklyReportId);

    // 根据周报ID分页查询顶级评论（不包含回复）
    @Query("SELECT c FROM WeeklyReportComment c WHERE c.weeklyReportId = :weeklyReportId AND c.parentCommentId IS NULL AND c.status = 'ACTIVE' ORDER BY c.createdAt DESC")
    Page<WeeklyReportComment> findTopLevelCommentsByWeeklyReportId(@Param("weeklyReportId") Long weeklyReportId, Pageable pageable);

    // 根据父评论ID查询所有回复
    @Query("SELECT c FROM WeeklyReportComment c WHERE c.parentCommentId = :parentCommentId AND c.status = 'ACTIVE' ORDER BY c.createdAt ASC")
    List<WeeklyReportComment> findRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    // 统计周报的评论总数
    @Query("SELECT COUNT(c) FROM WeeklyReportComment c WHERE c.weeklyReportId = :weeklyReportId AND c.status = 'ACTIVE'")
    long countByWeeklyReportIdAndStatusActive(@Param("weeklyReportId") Long weeklyReportId);

    // 统计评论的回复数量
    @Query("SELECT COUNT(c) FROM WeeklyReportComment c WHERE c.parentCommentId = :parentCommentId AND c.status = 'ACTIVE'")
    long countRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    // 根据用户ID查询其所有评论
    @Query("SELECT c FROM WeeklyReportComment c WHERE c.userId = :userId AND c.status = 'ACTIVE' ORDER BY c.createdAt DESC")
    Page<WeeklyReportComment> findByUserIdAndStatusActive(@Param("userId") Long userId, Pageable pageable);

    // 检查用户是否可以访问该评论（权限检查用）
    @Query("SELECT c FROM WeeklyReportComment c " +
           "JOIN WeeklyReport w ON c.weeklyReportId = w.id " +
           "WHERE c.id = :commentId AND (w.userId = :userId OR :isAdmin = true)")
    Optional<WeeklyReportComment> findCommentWithPermissionCheck(@Param("commentId") Long commentId, 
                                                                @Param("userId") Long userId, 
                                                                @Param("isAdmin") boolean isAdmin);

    // 删除评论（软删除）
    @Modifying
    @Query("UPDATE WeeklyReportComment c SET c.status = 'DELETED', c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = :commentId")
    int softDeleteComment(@Param("commentId") Long commentId);

    // 根据评论ID和用户ID查找评论（用于权限验证）
    @Query("SELECT c FROM WeeklyReportComment c WHERE c.id = :commentId AND c.userId = :userId AND c.status = 'ACTIVE'")
    Optional<WeeklyReportComment> findByIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    // 查找评论及其用户信息
    @Query("SELECT c FROM WeeklyReportComment c " +
           "LEFT JOIN FETCH c.user " +
           "WHERE c.id = :commentId AND c.status = 'ACTIVE'")
    Optional<WeeklyReportComment> findByIdWithUser(@Param("commentId") Long commentId);

    // 查找顶级评论及其用户信息（用于响应转换）
    @Query("SELECT c FROM WeeklyReportComment c " +
           "LEFT JOIN FETCH c.user " +
           "WHERE c.weeklyReportId = :weeklyReportId AND c.parentCommentId IS NULL AND c.status = 'ACTIVE' " +
           "ORDER BY c.createdAt DESC")
    Page<WeeklyReportComment> findTopLevelCommentsWithUser(@Param("weeklyReportId") Long weeklyReportId, Pageable pageable);

    // 查找回复及其用户信息
    @Query("SELECT c FROM WeeklyReportComment c " +
           "LEFT JOIN FETCH c.user " +
           "WHERE c.parentCommentId = :parentCommentId AND c.status = 'ACTIVE' " +
           "ORDER BY c.createdAt ASC")
    List<WeeklyReportComment> findRepliesWithUser(@Param("parentCommentId") Long parentCommentId);

    // 检查周报是否存在且已审核通过（只有APPROVED状态可评论）
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END " +
           "FROM WeeklyReport w " +
           "WHERE w.id = :weeklyReportId AND w.status = 'APPROVED'")
    boolean existsApprovedWeeklyReport(@Param("weeklyReportId") Long weeklyReportId);

    // 获取周报提交者ID
    @Query("SELECT w.userId FROM WeeklyReport w WHERE w.id = :weeklyReportId")
    Optional<Long> findWeeklyReportSubmitterId(@Param("weeklyReportId") Long weeklyReportId);
}