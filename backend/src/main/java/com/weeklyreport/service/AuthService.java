package com.weeklyreport.service;

import com.weeklyreport.dto.auth.*;
import com.weeklyreport.entity.User;
import com.weeklyreport.repository.UserRepository;
// import com.weeklyreport.repository.DepartmentRepository; // 简化版本中不需要
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Authentication service for user login, registration and token management
 */
@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    // @Autowired
    // private DepartmentRepository departmentRepository; // 简化版本中不需要

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.weeklyreport.security.JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticate user with username/email and password
     */
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("Attempting login for user: {}", loginRequest.getUsernameOrEmail());

        // Find user by username or email
        User user = findUserByUsernameOrEmail(loginRequest.getUsernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));

        // Check user status - only allow active users to login
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            switch (user.getStatus()) {
                case INACTIVE:
                    throw new BadCredentialsException("Account has been deactivated");
                default:
                    throw new BadCredentialsException("Account is not available");
            }
        }

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("Invalid password attempt for user: {}", user.getUsername());
            throw new BadCredentialsException("Invalid username/email or password");
        }

        // Last login tracking disabled in simplified version
        // user.setLastLogin(LocalDateTime.now());
        // userRepository.save(user);

        // Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        Long expiresIn = jwtTokenProvider.getAccessTokenValidityInSeconds();

        logger.info("User {} logged in successfully", user.getUsername());
        return AuthResponse.success(accessToken, refreshToken, expiresIn, user);
    }

    /**
     * Register new user
     */
    public AuthResponse register(RegisterRequest registerRequest) {
        logger.info("Attempting registration for user: {}", registerRequest.getUsername());

        // Validate password confirmation
        if (!registerRequest.isPasswordMatching()) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Employee ID check disabled in simplified entity

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        // Set first and last name instead of full name
        // Full name fields not available in simplified entity
        // Phone, position, employeeId not supported in simplified entity
        user.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : User.Role.MANAGER);
        user.setStatus(User.UserStatus.ACTIVE);

        // Set department if provided (simplified version - skip department assignment)
        if (registerRequest.getDepartmentId() != null) {
            // 简化版本中不支持部门管理，跳过部门分配
            logger.info("Department assignment skipped in simplified version");
        }

        // Save user
        user = userRepository.save(user);

        // Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        Long expiresIn = jwtTokenProvider.getAccessTokenValidityInSeconds();

        logger.info("User {} registered successfully", user.getUsername());
        return AuthResponse.success(accessToken, refreshToken, expiresIn, user);
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        logger.info("Attempting to refresh token");

        // Validate refresh token (will be implemented when Stream A completes)
        // String username = jwtTokenProvider.getUsernameFromToken(refreshTokenRequest.getRefreshToken());
        // if (!jwtTokenProvider.validateToken(refreshTokenRequest.getRefreshToken())) {
        //     throw new BadCredentialsException("Invalid refresh token");
        // }

        // For now, return a temporary response
        String username = "temp-user"; // This will be extracted from the JWT token
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        // Check user status - only allow active users to refresh tokens
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            switch (user.getStatus()) {
                case INACTIVE:
                    throw new BadCredentialsException("Account has been deactivated");
                default:
                    throw new BadCredentialsException("Account is not available");
            }
        }

        // Generate new tokens
        String accessToken = "temp-access-token"; // jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = "temp-refresh-token"; // jwtTokenProvider.generateRefreshToken(user);
        Long expiresIn = 3600L; // jwtTokenProvider.getAccessTokenValidityInSeconds();

        logger.info("Token refreshed successfully for user: {}", user.getUsername());
        return AuthResponse.success(accessToken, newRefreshToken, expiresIn, user);
    }

    /**
     * Logout user (invalidate tokens)
     */
    public void logout(String accessToken) {
        logger.info("Attempting logout");

        // Extract username from token (will be implemented when Stream A completes)
        // String username = jwtTokenProvider.getUsernameFromToken(accessToken);
        
        // Add token to blacklist or invalidate in some way
        // jwtTokenProvider.invalidateToken(accessToken);

        logger.info("User logged out successfully");
    }

    /**
     * Change user password
     */
    public void changePassword(String username, ChangePasswordRequest changePasswordRequest) {
        logger.info("Attempting password change for user: {}", username);

        // Validate new password confirmation
        if (!changePasswordRequest.isNewPasswordMatching()) {
            throw new IllegalArgumentException("New password confirmation does not match");
        }

        // Find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Check if new password is different from current
        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);

        logger.info("Password changed successfully for user: {}", username);
    }

    /**
     * Find user by username or email
     */
    private Optional<User> findUserByUsernameOrEmail(String usernameOrEmail) {
        // Try to find by username first
        Optional<User> user = userRepository.findByUsername(usernameOrEmail);
        
        // If not found, try by email
        if (user.isEmpty()) {
            user = userRepository.findByEmail(usernameOrEmail);
        }
        
        return user;
    }

    /**
     * Validate if user can register (additional business logic)
     */
    private void validateRegistration(RegisterRequest registerRequest) {
        // Additional validation logic can be added here
        // For example: domain restrictions, invitation codes, etc.
    }

    /**
     * Check if registration is enabled
     * This can be controlled by application properties
     */
    public boolean isRegistrationEnabled() {
        // This should be configurable via application.yml
        return true;
    }
}