package com.weeklyreport.user.controller;

import com.weeklyreport.common.dto.ApiResponse;
import com.weeklyreport.auth.dto.UpdateProfileRequest;
import com.weeklyreport.auth.dto.RegisterRequest;
import com.weeklyreport.user.dto.UpdateUserRequest;
import com.weeklyreport.user.entity.User;
import com.weeklyreport.core.security.CustomUserPrincipal;
import com.weeklyreport.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Management REST Controller
 * Handles user profile operations and user management
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private com.weeklyreport.core.security.JwtTokenProvider jwtTokenProvider;

    /**
     * Create new user - Admin/Super Admin only
     * POST /api/users
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> createUser(
            @Valid @RequestBody RegisterRequest registerRequest,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        try {
            logger.info("Admin {} attempting to create user: {}", currentUser.getUsername(), registerRequest.getUsername());

            // Check if username already exists
            if (!userService.isUsernameAvailable(registerRequest.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("该用户名已存在，请使用其他用户名"));
            }

            // Check if email already exists
            if (!userService.isEmailAvailable(registerRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("该邮箱已被注册，请使用其他邮箱"));
            }

            try {
                // Try creating user through service layer
                User newUser = new User();
                newUser.setUsername(registerRequest.getUsername());
                newUser.setEmail(registerRequest.getEmail());
                newUser.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : User.Role.MANAGER);
                newUser.setStatus(User.UserStatus.ACTIVE);

                User createdUser = userService.createUser(newUser, registerRequest.getPassword());

                logger.info("User created successfully by admin {}: {}", currentUser.getUsername(), createdUser.getUsername());
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("用户创建成功", createdUser));

            } catch (Exception serviceException) {
                logger.error("Service layer error creating user, trying fallback", serviceException);

                // Fallback: Create user with minimal validation
                User fallbackUser = new User();
                fallbackUser.setUsername(registerRequest.getUsername());
                fallbackUser.setEmail(registerRequest.getEmail());
                fallbackUser.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : User.Role.MANAGER);
                fallbackUser.setStatus(User.UserStatus.ACTIVE);

                // Note: Password encoding may not work in fallback mode
                try {
                    User savedUser = userService.createUser(fallbackUser, registerRequest.getPassword());
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(ApiResponse.success("用户创建成功", savedUser));
                } catch (Exception fallbackException) {
                    logger.error("Fallback user creation also failed", fallbackException);
                    throw serviceException; // Throw original exception
                }
            }

        } catch (IllegalArgumentException e) {
            logger.warn("User creation failed - validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("创建用户失败，请稍后重试"));
        }
    }

    /**
     * Get current user profile
     * GET /api/users/profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<User>> getCurrentUserProfile(@AuthenticationPrincipal CustomUserPrincipal currentUser) {
        try {
            logger.info("Fetching profile for user: {}", currentUser.getUsername());

            User user = userService.getUserProfile(currentUser.getUsername());

            logger.info("Profile fetched successfully for user: {}", currentUser.getUsername());
            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", user));

        } catch (UsernameNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("用户不存在"));
        } catch (Exception e) {
            logger.error("Error fetching user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取用户信息失败，请稍后重试"));
        }
    }

    /**
     * Update current user profile
     * PUT /api/users/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Object>> updateCurrentUserProfile(
            @Valid @RequestBody UpdateProfileRequest updateRequest,
            HttpServletRequest request) {
        try {
            // Extract username from JWT token (will be implemented when Stream A completes)
            String originalUsername = extractUsernameFromRequest(request);
            if (originalUsername == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("请先登录后再进行操作"));
            }

            logger.info("Updating profile for user: {}", originalUsername);

            // Check if username is being changed
            boolean usernameChanged = updateRequest.getUsername() != null &&
                                    !updateRequest.getUsername().equals(originalUsername);

            User updatedUser = userService.updateProfile(originalUsername, updateRequest);

            // If username changed, generate new JWT token
            if (usernameChanged) {
                logger.info("Username changed from {} to {}, generating new JWT token",
                           originalUsername, updatedUser.getUsername());

                String newAccessToken = jwtTokenProvider.generateAccessToken(updatedUser);
                String newRefreshToken = jwtTokenProvider.generateRefreshToken(updatedUser.getUsername());

                // Create response with new tokens
                var response = new java.util.HashMap<String, Object>();
                response.put("user", updatedUser);
                response.put("accessToken", newAccessToken);
                response.put("refreshToken", newRefreshToken);
                response.put("tokenRefreshed", true);

                logger.info("Profile updated successfully with new tokens for user: {}", updatedUser.getUsername());
                return ResponseEntity.ok(ApiResponse.success("用户信息更新成功，请使用新的登录令牌", response));
            } else {
                logger.info("Profile updated successfully for user: {}", originalUsername);
                return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", updatedUser));
            }

        } catch (UsernameNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("用户不存在"));
        } catch (IllegalArgumentException e) {
            logger.warn("Profile update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("更新用户信息失败，请稍后重试"));
        }
    }

    /**
     * Get user by ID - Admin/HR only
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long userId) {
        try {
            logger.info("Fetching user by ID: {}", userId);

            User user = userService.getUserById(userId);

            logger.info("User fetched successfully: {}", user.getUsername());
            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", user));

        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("用户不存在"));
        } catch (SecurityException e) {
            logger.warn("Access denied to user ID: {} - {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("没有权限进行此操作"));
        } catch (Exception e) {
            logger.error("Error fetching user by ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取用户信息失败，请稍后重试"));
        }
    }

    /**
     * Get all users with pagination - Admin/HR only
     * GET /api/users
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            Pageable pageable,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        try {
            long startTime = System.currentTimeMillis();
            logger.info("Fetching all users with pagination");

            Page<User> users;
            // Super admins should not see themselves in user management list
            boolean isSuperAdmin = currentUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_SUPER_ADMIN"));

            if (isSuperAdmin) {
                users = userService.getAllUsersExcludingCurrent(pageable, currentUser.getUsername());
            } else {
                users = userService.getAllUsers(pageable);
            }

            long endTime = System.currentTimeMillis();
            logger.info("Users fetched successfully, count: {}, took: {}ms",
                       users.getTotalElements(), (endTime - startTime));
            return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));

        } catch (SecurityException e) {
            logger.warn("Access denied to user list - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("没有权限进行此操作"));
        } catch (Exception e) {
            logger.error("Error fetching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取用户列表失败，请稍后重试"));
        }
    }

    /**
     * Fast user list - debugging performance issue
     * GET /api/users/fast
     */
    @GetMapping("/fast")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<User>>> getFastUsers(
            Pageable pageable,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        try {
            long startTime = System.currentTimeMillis();
            logger.info("Fetching users with fast query");

            Page<User> users = userService.getAllUsers(pageable);

            long endTime = System.currentTimeMillis();
            logger.info("Fast users query completed in {}ms, count: {}",
                       (endTime - startTime), users.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));

        } catch (Exception e) {
            logger.error("Error in fast user query", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取用户列表失败，请稍后重试"));
        }
    }

    /**
     * Search users by keyword - Super Admin only
     * GET /api/users/search?keyword={keyword}
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<User>>> searchUsers(
            @RequestParam String keyword,
            Pageable pageable) {
        try {
            logger.info("Searching users with keyword: {}", keyword);

            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("请输入搜索关键词"));
            }

            Page<User> users = userService.searchUsers(keyword.trim(), pageable);

            logger.info("User search completed, found: {}", users.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));

        } catch (SecurityException e) {
            logger.warn("Access denied to user search - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("没有权限进行此操作"));
        } catch (Exception e) {
            logger.error("Error searching users with keyword: {}", keyword, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("搜索用户失败，请稍后重试"));
        }
    }

    /**
     * Get users by role - Super Admin only
     * GET /api/users/role/{role}
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable User.Role role) {
        try {
            logger.info("Fetching users with role: {}", role);

            List<User> users = userService.getUsersByRole(role);

            logger.info("Users with role {} fetched successfully, count: {}", role, users.size());
            return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));

        } catch (SecurityException e) {
            logger.warn("Access denied to users by role - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("没有权限进行此操作"));
        } catch (Exception e) {
            logger.error("Error fetching users by role: {}", role, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("按角色获取用户列表失败，请稍后重试"));
        }
    }

    /**
     * Update user status - Admin/HR only
     * PUT /api/users/{userId}/status
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam User.UserStatus status) {
        try {
            logger.info("Updating status for user ID: {} to {}", userId, status);

            User updatedUser = userService.updateUserStatus(userId, status);

            logger.info("User status updated successfully: {}", updatedUser.getUsername());
            return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", updatedUser));

        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("用户不存在"));
        } catch (SecurityException e) {
            logger.warn("Access denied to update user status - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("没有权限进行此操作"));
        } catch (Exception e) {
            logger.error("Error updating user status for ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("更新用户状态失败，请稍后重试"));
        }
    }

    /**
     * Enable user account - Admin only
     * PUT /api/users/{userId}/enable
     */
    @PutMapping("/{userId}/enable")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> enableUser(@PathVariable Long userId) {
        try {
            logger.info("Enabling user ID: {}", userId);

            User updatedUser = userService.enableUser(userId);

            logger.info("User enabled successfully: {}", updatedUser.getUsername());
            return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", updatedUser));

        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("用户不存在"));
        } catch (SecurityException e) {
            logger.warn("Access denied to enable user - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("没有权限进行此操作"));
        } catch (Exception e) {
            logger.error("Error enabling user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("启用用户失败，请稍后重试"));
        }
    }

    /**
     * Disable user account (prevents login) - Admin only
     * PUT /api/users/{userId}/disable
     */
    @PutMapping("/{userId}/disable")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> disableUser(@PathVariable Long userId) {
        try {
            logger.info("Disabling user ID: {}", userId);

            User updatedUser = userService.disableUser(userId);

            logger.info("User disabled successfully: {}", updatedUser.getUsername());
            return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", updatedUser));

        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("用户不存在"));
        } catch (SecurityException e) {
            logger.warn("Access denied to disable user - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("没有权限进行此操作"));
        } catch (Exception e) {
            logger.error("Error disabling user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("停用用户失败，请稍后重试"));
        }
    }

    /**
     * Update user role - Admin only
     * PUT /api/users/{userId}/role
     */
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateUserRole(
            @PathVariable Long userId,
            @RequestParam User.Role role) {
        try {
            logger.info("Updating role for user ID: {} to {}", userId, role);

            User updatedUser = userService.updateUserRole(userId, role);

            logger.info("User role updated successfully: {}", updatedUser.getUsername());
            return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", updatedUser));

        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("用户不存在"));
        } catch (SecurityException e) {
            logger.warn("Access denied to update user role - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("没有权限进行此操作"));
        } catch (Exception e) {
            logger.error("Error updating user role for ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("更新用户角色失败，请稍后重试"));
        }
    }

    /**
     * Update user basic information - Admin only
     * PUT /api/users/{userId}
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest updateRequest) {
        try {
            logger.info("Updating user ID: {}", userId);

            User updatedUser = userService.updateUserById(userId, updateRequest);

            logger.info("User updated successfully: {}", updatedUser.getUsername());
            return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", updatedUser));

        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("用户不存在"));
        } catch (SecurityException e) {
            logger.warn("Access denied to update user - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("没有权限进行此操作"));
        } catch (Exception e) {
            logger.error("Error updating user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("更新用户信息失败，请稍后重试"));
        }
    }

    /**
     * Delete user (hard delete) - Super Admin only
     * DELETE /api/users/{userId}
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long userId) {
        try {
            logger.info("Soft deleting user ID: {}", userId);

            userService.deleteUser(userId);

            logger.info("User soft deleted successfully, ID: {}", userId);
            return ResponseEntity.ok(ApiResponse.success("删除用户成功", ""));

        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("用户不存在"));
        } catch (SecurityException e) {
            logger.warn("Access denied to delete user - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("没有权限进行此操作"));
        } catch (Exception e) {
            logger.error("Error deleting user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("删除用户失败，请稍后重试"));
        }
    }

    /**
     * Get user statistics - Admin/HR only
     * GET /api/users/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<ApiResponse<UserService.UserStatistics>> getUserStatistics() {
        try {
            logger.info("Fetching user statistics");

            UserService.UserStatistics statistics = userService.getUserStatistics();

            logger.info("User statistics fetched successfully");
            return ResponseEntity.ok(ApiResponse.success("获取用户统计数据成功", statistics));

        } catch (SecurityException e) {
            logger.warn("Access denied to user statistics - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("没有权限进行此操作"));
        } catch (Exception e) {
            logger.error("Error fetching user statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取用户统计数据失败，请稍后重试"));
        }
    }

    /**
     * Reset user password - Super Admin only
     * POST /api/users/{userId}/reset-password
     */
    @PostMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> resetUserPassword(
            @PathVariable Long userId,
            @RequestParam String newPassword) {
        try {
            logger.info("Resetting password for user ID: {}", userId);

            // Basic password validation
            if (newPassword == null || newPassword.length() < 8) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("新密码长度至少8位字符"));
            }

            userService.resetUserPassword(userId, newPassword);

            logger.info("Password reset successfully for user ID: {}", userId);
            return ResponseEntity.ok(ApiResponse.success("密码重置成功", ""));

        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("用户不存在"));
        } catch (SecurityException e) {
            logger.warn("Access denied to reset user password - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("没有权限进行此操作"));
        } catch (Exception e) {
            logger.error("Error resetting password for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("重置密码失败，请稍后重试"));
        }
    }

    /**
     * Extract username from JWT token in request
     * This will be properly implemented when Stream A completes the JWT provider
     */
    private String extractUsernameFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);

            try {
                return jwtTokenProvider.getUsernameFromToken(token);
            } catch (Exception e) {
                logger.error("Failed to extract username from token: {}", e.getMessage(), e);
                return null;
            }
        }
        return null;
    }
}
