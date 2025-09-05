package com.weeklyreport.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter that processes JWT tokens from HTTP requests
 * This filter runs once per request and validates JWT tokens from the Authorization header
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        logger.debug("Processing authentication for request: {}", requestURI);
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                if (jwtTokenProvider.validateToken(jwt)) {
                    // Only process access tokens for authentication
                    if (jwtTokenProvider.getTokenType(jwt) == JwtTokenProvider.TokenType.ACCESS) {
                        Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        logger.debug("Successfully authenticated user: {} for request: {}", 
                                   authentication.getName(), requestURI);
                    } else {
                        logger.debug("Ignoring non-access token for authentication");
                    }
                } else {
                    logger.debug("Invalid JWT token for request: {}", requestURI);
                    SecurityContextHolder.clearContext();
                }
            } else {
                logger.debug("No JWT token found in request: {}", requestURI);
            }
        } catch (Exception ex) {
            logger.error("Cannot set user authentication for request {}: {}", requestURI, ex.getMessage());
            SecurityContextHolder.clearContext();
            
            // For debugging purposes - can be removed in production
            if (logger.isDebugEnabled()) {
                logger.debug("Authentication error details", ex);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from the Authorization header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Check if request should be filtered
     * Override this method to skip filtering for specific endpoints
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Skip JWT processing for these paths
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/actuator/") ||
               path.startsWith("/api/h2-console/") ||
               path.equals("/api/health") ||
               path.equals("/api/") ||
               path.startsWith("/api/public/");
    }
}