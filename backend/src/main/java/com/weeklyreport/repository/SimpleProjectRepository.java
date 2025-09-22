package com.weeklyreport.repository;

import com.weeklyreport.entity.SimpleProject;
import com.weeklyreport.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 简化项目数据访问接口
 */
@Repository
public interface SimpleProjectRepository extends JpaRepository<SimpleProject, Long> {

    /**
     * 根据创建者查找项目 (带用户信息和审核人信息的fetch join)
     */
    @Query("SELECT p FROM SimpleProject p " +
           "LEFT JOIN FETCH p.createdBy " +
           "LEFT JOIN FETCH p.managerReviewer " +
           "LEFT JOIN FETCH p.adminReviewer " +
           "LEFT JOIN FETCH p.superAdminReviewer " +
           "WHERE p.createdBy = :createdBy ORDER BY p.createdAt DESC")
    List<SimpleProject> findByCreatedByOrderByCreatedAtDesc(@Param("createdBy") User createdBy);

    /**
     * 根据状态查找项目 (带用户信息和审核人信息的fetch join)
     */
    @Query("SELECT p FROM SimpleProject p " +
           "LEFT JOIN FETCH p.createdBy " +
           "LEFT JOIN FETCH p.managerReviewer " +
           "LEFT JOIN FETCH p.adminReviewer " +
           "LEFT JOIN FETCH p.superAdminReviewer " +
           "WHERE p.status = :status ORDER BY p.createdAt DESC")
    List<SimpleProject> findByStatusOrderByCreatedAtDesc(@Param("status") SimpleProject.ProjectStatus status);

    /**
     * 根据创建者和状态查找项目 (带用户信息和审核人信息的fetch join)
     */
    @Query("SELECT p FROM SimpleProject p " +
           "LEFT JOIN FETCH p.createdBy " +
           "LEFT JOIN FETCH p.managerReviewer " +
           "LEFT JOIN FETCH p.adminReviewer " +
           "LEFT JOIN FETCH p.superAdminReviewer " +
           "WHERE p.createdBy = :createdBy AND p.status = :status ORDER BY p.createdAt DESC")
    List<SimpleProject> findByCreatedByAndStatusOrderByCreatedAtDesc(@Param("createdBy") User createdBy, @Param("status") SimpleProject.ProjectStatus status);

    /**
     * 分页查询项目 (带用户信息和审核人信息的fetch join)
     */
    @Query("SELECT p FROM SimpleProject p " +
           "LEFT JOIN FETCH p.createdBy " +
           "LEFT JOIN FETCH p.managerReviewer " +
           "LEFT JOIN FETCH p.adminReviewer " +
           "LEFT JOIN FETCH p.superAdminReviewer " +
           "WHERE p.createdBy = :createdBy ORDER BY p.createdAt DESC")
    Page<SimpleProject> findByCreatedByOrderByCreatedAtDesc(@Param("createdBy") User createdBy, Pageable pageable);

    /**
     * 根据项目名称模糊查询 (带用户信息和审核人信息的fetch join)
     */
    @Query("SELECT p FROM SimpleProject p " +
           "LEFT JOIN FETCH p.createdBy " +
           "LEFT JOIN FETCH p.managerReviewer " +
           "LEFT JOIN FETCH p.adminReviewer " +
           "LEFT JOIN FETCH p.superAdminReviewer " +
           "WHERE p.projectName LIKE CONCAT('%', :projectName, '%') ORDER BY p.createdAt DESC")
    List<SimpleProject> findByProjectNameContainingOrderByCreatedAtDesc(@Param("projectName") String projectName);

    /**
     * 统计用户项目数量
     */
    long countByCreatedBy(User createdBy);

    /**
     * 根据ID查找项目 (带用户信息和审核者信息的fetch join)
     */
    @Query("SELECT p FROM SimpleProject p " +
           "LEFT JOIN FETCH p.createdBy " +
           "LEFT JOIN FETCH p.managerReviewer " +
           "LEFT JOIN FETCH p.adminReviewer " +
           "LEFT JOIN FETCH p.superAdminReviewer " +
           "WHERE p.id = :id")
    java.util.Optional<SimpleProject> findByIdWithCreatedBy(@Param("id") Long id);

    /**
     * 查找所有项目 (带用户信息和审核人信息的fetch join)
     */
    @Query("SELECT p FROM SimpleProject p " +
           "LEFT JOIN FETCH p.createdBy " +
           "LEFT JOIN FETCH p.managerReviewer " +
           "LEFT JOIN FETCH p.adminReviewer " +
           "LEFT JOIN FETCH p.superAdminReviewer " +
           "ORDER BY p.createdAt DESC")
    List<SimpleProject> findAllByOrderByCreatedAtDesc();

    /**
     * 统计各状态的项目数量
     */
    @Query("SELECT p.status, COUNT(p) FROM SimpleProject p GROUP BY p.status")
    List<Object[]> countByStatus();
}