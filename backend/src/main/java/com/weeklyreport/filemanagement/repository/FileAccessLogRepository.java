package com.weeklyreport.filemanagement.repository;

import com.weeklyreport.filemanagement.entity.FileAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件访问日志Repository
 */
@Repository
public interface FileAccessLogRepository extends JpaRepository<FileAccessLog, Long> {

    /**
     * 根据文件ID查询访问日志
     */
    Page<FileAccessLog> findByFileAttachmentIdOrderByAccessTimeDesc(Long fileAttachmentId, Pageable pageable);

    /**
     * 根据用户ID查询访问日志
     */
    Page<FileAccessLog> findByUserIdOrderByAccessTimeDesc(Long userId, Pageable pageable);

    /**
     * 根据操作类型查询日志
     */
    List<FileAccessLog> findByActionOrderByAccessTimeDesc(FileAccessLog.AccessAction action);

    /**
     * 统计文件的下载次数
     */
    @Query("SELECT COUNT(fal) FROM FileAccessLog fal WHERE fal.fileAttachmentId = :fileId AND fal.action = 'DOWNLOAD'")
    Long countDownloadsByFileId(@Param("fileId") Long fileId);

    /**
     * 统计用户的文件访问次数
     */
    Long countByUserIdAndAction(Long userId, FileAccessLog.AccessAction action);

    /**
     * 查询指定时间范围内的访问日志
     */
    @Query("SELECT fal FROM FileAccessLog fal WHERE fal.accessTime BETWEEN :startTime AND :endTime ORDER BY fal.accessTime DESC")
    List<FileAccessLog> findLogsByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询热门文件（按下载次数排序）
     */
    @Query("SELECT fal.fileAttachmentId, COUNT(fal) as downloadCount " +
           "FROM FileAccessLog fal " +
           "WHERE fal.action = 'DOWNLOAD' AND fal.accessTime >= :since " +
           "GROUP BY fal.fileAttachmentId " +
           "ORDER BY downloadCount DESC")
    List<Object[]> findMostDownloadedFiles(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 查询活跃用户（按操作次数排序）
     */
    @Query("SELECT fal.userId, COUNT(fal) as actionCount " +
           "FROM FileAccessLog fal " +
           "WHERE fal.accessTime >= :since AND fal.userId IS NOT NULL " +
           "GROUP BY fal.userId " +
           "ORDER BY actionCount DESC")
    List<Object[]> findMostActiveUsers(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 查询指定文件的最近访问记录
     */
    @Query("SELECT fal FROM FileAccessLog fal " +
           "WHERE fal.fileAttachmentId = :fileId " +
           "ORDER BY fal.accessTime DESC")
    List<FileAccessLog> findRecentAccessByFileId(@Param("fileId") Long fileId, Pageable pageable);

    /**
     * 删除过期的访问日志（数据清理）
     */
    void deleteByAccessTimeBefore(LocalDateTime cutoffTime);
}