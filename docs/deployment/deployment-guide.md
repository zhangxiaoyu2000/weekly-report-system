# Deployment Guide

This guide covers deploying the Weekly Report System across different environments using our automated deployment scripts and Docker-based infrastructure.

## Overview

The Weekly Report System supports three deployment environments:

- **Development**: Local development with hot-reload and debugging tools
- **Test**: Staging environment for integration testing and QA
- **Production**: Live production environment with full security and monitoring

## Prerequisites

### Required Software

- Docker Engine 20.10+
- Docker Compose 2.0+
- Git 2.30+
- curl or wget (for health checks)

### Required Access

- GitHub repository access
- Docker registry credentials (for production)
- Server access with sudo privileges
- Environment-specific configuration files

## Quick Deployment

### Development Environment

```bash
# Setup and deploy in one command
./deploy/scripts/setup.sh dev
./deploy/scripts/deploy.sh dev

# Or use the helper script
./dev.sh start
```

### Test Environment

```bash
./deploy/scripts/deploy.sh test
```

### Production Environment

```bash
# Production requires confirmation
./deploy/scripts/deploy.sh prod
```

## Detailed Deployment Process

### 1. Pre-Deployment Checklist

Before deploying to any environment, ensure:

- [ ] All tests pass locally
- [ ] Code is committed to the appropriate branch
- [ ] Environment configuration is updated
- [ ] Database migrations are prepared
- [ ] Dependencies are up to date
- [ ] Security configurations are reviewed (for production)

### 2. Environment Configuration

Each environment requires a configuration file and environment variables:

#### Configuration Files Location
```
deploy/
├── dev/
│   ├── config.yml
│   ├── .env
│   ├── docker-compose.yml
│   └── nginx.conf
├── test/
│   └── [same structure]
└── prod/
    └── [same structure]
```

#### Environment Variables Template

Create `.env` file in each environment directory:

```bash
# Database Configuration
DB_HOST=your-db-host
DB_NAME=weekly_report_prod
DB_USERNAME=your-username
DB_PASSWORD=your-secure-password

# Redis Configuration
REDIS_HOST=your-redis-host
REDIS_PASSWORD=your-redis-password

# JWT Configuration
JWT_SECRET=your-very-secure-jwt-secret-minimum-32-characters

# SMTP Configuration
SMTP_HOST=your-smtp-server
SMTP_PORT=587
SMTP_USER=your-email@domain.com
SMTP_PASSWORD=your-email-password

# File Storage (Production)
S3_BUCKET_NAME=your-s3-bucket
S3_REGION=us-west-2
S3_ACCESS_KEY=your-access-key
S3_SECRET_KEY=your-secret-key

# Docker Registry (Production)
DOCKER_REGISTRY=your-registry.com
DOCKER_USERNAME=your-username
DOCKER_PASSWORD=your-password
```

### 3. Deployment Script Options

The deployment script supports various options:

```bash
./deploy/scripts/deploy.sh <environment> [options]

Options:
  --skip-tests     Skip running tests before deployment
  --skip-build     Skip building the application
  --dry-run        Show what would be deployed
  --force          Force deployment even if tests fail

Examples:
  ./deploy/scripts/deploy.sh dev
  ./deploy/scripts/deploy.sh prod --skip-tests
  ./deploy/scripts/deploy.sh test --dry-run
```

### 4. Step-by-Step Deployment Process

#### Development Deployment

1. **Setup** (first time only):
   ```bash
   ./deploy/scripts/setup.sh dev
   ```

2. **Deploy**:
   ```bash
   ./deploy/scripts/deploy.sh dev
   ```

3. **Verify**:
   - Frontend: http://localhost:5173
   - Backend: http://localhost:8080/actuator/health
   - Database: http://localhost:8081 (phpMyAdmin)

#### Test Environment Deployment

1. **Prepare**:
   ```bash
   git checkout develop
   git pull origin develop
   ```

2. **Configure**:
   Update `deploy/test/.env` with test environment values

3. **Deploy**:
   ```bash
   ./deploy/scripts/deploy.sh test
   ```

4. **Verify**:
   Run automated tests against the test environment

#### Production Deployment

1. **Prepare**:
   ```bash
   git checkout main
   git pull origin main
   git tag v1.0.0  # Create release tag
   ```

2. **Security Check**:
   Review all configuration files for production readiness

3. **Backup** (automatic):
   The script automatically creates backups before production deployment

4. **Deploy**:
   ```bash
   ./deploy/scripts/deploy.sh prod
   ```

5. **Verify**:
   - Health checks pass
   - Smoke tests complete
   - Monitoring dashboards show green status

## Environment-Specific Configurations

### Development Environment

**Features**:
- Hot reload for frontend development
- Database seeding with sample data
- Email testing with MailHog
- Debug logging enabled
- CORS configured for local development

**Services**:
- MySQL (port 3306)
- Redis (port 6379)  
- Backend (port 8080)
- Frontend (port 5173)
- phpMyAdmin (port 8081)
- MailHog (port 8025)
- Nginx (port 80)

### Test Environment

**Features**:
- Automated testing integration
- Performance monitoring
- Staging data management
- CI/CD integration
- Email notifications enabled

**Configuration**:
- Scaled-down resource allocation
- Test-specific database
- Mock external services available
- Enhanced logging for debugging

### Production Environment

**Features**:
- SSL/TLS encryption
- Database backups
- Performance monitoring
- Security hardening
- Error alerting
- Auto-scaling capabilities

**Security Measures**:
- Secrets management
- Network isolation
- Regular security updates
- Access logging
- Intrusion detection

## Monitoring and Health Checks

### Health Check Endpoints

The deployment script automatically verifies these endpoints:

```bash
# Backend health
curl -f http://localhost:8080/actuator/health

# Database connectivity
curl -f http://localhost:8080/actuator/health/db

# Redis connectivity  
curl -f http://localhost:8080/actuator/health/redis
```

### Monitoring Setup

Each environment includes monitoring configurations:

- **Application metrics** via Spring Boot Actuator
- **Infrastructure metrics** via Docker stats
- **Log aggregation** (configurable)
- **Error tracking** (configurable)

### Log Locations

```bash
# Application logs
./logs/application.log

# Nginx logs
./deploy/[env]/logs/nginx/

# Database logs
docker logs weekly-report-mysql-[env]

# Container logs
docker-compose logs -f
```

## Rollback Procedures

### Automatic Rollback

If health checks fail after deployment, you can rollback:

```bash
# Rollback to previous version
./deploy/scripts/rollback.sh prod

# Rollback to specific version
./deploy/scripts/rollback.sh prod v1.2.3
```

### Manual Rollback

1. **Stop current services**:
   ```bash
   cd deploy/prod
   docker-compose down
   ```

2. **Restore backup**:
   ```bash
   # Database restore (if needed)
   mysql -u root -p weekly_report_prod < backup/backup-YYYYMMDD.sql
   ```

3. **Deploy previous version**:
   ```bash
   git checkout v1.2.3
   ./deploy/scripts/deploy.sh prod --skip-tests
   ```

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   ```bash
   # Check database status
   docker ps | grep mysql
   
   # Check logs
   docker logs weekly-report-mysql-prod
   
   # Verify configuration
   cat deploy/prod/.env | grep DB_
   ```

2. **Application Won't Start**
   ```bash
   # Check Java version
   java --version
   
   # Check port availability
   netstat -tlpn | grep :8080
   
   # Review application logs
   tail -f logs/application.log
   ```

3. **Frontend Build Issues**
   ```bash
   # Clear cache and rebuild
   cd frontend
   rm -rf node_modules package-lock.json
   npm install
   npm run build
   ```

4. **Docker Issues**
   ```bash
   # Check Docker system
   docker system df
   docker system prune -a
   
   # Restart Docker service
   sudo systemctl restart docker
   ```

### Getting Help

1. Check logs in the appropriate directory
2. Review health check outputs
3. Verify environment configuration
4. Search existing issues
5. Contact the DevOps team

## Maintenance

### Regular Tasks

1. **Daily**:
   - Monitor application logs
   - Check system resources
   - Verify backup completion

2. **Weekly**:
   - Update dependencies
   - Review security logs
   - Performance analysis

3. **Monthly**:
   - Security patches
   - Database optimization
   - Disaster recovery testing

### Automated Maintenance

The CI/CD pipeline handles:
- Dependency updates
- Security scanning
- Performance testing
- Backup verification

## Security Considerations

### Production Security Checklist

- [ ] SSL/TLS certificates configured
- [ ] Database connections encrypted
- [ ] Secrets stored securely (not in environment files)
- [ ] Network access restricted
- [ ] Regular security updates applied
- [ ] Audit logging enabled
- [ ] Backup encryption enabled
- [ ] Access controls implemented

### Secret Management

Use environment-specific secret management:

```bash
# Development (local .env files)
echo "JWT_SECRET=dev-secret" >> deploy/dev/.env

# Production (external secret management)
# Use AWS Secrets Manager, HashiCorp Vault, or similar
```

## Performance Tuning

### Database Optimization

```sql
-- Monitor slow queries
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;

-- Optimize indexes based on usage patterns
ANALYZE TABLE users, reports;
```

### Application Tuning

```yaml
# JVM tuning for production
JAVA_OPTS: "-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Nginx Optimization

```nginx
# Enable compression
gzip on;
gzip_types text/plain text/css application/json application/javascript;

# Enable caching
location /static/ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

---

For additional help or questions about deployment, please refer to the troubleshooting guide or contact the DevOps team.