package com.weeklyreport.exception;

/**
 * Custom exception for user management operations
 */
public class UserManagementException extends RuntimeException {

    private final String errorCode;

    public UserManagementException(String message) {
        super(message);
        this.errorCode = "USER_MANAGEMENT_ERROR";
    }

    public UserManagementException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public UserManagementException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "USER_MANAGEMENT_ERROR";
    }

    public UserManagementException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    // Specific user management error types
    public static class UserNotFoundException extends UserManagementException {
        public UserNotFoundException(String message) {
            super(message, "USER_NOT_FOUND");
        }
    }

    public static class UserAlreadyExistsException extends UserManagementException {
        public UserAlreadyExistsException(String message) {
            super(message, "USER_ALREADY_EXISTS");
        }
    }

    public static class InvalidUserDataException extends UserManagementException {
        public InvalidUserDataException(String message) {
            super(message, "INVALID_USER_DATA");
        }
    }

    public static class UserPermissionException extends UserManagementException {
        public UserPermissionException(String message) {
            super(message, "INSUFFICIENT_PERMISSIONS");
        }
    }

    public static class UserStatusException extends UserManagementException {
        public UserStatusException(String message) {
            super(message, "INVALID_USER_STATUS");
        }
    }
}