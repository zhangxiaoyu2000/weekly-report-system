package com.weeklyreport.core.controller;

import com.weeklyreport.common.dto.ApiResponse;
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

        return ApiResponse.success("服务运行正常", healthInfo);
    }

    @GetMapping("/ready")
    public ApiResponse<Map<String, Object>> readiness() {
        Map<String, Object> readinessInfo = new HashMap<>();
        readinessInfo.put("status", "READY");
        readinessInfo.put("timestamp", LocalDateTime.now());
        readinessInfo.put("service", "weekly-report-backend");
        readinessInfo.put("version", "1.0.0");
        readinessInfo.put("ready", true);

        return ApiResponse.success("服务就绪", readinessInfo);
    }

    @GetMapping("/live")
    public ApiResponse<Map<String, Object>> liveness() {
        Map<String, Object> livenessInfo = new HashMap<>();
        livenessInfo.put("status", "LIVE");
        livenessInfo.put("timestamp", LocalDateTime.now());
        livenessInfo.put("service", "weekly-report-backend");
        livenessInfo.put("version", "1.0.0");
        livenessInfo.put("live", true);

        return ApiResponse.success("服务正在运行", livenessInfo);
    }

    @GetMapping("/authenticated")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> authenticatedHealth(Authentication authentication) {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "AUTHENTICATED");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("user", authentication.getName());
        healthInfo.put("authorities", authentication.getAuthorities());

        return ApiResponse.success("身份验证成功", healthInfo);
    }
}
