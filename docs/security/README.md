# Security Documentation

This directory contains comprehensive security documentation for the Weekly Report System.

## 📁 Contents

### 📋 [API Security Guide](./api-security-guide.md)
Complete guide to the authentication and authorization system including:
- JWT token-based authentication flow
- Role-based access control (RBAC) implementation
- API endpoint documentation with examples
- Error handling and security headers
- Rate limiting and CORS configuration

### 🛡️ [Security Best Practices](./security-best-practices.md)
Comprehensive security guidelines covering:
- Password and authentication security
- Input validation and sanitization
- Database and API security
- Deployment and operational security
- Security testing and incident response

### 🧪 [Postman Collection](./weekly-report-auth-postman-collection.json)
Ready-to-use Postman collection for testing authentication endpoints:
- Login/logout scenarios for different user roles
- Registration and token refresh testing
- Authorization testing (success and failure cases)
- Security testing (CORS, headers, rate limiting)

## 🚀 Quick Start

### For Developers
1. Read the [API Security Guide](./api-security-guide.md) to understand the authentication system
2. Review [Security Best Practices](./security-best-practices.md) before implementing features
3. Import the [Postman Collection](./weekly-report-auth-postman-collection.json) for API testing

### For Testers
1. Import the Postman collection into your Postman workspace
2. Set up environment variables:
   - `base_url`: API base URL (e.g., `http://localhost:8080/api`)
3. Run the authentication tests to verify system behavior
4. Use the authorization tests to verify role-based access control

### For Operations Team
1. Review the security configuration sections in [Security Best Practices](./security-best-practices.md)
2. Follow the deployment checklist for secure production deployment
3. Set up monitoring and alerting as outlined in the operational security section

## 🎯 User Roles

The system implements role-based access control with five distinct roles:

| Role | Description | Key Permissions |
|------|-------------|----------------|
| **ADMIN** | System Administrator | Full system access, user management, system configuration |
| **HR_MANAGER** | Human Resources Manager | All user data, company-wide reports, employee management |
| **DEPARTMENT_MANAGER** | Department Manager | Department team management, department reports, approvals |
| **TEAM_LEADER** | Team Leader | Team member reports, team assignments, feedback |
| **EMPLOYEE** | Regular Employee | Personal reports, own profile management |

## 🔐 Security Features

### Authentication
- JWT token-based stateless authentication
- Refresh token rotation for enhanced security
- Password strength requirements and BCrypt encryption
- Account status validation (active/inactive/locked)

### Authorization
- Role-based access control (RBAC)
- Endpoint-level authorization
- Resource-level access control
- Permission inheritance model

### Security Measures
- Rate limiting on authentication endpoints
- CORS configuration for cross-origin requests
- Security headers (HSTS, CSP, X-Frame-Options, etc.)
- Input validation and sanitization
- SQL injection protection via JPA
- XSS prevention measures

## 🧪 Testing

### Test Coverage
The security testing suite includes:

#### Integration Tests (`/backend/src/test/java/com/weeklyreport/security/`)
- **AuthenticationIntegrationTest**: Login/logout, registration, token refresh
- **RoleBasedAccessControlTest**: Authorization testing for all user roles
- **SecurityConfigurationTest**: CORS, security headers, session management
- **JwtTokenTest**: Token generation, validation, expiration handling

#### Manual Testing
- Postman collection with 15+ test scenarios
- Automated test assertions for response validation
- Environment variable management for different test setups

### Running Tests

```bash
# Run all security tests
mvn test -Dtest="com.weeklyreport.security.**"

# Run specific test class
mvn test -Dtest="AuthenticationIntegrationTest"

# Run tests with specific profile
mvn test -Dspring.profiles.active=test
```

## 📊 Security Monitoring

### Audit Events
The system logs the following security events:
- Authentication attempts (success/failure)
- Authorization violations
- Token refresh activities
- Role changes and privilege escalations
- Administrative actions

### Metrics
- Failed login attempts per IP/user
- Token usage patterns
- API endpoint access patterns
- Error rates and response times

## 🔄 Stream C Implementation Status

This documentation is part of **Stream C: Security Testing & Documentation** for Issue #003.

### ✅ Completed Tasks
- [x] Security testing framework setup
- [x] Authentication API integration tests
- [x] JWT token validation tests
- [x] Role-based access control tests
- [x] Security configuration tests
- [x] API security documentation
- [x] Postman collection for manual testing
- [x] Security best practices guide

### ⏳ Dependencies
- **Stream A**: Security configuration and JWT implementation
- **Stream B**: Authentication REST APIs implementation

Once Stream A and B are completed, the test placeholders will be updated with actual implementations.

## 📞 Support

For security-related questions or concerns:
1. Review this documentation first
2. Check the test implementations for examples
3. Consult the security best practices guide
4. Contact the security team for sensitive issues

---

**Note**: This documentation serves as a comprehensive guide for the Weekly Report System's security implementation. It should be kept up-to-date as the system evolves and new security features are added.