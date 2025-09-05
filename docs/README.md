# Weekly Report System

A modern, full-stack web application for managing and generating weekly reports within organizations. Built with Spring Boot backend and Vue.js frontend, containerized with Docker for scalable deployment.

## 🚀 Features

- **User Management**: Role-based access control (Admin, Manager, Employee)
- **Report Creation**: Intuitive interface for weekly report submission
- **Dashboard**: Analytics and visualization of report data
- **Export Functions**: PDF and Excel export capabilities
- **Email Notifications**: Automated reminders and report distribution
- **Audit Logging**: Complete trail of user activities

## 🏗️ Architecture

### Backend
- **Framework**: Spring Boot 3.x
- **Database**: MySQL 8.0
- **Caching**: Redis
- **Authentication**: JWT with Spring Security
- **Documentation**: OpenAPI 3.0 (Swagger)

### Frontend
- **Framework**: Vue 3 with Composition API
- **Build Tool**: Vite
- **UI Library**: Element Plus
- **State Management**: Pinia
- **Routing**: Vue Router 4

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Reverse Proxy**: Nginx
- **CI/CD**: GitHub Actions
- **Environment Management**: Multi-environment configurations

## 🛠️ Development Setup

### Prerequisites

- **Node.js** 18+ and npm
- **Java** 17+
- **Maven** 3.8+
- **Docker** and Docker Compose
- **Git**

### Quick Start

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd weekly-report-system
   ```

2. **Run the setup script**:
   ```bash
   ./deploy/scripts/setup.sh dev
   ```

3. **Start the development environment**:
   ```bash
   ./dev.sh start
   ```

4. **Access the application**:
   - Frontend: http://localhost:5173
   - Backend API: http://localhost:8080/api
   - Database Admin: http://localhost:8081
   - Email Testing: http://localhost:8025

### Manual Setup

If you prefer manual setup or encounter issues with the automated script:

#### Backend Setup
```bash
cd backend
mvn clean install
mvn spring-boot:run -Dspring.profiles.active=development
```

#### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

#### Database Setup
```bash
cd deploy/dev
docker-compose up -d mysql redis
```

## 📁 Project Structure

```
weekly-report-system/
├── backend/                 # Spring Boot application
│   ├── src/main/java/      # Java source code
│   ├── src/main/resources/ # Configuration files
│   ├── src/test/          # Test files
│   └── pom.xml           # Maven configuration
├── frontend/              # Vue.js application
│   ├── src/              # Vue source code
│   ├── public/           # Static assets
│   ├── tests/            # Test files
│   └── package.json      # Node.js dependencies
├── deploy/               # Deployment configurations
│   ├── dev/             # Development environment
│   ├── test/            # Test environment
│   ├── prod/            # Production environment
│   └── scripts/         # Deployment scripts
├── docs/                # Documentation
├── .github/workflows/   # CI/CD pipelines
└── README.md           # This file
```

## 🧪 Testing

### Running Tests

```bash
# Backend tests
cd backend
mvn test

# Frontend tests
cd frontend
npm run test

# Integration tests
npm run test:e2e

# Code coverage
npm run test:coverage
```

### Test Configuration

- **Unit Tests**: Jest (Frontend), JUnit 5 (Backend)
- **Integration Tests**: Testcontainers (Backend), Cypress (Frontend)
- **Performance Tests**: K6 load testing
- **Security Tests**: OWASP dependency check

## 🚀 Deployment

### Development
```bash
./deploy/scripts/deploy.sh dev
```

### Production
```bash
./deploy/scripts/deploy.sh prod
```

### Environment Configuration

Each environment has its own configuration:
- `deploy/dev/config.yml` - Development settings
- `deploy/test/config.yml` - Test environment settings
- `deploy/prod/config.yml` - Production settings

Update the `.env` file in each environment directory with your specific values.

## 📊 Monitoring & Observability

### Health Checks
- Backend: `http://localhost:8080/actuator/health`
- Database connectivity and Redis availability checks
- Automated performance monitoring

### Logging
- Structured logging with correlation IDs
- Centralized log aggregation (configurable)
- Different log levels per environment

### Metrics
- Application metrics via Spring Boot Actuator
- Custom business metrics
- Performance dashboards (Grafana integration ready)

## 🔒 Security

### Authentication & Authorization
- JWT-based authentication
- Role-based access control (RBAC)
- Secure password hashing with BCrypt
- Session management with Redis

### Security Headers
- CSRF protection
- XSS protection
- Content Security Policy
- HTTPS enforcement (production)

### Data Protection
- Input validation and sanitization
- SQL injection prevention
- Sensitive data encryption
- Audit logging for compliance

## 🤝 Contributing

### Development Workflow

1. Create a feature branch from `develop`
2. Make your changes following the coding standards
3. Write tests for new functionality
4. Ensure all tests pass and code quality checks succeed
5. Submit a pull request to `develop`

### Code Quality Standards

- **ESLint** configuration for JavaScript/Vue
- **Checkstyle** and **SpotBugs** for Java
- **Prettier** for code formatting
- Minimum 80% test coverage required

### Commit Message Format

```
type(scope): description

feat(auth): add JWT token refresh mechanism
fix(reports): resolve date filtering issue
docs(api): update endpoint documentation
test(user): add unit tests for user service
```

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Issues**
   - Check MySQL container is running: `docker ps`
   - Verify connection settings in `deploy/dev/.env`

2. **Frontend Build Failures**
   - Clear node_modules: `rm -rf node_modules && npm install`
   - Check Node.js version compatibility

3. **Backend Startup Issues**
   - Verify Java version: `java -version`
   - Check Maven dependencies: `mvn dependency:tree`

4. **Docker Issues**
   - Restart Docker service
   - Clean Docker system: `docker system prune -a`

### Getting Help

- Check the [troubleshooting guide](docs/troubleshooting.md)
- Review application logs in the `logs/` directory
- Search existing GitHub issues
- Create a new issue with detailed reproduction steps

## 📚 API Documentation

Once the backend is running, access the API documentation at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

## 🗺️ Roadmap

### Version 1.0 (Current)
- ✅ Basic user management
- ✅ Report creation and submission
- ✅ Dashboard with basic analytics
- ✅ PDF export functionality

### Version 1.1 (Planned)
- 🔄 Advanced analytics and reporting
- 🔄 Mobile-responsive design improvements
- 🔄 Bulk operations for reports
- 🔄 Integration with external calendar systems

### Version 2.0 (Future)
- 📋 Real-time collaboration features
- 📋 Advanced workflow management
- 📋 Machine learning-based insights
- 📋 Mobile application

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Development Team**: Full-stack developers working on core functionality
- **DevOps Team**: Infrastructure and deployment automation
- **QA Team**: Testing and quality assurance

## 📞 Support

For technical support or questions:
- Create an issue in this repository
- Contact the development team
- Check the documentation in the `docs/` directory

---

Built with ❤️ using modern web technologies