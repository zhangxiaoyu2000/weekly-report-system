# Security Best Practices

## Overview
This document outlines security best practices for the Weekly Report System, covering development, deployment, and operational security measures.

## Authentication & Authorization

### Password Security
- **Minimum Requirements**: 8 characters with mixed case, numbers, and special characters
- **Storage**: Always use BCrypt with adequate salt rounds (minimum 10 for production)
- **Never** store passwords in plain text or reversible encryption
- Implement password complexity validation on both client and server side

```java
// Example password validation
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
         message = "Password must contain at least 8 characters with uppercase, lowercase, number and special character")
private String password;
```

### JWT Token Security
- **Secret Key**: Use strong, unique secrets (minimum 256 bits)
- **Expiration**: Short-lived access tokens (15 minutes to 1 hour)
- **Rotation**: Implement refresh token rotation
- **Claims**: Include minimal necessary information
- **Algorithm**: Use RS256 for production (asymmetric keys)

```yaml
# application-prod.yml
jwt:
  secret: ${JWT_SECRET:} # Set via environment variable
  expiration: 900000 # 15 minutes
  refresh-expiration: 604800000 # 7 days
  algorithm: RS256
```

### Session Management
- Use stateless authentication (JWT) for REST APIs
- Implement proper session timeout
- Secure session storage (HTTP-only cookies for web clients)
- Invalidate sessions on logout and password change

## Input Validation & Sanitization

### Server-Side Validation
- **Always** validate input on the server side
- Use Bean Validation annotations (@Valid, @NotNull, @Size, etc.)
- Sanitize input to prevent injection attacks
- Implement whitelist-based validation when possible

```java
@PostMapping("/auth/register")
public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
    // Server-side validation is automatically triggered by @Valid
    // Additional custom validation can be added here
}
```

### SQL Injection Prevention
- Use parameterized queries (JPA repositories handle this automatically)
- Never concatenate user input directly into SQL queries
- Use Spring Data JPA query methods or @Query with parameters

```java
// Safe - using Spring Data JPA
Optional<User> findByUsername(String username);

// Safe - parameterized native query
@Query(value = "SELECT * FROM users WHERE email = ?1 AND status = 'ACTIVE'", nativeQuery = true)
List<User> findActiveUsersByEmail(String email);

// UNSAFE - never do this
// @Query(value = "SELECT * FROM users WHERE username = '" + username + "'", nativeQuery = true)
```

### XSS Prevention
- Escape output in templates
- Use Content Security Policy (CSP) headers
- Validate and sanitize rich text inputs
- Use HTTPS to prevent man-in-the-middle attacks

## API Security

### HTTPS/TLS
- **Always** use HTTPS in production
- Implement HTTP to HTTPS redirect
- Use strong TLS configuration (TLS 1.2+)
- Configure HSTS headers

```yaml
# application-prod.yml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: weeklyreport
  port: 8443

# Redirect HTTP to HTTPS
management:
  server:
    ssl:
      enabled: true
```

### CORS Configuration
- Configure specific allowed origins (not `*` in production)
- Restrict allowed methods and headers
- Use credentials only when necessary

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "https://weeklyreport.company.com",
            "https://admin.weeklyreport.company.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

### Rate Limiting
- Implement rate limiting on authentication endpoints
- Use progressive delays for failed attempts
- Consider IP-based and user-based limits

```java
// Example using Bucket4j
@Component
public class RateLimitService {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    public boolean tryConsume(String key, int tokens) {
        return getBucket(key).tryConsume(tokens);
    }
    
    private Bucket getBucket(String key) {
        return cache.computeIfAbsent(key, this::createNewBucket);
    }
    
    private Bucket createNewBucket(String key) {
        return Bucket.builder()
            .addLimit(Bandwidth.simple(5, Duration.ofMinutes(15))) // 5 attempts per 15 minutes
            .build();
    }
}
```

## Database Security

### Connection Security
- Use connection pooling with proper limits
- Encrypt database connections (SSL/TLS)
- Use least privilege principle for database users
- Store connection credentials securely (environment variables)

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=true&requireSSL=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
```

### Data Encryption
- Encrypt sensitive data at rest
- Use database-level encryption for sensitive columns
- Implement field-level encryption for highly sensitive data

```java
// Example of sensitive data encryption
@Entity
public class User {
    
    @Column(name = "ssn")
    @Convert(converter = SensitiveDataConverter.class)
    private String socialSecurityNumber;
    
    // Other fields...
}
```

### Audit Logging
- Log all security-relevant events
- Include user ID, timestamp, action, and IP address
- Store logs securely and monitor for anomalies

```java
@EventListener
public class SecurityAuditListener {
    
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        auditService.logSecurityEvent(
            SecurityEvent.LOGIN_SUCCESS,
            event.getAuthentication().getName(),
            getClientIP()
        );
    }
    
    @EventListener  
    public void handleAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        auditService.logSecurityEvent(
            SecurityEvent.LOGIN_FAILURE,
            event.getAuthentication().getName(),
            getClientIP()
        );
    }
}
```

## Configuration Security

### Environment Variables
- Never store secrets in code or configuration files
- Use environment variables or secure configuration management
- Rotate secrets regularly

```bash
# Production environment variables
export JWT_SECRET="your-super-secure-jwt-secret-key-here"
export DB_PASSWORD="your-secure-database-password"
export ENCRYPTION_KEY="your-field-level-encryption-key"
```

### Spring Profiles
- Use different configurations for different environments
- Secure production profile configuration
- Disable debug features in production

```yaml
# application-prod.yml
logging:
  level:
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
    com.weeklyreport: INFO
  
spring:
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate # Never use 'create' or 'create-drop' in production
```

### Actuator Security
- Secure actuator endpoints in production
- Expose only necessary endpoints
- Use different port for management endpoints

```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
  server:
    port: 9090 # Different port for management
```

## Error Handling

### Security-Safe Error Messages
- Never expose internal system details in error messages
- Use generic error messages for security-sensitive operations
- Log detailed errors server-side for debugging

```java
@ControllerAdvice
public class SecurityExceptionHandler {
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthenticationException(AuthenticationException e) {
        // Log detailed error for debugging
        log.warn("Authentication failed: {}", e.getMessage());
        
        // Return generic error message to client
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Invalid credentials"));
    }
}
```

### Stack Trace Protection
- Never expose stack traces to clients
- Use custom error pages in production
- Implement global exception handling

## Client-Side Security

### Token Storage
- **Never** store tokens in localStorage for production apps
- Use HTTP-only cookies for web applications
- Implement secure token refresh logic

```javascript
// Good: Using HTTP-only cookies
// Set via server: Set-Cookie: access_token=...; HttpOnly; Secure; SameSite=Strict

// Acceptable for development: sessionStorage (better than localStorage)
sessionStorage.setItem('access_token', token);

// Bad: localStorage (persistent and accessible via XSS)
// localStorage.setItem('access_token', token); // DON'T DO THIS
```

### CSRF Protection
- Implement CSRF protection for state-changing operations
- Use double-submit cookie pattern or synchronizer token

```javascript
// Include CSRF token in requests
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
fetch('/api/reports', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'X-CSRF-TOKEN': csrfToken
  },
  body: JSON.stringify(reportData)
});
```

## Deployment Security

### Container Security
- Use official, minimal base images
- Regularly update dependencies and base images
- Scan images for vulnerabilities
- Run containers as non-root user

```dockerfile
# Dockerfile best practices
FROM openjdk:17-jre-slim

# Create non-root user
RUN addgroup --system --gid 1001 appuser && \
    adduser --system --uid 1001 --gid 1001 appuser

# Copy application
COPY --chown=appuser:appuser target/weekly-report.jar app.jar

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Network Security
- Use firewalls to restrict network access
- Implement network segmentation
- Use VPNs for administrative access

### Monitoring & Alerting
- Monitor for security events and anomalies
- Set up alerts for failed authentication attempts
- Regularly review security logs

```yaml
# Example monitoring configuration
management:
  metrics:
    export:
      prometheus:
        enabled: true
  endpoint:
    prometheus:
      enabled: true
    metrics:
      enabled: true
```

## Security Testing

### Automated Testing
- Include security tests in CI/CD pipeline
- Test authentication and authorization scenarios
- Perform dependency vulnerability scanning

```bash
#!/bin/bash
# Security testing script
mvn clean test # Run unit and integration tests
mvn dependency-check:check # Check for known vulnerabilities
docker run --rm -v $(pwd):/workspace securecodewarrior/github-action-add-sarif # SAST scanning
```

### Manual Testing
- Regular penetration testing
- Security code reviews
- Manual testing of authentication flows

## Incident Response

### Security Incident Handling
- Have an incident response plan
- Monitor for security breaches
- Implement breach notification procedures
- Regular security training for development team

### Data Breach Response
1. **Immediate**: Isolate affected systems
2. **Assessment**: Determine scope of breach
3. **Notification**: Inform relevant stakeholders
4. **Recovery**: Implement fixes and monitor
5. **Review**: Post-incident analysis and improvements

## Compliance & Governance

### Code Reviews
- Mandatory security review for authentication/authorization code
- Use static analysis tools (SonarQube, SpotBugs)
- Regular security training for developers

### Documentation
- Maintain security documentation
- Document security decisions and trade-offs
- Keep security runbooks up-to-date

### Regular Updates
- Keep dependencies up-to-date
- Regular security patches
- Monitor security advisories

## Security Checklist

### Development Checklist
- [ ] Input validation implemented server-side
- [ ] SQL injection protection in place
- [ ] XSS prevention measures implemented
- [ ] Authentication/authorization working correctly
- [ ] Sensitive data encrypted at rest and in transit
- [ ] Error messages don't expose sensitive information
- [ ] Security headers configured
- [ ] Rate limiting implemented
- [ ] Audit logging in place

### Deployment Checklist
- [ ] HTTPS/TLS configured properly
- [ ] Environment variables used for secrets
- [ ] Database connections secured
- [ ] Firewall rules configured
- [ ] Monitoring and alerting set up
- [ ] Security scanning integrated in CI/CD
- [ ] Backup and recovery procedures tested
- [ ] Incident response plan documented

### Operational Checklist
- [ ] Regular security updates applied
- [ ] Log monitoring active
- [ ] Access controls reviewed quarterly
- [ ] Security metrics tracked
- [ ] Incident response procedures tested
- [ ] Security training conducted
- [ ] Third-party dependencies audited
- [ ] Penetration testing scheduled

---

Remember: Security is not a one-time implementation but an ongoing process. Regular reviews, updates, and training are essential for maintaining a secure system.