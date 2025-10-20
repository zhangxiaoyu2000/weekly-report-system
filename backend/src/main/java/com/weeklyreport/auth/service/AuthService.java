package com.weeklyreport.auth.service;

import com.weeklyreport.auth.dto.*;
import com.weeklyreport.user.entity.User;
import com.weeklyreport.user.repository.UserRepository;
// import com.weeklyreport.repository.DepartmentRepository; // 简化版本中不需要
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private com.weeklyreport.core.security.JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TokenStoreService tokenStoreService;

    /**
     * Authenticate user with username/email and password
     */
    public AuthResponse login(LoginRequest loginRequest) {
        String credential = normalize(loginRequest.getUsernameOrEmail());
        String rawPassword = normalize(loginRequest.getPassword());

        if (credential == null || credential.isEmpty()) {
            throw new BadCredentialsException("用户名或邮箱不能为空");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new BadCredentialsException("密码不能为空");
        }

        logger.info("Attempting login for user: {}", credential);

        // Find user by username or email
        User user = findUserByUsernameOrEmail(credential)
                .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));

        // Check user status - only allow active users to login
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            switch (user.getStatus()) {
                case INACTIVE:
                    throw new BadCredentialsException("账户已被停用");
                default:
                    throw new BadCredentialsException("账户不可用");
            }
        }

        // Verify password
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            logger.warn("Invalid password attempt for user: {}", user.getUsername());
            throw new BadCredentialsException("用户名/邮箱或密码错误");
        }

        // Last login tracking disabled in simplified version
        // user.setLastLogin(LocalDateTime.now());
        // userRepository.save(user);

        // Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        long accessTtl = jwtTokenProvider.getAccessTokenValidityInSeconds();
        long refreshTtl = jwtTokenProvider.getRefreshTokenValidityInSeconds();
        Long expiresIn = accessTtl;

        // Store tokens in Redis for verification and revocation support
        tokenStoreService.storeAccessToken(user.getUsername(), accessToken, accessTtl);
        tokenStoreService.storeRefreshToken(user.getUsername(), refreshToken, refreshTtl);

        logger.info("User {} logged in successfully", user.getUsername());
        return AuthResponse.success(accessToken, refreshToken, expiresIn, user);
    }

    /**
     * Register new user
     */
    public AuthResponse register(RegisterRequest registerRequest) {
        String username = normalize(registerRequest.getUsername());
        String email = normalize(registerRequest.getEmail());
        String password = normalize(registerRequest.getPassword());

        logger.info("Attempting registration for user: {}", username);

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        // Update request payload with normalized values for downstream usage/logging
        registerRequest.setUsername(username);
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);

        // Password confirmation validation removed as method doesn't exist in RegisterRequest
        // This should be handled on the frontend or a confirmPassword field should be added to RegisterRequest

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        // Employee ID check disabled in simplified entity

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
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
        long accessTtl = jwtTokenProvider.getAccessTokenValidityInSeconds();
        long refreshTtl = jwtTokenProvider.getRefreshTokenValidityInSeconds();
        Long expiresIn = accessTtl;

        tokenStoreService.storeAccessToken(user.getUsername(), accessToken, accessTtl);
        tokenStoreService.storeRefreshToken(user.getUsername(), refreshToken, refreshTtl);

        logger.info("User {} registered successfully", user.getUsername());
        return AuthResponse.success(accessToken, refreshToken, expiresIn, user);
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        logger.info("Attempting to refresh token");

        String refreshToken = normalize(refreshTokenRequest.getRefreshToken());
        if (!StringUtils.hasText(refreshToken)) {
            throw new BadCredentialsException("刷新令牌不能为空");
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            tokenStoreService.removeRefreshToken(refreshToken);
            throw new BadCredentialsException("无效的刷新令牌");
        }

        com.weeklyreport.core.security.JwtTokenProvider.TokenType tokenType =
                jwtTokenProvider.getTokenType(refreshToken);
        if (tokenType != com.weeklyreport.core.security.JwtTokenProvider.TokenType.REFRESH) {
            throw new BadCredentialsException("提供的令牌不是刷新令牌");
        }

        if (!tokenStoreService.isRefreshTokenValid(refreshToken)) {
            throw new BadCredentialsException("刷新令牌已被撤销或已过期");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("用户不存在"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            switch (user.getStatus()) {
                case INACTIVE:
                    throw new BadCredentialsException("账户已被停用");
                default:
                    throw new BadCredentialsException("账户不可用");
            }
        }

        // Revoke existing tokens for the user before issuing new ones
        tokenStoreService.revokeUserTokens(user.getUsername());

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        long accessTtl = jwtTokenProvider.getAccessTokenValidityInSeconds();
        long refreshTtl = jwtTokenProvider.getRefreshTokenValidityInSeconds();
        Long expiresIn = accessTtl;

        tokenStoreService.storeAccessToken(user.getUsername(), accessToken, accessTtl);
        tokenStoreService.storeRefreshToken(user.getUsername(), newRefreshToken, refreshTtl);

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
        
        String normalizedToken = normalize(accessToken);
        if (normalizedToken != null && !normalizedToken.isEmpty()) {
            String usernameFromStore = tokenStoreService.removeAccessToken(normalizedToken);

            if (StringUtils.hasText(usernameFromStore)) {
                tokenStoreService.revokeUserTokens(usernameFromStore);
            } else if (jwtTokenProvider.validateToken(normalizedToken) &&
                    jwtTokenProvider.getTokenType(normalizedToken) ==
                            com.weeklyreport.core.security.JwtTokenProvider.TokenType.ACCESS) {
                String username = jwtTokenProvider.getUsernameFromToken(normalizedToken);
                if (StringUtils.hasText(username)) {
                    tokenStoreService.revokeUserTokens(username);
                }
            }
        }

        logger.info("User logged out successfully");
    }

    /**
     * Change user password
     */
    public void changePassword(String username, ChangePasswordRequest changePasswordRequest) {
        logger.info("Attempting password change for user: {}", username);

        // Validate new password confirmation
        if (!changePasswordRequest.isNewPasswordMatching()) {
            throw new IllegalArgumentException("新密码确认不匹配");
        }

        // Find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // Verify current password
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("当前密码错误");
        }

        // Check if new password is different from current
        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("新密码必须与当前密码不同");
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
        String value = normalize(usernameOrEmail);

        if (value == null || value.isEmpty()) {
            return Optional.empty();
        }

        // Try to find by username first
        Optional<User> user = userRepository.findByUsername(value);
        
        // If not found, try by email
        if (user.isEmpty()) {
            user = userRepository.findByEmail(value);
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

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
