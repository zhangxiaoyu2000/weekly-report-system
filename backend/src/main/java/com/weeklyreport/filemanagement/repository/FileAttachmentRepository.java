package com.weeklyreport.filemanagement.repository;

import com.weeklyreport.filemanagement.entity.FileAttachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文件附件Repository
 */
@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    /**
     * 根据上传状态查询文件
     */
    List<FileAttachment> findByUploadStatus(FileAttachment.UploadStatus uploadStatus);

    /**
     * 根据上传用户查询文件
     */
    Page<FileAttachment> findByUploadedByAndDeletedAtIsNull(Long uploadedBy, Pageable pageable);

    /**
     * 根据文件哈希查找文件（用于去重）
     */
    Optional<FileAttachment> findByFileHashAndDeletedAtIsNull(String fileHash);

    /**
     * 根据存储文件名查找文件
     */
    Optional<FileAttachment> findByStoredFilenameAndDeletedAtIsNull(String storedFilename);

    /**
     * 根据桶名和文件路径查找文件
     */
    Optional<FileAttachment> findByBucketNameAndFilePathAndDeletedAtIsNull(String bucketName, String filePath);

    /**
     * 查询过期的文件
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.expiresAt < :now AND f.deletedAt IS NULL")
    List<FileAttachment> findExpiredFiles(@Param("now") LocalDateTime now);

    /**
     * 查询需要清理的文件（已标记删除）
     */
    List<FileAttachment> findByDeletedAtIsNotNull();

    /**
     * 根据MIME类型查询文件
     */
    Page<FileAttachment> findByMimeTypeStartingWithAndDeletedAtIsNull(String mimeTypePrefix, Pageable pageable);

    /**
     * 统计用户上传的文件总大小
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileAttachment f WHERE f.uploadedBy = :userId AND f.deletedAt IS NULL")
    Long calculateTotalFileSizeByUser(@Param("userId") Long userId);

    /**
     * 统计用户上传的文件数量
     */
    Long countByUploadedByAndDeletedAtIsNull(Long uploadedBy);

    /**
     * 查询用户最近上传的文件
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.uploadedBy = :userId AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    Page<FileAttachment> findRecentFilesByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * 根据创建时间范围查询文件
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.createdAt BETWEEN :startDate AND :endDate AND f.deletedAt IS NULL")
    List<FileAttachment> findFilesByDateRange(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * 查询上传失败的文件
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.uploadStatus = 'FAILED' AND f.createdAt > :since")
    List<FileAttachment> findFailedUploads(@Param("since") LocalDateTime since);

    /**
     * 软删除文件
     */
    @Query("UPDATE FileAttachment f SET f.deletedAt = :now, f.uploadStatus = 'DELETED' WHERE f.id = :id")
    int softDeleteById(@Param("id") Long id, @Param("now") LocalDateTime now);
}