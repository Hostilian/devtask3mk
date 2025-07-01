# Document Matrix Makefile
.PHONY: help build test clean format docker-build docker-run docker-stop dev up down logs

# Default target
help: ## Show this help message
	@echo "Document Matrix - Available commands:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# Development commands
clean: ## Clean build artifacts
	sbt clean

format: ## Format code using scalafmt
	sbt scalafmtAll

format-check: ## Check code formatting
	sbt scalafmtCheckAll

compile: ## Compile the project
	sbt compile

test: ## Run tests
	sbt test

test-coverage: ## Run tests with coverage
	sbt coverage test coverageReport

build: ## Build the project
	sbt package

run-cli: ## Run CLI application
	sbt "runMain com.example.Cli"

run-server: ## Run server application
	sbt "runMain com.example.Server"

# Docker commands
docker-build: ## Build Docker image
	docker build -t document-matrix .

docker-run-server: ## Run Docker container in server mode
	docker run -d --name document-matrix-server -p 8081:8081 -e MODE=server document-matrix

docker-run-cli: ## Run Docker container in CLI mode
	docker run -it --name document-matrix-cli -e MODE=cli document-matrix

docker-stop: ## Stop and remove Docker containers
	docker stop document-matrix-server document-matrix-cli || true
	docker rm document-matrix-server document-matrix-cli || true

# Docker Compose commands
up: ## Start all services with docker-compose
	docker-compose up -d

up-dev: ## Start development environment
	docker-compose --profile dev up -d

up-monitoring: ## Start with monitoring stack
	docker-compose --profile monitoring up -d

up-cli: ## Start CLI service
	docker-compose --profile cli up

down: ## Stop all docker-compose services
	docker-compose down

logs: ## Show logs from all services
	docker-compose logs -f

logs-server: ## Show server logs
	docker-compose logs -f document-matrix-server

logs-cli: ## Show CLI logs
	docker-compose logs -f document-matrix-cli

# Development workflow
dev: ## Start development environment
	docker-compose --profile dev up -d sbt-dev

# CI/CD simulation
ci-format: ## Run format check (CI simulation)
	sbt scalafmtCheckAll

ci-test: ## Run all tests (CI simulation)
	sbt clean compile test

ci-build: ## Full CI build simulation
	make ci-format && make ci-test && make docker-build

# Performance testing
benchmark: ## Run JMH benchmarks
	sbt "test:runMain org.openjdk.jmh.Main"

# Security scanning
security-check: ## Run dependency security check
	sbt dependencyCheck

# Release workflow
release-dry: ## Dry run release
	@echo "Would tag version: $$(git describe --tags --abbrev=0 2>/dev/null || echo 'v0.1.0')"

# Utility commands
check-docker: ## Check Docker daemon status
	@docker version >/dev/null 2>&1 && echo "✓ Docker is running" || echo "✗ Docker is not running"

check-sbt: ## Check SBT installation
	@sbt about >/dev/null 2>&1 && echo "✓ SBT is available" || echo "✗ SBT is not available"

check-java: ## Check Java installation
	@java -version >/dev/null 2>&1 && echo "✓ Java is available" || echo "✗ Java is not available"

check-deps: check-java check-sbt check-docker ## Check all dependencies

# Quick start
quickstart: check-deps build docker-build up ## Quick start: build and run everything
	@echo ""
	@echo "🚀 Document Matrix is now running!"
	@echo "📊 Server: http://localhost:8081"
	@echo "🐳 Grafana: http://localhost:3000 (admin/admin)"
	@echo "📈 Prometheus: http://localhost:9090"
	@echo ""
	@echo "To run CLI: make up-cli"
	@echo "To see logs: make logs"
	@echo "To stop: make down"
