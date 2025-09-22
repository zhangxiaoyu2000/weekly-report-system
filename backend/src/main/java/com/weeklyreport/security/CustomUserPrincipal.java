package com.weeklyreport.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Custom user principal that implements Spring Security's UserDetails interface
 * Contains additional user information beyond the standard UserDetails
 */
public class CustomUserPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final String email;
    private final String fullName;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;

    public CustomUserPrincipal(Long userId, String username, String email, String fullName,
                              Collection<? extends GrantedAuthority> authorities) {
        this(userId, username, email, fullName, authorities, true, true, true, true);
    }

    public CustomUserPrincipal(Long userId, String username, String email, String fullName,
                              Collection<? extends GrantedAuthority> authorities,
                              boolean enabled, boolean accountNonExpired,
                              boolean accountNonLocked, boolean credentialsNonExpired) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // Password should not be stored in the principal for security reasons
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // Additional getters for custom fields
    public Long getUserId() {
        return userId;
    }
    
    public Long getId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }
    
    public boolean isAdmin() {
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomUserPrincipal)) return false;
        CustomUserPrincipal that = (CustomUserPrincipal) o;
        return userId != null && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CustomUserPrincipal{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", authorities=" + authorities +
                ", enabled=" + enabled +
                '}';
    }
}