package com.weeklyreport.security;

import com.weeklyreport.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for generating, validating and parsing JWT tokens
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String AUTHORITIES_KEY = "roles";
    private static final String USER_ID_KEY = "userId";
    private static final String FULL_NAME_KEY = "fullName";
    private static final String EMAIL_KEY = "email";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration:900000}") // 15 minutes
    private long accessTokenValidityInMilliseconds;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7 days
    private long refreshTokenValidityInMilliseconds;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate access token for authenticated user
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateAccessToken(userPrincipal.getUsername(), authentication.getAuthorities(), null);
    }

    /**
     * Generate access token for user entity
     */
    public String generateAccessToken(User user) {
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        return generateAccessToken(user.getUsername(), authorities, user);
    }

    /**
     * Generate access token with user details
     */
    private String generateAccessToken(String username, Collection<? extends GrantedAuthority> authorities, User user) {
        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .claim(AUTHORITIES_KEY, roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512);

        // Add additional user claims if user entity is provided
        if (user != null) {
            builder.claim(USER_ID_KEY, user.getId())
                   .claim(FULL_NAME_KEY, user.getFullName())
                   .claim(EMAIL_KEY, user.getEmail());
        }

        return builder.compact();
    }

    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Get username from JWT token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Get user ID from JWT token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Object userIdClaim = claims.get(USER_ID_KEY);
        if (userIdClaim instanceof Number) {
            return ((Number) userIdClaim).longValue();
        }
        return null;
    }

    /**
     * Get authorities from JWT token
     */
    public Collection<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String roles = claims.get(AUTHORITIES_KEY).toString();
        
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        
        logger.debug("Extracted authorities from token: {} for roles: {}", authorities, roles);
        
        return authorities;
    }

    /**
     * Create authentication object from JWT token
     */
    public Authentication getAuthentication(String token) {
        String username = getUsernameFromToken(token);
        Collection<? extends GrantedAuthority> authorities = getAuthoritiesFromToken(token);
        
        CustomUserPrincipal userPrincipal = new CustomUserPrincipal(
            getUserIdFromToken(token),
            username,
            getClaimFromToken(token, EMAIL_KEY),
            getClaimFromToken(token, FULL_NAME_KEY),
            authorities
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(userPrincipal, token, authorities);
        logger.debug("Created authentication for user {} with authorities: {}", username, authorities);
        
        return auth;
    }

    /**
     * Get specific claim from JWT token
     */
    private String getClaimFromToken(String token, String claimKey) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Object claim = claims.get(claimKey);
        return claim != null ? claim.toString() : null;
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        } catch (Exception ex) {
            logger.error("Error checking token expiration: {}", ex.getMessage());
            return true;
        }
    }

    /**
     * Get expiration date from token
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }

    /**
     * Get token type (access or refresh) based on expiration time
     */
    public TokenType getTokenType(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            Date issued = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getIssuedAt();
            
            long duration = expiration.getTime() - issued.getTime();
            
            // Determine token type based on duration
            if (duration <= accessTokenValidityInMilliseconds + 60000) { // +1 minute tolerance
                return TokenType.ACCESS;
            } else {
                return TokenType.REFRESH;
            }
        } catch (Exception ex) {
            logger.error("Error determining token type: {}", ex.getMessage());
            return TokenType.UNKNOWN;
        }
    }

    /**
     * Token type enumeration
     */
    /**
     * Get access token validity in seconds
     */
    public Long getAccessTokenValidityInSeconds() {
        return accessTokenValidityInMilliseconds / 1000;
    }

    /**
     * Get refresh token validity in seconds
     */
    public Long getRefreshTokenValidityInSeconds() {
        return refreshTokenValidityInMilliseconds / 1000;
    }

    public enum TokenType {
        ACCESS, REFRESH, UNKNOWN
    }
}