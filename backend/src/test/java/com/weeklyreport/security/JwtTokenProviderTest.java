package com.weeklyreport.security;

import com.weeklyreport.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider
 */
@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=testSecretKey",
    "jwt.access-token-expiration=3600000",  // 1 hour for testing
    "jwt.refresh-token-expiration=86400000" // 1 day for testing
})
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        
        // Set test properties using reflection
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "testSecretKey");
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenValidityInMilliseconds", 3600000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenValidityInMilliseconds", 86400000L);

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setRole(User.Role.EMPLOYEE);
        testUser.setStatus(User.UserStatus.ACTIVE);
    }

    @Test
    void testGenerateAccessTokenForUser() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGenerateRefreshToken() {
        String refreshToken = jwtTokenProvider.generateRefreshToken(testUser.getUsername());
        
        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 0);
    }

    @Test
    void testValidateValidToken() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        assertTrue(isValid);
    }

    @Test
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        
        assertFalse(isValid);
    }

    @Test
    void testGetUsernameFromToken() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        
        String username = jwtTokenProvider.getUsernameFromToken(token);
        
        assertEquals(testUser.getUsername(), username);
    }

    @Test
    void testGetUserIdFromToken() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        
        assertEquals(testUser.getId(), userId);
    }

    @Test
    void testGetAuthenticationFromToken() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        
        assertNotNull(authentication);
        assertEquals(testUser.getUsername(), authentication.getName());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
    }

    @Test
    void testGetExpirationFromToken() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        
        Date expiration = jwtTokenProvider.getExpirationFromToken(token);
        
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testTokenTypeDetection() {
        String accessToken = jwtTokenProvider.generateAccessToken(testUser);
        String refreshToken = jwtTokenProvider.generateRefreshToken(testUser.getUsername());
        
        JwtTokenProvider.TokenType accessType = jwtTokenProvider.getTokenType(accessToken);
        JwtTokenProvider.TokenType refreshType = jwtTokenProvider.getTokenType(refreshToken);
        
        assertEquals(JwtTokenProvider.TokenType.ACCESS, accessType);
        assertEquals(JwtTokenProvider.TokenType.REFRESH, refreshType);
    }

    @Test
    void testIsTokenExpired() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);
        
        assertFalse(isExpired);
    }

    @Test
    void testDifferentRoles() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setFullName("Admin User");
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setStatus(User.UserStatus.ACTIVE);

        String adminToken = jwtTokenProvider.generateAccessToken(adminUser);
        Authentication adminAuth = jwtTokenProvider.getAuthentication(adminToken);
        
        assertNotNull(adminAuth);
        assertTrue(adminAuth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}