package com.weeklyreport.controller;

import com.weeklyreport.dto.ApiResponse;
import com.weeklyreport.dto.auth.UpdateProfileRequest;
import com.weeklyreport.dto.auth.RegisterRequest;
import com.weeklyreport.dto.user.UpdateUserRequest;
import com.weeklyreport.entity.User;
import com.weeklyreport.security.CustomUserPrincipal;
import com.weeklyreport.service.UserService;
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
public class UserController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private com.weeklyreport.security.JwtTokenProvider jwtTokenProvider;

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
                return error("Username already exists", HttpStatus.CONFLICT);
            }

            // Check if email already exists  
            if (!userService.isEmailAvailable(registerRequest.getEmail())) {
                return error("Email already exists", HttpStatus.CONFLICT);
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
                        .body(ApiResponse.success("User created successfully", createdUser));
                        
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
                            .body(ApiResponse.success("User created successfully (simplified)", savedUser));
                } catch (Exception fallbackException) {
                    logger.error("Fallback user creation also failed", fallbackException);
                    throw serviceException; // Throw original exception
                }
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("User creation failed - validation error: {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating user", e);
            return error("Failed to create user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
            return success("Profile retrieved successfully", user);
            
        } catch (UsernameNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            return error("User not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error fetching user profile", e);
            return error("Failed to fetch user profile", HttpStatus.INTERNAL_SERVER_ERROR);
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
                return error("Authentication required", HttpStatus.UNAUTHORIZED);
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
                return success("Profile updated successfully - please use new tokens", response);
            } else {
                logger.info("Profile updated successfully for user: {}", originalUsername);
                return success("Profile updated successfully", updatedUser);
            }
            
        } catch (UsernameNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            return error("User not found", HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Profile update failed: {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error updating user profile", e);
            return error("Failed to update user profile", HttpStatus.INTERNAL_SERVER_ERROR);
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
            return success("User retrieved successfully", user);
            
        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return error("User not found", HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            logger.warn("Access denied to user ID: {} - {}", userId, e.getMessage());
            return error("Access denied", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error fetching user by ID: {}", userId, e);
            return error("Failed to fetch user", HttpStatus.INTERNAL_SERVER_ERROR);
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
            return success("Users retrieved successfully", users);
            
        } catch (SecurityException e) {
            logger.warn("Access denied to user list - {}", e.getMessage());
            return error("Access denied", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error fetching users", e);
            return error("Failed to fetch users", HttpStatus.INTERNAL_SERVER_ERROR);
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
            return success("Fast users retrieved successfully", users);
            
        } catch (Exception e) {
            logger.error("Error in fast user query", e);
            return error("Failed to fetch users", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Search users by keyword - Admin/HR/Manager only
     * GET /api/users/search?keyword={keyword}
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('DEPARTMENT_MANAGER')")
    public ResponseEntity<ApiResponse<Page<User>>> searchUsers(
            @RequestParam String keyword,
            Pageable pageable) {
        try {
            logger.info("Searching users with keyword: {}", keyword);
            
            if (keyword == null || keyword.trim().isEmpty()) {
                return error("Search keyword is required", HttpStatus.BAD_REQUEST);
            }
            
            Page<User> users = userService.searchUsers(keyword.trim(), pageable);
            
            logger.info("User search completed, found: {}", users.getTotalElements());
            return success("Users found successfully", users);
            
        } catch (SecurityException e) {
            logger.warn("Access denied to user search - {}", e.getMessage());
            return error("Access denied", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error searching users with keyword: {}", keyword, e);
            return error("Failed to search users", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get users by department - Manager can see their department
     * GET /api/users/department/{departmentId}
     */
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('DEPARTMENT_MANAGER')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByDepartment(
            @PathVariable Long departmentId,
            HttpServletRequest request) {
        try {
            // Extract username from JWT token (will be implemented when Stream A completes)
            String currentUsername = extractUsernameFromRequest(request);
            if (currentUsername == null) {
                return error("Authentication required", HttpStatus.UNAUTHORIZED);
            }

            logger.info("Fetching users for department: {}", departmentId);
            
            List<User> users = userService.getUsersByDepartment(departmentId, currentUsername);
            
            logger.info("Department users fetched successfully, count: {}", users.size());
            return success("Department users retrieved successfully", users);
            
        } catch (SecurityException e) {
            logger.warn("Access denied to department users - {}", e.getMessage());
            return error("Access denied", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error fetching users for department: {}", departmentId, e);
            return error("Failed to fetch department users", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get users by role - Admin/HR only
     * GET /api/users/role/{role}
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable User.Role role) {
        try {
            logger.info("Fetching users with role: {}", role);
            
            List<User> users = userService.getUsersByRole(role);
            
            logger.info("Users with role {} fetched successfully, count: {}", role, users.size());
            return success("Users retrieved successfully", users);
            
        } catch (SecurityException e) {
            logger.warn("Access denied to users by role - {}", e.getMessage());
            return error("Access denied", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error fetching users by role: {}", role, e);
            return error("Failed to fetch users by role", HttpStatus.INTERNAL_SERVER_ERROR);
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
            return success("User status updated successfully", updatedUser);
            
        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return error("User not found", HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            logger.warn("Access denied to update user status - {}", e.getMessage());
            return error("Access denied", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error updating user status for ID: {}", userId, e);
            return error("Failed to update user status", HttpStatus.INTERNAL_SERVER_ERROR);
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
            return success("User enabled successfully", updatedUser);
            
        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return error("User not found", HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            logger.warn("Access denied to enable user - {}", e.getMessage());
            return error("Access denied", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error enabling user ID: {}", userId, e);
            return error("Failed to enable user", HttpStatus.INTERNAL_SERVER_ERROR);
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
            return success("User disabled successfully", updatedUser);
            
        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return error("User not found", HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            logger.warn("Access denied to disable user - {}", e.getMessage());
            return error("Access denied", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error disabling user ID: {}", userId, e);
            return error("Failed to disable user", HttpStatus.INTERNAL_SERVER_ERROR);
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
            return success("User role updated successfully", updatedUser);
            
        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return error("User not found", HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            logger.warn("Access denied to update user role - {}", e.getMessage());
            return error("Access denied", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error updating user role for ID: {}", userId, e);
            return error("Failed to update user role", HttpStatus.INTERNAL_SERVER_ERROR);
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
            return success("User updated successfully", updatedUser);
            
        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return error("User not found", HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            logger.warn("Access denied to update user - {}", e.getMessage());
            return error("Access denied", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error updating user ID: {}", userId, e);
            return error("Failed to update user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Assign user to department - Admin/HR only
     * PUT /api/users/{userId}/department
     */
    @PutMapping("/{userId}/department")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> assignUserToDepartment(
            @PathVariable Long userId,
            @RequestParam(required = false) Long departmentId) {
        try {
            logger.info("Assigning user ID: {} to department ID: {}", userId, departmentId);
            
            User updatedUser = userService.assignUserToDepartment(userId, departmentId);
            
            logger.info("User assigned to department successfully: {}", updatedUser.getUsername());
            return success("User assigned to department successfully", updatedUser);
            
        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return error("User not found", HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid department assignment: {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            logger.warn("Access denied to assign user to department - {}", e.getMessage());
            return error("Access denied", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error assigning user to department, user ID: {}, dept ID: {}", userId, departmentId, e);
            return error("Failed to assign user to department", HttpStatus.INTERNAL_SERVER_ERROR);
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
            return success("User deleted successfully", "");
            
        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return error("User not found", HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            logger.warn("Access denied to delete user - {}", e.getMessage());
            return error("Access denied", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error deleting user ID: {}", userId, e);
            return error("Failed to delete user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get department managers
     * GET /api/users/department/{departmentId}/managers
     */
    @GetMapping("/department/{departmentId}/managers")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<User>>> getDepartmentManagers(@PathVariable Long departmentId) {
        try {
            logger.info("Fetching managers for department: {}", departmentId);
            
            List<User> managers = userService.getDepartmentManagers(departmentId);
            
            logger.info("Department managers fetched successfully, count: {}", managers.size());
            return success("Department managers retrieved successfully", managers);
            
        } catch (Exception e) {
            logger.error("Error fetching department managers for department: {}", departmentId, e);
            return error("Failed to fetch department managers", HttpStatus.INTERNAL_SERVER_ERROR);
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
            return success("User statistics retrieved successfully", statistics);
            
        } catch (SecurityException e) {
            logger.warn("Access denied to user statistics - {}", e.getMessage());
            return error("Access denied", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error fetching user statistics", e);
            return error("Failed to fetch user statistics", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Reset user password - Admin/HR only
     * POST /api/users/{userId}/reset-password
     */
    @PostMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<ApiResponse<String>> resetUserPassword(
            @PathVariable Long userId,
            @RequestParam String newPassword) {
        try {
            logger.info("Resetting password for user ID: {}", userId);
            
            // Basic password validation
            if (newPassword == null || newPassword.length() < 8) {
                return error("New password must be at least 8 characters long", HttpStatus.BAD_REQUEST);
            }
            
            userService.resetUserPassword(userId, newPassword);
            
            logger.info("Password reset successfully for user ID: {}", userId);
            return success("Password reset successfully", "");
            
        } catch (UsernameNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return error("User not found", HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            logger.warn("Access denied to reset user password - {}", e.getMessage());
            return error("Access denied", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error resetting password for user ID: {}", userId, e);
            return error("Failed to reset password", HttpStatus.INTERNAL_SERVER_ERROR);
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