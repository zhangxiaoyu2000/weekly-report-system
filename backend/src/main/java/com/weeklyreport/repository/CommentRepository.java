package com.weeklyreport.repository;

import com.weeklyreport.entity.Comment;
import com.weeklyreport.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Comment entity
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    // Find by weekly report
    List<Comment> findByWeeklyReportId(Long weeklyReportId);
    
    @Query("SELECT c FROM Comment c WHERE c.weeklyReport.id = :reportId ORDER BY c.createdAt ASC")
    List<Comment> findByWeeklyReportIdOrderByCreatedAt(@Param("reportId") Long reportId);
    
    @Query("SELECT c FROM Comment c WHERE c.weeklyReport.id = :reportId AND c.status = :status ORDER BY c.createdAt ASC")
    List<Comment> findByWeeklyReportIdAndStatus(@Param("reportId") Long reportId, 
                                               @Param("status") Comment.CommentStatus status);

    // Find by author
    List<Comment> findByAuthorId(Long authorId);
    
    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);
    
    List<Comment> findByAuthorIdAndStatus(Long authorId, Comment.CommentStatus status);

    // Find by type
    List<Comment> findByType(Comment.CommentType type);
    
    @Query("SELECT c FROM Comment c WHERE c.type = :type AND c.status = :status")
    List<Comment> findByTypeAndStatus(@Param("type") Comment.CommentType type, 
                                     @Param("status") Comment.CommentStatus status);

    // Find by status
    List<Comment> findByStatus(Comment.CommentStatus status);
    
    Page<Comment> findByStatus(Comment.CommentStatus status, Pageable pageable);

    // Find by priority
    List<Comment> findByPriorityGreaterThanEqual(Integer priority);
    
    @Query("SELECT c FROM Comment c WHERE c.priority >= :minPriority AND c.status = 'ACTIVE' ORDER BY c.priority DESC, c.createdAt ASC")
    List<Comment> findHighPriorityComments(@Param("minPriority") Integer priority);

    // Thread/Reply management
    List<Comment> findByParentIsNull(); // Root comments
    
    List<Comment> findByParentId(Long parentId);
    
    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);
    
    @Query("SELECT c FROM Comment c WHERE c.weeklyReport.id = :reportId AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findRootCommentsByReportId(@Param("reportId") Long reportId);

    // Find resolved/unresolved comments
    List<Comment> findByIsResolved(Boolean isResolved);
    
    @Query("SELECT c FROM Comment c WHERE c.isResolved = :isResolved AND c.status = 'ACTIVE'")
    List<Comment> findByIsResolvedAndActive(@Param("isResolved") Boolean isResolved);
    
    @Query("SELECT c FROM Comment c WHERE c.isResolved = false AND c.type IN ('QUESTION', 'CONCERN', 'REVISION') AND c.status = 'ACTIVE'")
    List<Comment> findUnresolvedActionableComments();

    // Find by resolver
    List<Comment> findByResolvedBy(User resolvedBy);
    
    @Query("SELECT c FROM Comment c WHERE c.resolvedBy = :resolvedBy AND c.resolvedAt >= :sinceDate")
    List<Comment> findResolvedByUserSince(@Param("resolvedBy") User resolvedBy, 
                                         @Param("sinceDate") LocalDateTime sinceDate);

    // Find by date ranges
    List<Comment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Comment> findByResolvedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT c FROM Comment c WHERE c.createdAt >= :sinceDate ORDER BY c.createdAt DESC")
    List<Comment> findRecentComments(@Param("sinceDate") LocalDateTime sinceDate);

    // Find private comments
    @Query("SELECT c FROM Comment c WHERE c.isPrivate = true AND c.author.id = :authorId")
    List<Comment> findPrivateCommentsByAuthor(@Param("authorId") Long authorId);
    
    @Query("SELECT c FROM Comment c WHERE c.weeklyReport.id = :reportId AND c.isPrivate = false AND c.status = 'ACTIVE' ORDER BY c.createdAt ASC")
    List<Comment> findPublicCommentsByReport(@Param("reportId") Long reportId);

    // Search methods
    @Query("SELECT c FROM Comment c WHERE " +
           "LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND " +
           "c.status = :status")
    Page<Comment> searchByKeywordAndStatus(@Param("keyword") String keyword, 
                                          @Param("status") Comment.CommentStatus status, 
                                          Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE " +
           "c.author.id = :authorId AND " +
           "LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Comment> searchByAuthorAndKeyword(@Param("authorId") Long authorId, 
                                          @Param("keyword") String keyword);

    // Find by tags
    @Query("SELECT c FROM Comment c WHERE c.tags LIKE CONCAT('%', :tag, '%')")
    List<Comment> findByTag(@Param("tag") String tag);
    
    @Query("SELECT DISTINCT c.tags FROM Comment c WHERE c.tags IS NOT NULL AND c.tags != ''")
    List<String> findAllTags();

    // Likes and engagement
    List<Comment> findByLikesCountGreaterThan(Integer likesCount);
    
    @Query("SELECT c FROM Comment c ORDER BY c.likesCount DESC")
    List<Comment> findMostLikedComments();

    // Statistics and counts
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.status = :status")
    long countByStatus(@Param("status") Comment.CommentStatus status);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.weeklyReport.id = :reportId")
    long countByWeeklyReportId(@Param("reportId") Long reportId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author.id = :authorId")
    long countByAuthorId(@Param("authorId") Long authorId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.type = :type")
    long countByType(@Param("type") Comment.CommentType type);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.isResolved = :isResolved")
    long countByResolved(@Param("isResolved") Boolean isResolved);

    // Department-based queries removed - not supported in simplified schema

    // Analytics queries
    @Query("SELECT c.type, COUNT(c) FROM Comment c WHERE c.status = 'ACTIVE' GROUP BY c.type")
    List<Object[]> countCommentsByType();
    
    @Query("SELECT c.weeklyReport.id, COUNT(c) FROM Comment c WHERE c.status = 'ACTIVE' GROUP BY c.weeklyReport.id ORDER BY COUNT(c) DESC")
    List<Object[]> countCommentsByReport();
    
    @Query("SELECT c.author.id, c.author.username, COUNT(c) FROM Comment c WHERE c.createdAt >= :startDate GROUP BY c.author.id, c.author.username ORDER BY COUNT(c) DESC")
    List<Object[]> getCommentAuthorStats(@Param("startDate") LocalDateTime startDate);

    // Activity tracking
    @Query("SELECT c.author.id, c.author.username, COUNT(c), AVG(c.likesCount) FROM Comment c WHERE c.createdAt >= :startDate AND c.status = 'ACTIVE' GROUP BY c.author.id, c.author.username ORDER BY COUNT(c) DESC")
    List<Object[]> getCommentActivityStats(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT DATE(c.createdAt), COUNT(c) FROM Comment c WHERE c.createdAt >= :startDate GROUP BY DATE(c.createdAt) ORDER BY DATE(c.createdAt)")
    List<Object[]> getCommentCountByDate(@Param("startDate") LocalDateTime startDate);

    // Thread statistics
    @Query("SELECT c FROM Comment c WHERE c.parent IS NULL ORDER BY (SELECT COUNT(r) FROM Comment r WHERE r.parent = c) DESC")
    List<Comment> findThreadsByReplyCount();
    
    @Query("SELECT c, (SELECT COUNT(r) FROM Comment r WHERE r.parent = c) as replyCount FROM Comment c WHERE c.parent IS NULL ORDER BY replyCount DESC")
    List<Object[]> getThreadsWithReplyCounts();

    // Pending actions
    @Query("SELECT c FROM Comment c WHERE c.status = 'FLAGGED' ORDER BY c.createdAt ASC")
    List<Comment> findFlaggedComments();
    
    @Query("SELECT c FROM Comment c WHERE c.type IN ('QUESTION', 'CONCERN') AND c.isResolved = false AND c.status = 'ACTIVE'")
    List<Comment> findPendingActionItems();

    // Complex filtering
    @Query("SELECT c FROM Comment c WHERE " +
           "(:reportId IS NULL OR c.weeklyReport.id = :reportId) AND " +
           "(:authorId IS NULL OR c.author.id = :authorId) AND " +
           "(:type IS NULL OR c.type = :type) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:isResolved IS NULL OR c.isResolved = :isResolved) AND " +
           "(:minPriority IS NULL OR c.priority >= :minPriority) " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> findCommentsWithFilters(@Param("reportId") Long reportId,
                                         @Param("authorId") Long authorId,
                                         @Param("type") Comment.CommentType type,
                                         @Param("status") Comment.CommentStatus status,
                                         @Param("isResolved") Boolean isResolved,
                                         @Param("minPriority") Integer minPriority,
                                         Pageable pageable);

    // Update operations
    @Query("UPDATE Comment c SET c.status = :newStatus WHERE c.id IN :commentIds")
    int updateStatusBatch(@Param("commentIds") List<Long> commentIds, 
                         @Param("newStatus") Comment.CommentStatus newStatus);
    
    @Query("UPDATE Comment c SET c.likesCount = c.likesCount + 1 WHERE c.id = :commentId")
    int incrementLikesCount(@Param("commentId") Long commentId);
    
    @Query("UPDATE Comment c SET c.likesCount = c.likesCount - 1 WHERE c.id = :commentId AND c.likesCount > 0")
    int decrementLikesCount(@Param("commentId") Long commentId);
    
    @Query("UPDATE Comment c SET c.isResolved = true, c.resolvedAt = :resolvedAt, c.resolvedBy = :resolvedBy WHERE c.id = :commentId")
    int resolveComment(@Param("commentId") Long commentId, 
                      @Param("resolvedAt") LocalDateTime resolvedAt, 
                      @Param("resolvedBy") User resolvedBy);

    // Cleanup operations
    @Query("SELECT c FROM Comment c WHERE c.status = 'DELETED' AND c.updatedAt < :cutoffDate")
    List<Comment> findDeletedCommentsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("DELETE FROM Comment c WHERE c.status = 'DELETED' AND c.updatedAt < :cutoffDate")
    int permanentlyDeleteOldComments(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Notification queries
    @Query("SELECT c FROM Comment c WHERE c.weeklyReport.userId = :authorId AND c.author.id != :authorId AND c.createdAt >= :sinceDate AND c.status = 'ACTIVE'")
    List<Comment> findCommentsForNotification(@Param("authorId") Long authorId, 
                                             @Param("sinceDate") LocalDateTime sinceDate);
    
    @Query("SELECT c FROM Comment c WHERE c.parent.author.id = :authorId AND c.author.id != :authorId AND c.createdAt >= :sinceDate AND c.status = 'ACTIVE'")
    List<Comment> findRepliesForNotification(@Param("authorId") Long authorId, 
                                            @Param("sinceDate") LocalDateTime sinceDate);
}