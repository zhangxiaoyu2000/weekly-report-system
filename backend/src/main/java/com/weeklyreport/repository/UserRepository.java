package com.weeklyreport.repository;

import com.weeklyreport.dto.user.UserListDTO;
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
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    // Find by status
    List<User> findByStatus(User.UserStatus status);
    
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);
    
    // Find by status not equal (exclude deleted users)
    Page<User> findByStatusNot(User.UserStatus status, Pageable pageable);

    // Find by role
    List<User> findByRole(User.Role role);
    
    Page<User> findByRole(User.Role role, Pageable pageable);
    
    List<User> findByRoleIn(List<User.Role> roles);

    // Department-related queries removed as User entity no longer has department field

    // Search methods - firstName and lastName removed, use username only
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "u.status = :status")
    Page<User> searchByKeywordAndStatus(@Param("keyword") String keyword, 
                                       @Param("status") User.UserStatus status, 
                                       Pageable pageable);

    // Department-specific search removed as User entity no longer has department field

    // Note: position field removed from User entity

    // Find managers and team leaders
    @Query("SELECT u FROM User u WHERE u.role IN :managerRoles AND u.status = 'ACTIVE'")
    List<User> findManagers(@Param("managerRoles") List<User.Role> managerRoles);

    // Find users created in time range
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countUsersCreatedAfter(@Param("startDate") LocalDateTime startDate);

    // Last login queries removed as User entity no longer has lastLogin field

    // Statistics methods
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") User.UserStatus status);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.Role role);
    
    // Department count queries removed as User entity no longer has department field
    
    @Query("SELECT u.role, COUNT(u) FROM User u WHERE u.status = 'ACTIVE' GROUP BY u.role")
    List<Object[]> countActiveUsersByRole();

    // Update methods (for batch operations)
    @Query("UPDATE User u SET u.status = :newStatus WHERE u.id IN :userIds")
    int updateStatusBatch(@Param("userIds") List<Long> userIds, 
                         @Param("newStatus") User.UserStatus newStatus);

    // Find users for reporting hierarchy - department filter removed
    
    // Most active reporters query removed as User entity no longer has weeklyReports relationship

    // Find users without certain relationships - department check removed
    
    // Query removed as WeeklyReport entity no longer has author field

    // Complex queries for user management - department filter removed
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND " +
           "(:role IS NULL OR u.role = :role)")
    Page<User> findUsersWithFilters(@Param("role") User.Role role,
                                   Pageable pageable);

    // Email and notification queries
    @Query("SELECT u.email FROM User u WHERE u.status = 'ACTIVE' AND u.role IN :roles")
    List<String> findEmailsByRoles(@Param("roles") List<User.Role> roles);
    
    // Department batch queries removed as User entity no longer has department field

    // Performance optimized queries using native SQL - simplified for current schema
    @Query(value = "SELECT u.id, u.username, u.username as full_name, u.email, " +
           "u.role, u.status, " +
           "u.created_at, u.updated_at " +
           "FROM users u " +
           "ORDER BY u.created_at DESC",
           nativeQuery = true)
    Page<Object[]> findAllUserListNative(Pageable pageable);

    // Note: UserListDTO queries temporarily removed - need to update DTO first

    // Methods to exclude current user (for super admin user management)
    Page<User> findByStatusNotAndUsernameNot(User.UserStatus status, String username, Pageable pageable);
    
    // Find all users excluding specific username
    Page<User> findByUsernameNot(String username, Pageable pageable);

    @Query(value = "SELECT u.id, u.username, u.username as full_name, u.email, " +
           "u.role, u.status, " +
           "u.created_at, u.updated_at " +
           "FROM users u " +
           "WHERE u.username != :excludeUsername " +
           "ORDER BY u.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM users u WHERE u.username != :excludeUsername",
           nativeQuery = true)
    Page<Object[]> findAllUserListNativeExcludingCurrent(@Param("excludeUsername") String excludeUsername, Pageable pageable);
}