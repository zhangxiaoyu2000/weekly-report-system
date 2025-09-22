package com.weeklyreport.controller;

import com.weeklyreport.entity.User;
import com.weeklyreport.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 调试控制器 - 用于诊断登录问题
 */
@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/user/{username}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable String username) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            result.put("found", true);
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("email", user.getEmail());
            result.put("role", user.getRole().toString());
            result.put("status", user.getStatus().toString());
            result.put("passwordLength", user.getPassword() != null ? user.getPassword().length() : 0);
            result.put("passwordStartsWith", user.getPassword() != null ? user.getPassword().substring(0, Math.min(10, user.getPassword().length())) : "null");
            result.put("isBCryptFormat", user.getPassword() != null && user.getPassword().startsWith("$2"));
        } else {
            result.put("found", false);
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-password")
    public ResponseEntity<Map<String, Object>> testPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        
        Map<String, Object> result = new HashMap<>();
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // 测试密码匹配
            boolean matches = passwordEncoder.matches(password, user.getPassword());
            
            result.put("username", username);
            result.put("passwordProvided", password);
            result.put("storedPasswordHash", user.getPassword());
            result.put("passwordMatches", matches);
            result.put("userStatus", user.getStatus().toString());
            
            // 额外测试：直接加密提供的密码
            String newHash = passwordEncoder.encode(password);
            result.put("newPasswordHash", newHash);
            result.put("newHashMatches", passwordEncoder.matches(password, newHash));
            
        } else {
            result.put("error", "User not found");
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reset-user-password")
    public ResponseEntity<Map<String, Object>> resetUserPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String newPassword = request.get("newPassword");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String oldPassword = user.getPassword();
                
                // 重新加密密码
                String encodedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encodedPassword);
                userRepository.save(user);
                
                result.put("success", true);
                result.put("username", username);
                result.put("oldPassword", oldPassword);
                result.put("newPassword", encodedPassword);
                result.put("message", "密码已重置");
            } else {
                result.put("success", false);
                result.put("error", "User not found");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 检查AIController是否被注册为Spring Bean
     * GET /debug/ai-controller
     */
    @GetMapping("/ai-controller")
    public ResponseEntity<Map<String, Object>> checkAIController() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 由于DebugController不能直接注入ApplicationContext，我们用另一种方法
            // 检查相关的service是否存在
            result.put("message", "AI Controller debug check");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            // 列出所有活跃的URL映射
            result.put("note", "Check server logs for actual mappings registered");
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 检查当前登录用户信息 - 用于调试审批人ID问题
     * GET /debug/current-user
     */
    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUserInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取Spring Security认证信息
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                
            if (auth == null || !auth.isAuthenticated()) {
                result.put("authenticated", false);
                result.put("error", "No authentication found");
                return ResponseEntity.ok(result);
            }
            
            String username = auth.getName();
            result.put("authenticated", true);
            result.put("authenticationName", username);
            result.put("authorities", auth.getAuthorities().toString());
            
            // 从数据库获取用户详细信息
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                result.put("userId", user.getId());
                result.put("username", user.getUsername());
                result.put("email", user.getEmail());
                result.put("role", user.getRole().toString());
                result.put("fullName", user.getFullName());
                result.put("status", user.getStatus().toString());
                
                result.put("message", "如果这个用户进行审批操作，adminReviewerId 将设为 " + user.getId());
            } else {
                result.put("error", "User not found in database");
            }
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 模拟审批操作 - 测试用户ID传递
     * POST /debug/simulate-approval/{projectId}
     */
    @PostMapping("/simulate-approval/{projectId}")
    public ResponseEntity<Map<String, Object>> simulateApproval(@PathVariable Long projectId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取当前用户（模拟ProjectController中的getCurrentUser逻辑）
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                
            if (auth == null || !auth.isAuthenticated()) {
                result.put("error", "用户未认证");
                return ResponseEntity.status(401).body(result);
            }
            
            String username = auth.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                result.put("error", "用户不存在");
                return ResponseEntity.status(404).body(result);
            }
            
            User currentUser = userOpt.get();
            Long currentUserId = currentUser.getId();
            
            result.put("projectId", projectId);
            result.put("currentUserId", currentUserId);
            result.put("currentUsername", currentUser.getUsername());
            result.put("currentUserRole", currentUser.getRole().toString());
            result.put("wouldSetAdminReviewerId", currentUserId);
            result.put("message", "模拟审批：如果真实执行，adminReviewerId 将设为 " + currentUserId);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
}