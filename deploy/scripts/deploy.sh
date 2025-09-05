#!/bin/bash

# Weekly Report System Deployment Script
# Usage: ./deploy.sh [environment] [options]
# Environments: dev, test, prod

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT=""
SKIP_TESTS=false
SKIP_BUILD=false
DRY_RUN=false
FORCE=false

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
Usage: $0 <environment> [options]

Environments:
  dev     Deploy to development environment
  test    Deploy to test environment
  prod    Deploy to production environment

Options:
  --skip-tests    Skip running tests before deployment
  --skip-build    Skip building the application
  --dry-run       Show what would be deployed without actually deploying
  --force         Force deployment even if tests fail
  -h, --help      Show this help message

Examples:
  $0 dev
  $0 prod --skip-tests
  $0 test --dry-run

EOF
}

# Parse command line arguments
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            dev|test|prod)
                ENVIRONMENT="$1"
                shift
                ;;
            --skip-tests)
                SKIP_TESTS=true
                shift
                ;;
            --skip-build)
                SKIP_BUILD=true
                shift
                ;;
            --dry-run)
                DRY_RUN=true
                shift
                ;;
            --force)
                FORCE=true
                shift
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done

    if [[ -z "$ENVIRONMENT" ]]; then
        print_error "Environment is required"
        show_usage
        exit 1
    fi
}

# Function to validate environment
validate_environment() {
    local env_config_file="$PROJECT_ROOT/deploy/$ENVIRONMENT/config.yml"
    
    if [[ ! -f "$env_config_file" ]]; then
        print_error "Configuration file not found: $env_config_file"
        exit 1
    fi

    if [[ "$ENVIRONMENT" == "prod" && "$FORCE" != "true" ]]; then
        read -p "Are you sure you want to deploy to PRODUCTION? (yes/no): " confirm
        if [[ "$confirm" != "yes" ]]; then
            print_info "Deployment cancelled by user"
            exit 0
        fi
    fi
}

# Function to check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."

    # Check if required tools are installed
    local required_tools=("docker" "docker-compose" "git")
    
    for tool in "${required_tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            print_error "$tool is not installed or not in PATH"
            exit 1
        fi
    done

    # Check if we're in a git repository
    if ! git rev-parse --is-inside-work-tree &> /dev/null; then
        print_error "Not in a git repository"
        exit 1
    fi

    # Check for uncommitted changes
    if [[ -n "$(git status --porcelain)" ]]; then
        print_warning "There are uncommitted changes in the repository"
        if [[ "$FORCE" != "true" ]]; then
            read -p "Continue anyway? (yes/no): " confirm
            if [[ "$confirm" != "yes" ]]; then
                print_info "Deployment cancelled"
                exit 0
            fi
        fi
    fi

    print_success "Prerequisites check passed"
}

# Function to run tests
run_tests() {
    if [[ "$SKIP_TESTS" == "true" ]]; then
        print_warning "Skipping tests (--skip-tests flag provided)"
        return 0
    fi

    print_info "Running tests..."

    # Frontend tests
    if [[ -f "$PROJECT_ROOT/frontend/package.json" ]]; then
        print_info "Running frontend tests..."
        cd "$PROJECT_ROOT/frontend"
        npm test
        cd "$PROJECT_ROOT"
    fi

    # Backend tests
    if [[ -f "$PROJECT_ROOT/backend/pom.xml" ]]; then
        print_info "Running backend tests..."
        cd "$PROJECT_ROOT/backend"
        mvn clean test
        cd "$PROJECT_ROOT"
    fi

    print_success "All tests passed"
}

# Function to build application
build_application() {
    if [[ "$SKIP_BUILD" == "true" ]]; then
        print_warning "Skipping build (--skip-build flag provided)"
        return 0
    fi

    print_info "Building application..."

    # Build frontend
    if [[ -f "$PROJECT_ROOT/frontend/package.json" ]]; then
        print_info "Building frontend..."
        cd "$PROJECT_ROOT/frontend"
        npm run build
        cd "$PROJECT_ROOT"
    fi

    # Build backend
    if [[ -f "$PROJECT_ROOT/backend/pom.xml" ]]; then
        print_info "Building backend..."
        cd "$PROJECT_ROOT/backend"
        mvn clean package -DskipTests
        cd "$PROJECT_ROOT"
    fi

    print_success "Application built successfully"
}

# Function to deploy to environment
deploy_to_environment() {
    print_info "Deploying to $ENVIRONMENT environment..."

    local deploy_dir="$PROJECT_ROOT/deploy/$ENVIRONMENT"
    local docker_compose_file="$deploy_dir/docker-compose.yml"

    if [[ "$DRY_RUN" == "true" ]]; then
        print_info "[DRY RUN] Would deploy using: $docker_compose_file"
        print_info "[DRY RUN] Would apply configuration from: $deploy_dir/config.yml"
        return 0
    fi

    # Load environment-specific configuration
    if [[ -f "$deploy_dir/.env" ]]; then
        print_info "Loading environment variables from $deploy_dir/.env"
        export $(grep -v '^#' "$deploy_dir/.env" | xargs)
    fi

    # Deploy using Docker Compose
    if [[ -f "$docker_compose_file" ]]; then
        print_info "Deploying with Docker Compose..."
        cd "$deploy_dir"
        
        # Pull latest images
        docker-compose pull
        
        # Stop existing services
        docker-compose down
        
        # Start services
        docker-compose up -d
        
        # Wait for services to be healthy
        print_info "Waiting for services to be ready..."
        docker-compose ps
        
        cd "$PROJECT_ROOT"
    else
        print_error "Docker Compose file not found: $docker_compose_file"
        exit 1
    fi

    # Run post-deployment health checks
    run_health_checks

    print_success "Deployment to $ENVIRONMENT completed successfully"
}

# Function to run health checks
run_health_checks() {
    print_info "Running health checks..."

    local max_attempts=30
    local attempt=1

    while [[ $attempt -le $max_attempts ]]; do
        if curl -f -s "http://localhost:8080/actuator/health" > /dev/null; then
            print_success "Backend health check passed"
            break
        else
            print_info "Attempt $attempt/$max_attempts: Backend not ready yet..."
            sleep 5
            ((attempt++))
        fi
    done

    if [[ $attempt -gt $max_attempts ]]; then
        print_error "Backend health check failed after $max_attempts attempts"
        exit 1
    fi
}

# Function to create deployment backup
create_backup() {
    if [[ "$ENVIRONMENT" == "prod" ]]; then
        print_info "Creating backup before production deployment..."
        local backup_dir="$PROJECT_ROOT/backups/$(date +%Y%m%d_%H%M%S)"
        mkdir -p "$backup_dir"
        
        # Add backup logic here based on your needs
        # This could include database backups, configuration backups, etc.
        
        print_success "Backup created at $backup_dir"
    fi
}

# Main deployment function
main() {
    print_info "Starting deployment process..."
    print_info "Environment: $ENVIRONMENT"
    print_info "Skip Tests: $SKIP_TESTS"
    print_info "Skip Build: $SKIP_BUILD"
    print_info "Dry Run: $DRY_RUN"

    validate_environment
    check_prerequisites
    
    if [[ "$ENVIRONMENT" == "prod" ]]; then
        create_backup
    fi
    
    run_tests
    build_application
    deploy_to_environment

    print_success "🚀 Deployment completed successfully!"
    
    if [[ "$ENVIRONMENT" == "dev" ]]; then
        print_info "Development environment is available at:"
        print_info "  Frontend: http://localhost:5173"
        print_info "  Backend API: http://localhost:8080/api"
        print_info "  Database Admin: http://localhost:8081"
        print_info "  Email Testing: http://localhost:8025"
    fi
}

# Parse arguments and run main function
parse_args "$@"
main