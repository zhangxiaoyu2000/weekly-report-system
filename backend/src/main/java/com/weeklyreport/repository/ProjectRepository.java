package com.weeklyreport.repository;

import com.weeklyreport.entity.Project;
import com.weeklyreport.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Project entities
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Find projects by name containing the search term (case insensitive)
     */
    @Query("SELECT p FROM Project p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Project> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find projects by status
     */
    List<Project> findByStatus(Project.ProjectStatus status);

    /**
     * Find projects by owner
     */
    List<Project> findByOwner(User owner);

    /**
     * Find projects by owner and status
     */
    List<Project> findByOwnerAndStatus(User owner, Project.ProjectStatus status);

    /**
     * Find projects by priority
     */
    List<Project> findByPriority(Project.ProjectPriority priority);

    /**
     * Find projects that start within a date range
     */
    @Query("SELECT p FROM Project p WHERE p.startDate >= :startDate AND p.startDate <= :endDate")
    List<Project> findByStartDateBetween(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);

    /**
     * Find projects that end within a date range
     */
    @Query("SELECT p FROM Project p WHERE p.endDate >= :startDate AND p.endDate <= :endDate")
    List<Project> findByEndDateBetween(@Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);

    /**
     * Find active projects (PLANNING, ACTIVE, ON_HOLD)
     */
    @Query("SELECT p FROM Project p WHERE p.status IN ('PLANNING', 'ACTIVE', 'ON_HOLD')")
    List<Project> findActiveProjects();

    /**
     * Find projects where user is a member
     */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.members pm WHERE pm.user = :user AND pm.status = 'ACTIVE'")
    List<Project> findProjectsByMember(@Param("user") User user);

    /**
     * Find projects where user is a member with specific role
     */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.members pm WHERE pm.user = :user AND pm.role = :role AND pm.status = 'ACTIVE'")
    List<Project> findProjectsByMemberAndRole(@Param("user") User user, 
                                            @Param("role") String role);

    /**
     * Find projects by owner or member
     */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.members pm WHERE p.owner = :user OR (pm.user = :user AND pm.status = 'ACTIVE')")
    List<Project> findProjectsByOwnerOrMember(@Param("user") User user);

    /**
     * Count projects by status
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    long countByStatus(@Param("status") Project.ProjectStatus status);

    /**
     * Count projects by owner
     */
    long countByOwner(User owner);

    /**
     * Find projects with pagination
     */
    Page<Project> findAll(Pageable pageable);

    /**
     * Find projects by status with pagination
     */
    Page<Project> findByStatus(Project.ProjectStatus status, Pageable pageable);

    /**
     * Find projects by owner with pagination
     */
    Page<Project> findByOwner(User owner, Pageable pageable);

    /**
     * Search projects by name or description with pagination
     */
    @Query("SELECT p FROM Project p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Project> searchProjects(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find overdue projects (end date passed but status not COMPLETED or CANCELLED)
     */
    @Query("SELECT p FROM Project p WHERE p.endDate < :currentDate AND p.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Project> findOverdueProjects(@Param("currentDate") LocalDate currentDate);

    /**
     * Find projects ending soon (within specified days)
     */
    @Query("SELECT p FROM Project p WHERE p.endDate BETWEEN :currentDate AND :endDate AND p.status IN ('PLANNING', 'ACTIVE')")
    List<Project> findProjectsEndingSoon(@Param("currentDate") LocalDate currentDate, 
                                        @Param("endDate") LocalDate endDate);

    /**
     * Get project statistics
     */
    @Query("SELECT p.status, COUNT(p) FROM Project p GROUP BY p.status")
    List<Object[]> getProjectStatsByStatus();

    /**
     * Check if project name exists for a different project
     */
    @Query("SELECT COUNT(p) > 0 FROM Project p WHERE LOWER(p.name) = LOWER(:name) AND (:id IS NULL OR p.id != :id)")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Long id);
}