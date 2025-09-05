package com.weeklyreport.service;

import com.weeklyreport.dto.auth.*;
import com.weeklyreport.entity.User;
import com.weeklyreport.repository.UserRepository;
import com.weeklyreport.repository.DepartmentRepository;
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

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Note: JwtTokenProvider will be injected when Stream A completes the security configuration
    // @Autowired
    // private JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticate user with username/email and password
     */
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("Attempting login for user: {}", loginRequest.getUsernameOrEmail());

        // Find user by username or email
        User user = findUserByUsernameOrEmail(loginRequest.getUsernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));

        // Check user status
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BadCredentialsException("Account is not active");
        }

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("Invalid password attempt for user: {}", user.getUsername());
            throw new BadCredentialsException("Invalid username/email or password");
        }

        // Update last login time
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT tokens (will be implemented when Stream A completes)
        String accessToken = "temp-access-token"; // jwtTokenProvider.generateAccessToken(user);
        String refreshToken = "temp-refresh-token"; // jwtTokenProvider.generateRefreshToken(user);
        Long expiresIn = 3600L; // jwtTokenProvider.getAccessTokenValidityInSeconds();

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

        // Check if employee ID already exists (if provided)
        if (registerRequest.getEmployeeId() != null && 
            !registerRequest.getEmployeeId().isEmpty() &&
            userRepository.existsByEmployeeId(registerRequest.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setEmployeeId(registerRequest.getEmployeeId());
        user.setPhone(registerRequest.getPhone());
        user.setPosition(registerRequest.getPosition());
        user.setRole(User.Role.EMPLOYEE); // Default role
        user.setStatus(User.UserStatus.ACTIVE);

        // Set department if provided
        if (registerRequest.getDepartmentId() != null) {
            departmentRepository.findById(registerRequest.getDepartmentId())
                    .ifPresent(user::setDepartment);
        }

        // Save user
        user = userRepository.save(user);

        // Generate JWT tokens (will be implemented when Stream A completes)
        String accessToken = "temp-access-token"; // jwtTokenProvider.generateAccessToken(user);
        String refreshToken = "temp-refresh-token"; // jwtTokenProvider.generateRefreshToken(user);
        Long expiresIn = 3600L; // jwtTokenProvider.getAccessTokenValidityInSeconds();

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

        // Check user status
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BadCredentialsException("Account is not active");
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