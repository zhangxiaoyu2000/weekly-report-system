package com.weeklyreport.core.security;

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

    @Autowired
    private com.weeklyreport.auth.service.TokenStoreService tokenStoreService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        logger.debug("Processing authentication for request: {}", requestURI);
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                logger.debug("JWT token found in request {}, length: {}", requestURI, jwt.length());
                
                boolean signatureValid = jwtTokenProvider.validateToken(jwt);
                if (signatureValid && !tokenStoreService.isAccessTokenValid(jwt)) {
                    logger.warn("Access token not found in Redis (possibly revoked) for request: {}", requestURI);
                    signatureValid = false;
                }

                if (signatureValid) {
                    logger.debug("JWT token validation successful for request: {}", requestURI);
                    
                    // Only process access tokens for authentication
                    JwtTokenProvider.TokenType tokenType = jwtTokenProvider.getTokenType(jwt);
                    logger.debug("Token type: {} for request: {}", tokenType, requestURI);
                    
                    if (tokenType == JwtTokenProvider.TokenType.ACCESS) {
                        Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        logger.debug("Successfully authenticated user: {} for request: {}", 
                                   authentication.getName(), requestURI);
                        logger.debug("User authorities: {}", authentication.getAuthorities());
                    } else {
                        logger.debug("Ignoring non-access token for authentication: {}", tokenType);
                    }
                } else {
                    logger.warn("JWT token validation failed for request: {}", requestURI);
                    logger.debug("Invalid token details: prefix={}, length={}", 
                               jwt.substring(0, Math.min(10, jwt.length())), jwt.length());
                    SecurityContextHolder.clearContext();
                }
            } else {
                logger.debug("No JWT token found in request: {}", requestURI);
                logger.debug("Authorization header: {}", request.getHeader(AUTHORIZATION_HEADER));
            }
        } catch (Exception ex) {
            logger.error("JWT认证异常 - 请求: {}, 异常类型: {}, 详细信息: {}", 
                        requestURI, ex.getClass().getSimpleName(), ex.getMessage());
            
            // 详细的认证错误诊断
            logger.error("=== JWT认证错误诊断 ===");
            logger.error("请求URI: {}", requestURI);
            logger.error("请求方法: {}", request.getMethod());
            logger.error("Authorization头: {}", request.getHeader(AUTHORIZATION_HEADER));
            logger.error("用户代理: {}", request.getHeader("User-Agent"));
            logger.error("远程地址: {}", request.getRemoteAddr());
            
            try {
                String jwt = getJwtFromRequest(request);
                if (jwt != null) {
                    logger.error("提取的JWT长度: {}", jwt.length());
                    logger.error("JWT前缀: {}", jwt.substring(0, Math.min(20, jwt.length())));
                }
            } catch (Exception jwtEx) {
                logger.error("无法分析JWT: {}", jwtEx.getMessage());
            }
            
            logger.error("=== JWT认证错误诊断结束 ===");
            
            SecurityContextHolder.clearContext();
            
            if (logger.isDebugEnabled()) {
                logger.debug("Authentication error stack trace", ex);
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
        
        // Remove context path for matching
        if (path.startsWith("/api")) {
            path = path.substring(4); // Remove "/api" prefix
        }
        
        // Skip JWT processing for these paths
        return path.startsWith("/auth/") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/health") ||  // Allow all health paths
               path.equals("/") ||
               path.startsWith("/public/");
    }
}
