package com.weeklyreport.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Access Denied Handler that handles authorization failures
 * Returns a custom JSON error response when access is denied
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtAccessDeniedHandler.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";
        
        logger.warn("Access denied for user: {} attempting to access: {} from IP: {}. Error: {}",
                   username,
                   request.getRequestURI(),
                   getClientIpAddress(request),
                   accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = createErrorResponse(request, accessDeniedException, username);
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Create standardized error response
     */
    private Map<String, Object> createErrorResponse(HttpServletRequest request,
                                                   AccessDeniedException accessDeniedException,
                                                   String username) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpServletResponse.SC_FORBIDDEN);
        errorResponse.put("error", "Access Denied");
        errorResponse.put("message", "Insufficient privileges to access this resource");
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("errorCode", "ACCESS_DENIED");
        
        // Add user context if available
        if (!"anonymous".equals(username)) {
            errorResponse.put("user", username);
        }
        
        // Add required authorities if available from the exception
        String requiredAuthority = extractRequiredAuthority(accessDeniedException);
        if (requiredAuthority != null) {
            errorResponse.put("requiredAuthority", requiredAuthority);
        }
        
        return errorResponse;
    }

    /**
     * Extract required authority from access denied exception
     */
    private String extractRequiredAuthority(AccessDeniedException accessDeniedException) {
        String message = accessDeniedException.getMessage();
        if (message != null && message.contains("hasAuthority")) {
            // Try to extract authority from message like "Access is denied (hasAuthority('ADMIN_WRITE'))"
            int start = message.indexOf("hasAuthority('");
            if (start != -1) {
                int end = message.indexOf("')", start);
                if (end != -1) {
                    return message.substring(start + 14, end);
                }
            }
        }
        return null;
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