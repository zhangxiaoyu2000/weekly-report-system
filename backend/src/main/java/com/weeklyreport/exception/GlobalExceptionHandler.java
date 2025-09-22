package com.weeklyreport.exception;

import com.weeklyreport.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Global exception handler for authentication and user management APIs
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Validation failed", errors));
    }

    /**
     * Handle constraint violations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        
        Map<String, String> errors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }

        logger.warn("Constraint violations: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Validation failed", errors));
    }

    /**
     * Handle Spring Security BadCredentialsException
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        
        logger.warn("Bad credentials: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        
        // Check if this is an account status issue and return specific message
        String message = ex.getMessage();
        if (message.contains("deactivated")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("账户已被停用，请联系管理员重新启用"));
        } else if (message.contains("locked")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("账户已被锁定，请联系管理员解锁"));
        } else if (message.contains("deleted")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("账户已被删除，请联系管理员"));
        } else {
            // For password errors and other authentication failures, use generic message
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid username/email or password"));
        }
    }

    /**
     * Handle Spring Security AccessDeniedException
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        logger.warn("Access denied: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied"));
    }

    /**
     * Handle UsernameNotFoundException
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUsernameNotFoundException(
            UsernameNotFoundException ex, WebRequest request) {
        
        logger.warn("User not found: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found"));
    }

    /**
     * Handle custom AuthenticationException
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        logger.warn("Authentication error [{}]: {} - Request: {}", 
                   ex.getErrorCode(), ex.getMessage(), request.getDescription(false));
        
        HttpStatus status = switch (ex.getErrorCode()) {
            case "INVALID_CREDENTIALS", "TOKEN_EXPIRED", "INVALID_TOKEN" -> HttpStatus.UNAUTHORIZED;
            case "ACCOUNT_LOCKED", "ACCOUNT_DISABLED" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.UNAUTHORIZED;
        };

        return ResponseEntity.status(status)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle custom UserManagementException
     */
    @ExceptionHandler(UserManagementException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserManagementException(
            UserManagementException ex, WebRequest request) {
        
        logger.warn("User management error [{}]: {} - Request: {}", 
                   ex.getErrorCode(), ex.getMessage(), request.getDescription(false));
        
        HttpStatus status = switch (ex.getErrorCode()) {
            case "USER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "USER_ALREADY_EXISTS", "INVALID_USER_DATA" -> HttpStatus.BAD_REQUEST;
            case "INSUFFICIENT_PERMISSIONS" -> HttpStatus.FORBIDDEN;
            case "INVALID_USER_STATUS" -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return ResponseEntity.status(status)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        logger.warn("Invalid argument: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        
        logger.warn("Invalid state: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle SecurityException
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Object>> handleSecurityException(
            SecurityException ex, WebRequest request) {
        
        logger.warn("Security violation: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Security violation: " + ex.getMessage()));
    }

    /**
     * Handle generic RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        logger.error("Runtime exception: {} - Request: {}", ex.getMessage(), request.getDescription(false), ex);
        
        // 详细的错误诊断信息
        logger.error("=== 详细运行时异常诊断 ===");
        logger.error("异常类型: {}", ex.getClass().getName());
        logger.error("异常消息: {}", ex.getMessage());
        logger.error("请求路径: {}", request.getDescription(false));
        
        // 记录请求头信息
        try {
            logger.error("请求参数: {}", request.getParameterMap());
        } catch (Exception e) {
            logger.error("无法获取请求参数: {}", e.getMessage());
        }
        
        // 记录系统资源状态
        try {
            Runtime runtime = Runtime.getRuntime();
            logger.error("JVM内存状态: 已用{}MB / 总共{}MB / 最大{}MB", 
                (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024,
                runtime.totalMemory() / 1024 / 1024,
                runtime.maxMemory() / 1024 / 1024);
        } catch (Exception e) {
            logger.error("无法获取系统资源信息: {}", e.getMessage());
        }
        
        // 记录线程信息
        try {
            Thread currentThread = Thread.currentThread();
            logger.error("当前线程: {} (状态: {})", currentThread.getName(), currentThread.getState());
        } catch (Exception e) {
            logger.error("无法获取线程信息: {}", e.getMessage());
        }
        
        logger.error("=== 异常诊断结束 ===");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }

    /**
     * Handle NoHandlerFoundException - returns 404 instead of 500
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {
        
        logger.warn("No handler found for request: {} - {}", ex.getHttpMethod(), ex.getRequestURL());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Endpoint not found: " + ex.getHttpMethod() + " " + ex.getRequestURL()));
    }

    /**
     * Handle generic Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        logger.error("Unexpected exception: {} - Request: {}", ex.getMessage(), request.getDescription(false), ex);
        
        // 超详细的全局异常诊断
        logger.error("=== 全局异常详细诊断 ===");
        logger.error("异常完整类名: {}", ex.getClass().getName());
        logger.error("异常消息: {}", ex.getMessage());
        logger.error("异常原因: {}", ex.getCause() != null ? ex.getCause().toString() : "无");
        logger.error("请求完整信息: {}", request.getDescription(true));
        
        // 记录异常调用栈中的关键信息
        StackTraceElement[] stackTrace = ex.getStackTrace();
        if (stackTrace.length > 0) {
            logger.error("异常发生位置: {}:{}:{}", 
                stackTrace[0].getClassName(), 
                stackTrace[0].getMethodName(), 
                stackTrace[0].getLineNumber());
                
            // 记录前3个栈帧
            for (int i = 0; i < Math.min(3, stackTrace.length); i++) {
                StackTraceElement element = stackTrace[i];
                logger.error("栈帧[{}]: {}:{}:{}", i, 
                    element.getClassName(), element.getMethodName(), element.getLineNumber());
            }
        }
        
        // 如果有原因异常，也记录其信息
        if (ex.getCause() != null) {
            logger.error("根本原因异常: {}", ex.getCause().getClass().getName());
            logger.error("根本原因消息: {}", ex.getCause().getMessage());
        }
        
        // 环境信息
        try {
            logger.error("当前时间: {}", java.time.LocalDateTime.now());
            logger.error("可用处理器数: {}", Runtime.getRuntime().availableProcessors());
        } catch (Exception e) {
            logger.error("无法获取环境信息: {}", e.getMessage());
        }
        
        logger.error("=== 全局异常诊断结束 ===");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
    }
}