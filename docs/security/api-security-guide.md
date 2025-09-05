# API Security Guide

## Overview
This document provides comprehensive information about the security implementation in the Weekly Report System, including authentication, authorization, and best practices for API usage.

## Authentication System

### JWT Token-Based Authentication
The system uses JSON Web Tokens (JWT) for stateless authentication:

- **Access Token**: Short-lived token (1 hour) for API access
- **Refresh Token**: Long-lived token (7 days) for token renewal
- **Token Structure**: Standard JWT with header, payload, and signature

### Authentication Flow
1. User submits credentials to `/api/auth/login`
2. System validates credentials and user status
3. If valid, returns access token and refresh token
4. Client includes access token in `Authorization` header for subsequent requests
5. When access token expires, client uses refresh token to get new tokens

## API Endpoints

### Authentication Endpoints

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Success Response (200)**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "user@example.com",
      "fullName": "John Doe",
      "role": "EMPLOYEE",
      "department": {
        "id": 1,
        "name": "Engineering"
      }
    }
  }
}
```

**Error Response (401)**
```json
{
  "success": false,
  "message": "Invalid credentials",
  "timestamp": "2025-09-05T10:30:00Z"
}
```

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "SecurePassword123!",
  "fullName": "Jane Doe",
  "employeeId": "EMP001"
}
```

#### Token Refresh
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Logout
```http
POST /api/auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Get Profile
```http
GET /api/auth/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Authorization & Role-Based Access Control

### User Roles

#### ADMIN (System Administrator)
- **Permissions**: Full system access
- **Endpoints**: All endpoints including `/api/admin/*`
- **Capabilities**:
  - User management (create, update, delete, role assignment)
  - System configuration
  - Global report access and management
  - Department management

#### HR_MANAGER (Human Resources Manager)
- **Permissions**: HR-related operations across all departments
- **Endpoints**: `/api/users/*`, `/api/reports/all`, `/api/departments/*`
- **Capabilities**:
  - View all user profiles and reports
  - Generate company-wide reports
  - Manage employee information
  - Access performance analytics

#### DEPARTMENT_MANAGER (Department Manager)
- **Permissions**: Department-specific management
- **Endpoints**: `/api/users/department`, `/api/reports/department`
- **Capabilities**:
  - View and manage department team members
  - Access department reports and analytics
  - Approve/reject team reports
  - Assign team leaders

#### TEAM_LEADER (Team Leader)
- **Permissions**: Team-specific management
- **Endpoints**: `/api/users/team`, `/api/reports/team`
- **Capabilities**:
  - View team member reports
  - Provide feedback on team reports
  - Manage team assignments

#### EMPLOYEE (Regular Employee)
- **Permissions**: Personal data and report management
- **Endpoints**: `/api/auth/profile`, `/api/reports/my`
- **Capabilities**:
  - Create and edit personal weekly reports
  - View own report history
  - Update personal profile information

### Access Control Examples

```http
# Admin accessing user management
GET /api/admin/users
Authorization: Bearer <admin_token>
# ✅ Allowed

# Employee trying to access admin endpoints
GET /api/admin/users
Authorization: Bearer <employee_token>
# ❌ 403 Forbidden

# Department manager accessing department reports
GET /api/reports/department
Authorization: Bearer <manager_token>
# ✅ Allowed

# Employee accessing own reports
GET /api/reports/my
Authorization: Bearer <employee_token>
# ✅ Allowed
```

## Security Headers

All API responses include security headers:

```http
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: default-src 'self'
```

## CORS Configuration

Cross-Origin Resource Sharing is configured for:
- **Allowed Origins**: `http://localhost:3000`, `https://weekly-report.company.com`
- **Allowed Methods**: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`
- **Allowed Headers**: `Content-Type`, `Authorization`, `X-Requested-With`
- **Credentials**: Supported for authenticated requests

## Error Handling

### Authentication Errors

#### 401 Unauthorized
```json
{
  "success": false,
  "message": "Authentication required",
  "code": "AUTH_REQUIRED",
  "timestamp": "2025-09-05T10:30:00Z"
}
```

#### 401 Invalid Token
```json
{
  "success": false,
  "message": "Invalid or expired token",
  "code": "INVALID_TOKEN",
  "timestamp": "2025-09-05T10:30:00Z"
}
```

### Authorization Errors

#### 403 Forbidden
```json
{
  "success": false,
  "message": "Access denied. Insufficient privileges",
  "code": "ACCESS_DENIED",
  "timestamp": "2025-09-05T10:30:00Z"
}
```

### Validation Errors

#### 400 Bad Request
```json
{
  "success": false,
  "message": "Validation failed",
  "code": "VALIDATION_ERROR",
  "errors": [
    {
      "field": "password",
      "message": "Password must be at least 8 characters"
    }
  ],
  "timestamp": "2025-09-05T10:30:00Z"
}
```

## Rate Limiting

Authentication endpoints have rate limiting:
- **Login attempts**: 5 attempts per IP per 15 minutes
- **Registration**: 3 attempts per IP per hour
- **Token refresh**: 10 attempts per user per minute

#### Rate Limit Response
```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/json

{
  "success": false,
  "message": "Too many login attempts. Please try again in 15 minutes",
  "code": "RATE_LIMIT_EXCEEDED",
  "retryAfter": 900,
  "timestamp": "2025-09-05T10:30:00Z"
}
```

## Password Requirements

- **Minimum length**: 8 characters
- **Required characters**: At least one uppercase, lowercase, number, and special character
- **Prohibited patterns**: No common passwords, no username in password
- **Encryption**: BCrypt with salt rounds configuration

Example strong password: `MySecure123!Pass`

## Token Security

### Access Token
- **Expiration**: 1 hour
- **Algorithm**: HS256 (HMAC SHA-256)
- **Claims**: user ID, username, role, permissions
- **Storage**: Client should store securely (not in localStorage for production)

### Refresh Token
- **Expiration**: 7 days
- **Storage**: HTTP-only cookie (recommended) or secure storage
- **Rotation**: New refresh token issued on each refresh

## Implementation Notes

### Client-Side Integration

```javascript
// Login
const login = async (credentials) => {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(credentials)
  });
  
  if (response.ok) {
    const data = await response.json();
    // Store tokens securely
    localStorage.setItem('access_token', data.data.token);
    localStorage.setItem('refresh_token', data.data.refreshToken);
    return data;
  }
  throw new Error('Login failed');
};

// Authenticated request
const makeAuthenticatedRequest = async (url, options = {}) => {
  const token = localStorage.getItem('access_token');
  
  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${token}`
    }
  });
  
  if (response.status === 401) {
    // Token expired, try refresh
    const refreshed = await refreshToken();
    if (refreshed) {
      // Retry request with new token
      return makeAuthenticatedRequest(url, options);
    } else {
      // Redirect to login
      window.location.href = '/login';
    }
  }
  
  return response;
};
```

## Security Testing

The system includes comprehensive security tests:
- Authentication flow testing
- Authorization boundary testing
- Token validation and expiration testing
- Role-based access control testing
- Security configuration testing

See the test files in `/backend/src/test/java/com/weeklyreport/security/` for detailed test implementations.

## Monitoring and Logging

Security events are logged for monitoring:
- Failed login attempts
- Unauthorized access attempts
- Token refresh activities
- Role escalation attempts
- Administrative actions

---

**Note**: This documentation assumes Stream A (Security Configuration) and Stream B (Authentication APIs) are fully implemented. Some endpoints and features may be placeholder content until implementation is complete.