#!/bin/bash

# Document Matrix Development Helper Script
# This script provides convenient commands for local development

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
log() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

# Function to check prerequisites
check_prerequisites() {
    header "Checking Prerequisites"

    # Check Docker
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed or not in PATH"
        exit 1
    fi
    log "✓ Docker found: $(docker --version)"

    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        warn "docker-compose not found, trying 'docker compose'"
        if ! docker compose version &> /dev/null; then
            error "Docker Compose is not available"
            exit 1
        fi
        DOCKER_COMPOSE="docker compose"
    else
        DOCKER_COMPOSE="docker-compose"
    fi
    log "✓ Docker Compose found"

    # Check Java
    if ! command -v java &> /dev/null; then
        warn "Java not found in PATH"
    else
        log "✓ Java found: $(java -version 2>&1 | head -n 1)"
    fi

    # Check SBT
    if ! command -v sbt &> /dev/null; then
        warn "SBT not found in PATH"
    else
        log "✓ SBT found"
    fi

    # Check Make
    if ! command -v make &> /dev/null; then
        warn "Make not found - Makefile commands not available"
    else
        log "✓ Make found"
    fi
}

# Function to clean up development environment
cleanup() {
    header "Cleaning Up Development Environment"

    log "Stopping Docker containers..."
    $DOCKER_COMPOSE down --remove-orphans || true

    log "Removing unused Docker resources..."
    docker system prune -f || true

    log "Cleaning SBT build artifacts..."
    if command -v sbt &> /dev/null; then
        sbt clean || warn "Failed to clean SBT artifacts"
    fi

    log "✓ Cleanup completed"
}

# Function to setup development environment
setup() {
    header "Setting Up Development Environment"

    log "Building Docker images..."
    docker build -t document-matrix .

    log "Starting development services..."
    $DOCKER_COMPOSE --profile dev up -d

    log "✓ Development environment ready"
    log "Run './dev.sh status' to check service status"
}

# Function to run tests
test() {
    header "Running Tests"

    if command -v sbt &> /dev/null; then
        log "Running SBT tests..."
        sbt test
    else
        log "Running tests in Docker..."
        docker run --rm -v "$(pwd)":/app -w /app sbtscala/scala-sbt:1.11.0-scala3.4.3-openjdk-21 sbt test
    fi

    log "✓ Tests completed"
}

# Function to format code
format() {
    header "Formatting Code"

    if command -v sbt &> /dev/null; then
        log "Running scalafmt..."
        sbt scalafmtAll
    else
        log "Running scalafmt in Docker..."
        docker run --rm -v "$(pwd)":/app -w /app sbtscala/scala-sbt:1.11.0-scala3.4.3-openjdk-21 sbt scalafmtAll
    fi

    log "✓ Code formatted"
}

# Function to show service status
status() {
    header "Service Status"

    log "Docker containers:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

    echo ""
    log "Available services:"
    echo "  • Server: http://localhost:8081"
    echo "  • Grafana: http://localhost:3000 (admin/admin)"
    echo "  • Prometheus: http://localhost:9090"
}

# Function to show logs
logs() {
    local service="${1:-}"

    if [ -n "$service" ]; then
        header "Logs for $service"
        $DOCKER_COMPOSE logs -f "$service"
    else
        header "All Service Logs"
        $DOCKER_COMPOSE logs -f
    fi
}

# Function to run CI simulation
ci() {
    header "Simulating CI Pipeline"

    log "Step 1: Format check..."
    format

    log "Step 2: Compile..."
    if command -v sbt &> /dev/null; then
        sbt compile
    else
        docker run --rm -v "$(pwd)":/app -w /app sbtscala/scala-sbt:1.11.0-scala3.4.3-openjdk-21 sbt compile
    fi

    log "Step 3: Run tests..."
    test

    log "Step 4: Build Docker image..."
    docker build -t document-matrix-ci .

    log "✓ CI simulation completed successfully"
}

# Function to show help
show_help() {
    echo "Document Matrix Development Helper"
    echo ""
    echo "Usage: $0 COMMAND [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  check      Check prerequisites"
    echo "  setup      Setup development environment"
    echo "  cleanup    Clean up development environment"
    echo "  test       Run tests"
    echo "  format     Format code"
    echo "  status     Show service status"
    echo "  logs       Show logs (optionally for specific service)"
    echo "  ci         Simulate CI pipeline"
    echo "  help       Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 setup              # Setup development environment"
    echo "  $0 logs server         # Show server logs"
    echo "  $0 ci                  # Run CI simulation"
    echo ""
    echo "For more information, see PIPELINE_DOCUMENTATION.md"
}

# Main script logic
case "${1:-help}" in
    check)
        check_prerequisites
        ;;
    setup)
        check_prerequisites
        setup
        ;;
    cleanup)
        cleanup
        ;;
    test)
        test
        ;;
    format)
        format
        ;;
    status)
        status
        ;;
    logs)
        logs "$2"
        ;;
    ci)
        check_prerequisites
        ci
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
