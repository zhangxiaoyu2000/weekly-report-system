package com.weeklyreport.security;

import com.weeklyreport.entity.User;
import com.weeklyreport.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomUserDetailsService
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setRole(User.Role.EMPLOYEE);
        testUser.setStatus(User.UserStatus.ACTIVE);
    }

    @Test
    void testLoadUserByUsernameSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });
        
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void testLoadUserByEmailSuccess() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByEmail("test@example.com");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testLoadUserByIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserById(1L);

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        
        verify(userRepository).findById(1L);
    }

    @Test
    void testUserAccountValidation() {
        // Test active user
        assertTrue(userDetailsService.isUserAccountValid(testUser));

        // Test inactive user
        testUser.setStatus(User.UserStatus.INACTIVE);
        assertFalse(userDetailsService.isUserAccountValid(testUser));

        // Test locked user
        testUser.setStatus(User.UserStatus.LOCKED);
        assertFalse(userDetailsService.isUserAccountValid(testUser));

        // Test deleted user
        testUser.setStatus(User.UserStatus.DELETED);
        assertFalse(userDetailsService.isUserAccountValid(testUser));

        // Test null user
        assertFalse(userDetailsService.isUserAccountValid(null));
    }

    @Test
    void testUserWithDifferentRoles() {
        // Test admin user
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setStatus(User.UserStatus.ACTIVE);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        UserDetails adminDetails = userDetailsService.loadUserByUsername("admin");
        CustomUserPrincipal adminPrincipal = (CustomUserPrincipal) adminDetails;

        assertTrue(adminPrincipal.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(adminPrincipal.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ADMIN_READ")));
        assertTrue(adminPrincipal.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("USER_MANAGEMENT")));
    }

    @Test
    void testEmployeePermissions() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) userDetails;

        assertTrue(userPrincipal.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYEE")));
        assertTrue(userPrincipal.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("REPORT_READ")));
        assertTrue(userPrincipal.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("REPORT_WRITE")));
        assertTrue(userPrincipal.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("PROFILE_READ")));
    }

    @Test
    void testGetUserByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User user = userDetailsService.getUserByUsername("testuser");

        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testGetUserByEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User user = userDetailsService.getUserByEmail("test@example.com");

        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testLockedUserAccountFlags() {
        testUser.setStatus(User.UserStatus.LOCKED);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertTrue(userDetails.isEnabled());  // Still enabled but locked
        assertFalse(userDetails.isAccountNonLocked());  // Account is locked
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    void testDeletedUserAccountFlags() {
        testUser.setStatus(User.UserStatus.DELETED);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertTrue(userDetails.isEnabled());  // Still enabled
        assertTrue(userDetails.isAccountNonLocked());  
        assertFalse(userDetails.isAccountNonExpired());  // Account is expired (deleted)
        assertTrue(userDetails.isCredentialsNonExpired());
    }
}