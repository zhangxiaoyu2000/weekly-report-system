package com.weeklyreport.security;

import com.weeklyreport.entity.User;
import com.weeklyreport.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetailsService implementation that loads user information from the database
 * Integrates with the User entity and provides role-based authorities
 */
@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        return createUserPrincipal(user);
    }

    /**
     * Load user by email address
     */
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        return createUserPrincipal(user);
    }

    /**
     * Load user by user ID
     */
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        logger.debug("Loading user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new UsernameNotFoundException("User not found with ID: " + id);
                });

        return createUserPrincipal(user);
    }

    /**
     * Create CustomUserPrincipal from User entity
     */
    private CustomUserPrincipal createUserPrincipal(User user) {
        Collection<GrantedAuthority> authorities = getAuthorities(user);
        
        boolean enabled = user.getStatus() == User.UserStatus.ACTIVE;
        boolean accountNonLocked = user.getStatus() != User.UserStatus.LOCKED;
        boolean accountNonExpired = user.getStatus() != User.UserStatus.DELETED;
        boolean credentialsNonExpired = true; // Could be enhanced with password expiration logic

        logger.debug("Created user principal for user: {} with authorities: {}", 
                    user.getUsername(), authorities);

        return new CustomUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                authorities,
                enabled,
                accountNonExpired,
                accountNonLocked,
                credentialsNonExpired
        );
    }

    /**
     * Get granted authorities for the user based on their role
     */
    private Collection<GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role-based authority
        String roleAuthority = "ROLE_" + user.getRole().name();
        authorities.add(new SimpleGrantedAuthority(roleAuthority));
        
        // Add additional permissions based on role
        switch (user.getRole()) {
            case ADMIN:
                authorities.add(new SimpleGrantedAuthority("ADMIN_READ"));
                authorities.add(new SimpleGrantedAuthority("ADMIN_WRITE"));
                authorities.add(new SimpleGrantedAuthority("USER_MANAGEMENT"));
                authorities.add(new SimpleGrantedAuthority("SYSTEM_CONFIG"));
                // Admin has all permissions (fallthrough)
                
            case HR_MANAGER:
                authorities.add(new SimpleGrantedAuthority("HR_READ"));
                authorities.add(new SimpleGrantedAuthority("HR_WRITE"));
                authorities.add(new SimpleGrantedAuthority("ALL_REPORTS_READ"));
                authorities.add(new SimpleGrantedAuthority("EMPLOYEE_MANAGEMENT"));
                // HR Manager has department permissions (fallthrough)
                
            case DEPARTMENT_MANAGER:
                authorities.add(new SimpleGrantedAuthority("DEPARTMENT_READ"));
                authorities.add(new SimpleGrantedAuthority("DEPARTMENT_WRITE"));
                authorities.add(new SimpleGrantedAuthority("DEPARTMENT_REPORTS_READ"));
                authorities.add(new SimpleGrantedAuthority("TEAM_MANAGEMENT"));
                // Department Manager has team leader permissions (fallthrough)
                
            case TEAM_LEADER:
                authorities.add(new SimpleGrantedAuthority("TEAM_READ"));
                authorities.add(new SimpleGrantedAuthority("TEAM_REPORTS_READ"));
                authorities.add(new SimpleGrantedAuthority("REPORT_REVIEW"));
                // Team Leader has employee permissions (fallthrough)
                
            case EMPLOYEE:
                authorities.add(new SimpleGrantedAuthority("REPORT_READ"));
                authorities.add(new SimpleGrantedAuthority("REPORT_WRITE"));
                authorities.add(new SimpleGrantedAuthority("PROFILE_READ"));
                authorities.add(new SimpleGrantedAuthority("PROFILE_WRITE"));
                break;
                
            default:
                logger.warn("Unknown role for user {}: {}", user.getUsername(), user.getRole());
                // Only basic permissions for unknown roles
                authorities.add(new SimpleGrantedAuthority("PROFILE_READ"));
                break;
        }
        
        logger.debug("Assigned authorities for user {} with role {}: {}", 
                    user.getUsername(), user.getRole(), authorities);
                    
        return authorities;
    }

    /**
     * Check if user account is active and can be authenticated
     */
    public boolean isUserAccountValid(User user) {
        return user != null && 
               user.getStatus() == User.UserStatus.ACTIVE;
    }

    /**
     * Get User entity by username (for service layer usage)
     */
    public User getUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    /**
     * Get User entity by email (for service layer usage)
     */
    public User getUserByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}