package com.weeklyreport.controller;

import com.weeklyreport.dto.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for API status monitoring
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "weekly-report-backend");
        healthInfo.put("version", "1.0.0");
        
        return ApiResponse.success("Service is healthy", healthInfo);
    }

    @GetMapping("/authenticated")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> authenticatedHealth(Authentication authentication) {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "AUTHENTICATED");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("user", authentication.getName());
        healthInfo.put("authorities", authentication.getAuthorities());
        
        return ApiResponse.success("Authentication verified", healthInfo);
    }
}