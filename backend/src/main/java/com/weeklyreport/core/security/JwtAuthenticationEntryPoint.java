package com.weeklyreport.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Authentication Entry Point that handles authentication failures
 * Returns a custom JSON error response when authentication fails
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        logger.error("Unauthorized access attempt to: {} from IP: {}. Error: {}", 
                    request.getRequestURI(), 
                    getClientIpAddress(request),
                    authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = createErrorResponse(request, authException);
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Create standardized error response
     */
    private Map<String, Object> createErrorResponse(HttpServletRequest request, 
                                                   AuthenticationException authException) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "未授权");
        errorResponse.put("message", "身份验证失败，请重新登录");
        errorResponse.put("path", request.getRequestURI());
        
        // Add more context based on the type of authentication exception
        String errorCode = getErrorCode(authException);
        if (errorCode != null) {
            errorResponse.put("errorCode", errorCode);
        }
        
        return errorResponse;
    }

    /**
     * Get specific error code based on exception type
     */
    private String getErrorCode(AuthenticationException authException) {
        String exceptionName = authException.getClass().getSimpleName();
        
        switch (exceptionName) {
            case "BadCredentialsException":
                return "INVALID_CREDENTIALS";
            case "DisabledException":
                return "ACCOUNT_DISABLED";
            case "AccountExpiredException":
                return "ACCOUNT_EXPIRED";
            case "LockedException":
                return "ACCOUNT_LOCKED";
            case "CredentialsExpiredException":
                return "CREDENTIALS_EXPIRED";
            case "UsernameNotFoundException":
                return "USER_NOT_FOUND";
            case "InsufficientAuthenticationException":
                return "INSUFFICIENT_AUTHENTICATION";
            default:
                return "AUTHENTICATION_FAILED";
        }
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}