package com.weeklyreport.service;

import com.weeklyreport.dto.auth.UpdateProfileRequest;
import com.weeklyreport.entity.User;
import com.weeklyreport.repository.UserRepository;
import com.weeklyreport.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User management service
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get user profile by username
     */
    public User getUserProfile(String username) {
        logger.info("Fetching profile for user: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Update user profile
     */
    public User updateProfile(String username, UpdateProfileRequest updateRequest) {
        logger.info("Updating profile for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Update allowed fields
        if (updateRequest.getFullName() != null) {
            user.setFullName(updateRequest.getFullName());
        }
        
        if (updateRequest.getEmployeeId() != null) {
            // Check if employee ID is unique (exclude current user)
            boolean exists = userRepository.existsByEmployeeId(updateRequest.getEmployeeId()) &&
                    !updateRequest.getEmployeeId().equals(user.getEmployeeId());
            if (exists) {
                throw new IllegalArgumentException("Employee ID already exists");
            }
            user.setEmployeeId(updateRequest.getEmployeeId());
        }
        
        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }
        
        if (updateRequest.getPosition() != null) {
            user.setPosition(updateRequest.getPosition());
        }
        
        if (updateRequest.getAvatarUrl() != null) {
            user.setAvatarUrl(updateRequest.getAvatarUrl());
        }

        user = userRepository.save(user);
        logger.info("Profile updated successfully for user: {}", username);
        return user;
    }

    /**
     * Get user by ID - Admin only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public User getUserById(Long userId) {
        logger.info("Fetching user by ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Get all users with pagination - Admin/HR only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public Page<User> getAllUsers(Pageable pageable) {
        logger.info("Fetching all users with pagination");
        return userRepository.findAll(pageable);
    }

    /**
     * Search users by keyword - Admin/HR/Manager only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('DEPARTMENT_MANAGER')")
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        logger.info("Searching users with keyword: {}", keyword);
        return userRepository.searchByKeywordAndStatus(keyword, User.UserStatus.ACTIVE, pageable);
    }

    /**
     * Get users by department - Manager can see their department
     */
    public List<User> getUsersByDepartment(Long departmentId, String currentUsername) {
        logger.info("Fetching users for department: {}", departmentId);
        
        User currentUser = getUserProfile(currentUsername);
        
        // Check permissions
        if (!hasPermissionToViewDepartment(currentUser, departmentId)) {
            throw new SecurityException("Access denied to department users");
        }
        
        return userRepository.findByDepartmentIdAndStatus(departmentId, User.UserStatus.ACTIVE);
    }

    /**
     * Get users by role - Admin/HR only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public List<User> getUsersByRole(User.Role role) {
        logger.info("Fetching users with role: {}", role);
        return userRepository.findByRole(role);
    }

    /**
     * Update user status - Admin/HR only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public User updateUserStatus(Long userId, User.UserStatus status) {
        logger.info("Updating status for user ID: {} to {}", userId, status);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        user.setStatus(status);
        user = userRepository.save(user);

        logger.info("Status updated successfully for user: {}", user.getUsername());
        return user;
    }

    /**
     * Update user role - Admin only
     */
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUserRole(Long userId, User.Role role) {
        logger.info("Updating role for user ID: {} to {}", userId, role);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        user.setRole(role);
        user = userRepository.save(user);

        logger.info("Role updated successfully for user: {}", user.getUsername());
        return user;
    }

    /**
     * Assign user to department - Admin/HR only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public User assignUserToDepartment(Long userId, Long departmentId) {
        logger.info("Assigning user ID: {} to department ID: {}", userId, departmentId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresentOrElse(
                user::setDepartment,
                () -> { throw new IllegalArgumentException("Department not found with ID: " + departmentId); }
            );
        } else {
            user.setDepartment(null);
        }

        user = userRepository.save(user);
        logger.info("User assigned to department successfully");
        return user;
    }

    /**
     * Delete user (soft delete) - Admin only
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long userId) {
        logger.info("Soft deleting user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        user.setStatus(User.UserStatus.DELETED);
        userRepository.save(user);

        logger.info("User soft deleted successfully: {}", user.getUsername());
    }

    /**
     * Get department managers
     */
    public List<User> getDepartmentManagers(Long departmentId) {
        logger.info("Fetching managers for department: {}", departmentId);
        return userRepository.findDepartmentManagers(departmentId);
    }

    /**
     * Get all active users count
     */
    public long getActiveUsersCount() {
        return userRepository.countByStatus(User.UserStatus.ACTIVE);
    }

    /**
     * Get users who never logged in
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public List<User> getUsersNeverLoggedIn(int daysCutoff) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysCutoff);
        return userRepository.findUsersNeverLoggedIn(cutoffDate);
    }

    /**
     * Get inactive users
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public List<User> getInactiveUsers(int daysCutoff) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysCutoff);
        return userRepository.findInactiveUsers(cutoffDate);
    }

    /**
     * Check if current user has permission to view department users
     */
    private boolean hasPermissionToViewDepartment(User currentUser, Long departmentId) {
        // Admins and HR can see all departments
        if (currentUser.getRole() == User.Role.ADMIN || 
            currentUser.getRole() == User.Role.HR_MANAGER) {
            return true;
        }
        
        // Department managers can see their own department
        if (currentUser.getRole() == User.Role.DEPARTMENT_MANAGER ||
            currentUser.getRole() == User.Role.TEAM_LEADER) {
            return currentUser.getDepartment() != null && 
                   currentUser.getDepartment().getId().equals(departmentId);
        }
        
        return false;
    }

    /**
     * Reset user password - Admin/HR only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public void resetUserPassword(Long userId, String newPassword) {
        logger.info("Resetting password for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password reset successfully for user: {}", user.getUsername());
    }

    /**
     * Check if username is available
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * Check if email is available
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * Check if employee ID is available
     */
    public boolean isEmployeeIdAvailable(String employeeId) {
        return !userRepository.existsByEmployeeId(employeeId);
    }

    /**
     * Get user statistics
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public UserStatistics getUserStatistics() {
        return new UserStatistics(
            userRepository.countByStatus(User.UserStatus.ACTIVE),
            userRepository.countByStatus(User.UserStatus.INACTIVE),
            userRepository.countByStatus(User.UserStatus.LOCKED),
            userRepository.countByRole(User.Role.ADMIN),
            userRepository.countByRole(User.Role.HR_MANAGER),
            userRepository.countByRole(User.Role.DEPARTMENT_MANAGER),
            userRepository.countByRole(User.Role.TEAM_LEADER),
            userRepository.countByRole(User.Role.EMPLOYEE)
        );
    }

    /**
     * User statistics DTO
     */
    public static class UserStatistics {
        private final long activeUsers;
        private final long inactiveUsers;
        private final long lockedUsers;
        private final long admins;
        private final long hrManagers;
        private final long departmentManagers;
        private final long teamLeaders;
        private final long employees;

        public UserStatistics(long activeUsers, long inactiveUsers, long lockedUsers, 
                            long admins, long hrManagers, long departmentManagers, 
                            long teamLeaders, long employees) {
            this.activeUsers = activeUsers;
            this.inactiveUsers = inactiveUsers;
            this.lockedUsers = lockedUsers;
            this.admins = admins;
            this.hrManagers = hrManagers;
            this.departmentManagers = departmentManagers;
            this.teamLeaders = teamLeaders;
            this.employees = employees;
        }

        // Getters
        public long getActiveUsers() { return activeUsers; }
        public long getInactiveUsers() { return inactiveUsers; }
        public long getLockedUsers() { return lockedUsers; }
        public long getAdmins() { return admins; }
        public long getHrManagers() { return hrManagers; }
        public long getDepartmentManagers() { return departmentManagers; }
        public long getTeamLeaders() { return teamLeaders; }
        public long getEmployees() { return employees; }
        public long getTotalUsers() { return activeUsers + inactiveUsers + lockedUsers; }
    }
}