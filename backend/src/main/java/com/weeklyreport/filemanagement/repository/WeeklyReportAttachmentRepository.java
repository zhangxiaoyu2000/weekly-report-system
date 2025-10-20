package com.weeklyreport.filemanagement.repository;

import com.weeklyreport.filemanagement.entity.WeeklyReportAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 周报文件关联Repository
 */
@Repository
public interface WeeklyReportAttachmentRepository extends JpaRepository<WeeklyReportAttachment, Long> {

    /**
     * 根据周报ID查询所有附件
     */
    @Query("SELECT wra FROM WeeklyReportAttachment wra " +
           "JOIN FETCH wra.fileAttachment fa " +
           "WHERE wra.weeklyReportId = :weeklyReportId " +
           "AND fa.deletedAt IS NULL " +
           "ORDER BY wra.displayOrder ASC, wra.createdAt ASC")
    List<WeeklyReportAttachment> findByWeeklyReportIdWithFileAttachment(@Param("weeklyReportId") Long weeklyReportId);

    /**
     * 根据周报ID和附件类型查询附件
     */
    @Query("SELECT wra FROM WeeklyReportAttachment wra " +
           "JOIN FETCH wra.fileAttachment fa " +
           "WHERE wra.weeklyReportId = :weeklyReportId " +
           "AND wra.attachmentType = :attachmentType " +
           "AND fa.deletedAt IS NULL " +
           "ORDER BY wra.displayOrder ASC")
    List<WeeklyReportAttachment> findByWeeklyReportIdAndAttachmentType(
            @Param("weeklyReportId") Long weeklyReportId, 
            @Param("attachmentType") WeeklyReportAttachment.AttachmentType attachmentType);

    /**
     * 根据周报ID和任务ID查询附件
     */
    List<WeeklyReportAttachment> findByWeeklyReportIdAndRelatedTaskIdOrderByDisplayOrderAsc(
            Long weeklyReportId, Long relatedTaskId);

    /**
     * 根据周报ID、项目ID和阶段ID查询附件
     */
    List<WeeklyReportAttachment> findByWeeklyReportIdAndRelatedProjectIdAndRelatedPhaseIdOrderByDisplayOrderAsc(
            Long weeklyReportId, Long relatedProjectId, Long relatedPhaseId);

    /**
     * 根据文件ID查询所有关联的周报
     */
    List<WeeklyReportAttachment> findByFileAttachmentId(Long fileAttachmentId);

    /**
     * 检查周报和文件的关联是否存在
     */
    Optional<WeeklyReportAttachment> findByWeeklyReportIdAndFileAttachmentId(
            Long weeklyReportId, Long fileAttachmentId);

    /**
     * 统计周报的附件数量
     */
    Long countByWeeklyReportId(Long weeklyReportId);

    /**
     * 根据附件类型统计数量
     */
    Long countByAttachmentType(WeeklyReportAttachment.AttachmentType attachmentType);

    /**
     * 删除周报的所有附件关联
     */
    void deleteByWeeklyReportId(Long weeklyReportId);

    /**
     * 删除特定的文件关联
     */
    void deleteByWeeklyReportIdAndFileAttachmentId(Long weeklyReportId, Long fileAttachmentId);

    /**
     * 查询周报中特定类型的附件数量
     */
    @Query("SELECT COUNT(wra) FROM WeeklyReportAttachment wra " +
           "JOIN wra.fileAttachment fa " +
           "WHERE wra.weeklyReportId = :weeklyReportId " +
           "AND wra.attachmentType = :attachmentType " +
           "AND fa.deletedAt IS NULL")
    Long countByWeeklyReportIdAndAttachmentType(
            @Param("weeklyReportId") Long weeklyReportId, 
            @Param("attachmentType") WeeklyReportAttachment.AttachmentType attachmentType);

    /**
     * 获取周报附件的最大显示顺序
     */
    @Query("SELECT COALESCE(MAX(wra.displayOrder), 0) FROM WeeklyReportAttachment wra " +
           "WHERE wra.weeklyReportId = :weeklyReportId")
    Integer getMaxDisplayOrderByWeeklyReportId(@Param("weeklyReportId") Long weeklyReportId);
}