# Getting Started with Weekly Report System Development

This guide will help you set up your development environment and understand the project structure.

## Prerequisites

Before you begin, ensure you have the following installed on your system:

### Required Software

- **Node.js** (18.x or later)
- **npm** (9.x or later)
- **Java** (17 or later)
- **Maven** (3.8 or later)
- **Docker** and **Docker Compose**
- **Git**

### Optional but Recommended

- **Visual Studio Code** with the following extensions:
  - Vue Language Features (Volar)
  - Java Extension Pack
  - Docker
  - GitLens
  - Prettier
  - ESLint

### Verifying Installation

Run these commands to verify your setup:

```bash
node --version    # Should show v18.x.x or later
npm --version     # Should show 9.x.x or later
java --version    # Should show Java 17 or later
mvn --version     # Should show Maven 3.8 or later
docker --version  # Should show Docker version
git --version     # Should show Git version
```

## Project Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd weekly-report-system
```

### 2. Automated Setup

Use our setup script to configure everything automatically:

```bash
./deploy/scripts/setup.sh dev
```

This script will:
- Check all prerequisites
- Create necessary directories
- Set up environment configuration files
- Initialize the database
- Install all dependencies
- Start development services

### 3. Manual Setup (Alternative)

If you prefer to set up manually or the automated script encounters issues:

#### Backend Setup

```bash
cd backend
mvn clean install -DskipTests
```

#### Frontend Setup

```bash
cd frontend
npm install
```

#### Infrastructure Setup

```bash
cd deploy/dev
docker-compose up -d mysql redis mailhog phpmyadmin
```

## Development Workflow

### Starting the Development Environment

```bash
# Option 1: Use the helper script (recommended)
./dev.sh start

# Option 2: Manual startup
cd deploy/dev && docker-compose up -d
cd ../../backend && mvn spring-boot:run -Dspring.profiles.active=development &
cd ../frontend && npm run dev
```

### Accessing Services

Once started, you can access:

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Database Admin (phpMyAdmin)**: http://localhost:8081
- **Email Testing (MailHog)**: http://localhost:8025
- **Redis**: localhost:6379

### Default Credentials

#### Database (phpMyAdmin)
- **Server**: mysql
- **Username**: root
- **Password**: rootpassword

#### Application (when backend is ready)
- **Username**: admin
- **Password**: admin123

## Project Structure Overview

```
weekly-report-system/
├── backend/                    # Spring Boot application
│   ├── src/main/java/         # Main Java source code
│   │   ├── controller/        # REST controllers
│   │   ├── service/           # Business logic
│   │   ├── repository/        # Data access layer
│   │   ├── model/             # Entity models
│   │   ├── dto/               # Data transfer objects
│   │   ├── config/            # Configuration classes
│   │   └── exception/         # Custom exceptions
│   ├── src/main/resources/    # Configuration files
│   │   ├── application.yml    # Main configuration
│   │   ├── application-dev.yml # Development config
│   │   └── db/migration/      # Database migrations
│   ├── src/test/              # Test files
│   └── pom.xml               # Maven dependencies
├── frontend/                  # Vue.js application
│   ├── src/                  # Vue source code
│   │   ├── components/       # Reusable components
│   │   ├── views/            # Page components
│   │   ├── stores/           # Pinia state management
│   │   ├── router/           # Vue Router configuration
│   │   ├── services/         # API service classes
│   │   ├── utils/            # Utility functions
│   │   └── assets/           # Static assets
│   ├── public/               # Public static files
│   ├── tests/                # Test files
│   └── package.json          # Dependencies
├── deploy/                   # Deployment configurations
└── docs/                     # Project documentation
```

## Development Guidelines

### Code Organization

#### Backend (Spring Boot)
- Follow standard Spring Boot project structure
- Use dependency injection with `@Autowired` or constructor injection
- Implement proper error handling with custom exceptions
- Write comprehensive unit and integration tests
- Use DTOs for API requests/responses

#### Frontend (Vue.js)
- Use Vue 3 Composition API
- Implement proper component composition
- Use Pinia for state management
- Follow Vue.js style guide conventions
- Write unit tests for components and utilities

### Coding Standards

#### Java/Spring Boot
- Use Java naming conventions (camelCase for variables/methods, PascalCase for classes)
- Maximum line length: 120 characters
- Use meaningful variable and method names
- Include JavaDoc for public methods
- Follow SOLID principles

#### JavaScript/Vue.js
- Use ES6+ features
- Follow ESLint configuration
- Use Prettier for code formatting
- Implement proper error boundaries
- Use TypeScript for complex components (optional but recommended)

### Testing Strategy

#### Backend Testing
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report
```

#### Frontend Testing
```bash
# Run unit tests
npm run test

# Run tests in watch mode
npm run test:watch

# Run E2E tests
npm run test:e2e

# Generate coverage report
npm run test:coverage
```

### Database Development

#### Migrations
- Create new migrations in `backend/src/main/resources/db/migration/`
- Use Flyway naming convention: `V{version}__{description}.sql`
- Never modify existing migrations in production

#### Example Migration
```sql
-- V1.1__Create_reports_table.sql
CREATE TABLE reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    week_start_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Common Development Tasks

### Adding a New API Endpoint

1. Create/update the entity in `backend/src/main/java/model/`
2. Create repository interface in `backend/src/main/java/repository/`
3. Implement service logic in `backend/src/main/java/service/`
4. Create controller in `backend/src/main/java/controller/`
5. Write tests for all layers
6. Update API documentation

### Creating a New Vue Component

1. Create component file in `frontend/src/components/`
2. Implement component logic using Composition API
3. Add component to appropriate view or parent component
4. Write unit tests
5. Update routing if necessary

### Environment Variables

Create `.env` files in deployment directories:

```bash
# deploy/dev/.env
DB_HOST=localhost
DB_USERNAME=dev_user
DB_PASSWORD=dev_password
JWT_SECRET=your-development-secret
SMTP_HOST=localhost
SMTP_PORT=1025
```

## Debugging

### Backend Debugging
- Use your IDE's debugging features
- Enable debug logging in `application-dev.yml`
- Use Spring Boot Actuator endpoints for health checks
- Monitor logs in `logs/` directory

### Frontend Debugging
- Use browser developer tools
- Install Vue DevTools browser extension
- Use console.log for simple debugging
- Implement proper error boundaries

### Database Debugging
- Use phpMyAdmin at http://localhost:8081
- Monitor database logs in Docker container
- Use MySQL Workbench for complex queries

## Troubleshooting

### Common Issues

1. **Port conflicts**: Check if ports 3306, 6379, 8080, 5173 are available
2. **Permission errors**: Ensure Docker has proper permissions
3. **Database connection issues**: Wait for MySQL container to fully start
4. **Hot reload not working**: Check file watchers in your IDE

### Getting Help

1. Check the application logs
2. Review the troubleshooting guide in `docs/troubleshooting.md`
3. Search existing GitHub issues
4. Ask team members or create a new issue

## Next Steps

1. Explore the codebase structure
2. Run the existing tests to ensure everything works
3. Try creating a simple feature to understand the workflow
4. Read the API documentation
5. Join the development team meetings

Happy coding! 🚀