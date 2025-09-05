package com.weeklyreport.exception;

/**
 * Custom authentication exception
 */
public class AuthenticationException extends RuntimeException {

    private final String errorCode;

    public AuthenticationException(String message) {
        super(message);
        this.errorCode = "AUTH_ERROR";
    }

    public AuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTH_ERROR";
    }

    public AuthenticationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    // Specific authentication error types
    public static class InvalidCredentialsException extends AuthenticationException {
        public InvalidCredentialsException(String message) {
            super(message, "INVALID_CREDENTIALS");
        }
    }

    public static class AccountLockedException extends AuthenticationException {
        public AccountLockedException(String message) {
            super(message, "ACCOUNT_LOCKED");
        }
    }

    public static class AccountDisabledException extends AuthenticationException {
        public AccountDisabledException(String message) {
            super(message, "ACCOUNT_DISABLED");
        }
    }

    public static class TokenExpiredException extends AuthenticationException {
        public TokenExpiredException(String message) {
            super(message, "TOKEN_EXPIRED");
        }
    }

    public static class InvalidTokenException extends AuthenticationException {
        public InvalidTokenException(String message) {
            super(message, "INVALID_TOKEN");
        }
    }
}