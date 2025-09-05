#!/bin/bash

# Weekly Report System Environment Setup Script
# Usage: ./setup.sh [environment]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT="${1:-dev}"

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    cat << EOF
Usage: $0 [environment]

Environments:
  dev     Setup development environment (default)
  test    Setup test environment
  prod    Setup production environment

This script will:
  1. Check system prerequisites
  2. Install required dependencies
  3. Set up environment configuration
  4. Initialize database and services
  5. Create initial admin user (dev environment only)

EOF
}

# Function to check system prerequisites
check_prerequisites() {
    print_info "Checking system prerequisites..."

    # Check operating system
    if [[ "$OSTYPE" == "darwin"* ]]; then
        print_info "Detected macOS"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        print_info "Detected Linux"
    else
        print_warning "Unsupported operating system: $OSTYPE"
    fi

    # Check required tools
    local required_tools=("git" "curl" "node" "npm" "docker" "docker-compose")
    local missing_tools=()

    for tool in "${required_tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            missing_tools+=("$tool")
        else
            local version
            case "$tool" in
                node)
                    version=$(node --version)
                    print_info "✓ Node.js: $version"
                    ;;
                npm)
                    version=$(npm --version)
                    print_info "✓ npm: $version"
                    ;;
                docker)
                    version=$(docker --version)
                    print_info "✓ Docker: $version"
                    ;;
                docker-compose)
                    version=$(docker-compose --version)
                    print_info "✓ Docker Compose: $version"
                    ;;
                *)
                    print_info "✓ $tool is installed"
                    ;;
            esac
        fi
    done

    # Check Java for backend
    if ! command -v java &> /dev/null; then
        print_warning "Java is not installed. Backend development requires Java 17+"
    else
        local java_version=$(java -version 2>&1 | grep "version" | awk '{print $3}' | tr -d '"')
        print_info "✓ Java: $java_version"
    fi

    # Check Maven for backend
    if ! command -v mvn &> /dev/null; then
        print_warning "Maven is not installed. Backend development requires Maven 3.6+"
    else
        local maven_version=$(mvn --version | grep "Apache Maven" | awk '{print $3}')
        print_info "✓ Maven: $maven_version"
    fi

    if [[ ${#missing_tools[@]} -gt 0 ]]; then
        print_error "Missing required tools: ${missing_tools[*]}"
        print_info "Please install the missing tools and run this script again."
        
        if [[ "$OSTYPE" == "darwin"* ]]; then
            print_info "On macOS, you can install most tools using Homebrew:"
            print_info "  brew install node docker docker-compose"
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            print_info "On Ubuntu/Debian:"
            print_info "  sudo apt-get update"
            print_info "  sudo apt-get install nodejs npm docker.io docker-compose"
        fi
        
        exit 1
    fi

    print_success "All prerequisites are satisfied"
}

# Function to setup environment configuration
setup_environment_config() {
    print_info "Setting up $ENVIRONMENT environment configuration..."

    local env_dir="$PROJECT_ROOT/deploy/$ENVIRONMENT"
    local env_file="$env_dir/.env"
    
    # Create .env file if it doesn't exist
    if [[ ! -f "$env_file" ]]; then
        print_info "Creating environment file: $env_file"
        
        cat > "$env_file" << EOF
# Environment Configuration for $ENVIRONMENT
ENVIRONMENT=$ENVIRONMENT

# Database Configuration
DB_HOST=localhost
DB_NAME=weekly_report_$ENVIRONMENT
DB_USERNAME=${ENVIRONMENT}_user
DB_PASSWORD=${ENVIRONMENT}_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PASSWORD=

# JWT Configuration
JWT_SECRET=$(openssl rand -base64 32)

# Email Configuration (update with your SMTP settings)
SMTP_HOST=smtp.mailtrap.io
SMTP_PORT=2525
SMTP_USER=
SMTP_PASSWORD=

# File Storage Configuration
S3_BUCKET_NAME=weekly-report-$ENVIRONMENT
S3_REGION=us-west-2
S3_ACCESS_KEY=
S3_SECRET_KEY=

# Docker Registry Configuration
DOCKER_REGISTRY=your-registry.com
DOCKER_USERNAME=
DOCKER_PASSWORD=
EOF
        
        print_success "Environment file created. Please update the values in $env_file"
    else
        print_info "Environment file already exists: $env_file"
    fi
}

# Function to setup project directories
setup_project_structure() {
    print_info "Setting up project directory structure..."

    local directories=(
        "$PROJECT_ROOT/logs"
        "$PROJECT_ROOT/uploads"
        "$PROJECT_ROOT/backups"
        "$PROJECT_ROOT/deploy/$ENVIRONMENT/logs"
        "$PROJECT_ROOT/deploy/$ENVIRONMENT/data"
    )

    for dir in "${directories[@]}"; do
        if [[ ! -d "$dir" ]]; then
            mkdir -p "$dir"
            print_info "Created directory: $dir"
        fi
    done

    # Set appropriate permissions
    chmod 755 "$PROJECT_ROOT/uploads"
    chmod 755 "$PROJECT_ROOT/logs"
    
    print_success "Project structure setup completed"
}

# Function to initialize database
initialize_database() {
    if [[ "$ENVIRONMENT" != "dev" ]]; then
        print_info "Skipping database initialization for $ENVIRONMENT environment"
        return 0
    fi

    print_info "Initializing database for development..."

    local deploy_dir="$PROJECT_ROOT/deploy/$ENVIRONMENT"
    local init_script_dir="$deploy_dir/init-scripts"
    
    # Create database initialization scripts directory
    mkdir -p "$init_script_dir"
    
    # Create initial database schema script
    cat > "$init_script_dir/01-create-schema.sql" << 'EOF'
-- Initial database schema for Weekly Report System
-- This will be replaced with proper migrations when backend is implemented

CREATE DATABASE IF NOT EXISTS weekly_report_dev;
USE weekly_report_dev;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    role ENUM('ADMIN', 'MANAGER', 'EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create initial admin user (password: admin123)
INSERT IGNORE INTO users (username, email, password_hash, first_name, last_name, role) VALUES 
('admin', 'admin@example.com', '$2a$10$8K1p/a0dL2AZ.x8/q6uXWeOYzuUyKA0QCX2rQF2NVJ1HG1/R8L9TO', 'System', 'Administrator', 'ADMIN');

-- Create indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_active ON users(active);
EOF

    # Create sample data script
    cat > "$init_script_dir/02-sample-data.sql" << 'EOF'
-- Sample data for development
USE weekly_report_dev;

-- Sample users
INSERT IGNORE INTO users (username, email, password_hash, first_name, last_name, role) VALUES 
('john.doe', 'john.doe@example.com', '$2a$10$8K1p/a0dL2AZ.x8/q6uXWeOYzuUyKA0QCX2rQF2NVJ1HG1/R8L9TO', 'John', 'Doe', 'EMPLOYEE'),
('jane.smith', 'jane.smith@example.com', '$2a$10$8K1p/a0dL2AZ.x8/q6uXWeOYzuUyKA0QCX2rQF2NVJ1HG1/R8L9TO', 'Jane', 'Smith', 'MANAGER');
EOF

    print_success "Database initialization scripts created"
}

# Function to install dependencies
install_dependencies() {
    print_info "Installing project dependencies..."

    # Install frontend dependencies if package.json exists
    if [[ -f "$PROJECT_ROOT/frontend/package.json" ]]; then
        print_info "Installing frontend dependencies..."
        cd "$PROJECT_ROOT/frontend"
        npm install
        cd "$PROJECT_ROOT"
        print_success "Frontend dependencies installed"
    fi

    # Install backend dependencies if pom.xml exists
    if [[ -f "$PROJECT_ROOT/backend/pom.xml" ]]; then
        print_info "Installing backend dependencies..."
        cd "$PROJECT_ROOT/backend"
        mvn clean install -DskipTests
        cd "$PROJECT_ROOT"
        print_success "Backend dependencies installed"
    fi

    # Install root level dependencies for tooling
    if [[ -f "$PROJECT_ROOT/package.json" ]]; then
        print_info "Installing root dependencies..."
        cd "$PROJECT_ROOT"
        npm install
        print_success "Root dependencies installed"
    fi
}

# Function to start services
start_services() {
    if [[ "$ENVIRONMENT" != "dev" ]]; then
        print_info "Service startup is only automated for development environment"
        return 0
    fi

    print_info "Starting development services..."

    local deploy_dir="$PROJECT_ROOT/deploy/$ENVIRONMENT"
    local docker_compose_file="$deploy_dir/docker-compose.yml"

    if [[ -f "$docker_compose_file" ]]; then
        cd "$deploy_dir"
        
        print_info "Starting infrastructure services (database, redis, etc.)..."
        docker-compose up -d mysql redis mailhog phpmyadmin
        
        print_info "Waiting for database to be ready..."
        sleep 30
        
        print_success "Infrastructure services started"
        print_info "Services available at:"
        print_info "  MySQL: localhost:3306"
        print_info "  Redis: localhost:6379"
        print_info "  phpMyAdmin: http://localhost:8081"
        print_info "  MailHog: http://localhost:8025"
        
        cd "$PROJECT_ROOT"
    fi
}

# Function to run post-setup tasks
post_setup_tasks() {
    print_info "Running post-setup tasks..."

    # Create .gitignore entries for generated files
    local gitignore_additions=(
        "logs/"
        "uploads/"
        "deploy/*/data/"
        "deploy/*/.env"
        "*.log"
    )

    for entry in "${gitignore_additions[@]}"; do
        if ! grep -q "$entry" "$PROJECT_ROOT/.gitignore" 2>/dev/null; then
            echo "$entry" >> "$PROJECT_ROOT/.gitignore"
        fi
    done

    # Create useful aliases/scripts
    cat > "$PROJECT_ROOT/dev.sh" << 'EOF'
#!/bin/bash
# Development helper script

case "$1" in
    start)
        echo "Starting development environment..."
        cd deploy/dev && docker-compose up -d
        ;;
    stop)
        echo "Stopping development environment..."
        cd deploy/dev && docker-compose down
        ;;
    logs)
        cd deploy/dev && docker-compose logs -f
        ;;
    restart)
        echo "Restarting development environment..."
        cd deploy/dev && docker-compose restart
        ;;
    *)
        echo "Usage: $0 {start|stop|logs|restart}"
        ;;
esac
EOF
    chmod +x "$PROJECT_ROOT/dev.sh"

    print_success "Post-setup tasks completed"
}

# Function to display final instructions
display_final_instructions() {
    print_success "🎉 Environment setup completed successfully!"
    
    echo
    print_info "Next steps:"
    echo "1. Update configuration in deploy/$ENVIRONMENT/.env"
    echo "2. Review configuration in deploy/$ENVIRONMENT/config.yml"
    
    if [[ "$ENVIRONMENT" == "dev" ]]; then
        echo "3. Start development environment: ./dev.sh start"
        echo "4. Access the application:"
        echo "   - Frontend: http://localhost:5173 (when implemented)"
        echo "   - Backend API: http://localhost:8080/api (when implemented)"
        echo "   - Database Admin: http://localhost:8081"
        echo "   - Email Testing: http://localhost:8025"
        echo
        print_info "Default admin credentials (when backend is ready):"
        echo "   Username: admin"
        echo "   Password: admin123"
    fi
    
    echo
    print_info "Available commands:"
    echo "   ./dev.sh start    - Start development services"
    echo "   ./dev.sh stop     - Stop development services"
    echo "   ./dev.sh logs     - View service logs"
    echo "   ./deploy/scripts/deploy.sh dev - Full deployment"
}

# Main setup function
main() {
    print_info "Setting up Weekly Report System - $ENVIRONMENT environment"
    echo

    if [[ "$1" == "-h" || "$1" == "--help" ]]; then
        show_usage
        exit 0
    fi

    check_prerequisites
    setup_environment_config
    setup_project_structure
    initialize_database
    install_dependencies
    start_services
    post_setup_tasks
    display_final_instructions
}

# Run main function
main "$@"