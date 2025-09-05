package com.weeklyreport.controller;

import com.weeklyreport.dto.ApiResponse;
import com.weeklyreport.dto.auth.UpdateProfileRequest;
import com.weeklyreport.entity.User;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Management REST Controller
 * Handles user profile operations and user management
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * Get current user profile
     * GET /api/users/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getCurrentUserProfile(HttpServletRequest request) {
        try {
            // Extract username from JWT token (will be implemented when Stream A completes)
            String username = extractUsernameFromRequest(request);
            if (username == null) {
                return error("Authentication required", HttpStatus.UNAUTHORIZED);
            }

            logger.info("Fetching profile for user: {}", username);
            
            User user = userService.getUserProfile(username);
            
            logger.info("Profile fetched successfully for user: {}", username);
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
    public ResponseEntity<ApiResponse<User>> updateCurrentUserProfile(
            @Valid @RequestBody UpdateProfileRequest updateRequest,
            HttpServletRequest request) {
        try {
            // Extract username from JWT token (will be implemented when Stream A completes)
            String username = extractUsernameFromRequest(request);
            if (username == null) {
                return error("Authentication required", HttpStatus.UNAUTHORIZED);
            }

            logger.info("Updating profile for user: {}", username);
            
            User updatedUser = userService.updateProfile(username, updateRequest);
            
            logger.info("Profile updated successfully for user: {}", username);
            return success("Profile updated successfully", updatedUser);
            
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
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(Pageable pageable) {
        try {
            logger.info("Fetching all users with pagination");
            
            Page<User> users = userService.getAllUsers(pageable);
            
            logger.info("Users fetched successfully, count: {}", users.getTotalElements());
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
     * Search users by keyword - Admin/HR/Manager only
     * GET /api/users/search?keyword={keyword}
     */
    @GetMapping("/search")
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
     * Update user role - Admin only
     * PUT /api/users/{userId}/role
     */
    @PutMapping("/{userId}/role")
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
     * Assign user to department - Admin/HR only
     * PUT /api/users/{userId}/department
     */
    @PutMapping("/{userId}/department")
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
     * Delete user (soft delete) - Admin only
     * DELETE /api/users/{userId}
     */
    @DeleteMapping("/{userId}")
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
            
            // This will be implemented when Stream A completes
            // return jwtTokenProvider.getUsernameFromToken(token);
            return "temp-user"; // Temporary placeholder
        }
        return null;
    }
}