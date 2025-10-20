package com.weeklyreport.user.service;

import com.weeklyreport.auth.dto.UpdateProfileRequest;
import com.weeklyreport.user.dto.UpdateUserRequest;
import com.weeklyreport.user.dto.UserListDTO;
import com.weeklyreport.user.entity.User;
import com.weeklyreport.user.repository.UserRepository;
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
import java.util.ArrayList;
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
        logger.info("Update request - Username: {}, Email: {}", updateRequest.getUsername(), updateRequest.getEmail());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        logger.info("Current user - Username: {}, Email: {}", user.getUsername(), user.getEmail());

        // Check if new username is different and available
        if (updateRequest.getUsername() != null && !updateRequest.getUsername().equals(user.getUsername())) {
            if (!isUsernameAvailable(updateRequest.getUsername())) {
                throw new IllegalArgumentException("用户名已存在: " + updateRequest.getUsername());
            }
            user.setUsername(updateRequest.getUsername());
            logger.info("Username updated from {} to {}", username, updateRequest.getUsername());
        }

        // Check if new email is different and available
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (!isEmailAvailable(updateRequest.getEmail())) {
                throw new IllegalArgumentException("邮箱已存在: " + updateRequest.getEmail());
            }
            user.setEmail(updateRequest.getEmail());
            logger.info("Email updated for user: {}", user.getUsername());
        }

        // Update allowed fields
        if (updateRequest.getFullName() != null) {
            // Full name is computed automatically from first and last name
        }
        
        if (updateRequest.getEmployeeId() != null) {
            // Employee ID not supported in simplified entity
        }
        
        if (updateRequest.getPhone() != null) {
            // Phone not supported in simplified entity
        }
        
        if (updateRequest.getPosition() != null) {
            // Position not supported in simplified entity
        }
        
        if (updateRequest.getAvatarUrl() != null) {
            // Avatar URL not supported in simplified entity
        }

        logger.info("Before save - Username: {}, Email: {}", user.getUsername(), user.getEmail());
        user = userRepository.save(user);
        logger.info("After save - Username: {}, Email: {}", user.getUsername(), user.getEmail());
        logger.info("Profile updated successfully for user: {}", user.getUsername());
        return user;
    }

    /**
     * Get user by ID - Admin, Manager, Super Admin
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public User getUserById(Long userId) {
        logger.info("Fetching user by ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Get basic user info by ID - for display purposes
     * Available to all authenticated users (MANAGER, ADMIN, SUPER_ADMIN)
     */
    public User getUserBasicInfo(Long userId) {
        logger.info("Fetching basic user info by ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Get all users with pagination - Admin/HR/Super Admin only
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('SUPER_ADMIN')")
    public Page<User> getAllUsers(Pageable pageable) {
        long startTime = System.currentTimeMillis();
        logger.info("Fetching all users with pagination");
        
        // 查询所有用户（现在只有ACTIVE和INACTIVE状态）
        Page<User> result = userRepository.findAll(pageable);
        
        long endTime = System.currentTimeMillis();
        logger.info("Users query completed in {}ms, total count: {}", 
                   (endTime - startTime), result.getTotalElements());
        
        return result;
    }

    /**
     * Get all users with optimized performance - Admin/HR/Super Admin only
     * Returns UserListDTO instead of full User entity to improve performance
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('SUPER_ADMIN')")
    public Page<UserListDTO> getAllUsersOptimized(Pageable pageable) {
        logger.info("Fetching all users with pagination (optimized with native query)");
        Page<Object[]> rawResults = userRepository.findAllUserListNative(pageable);
        
        return rawResults.map(row -> new UserListDTO(
            (Long) row[0],           // id
            (String) row[1],         // username
            (String) row[2],         // full_name (username as alias)
            (String) row[3],         // email
            null,                    // position (not available in simplified schema)
            (String) row[4],         // role
            (String) row[5],         // status
            null,                    // department_name (not available)
            null,                    // last_login_time (not available)
            (java.time.LocalDateTime) row[6]   // created_at
        ));
    }

    /**
     * Get all users excluding current user - Super Admin only
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Page<User> getAllUsersExcludingCurrent(Pageable pageable, String currentUsername) {
        long startTime = System.currentTimeMillis();
        logger.info("Fetching all users excluding current user: {}", currentUsername);
        
        // 查询所有非删除状态的用户，排除当前用户
        Page<User> result = userRepository.findByUsernameNot(currentUsername, pageable);
        
        long endTime = System.currentTimeMillis();
        logger.info("Users query (excluding current) completed in {}ms, total count: {}", 
                   (endTime - startTime), result.getTotalElements());
        
        return result;
    }

    /**
     * Get all users with optimized performance excluding current user - Super Admin only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Page<UserListDTO> getAllUsersOptimizedExcludingCurrent(Pageable pageable, String currentUsername) {
        logger.info("Fetching all users excluding current user (optimized): {}", currentUsername);
        Page<Object[]> rawResults = userRepository.findAllUserListNativeExcludingCurrent(currentUsername, pageable);
        
        return rawResults.map(row -> new UserListDTO(
            (Long) row[0],           // id
            (String) row[1],         // username
            (String) row[2],         // full_name (username as alias)
            (String) row[3],         // email
            null,                    // position (not available in simplified schema)
            (String) row[4],         // role
            (String) row[5],         // status
            null,                    // department_name (not available)
            null,                    // last_login_time (not available)
            (java.time.LocalDateTime) row[6]   // created_at
        ));
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
     * Search users with optimized performance - Admin/HR/Manager only
     * Returns UserListDTO instead of full User entity to improve performance
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('DEPARTMENT_MANAGER')")
    public Page<UserListDTO> searchUsersOptimized(String keyword, Pageable pageable) {
        logger.info("Searching users with keyword (optimized): {}", keyword);
        // Search functionality disabled in simplified version
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(UserListDTO::new);
    }

    /**
     * Get users by role - Admin/HR only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('SUPER_ADMIN')")
    public List<User> getUsersByRole(User.Role role) {
        logger.info("Fetching users with role: {}", role);
        return userRepository.findByRole(role);
    }

    /**
     * Update user status - Admin only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public User updateUserStatus(Long userId, User.UserStatus status) {
        logger.info("Updating status for user ID: {} to {}", userId, status);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        // Protect super admins from being deactivated
        if (user.getRole() == User.Role.SUPER_ADMIN) {
            if (status == User.UserStatus.INACTIVE) {
                throw new SecurityException("超级管理员不能被停用");
            }
        }

        user.setStatus(status);
        user = userRepository.save(user);

        logger.info("Status updated successfully for user: {}", user.getUsername());
        return user;
    }

    /**
     * Enable user account - Admin only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public User enableUser(Long userId) {
        logger.info("Enabling user ID: {}", userId);
        return updateUserStatus(userId, User.UserStatus.ACTIVE);
    }

    /**
     * Disable user account (prevents login) - Admin only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public User disableUser(Long userId) {
        logger.info("Disabling user ID: {}", userId);
        return updateUserStatus(userId, User.UserStatus.INACTIVE);
    }

    /**
     * Update user role - Admin only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public User updateUserRole(Long userId, User.Role role) {
        logger.info("Updating role for user ID: {} to {}", userId, role);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        // Protect super admins from role changes
        if (user.getRole() == User.Role.SUPER_ADMIN && role != User.Role.SUPER_ADMIN) {
            throw new SecurityException("超级管理员角色不能被降级");
        }

        user.setRole(role);
        user = userRepository.save(user);

        logger.info("Role updated successfully for user: {}", user.getUsername());
        return user;
    }

    /**
     * Update user by ID - Super Admin only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public User updateUserById(Long userId, UpdateUserRequest updateRequest) {
        logger.info("Updating user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        
        // Update allowed fields
        if (updateRequest.getUsername() != null && !updateRequest.getUsername().equals(user.getUsername())) {
            // Check if username is unique
            if (userRepository.existsByUsername(updateRequest.getUsername())) {
                throw new IllegalArgumentException("用户名已存在");
            }
            user.setUsername(updateRequest.getUsername());
        }
        
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            // Check if email is unique
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new IllegalArgumentException("邮箱已存在");
            }
            user.setEmail(updateRequest.getEmail());
        }
        
        if (updateRequest.getFullName() != null) {
            // Full name is computed automatically from first and last name
        }
        
        if (updateRequest.getRole() != null) {
            user.setRole(updateRequest.getRole());
        }
        
        user = userRepository.save(user);
        logger.info("User updated successfully: {}", user.getUsername());
        return user;
    }

    /**
     * Delete user (hard delete) - Super Admin only
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void deleteUser(Long userId) {
        logger.info("Deleting user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        // Protect super admins from being deleted
        if (user.getRole() == User.Role.SUPER_ADMIN) {
            throw new SecurityException("超级管理员不能被删除");
        }

        // Hard delete the user from database
        userRepository.delete(user);

        logger.info("User deleted successfully: {}", user.getUsername());
    }

    /**
     * Get all active users count
     */
    public long getActiveUsersCount() {
        return userRepository.countByStatus(User.UserStatus.ACTIVE);
    }

    /**
     * Get users who never logged in - not supported in simplified schema
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public List<User> getUsersNeverLoggedIn(int daysCutoff) {
        // Simplified schema doesn't track login history
        return new ArrayList<>();
    }

    /**
     * Get inactive users - not supported in simplified schema
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public List<User> getInactiveUsers(int daysCutoff) {
        // Simplified schema doesn't track login history
        return new ArrayList<>();
    }

    /**
     * Reset user password - Admin/HR only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('SUPER_ADMIN')")
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
        // Employee ID check disabled in simplified entity
        return true;
    }

    /**
     * Create a new user - Admin/Super Admin only
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public User createUser(User user, String password) {
        logger.info("Creating new user: {}", user.getUsername());

        // Validate required fields
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名是必须的");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱是必须的");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码是必须的");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        // Set defaults if not provided
        if (user.getRole() == null) {
            user.setRole(User.Role.MANAGER);
        }
        if (user.getStatus() == null) {
            user.setStatus(User.UserStatus.ACTIVE);
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(password));

        // Set creation timestamp
        user.setCreatedAt(LocalDateTime.now());

        // Save user
        User savedUser = userRepository.save(user);
        
        logger.info("User created successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    /**
     * Get user statistics
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('SUPER_ADMIN')")
    public UserStatistics getUserStatistics() {
        return new UserStatistics(
            userRepository.countByStatus(User.UserStatus.ACTIVE),
            userRepository.countByStatus(User.UserStatus.INACTIVE),
            0L, // 不再有SUSPENDED状态
            userRepository.countByRole(User.Role.ADMIN),
            userRepository.countByRole(User.Role.MANAGER)
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
        private final long managers;

        public UserStatistics(long activeUsers, long inactiveUsers, long lockedUsers, 
                            long admins, long managers) {
            this.activeUsers = activeUsers;
            this.inactiveUsers = inactiveUsers;
            this.lockedUsers = lockedUsers;
            this.admins = admins;
            this.managers = managers;
        }

        // Getters
        public long getActiveUsers() { return activeUsers; }
        public long getInactiveUsers() { return inactiveUsers; }
        public long getLockedUsers() { return lockedUsers; }
        public long getAdmins() { return admins; }
        public long getManagers() { return managers; }
        public long getTotalUsers() { return activeUsers + inactiveUsers + lockedUsers; }
    }
}
