package com.weeklyreport.core.exception;

import com.weeklyreport.common.dto.ApiResponse;
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
        Map<String, String> userFriendlyErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            
            // 生成用户友好的错误信息
            String userFriendlyMessage = generateUserFriendlyValidationMessage(fieldName, errorMessage);
            userFriendlyErrors.put(fieldName, userFriendlyMessage);
        });

        // 记录详细验证错误到日志，但不暴露给客户端
        logger.warn("Validation failed for fields: {}", errors);
        
        // 返回用户友好的错误信息，不暴露技术细节
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("输入验证失败，请检查您的输入", userFriendlyErrors));
    }
    
    /**
     * Generate user-friendly validation error messages
     */
    private String generateUserFriendlyValidationMessage(String fieldName, String originalMessage) {
        // 特殊字段的友好提示
        return switch (fieldName) {
            case "password" -> {
                if (originalMessage.contains("between 8 and 100")) {
                    yield "密码长度必须在8-100个字符之间";
                } else if (originalMessage.contains("cannot be blank")) {
                    yield "请输入密码";
                } else if (originalMessage.contains("lowercase")) {
                    yield "密码必须包含大小写字母和数字";
                }
                yield "密码格式不正确";
            }
            case "usernameOrEmail" -> {
                if (originalMessage.contains("cannot be blank")) {
                    yield "请输入用户名或邮箱";
                } else if (originalMessage.contains("between 3 and 100")) {
                    yield "用户名或邮箱长度必须在3-100个字符之间";
                }
                yield "用户名或邮箱格式不正确";
            }
            case "username" -> {
                if (originalMessage.contains("cannot be blank")) {
                    yield "请输入用户名";
                } else if (originalMessage.contains("between")) {
                    yield "用户名长度不符合要求";
                } else if (originalMessage.contains("letters, numbers, and underscores")) {
                    yield "用户名只能包含字母、数字和下划线";
                }
                yield "用户名格式不正确";
            }
            case "email" -> {
                if (originalMessage.contains("cannot be blank")) {
                    yield "请输入邮箱地址";
                } else if (originalMessage.contains("should be valid")) {
                    yield "请输入有效的邮箱地址";
                }
                yield "邮箱格式不正确";
            }
            case "fullName" -> {
                if (originalMessage.contains("cannot be blank")) {
                    yield "请输入姓名";
                }
                yield "姓名格式不正确";
            }
            case "refreshToken" -> "刷新令牌不能为空";
            default -> originalMessage; // 如果没有特殊处理，返回原始消息
        };
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

        // 记录详细约束违反信息到日志，但不暴露给客户端
        logger.warn("Constraint violations: {}", errors);
        
        // 返回用户友好的错误信息
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("数据验证失败，请检查您的输入", errors));
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
                    .body(ApiResponse.error("账户已被停用，请联系管理员重新启用您的账户"));
        } else if (message.contains("locked")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("账户已被锁定，请联系管理员解锁或稍后重试"));
        } else if (message.contains("deleted")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("账户已被删除，请联系管理员或重新注册"));
        } else if (message.contains("用户名或邮箱不能为空")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("请输入用户名或邮箱地址"));
        } else if (message.contains("密码不能为空")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("请输入密码"));
        } else if (message.contains("用户不存在") || message.contains("用户名/邮箱或密码错误")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户名或邮箱不存在，请检查输入或注册新账户"));
        } else if (message.contains("密码") || message.contains("凭据")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("密码错误，请重新输入或点击忘记密码"));
        } else {
            // For other authentication failures, use user-friendly generic message
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("登录失败，请检查用户名/邮箱和密码是否正确"));
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
                .body(ApiResponse.error("访问被拒绝，您没有权限执行此操作"));
    }

    /**
     * Handle UsernameNotFoundException
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUsernameNotFoundException(
            UsernameNotFoundException ex, WebRequest request) {

        logger.warn("User not found: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("用户不存在，请检查用户名或邮箱是否正确"));
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
                .body(ApiResponse.error("安全违规，访问被拒绝"));
    }

    /**
     * Handle generic RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        // 详细的错误诊断信息 - 仅记录到日志
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

        // 记录完整异常堆栈
        logger.error("异常堆栈信息: ", ex);
        logger.error("=== 异常诊断结束 ===");

        // 返回用户友好的错误信息，不暴露系统内部细节
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("服务器内部错误，请稍后重试"));
    }

    /**
     * Handle NoHandlerFoundException - returns 404 instead of 500
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {

        logger.warn("No handler found for request: {} - {}", ex.getHttpMethod(), ex.getRequestURL());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("请求的接口不存在，请检查URL是否正确"));
    }

    /**
     * Handle generic Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, WebRequest request) {

        // 超详细的全局异常诊断 - 仅记录到日志
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

        // 记录完整异常堆栈
        logger.error("完整异常堆栈: ", ex);
        logger.error("=== 全局异常诊断结束 ===");

        // 返回用户友好的错误信息，不暴露系统内部细节
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系统发生未知错误，请联系管理员"));
    }
}
