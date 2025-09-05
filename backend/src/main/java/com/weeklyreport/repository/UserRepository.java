package com.weeklyreport.repository;

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
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // Basic finder methods
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmployeeId(String employeeId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByEmployeeId(String employeeId);

    // Find by status
    List<User> findByStatus(User.UserStatus status);
    
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);

    // Find by role
    List<User> findByRole(User.Role role);
    
    Page<User> findByRole(User.Role role, Pageable pageable);
    
    List<User> findByRoleIn(List<User.Role> roles);

    // Find by department
    List<User> findByDepartmentId(Long departmentId);
    
    Page<User> findByDepartmentId(Long departmentId, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId AND u.status = :status")
    List<User> findByDepartmentIdAndStatus(@Param("departmentId") Long departmentId, 
                                          @Param("status") User.UserStatus status);

    // Find by department hierarchy (including child departments)
    @Query("SELECT u FROM User u WHERE u.department.path LIKE CONCAT(:departmentPath, '%')")
    List<User> findByDepartmentHierarchy(@Param("departmentPath") String departmentPath);

    // Search methods
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "u.status = :status")
    Page<User> searchByKeywordAndStatus(@Param("keyword") String keyword, 
                                       @Param("status") User.UserStatus status, 
                                       Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "u.department.id = :departmentId")
    List<User> searchByKeywordInDepartment(@Param("keyword") String keyword, 
                                          @Param("departmentId") Long departmentId);

    // Find by position
    List<User> findByPosition(String position);
    
    @Query("SELECT DISTINCT u.position FROM User u WHERE u.position IS NOT NULL ORDER BY u.position")
    List<String> findAllPositions();

    // Find managers and team leaders
    @Query("SELECT u FROM User u WHERE u.role IN :managerRoles AND u.status = 'ACTIVE'")
    List<User> findManagers(@Param("managerRoles") List<User.Role> managerRoles);
    
    @Query("SELECT u FROM User u WHERE u.role IN ('DEPARTMENT_MANAGER', 'TEAM_LEADER') AND u.department.id = :departmentId")
    List<User> findDepartmentManagers(@Param("departmentId") Long departmentId);

    // Find users created in time range
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countUsersCreatedAfter(@Param("startDate") LocalDateTime startDate);

    // Find users by last login
    @Query("SELECT u FROM User u WHERE u.lastLoginTime < :cutoffDate AND u.status = 'ACTIVE'")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT u FROM User u WHERE u.lastLoginTime IS NULL AND u.createdAt < :cutoffDate")
    List<User> findUsersNeverLoggedIn(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Statistics methods
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") User.UserStatus status);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.Role role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.department.id = :departmentId")
    long countByDepartmentId(@Param("departmentId") Long departmentId);

    // Department statistics
    @Query("SELECT u.department.id, COUNT(u) FROM User u WHERE u.status = 'ACTIVE' GROUP BY u.department.id")
    List<Object[]> countActiveUsersByDepartment();
    
    @Query("SELECT u.role, COUNT(u) FROM User u WHERE u.status = 'ACTIVE' GROUP BY u.role")
    List<Object[]> countActiveUsersByRole();

    // Update methods (for batch operations)
    @Query("UPDATE User u SET u.status = :newStatus WHERE u.id IN :userIds")
    int updateStatusBatch(@Param("userIds") List<Long> userIds, 
                         @Param("newStatus") User.UserStatus newStatus);
    
    @Query("UPDATE User u SET u.lastLoginTime = :loginTime WHERE u.id = :userId")
    int updateLastLoginTime(@Param("userId") Long userId, 
                           @Param("loginTime") LocalDateTime loginTime);

    // Find users for reporting hierarchy
    @Query("SELECT u FROM User u WHERE u.role = 'EMPLOYEE' AND u.department.id = :departmentId AND u.status = 'ACTIVE'")
    List<User> findEmployeesInDepartment(@Param("departmentId") Long departmentId);
    
    @Query("SELECT u FROM User u JOIN u.weeklyReports r WHERE r.reportWeek = :weekStart GROUP BY u ORDER BY COUNT(r) DESC")
    List<User> findMostActiveReporters(@Param("weekStart") java.time.LocalDate weekStart);

    // Find users without certain relationships
    @Query("SELECT u FROM User u WHERE u.department IS NULL")
    List<User> findUsersWithoutDepartment();
    
    @Query("SELECT u FROM User u WHERE NOT EXISTS (SELECT r FROM WeeklyReport r WHERE r.author = u)")
    List<User> findUsersWithoutReports();

    // Complex queries for user management
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND " +
           "(:departmentId IS NULL OR u.department.id = :departmentId) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:position IS NULL OR u.position = :position)")
    Page<User> findUsersWithFilters(@Param("departmentId") Long departmentId,
                                   @Param("role") User.Role role,
                                   @Param("position") String position,
                                   Pageable pageable);

    // Email and notification queries
    @Query("SELECT u.email FROM User u WHERE u.status = 'ACTIVE' AND u.role IN :roles")
    List<String> findEmailsByRoles(@Param("roles") List<User.Role> roles);
    
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.department.id IN :departmentIds")
    List<User> findByDepartmentIds(@Param("departmentIds") List<Long> departmentIds);
}