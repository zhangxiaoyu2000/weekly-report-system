package com.weeklyreport.project.repository;

import com.weeklyreport.project.entity.Project;
import com.weeklyreport.project.entity.ProjectMember;
import com.weeklyreport.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ProjectMember entities
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    /**
     * Find project member by project and user
     */
    Optional<ProjectMember> findByProjectAndUser(Project project, User user);

    /**
     * Find project member by project ID and user ID
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.id = :userId")
    Optional<ProjectMember> findByProjectIdAndUserId(@Param("projectId") Long projectId, 
                                                    @Param("userId") Long userId);

    /**
     * Find all members of a project
     */
    List<ProjectMember> findByProject(Project project);

    /**
     * Find all members of a project by project ID
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId")
    List<ProjectMember> findByProjectId(@Param("projectId") Long projectId);

    /**
     * Find active members of a project
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project = :project AND pm.status = 'ACTIVE'")
    List<ProjectMember> findActiveByProject(@Param("project") Project project);

    /**
     * Find active members of a project by project ID
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.status = 'ACTIVE'")
    List<ProjectMember> findActiveByProjectId(@Param("projectId") Long projectId);

    /**
     * Find all projects where user is a member
     */
    List<ProjectMember> findByUser(User user);

    /**
     * Find active project memberships for a user
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.user = :user AND pm.status = 'ACTIVE'")
    List<ProjectMember> findActiveByUser(@Param("user") User user);

    /**
     * Find project members by role
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project = :project AND pm.role = :role")
    List<ProjectMember> findByProjectAndRole(@Param("project") Project project, 
                                           @Param("role") ProjectMember.ProjectRole role);

    /**
     * Find project managers of a project
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project = :project AND pm.role = 'PROJECT_MANAGER' AND pm.status = 'ACTIVE'")
    List<ProjectMember> findProjectManagers(@Param("project") Project project);

    /**
     * Find project members with management permissions
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project = :project AND pm.role IN ('PROJECT_MANAGER', 'TECH_LEAD') AND pm.status = 'ACTIVE'")
    List<ProjectMember> findMembersWithManagementPermission(@Param("project") Project project);

    /**
     * Check if user is a member of the project
     */
    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.project = :project AND pm.user = :user AND pm.status = 'ACTIVE'")
    boolean existsByProjectAndUser(@Param("project") Project project, @Param("user") User user);

    /**
     * Check if user is a member of the project by IDs
     */
    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.id = :userId AND pm.status = 'ACTIVE'")
    boolean existsByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * Check if user has specific role in project
     */
    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.project = :project AND pm.user = :user AND pm.role = :role AND pm.status = 'ACTIVE'")
    boolean existsByProjectAndUserAndRole(@Param("project") Project project, 
                                         @Param("user") User user, 
                                         @Param("role") ProjectMember.ProjectRole role);

    /**
     * Check if user has management permission in project
     */
    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.project = :project AND pm.user = :user AND pm.role IN ('PROJECT_MANAGER', 'TECH_LEAD') AND pm.status = 'ACTIVE'")
    boolean hasManagementPermission(@Param("project") Project project, @Param("user") User user);

    /**
     * Count active members in project
     */
    @Query("SELECT COUNT(pm) FROM ProjectMember pm WHERE pm.project = :project AND pm.status = 'ACTIVE'")
    long countActiveMembers(@Param("project") Project project);

    /**
     * Count members by role in project
     */
    @Query("SELECT COUNT(pm) FROM ProjectMember pm WHERE pm.project = :project AND pm.role = :role AND pm.status = 'ACTIVE'")
    long countMembersByRole(@Param("project") Project project, @Param("role") ProjectMember.ProjectRole role);

    /**
     * Find members with pagination
     */
    Page<ProjectMember> findByProject(Project project, Pageable pageable);

    /**
     * Find active members with pagination
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project = :project AND pm.status = 'ACTIVE'")
    Page<ProjectMember> findActiveByProject(@Param("project") Project project, Pageable pageable);

    /**
     * Update member role
     */
    @Modifying
    @Transactional
    @Query("UPDATE ProjectMember pm SET pm.role = :role, pm.updatedAt = :updatedAt WHERE pm.id = :id")
    int updateMemberRole(@Param("id") Long id, 
                        @Param("role") ProjectMember.ProjectRole role, 
                        @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update member status
     */
    @Modifying
    @Transactional
    @Query("UPDATE ProjectMember pm SET pm.status = :status, pm.updatedAt = :updatedAt WHERE pm.id = :id")
    int updateMemberStatus(@Param("id") Long id, 
                          @Param("status") ProjectMember.MemberStatus status, 
                          @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update member status and left date when removing
     */
    @Modifying
    @Transactional
    @Query("UPDATE ProjectMember pm SET pm.status = 'REMOVED', pm.leftDate = :leftDate, pm.updatedAt = :updatedAt WHERE pm.id = :id")
    int removeMember(@Param("id") Long id, 
                    @Param("leftDate") LocalDateTime leftDate, 
                    @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Remove all members from project (soft delete)
     */
    @Modifying
    @Transactional
    @Query("UPDATE ProjectMember pm SET pm.status = 'REMOVED', pm.leftDate = :leftDate, pm.updatedAt = :updatedAt WHERE pm.project = :project")
    int removeAllMembers(@Param("project") Project project, 
                        @Param("leftDate") LocalDateTime leftDate, 
                        @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Get member statistics by role
     */
    @Query("SELECT pm.role, COUNT(pm) FROM ProjectMember pm WHERE pm.project = :project AND pm.status = 'ACTIVE' GROUP BY pm.role")
    List<Object[]> getMemberStatsByRole(@Param("project") Project project);

    /**
     * Find members who joined within a date range
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project = :project AND pm.joinedDate BETWEEN :startDate AND :endDate")
    List<ProjectMember> findMembersJoinedBetween(@Param("project") Project project, 
                                                @Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * Find members by status
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project = :project AND pm.status = :status")
    List<ProjectMember> findByProjectAndStatus(@Param("project") Project project, 
                                             @Param("status") ProjectMember.MemberStatus status);

    /**
     * Search members by user details
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project = :project AND " +
           "(LOWER(pm.user.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pm.user.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<ProjectMember> searchMembers(@Param("project") Project project, 
                                     @Param("searchTerm") String searchTerm);
}