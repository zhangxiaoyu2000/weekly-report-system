package com.weeklyreport.project.repository;

import com.weeklyreport.project.entity.ProjectPhase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProjectPhaseRepository extends JpaRepository<ProjectPhase, Long> {
    
    // 按项目ID查找阶段，按创建时间排序
    @Query("SELECT p FROM ProjectPhase p WHERE p.projectId = :projectId ORDER BY p.createdAt ASC")
    List<ProjectPhase> findByProjectIdOrderByCreatedAt(@Param("projectId") Long projectId);
    
    @Query("SELECT p FROM ProjectPhase p WHERE p.projectId = :projectId")
    Page<ProjectPhase> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);
    
    // 按阶段名称查找（用于检查重复）
    @Query("SELECT COUNT(p) > 0 FROM ProjectPhase p WHERE p.projectId = :projectId AND p.phaseName = :phaseName")
    boolean existsByProjectIdAndPhaseName(@Param("projectId") Long projectId, @Param("phaseName") String phaseName);
    
    @Query("SELECT COUNT(p) FROM ProjectPhase p WHERE p.projectId = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);
    
    // 按完成状态查找 - 基于expectedResults字段判断是否定义完整
    @Query("SELECT p FROM ProjectPhase p WHERE p.projectId = :projectId AND p.expectedResults IS NOT NULL AND p.expectedResults != '' ORDER BY p.createdAt ASC")
    List<ProjectPhase> findCompletedByProjectId(@Param("projectId") Long projectId);
    
    @Query("SELECT p FROM ProjectPhase p WHERE p.projectId = :projectId AND (p.expectedResults IS NULL OR p.expectedResults = '') ORDER BY p.createdAt ASC")
    List<ProjectPhase> findIncompleteByProjectId(@Param("projectId") Long projectId);
    
    // 删除项目的所有阶段
    @Modifying
    @Transactional
    @Query("DELETE FROM ProjectPhase p WHERE p.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}