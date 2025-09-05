package com.weeklyreport.controller;

import com.weeklyreport.dto.ApiResponse;
import com.weeklyreport.dto.auth.*;
import com.weeklyreport.service.AuthService;
import com.weeklyreport.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST Controller
 * Handles user authentication operations: login, register, refresh, logout
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    /**
     * User login endpoint
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login attempt for user: {}", loginRequest.getUsernameOrEmail());
            
            AuthResponse authResponse = authService.login(loginRequest);
            
            logger.info("Login successful for user: {}", loginRequest.getUsernameOrEmail());
            return success("Login successful", authResponse);
            
        } catch (BadCredentialsException e) {
            logger.warn("Login failed for user: {} - {}", loginRequest.getUsernameOrEmail(), e.getMessage());
            return error("Invalid username/email or password", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("Login error for user: {} - {}", loginRequest.getUsernameOrEmail(), e.getMessage(), e);
            return error("Login failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * User registration endpoint
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            logger.info("Registration attempt for user: {}", registerRequest.getUsername());

            // Check if registration is enabled
            if (!authService.isRegistrationEnabled()) {
                return error("Registration is currently disabled", HttpStatus.FORBIDDEN);
            }

            // Validate password confirmation
            if (!registerRequest.isPasswordMatching()) {
                return error("Password confirmation does not match", HttpStatus.BAD_REQUEST);
            }
            
            AuthResponse authResponse = authService.register(registerRequest);
            
            logger.info("Registration successful for user: {}", registerRequest.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Registration successful", authResponse));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed for user: {} - {}", registerRequest.getUsername(), e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Registration error for user: {} - {}", registerRequest.getUsername(), e.getMessage(), e);
            return error("Registration failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Refresh access token endpoint
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            logger.info("Token refresh attempt");
            
            AuthResponse authResponse = authService.refreshToken(refreshTokenRequest);
            
            logger.info("Token refresh successful");
            return success("Token refreshed successfully", authResponse);
            
        } catch (BadCredentialsException e) {
            logger.warn("Token refresh failed - {}", e.getMessage());
            return error("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("Token refresh error - {}", e.getMessage(), e);
            return error("Token refresh failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * User logout endpoint
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        try {
            logger.info("Logout attempt");
            
            // Extract token from Authorization header
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return error("Authorization token required", HttpStatus.BAD_REQUEST);
            }
            
            authService.logout(token);
            
            logger.info("Logout successful");
            return success("Logout successful", "");
            
        } catch (Exception e) {
            logger.error("Logout error - {}", e.getMessage(), e);
            return error("Logout failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Change password endpoint
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest,
            HttpServletRequest request) {
        try {
            logger.info("Password change attempt");

            // Extract username from token (will be implemented when Stream A completes)
            String username = extractUsernameFromRequest(request);
            if (username == null) {
                return error("Authentication required", HttpStatus.UNAUTHORIZED);
            }

            // Validate new password confirmation
            if (!changePasswordRequest.isNewPasswordMatching()) {
                return error("New password confirmation does not match", HttpStatus.BAD_REQUEST);
            }
            
            authService.changePassword(username, changePasswordRequest);
            
            logger.info("Password changed successfully for user: {}", username);
            return success("Password changed successfully", "");
            
        } catch (BadCredentialsException e) {
            logger.warn("Password change failed - {}", e.getMessage());
            return error("Current password is incorrect", HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            logger.warn("Password change failed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Password change error - {}", e.getMessage(), e);
            return error("Password change failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if username is available
     * GET /api/auth/check-username?username={username}
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(@RequestParam String username) {
        try {
            // Basic validation
            if (username == null || username.trim().isEmpty()) {
                return error("Username is required", HttpStatus.BAD_REQUEST);
            }

            if (username.length() < 3 || username.length() > 50) {
                return error("Username must be between 3 and 50 characters", HttpStatus.BAD_REQUEST);
            }

            // Check if username matches pattern
            if (!username.matches("^[a-zA-Z0-9_]+$")) {
                return error("Username can only contain letters, numbers and underscores", HttpStatus.BAD_REQUEST);
            }

            boolean available = !authService.isRegistrationEnabled() ? false : 
                               userService.isUsernameAvailable(username);
            
            return success(available ? "Username is available" : "Username is already taken", available);
                
        } catch (Exception e) {
            logger.error("Error checking username availability - {}", e.getMessage(), e);
            return error("Server error while checking username availability", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if email is available
     * GET /api/auth/check-email?email={email}
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(@RequestParam String email) {
        try {
            // Basic validation
            if (email == null || email.trim().isEmpty()) {
                return error("Email is required", HttpStatus.BAD_REQUEST);
            }

            // Simple email pattern check
            if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
                return error("Invalid email format", HttpStatus.BAD_REQUEST);
            }

            boolean available = !authService.isRegistrationEnabled() ? false : 
                               userService.isEmailAvailable(email);
            
            return success(available ? "Email is available" : "Email is already registered", available);
                
        } catch (Exception e) {
            logger.error("Error checking email availability - {}", e.getMessage(), e);
            return error("Server error while checking email availability", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Extract username from JWT token in request
     * This will be properly implemented when Stream A completes the JWT provider
     */
    private String extractUsernameFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        
        // This will be implemented when Stream A completes
        // return jwtTokenProvider.getUsernameFromToken(token);
        return "temp-user"; // Temporary placeholder
    }
}