# Document Matrix - CI/CD Pipeline Documentation

This document describes the comprehensive CI/CD pipeline and DevOps features available in the Document Matrix project.

## 🚀 Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 21+
- SBT (Scala Build Tool)
- Make (optional, for convenience commands)

### Running the Application

```bash
# Quick start everything
make quickstart

# Or manually:
docker-compose up -d

# CLI mode
docker-compose --profile cli up

# With monitoring
docker-compose --profile monitoring up -d
```

## 🔄 CI/CD Pipeline Features

### GitHub Actions Workflows

#### 1. **Main CI/CD Pipeline** (`.github/workflows/ci.yml`)

**Triggers:**
- Push to `main` and `develop` branches
- Pull requests to `main` and `develop`
- Git tags starting with `v*`

**Jobs:**

1. **Format and Lint**
   - Automatically formats code with scalafmt
   - Commits formatting changes back to the repo
   - Caches SBT dependencies for faster builds

2. **Build and Test**
   - Matrix build across Java versions
   - Runs comprehensive test suites
   - Generates test reports
   - Uploads build artifacts

3. **Security Scan**
   - Dependency vulnerability checking
   - Security analysis of dependencies

4. **Docker Build**
   - Multi-platform builds (amd64, arm64)
   - Pushes to GitHub Container Registry
   - Implements build caching for efficiency
   - Tags with branch names, SHAs, and semantic versions

5. **Performance Testing**
   - JMH benchmarks on main branch
   - Performance regression detection

6. **Environment Deployments**
   - Staging deployment on `develop` branch
   - Production deployment on `main` branch

#### 2. **Release Pipeline** (`.github/workflows/release.yml`)

**Triggers:**
- Git tags matching `v*` pattern

**Features:**
- Automatic GitHub releases
- Multi-platform Docker image publishing
- Changelog generation
- JAR artifact uploads
- Semantic versioning support

### Pipeline Improvements

#### Automatic Code Formatting
The pipeline now automatically formats code instead of just failing on formatting issues:
- Runs `scalafmt` on all Scala files
- Commits changes back to the repository
- Uses `[skip ci]` to prevent infinite loops

#### Enhanced Docker Support
- Multi-stage builds for smaller images
- Health checks and monitoring
- Security improvements (non-root user)
- Platform-specific optimizations

#### Caching Strategy
- SBT dependency caching
- Docker layer caching
- Coursier cache optimization

## 🐳 Docker Features

### Multi-Stage Dockerfile
```dockerfile
# Builder stage for compilation
FROM sbtscala/scala-sbt:openjdk-21_1.8.0_3.4.3 AS builder
# Runtime stage for minimal final image
FROM eclipse-temurin:21-jre-alpine AS runtime
```

### Security Features
- Non-root user execution
- Minimal Alpine-based runtime
- Health check endpoints
- Proper file permissions

### Container Modes
- **Server Mode**: `MODE=server` - Runs HTTP server on port 8081
- **CLI Mode**: `MODE=cli` - Interactive command-line interface

## 📊 Monitoring and Observability

### Prometheus Metrics
- Application performance metrics
- JVM metrics and garbage collection
- Custom business metrics

### Grafana Dashboards
- Real-time application monitoring
- Performance visualization
- Health status tracking

### Logging
- Structured logging with timestamps
- Separate log files for different modes
- Container log aggregation

## 🛠 Development Workflow

### Local Development
```bash
# Start development environment
make dev
# or
docker-compose --profile dev up -d

# Format code
make format

# Run tests
make test

# Build Docker image
make docker-build
```

### Available Make Commands
```bash
make help                 # Show all available commands
make quickstart          # Build and start everything
make ci-build            # Simulate CI build locally
make security-check      # Run dependency security scan
make benchmark           # Run performance benchmarks
```

### Docker Compose Profiles
- `default`: Server mode only
- `cli`: Command-line interface
- `dev`: Development environment with SBT
- `monitoring`: Includes Prometheus and Grafana

## 🔒 Security Features

### Dependency Scanning
- Automated vulnerability detection
- OWASP dependency check integration
- Security alerts in CI/CD pipeline

### Container Security
- Non-root user execution
- Minimal attack surface
- Regular base image updates
- Secret management best practices

### Access Control
- GitHub Container Registry integration
- Branch protection rules
- Environment-specific deployments

## 📈 Performance Optimization

### Build Performance
- Multi-layer Docker caching
- SBT dependency caching
- Parallel job execution
- Incremental compilation

### Runtime Performance
- JVM tuning parameters
- Memory optimization
- Health check configuration
- Resource limits

## 🚀 Deployment Strategies

### Environment Promotion
1. **Development**: Feature branches → automatic builds
2. **Staging**: `develop` branch → staging environment
3. **Production**: `main` branch → production environment

### Release Process
1. Create release branch from `develop`
2. Update version numbers
3. Create git tag: `git tag v1.0.0`
4. Push tag: `git push origin v1.0.0`
5. Automatic release creation and deployment

## 🔧 Configuration

### Environment Variables
```bash
# Application configuration
MODE=server|cli           # Application mode
JAVA_OPTS="-Xmx512m"     # JVM options

# Docker configuration
REGISTRY=ghcr.io         # Container registry
IMAGE_NAME=repo/app      # Image name
```

### Build Configuration
- `build.sbt`: Project dependencies and settings
- `.scalafmt.conf`: Code formatting rules
- `project/plugins.sbt`: SBT plugins
- `docker-compose.yml`: Service orchestration

## 📋 Troubleshooting

### Common Issues

1. **Formatting Failures**
   ```bash
   # Fix locally
   sbt scalafmtAll
   git add -A && git commit -m "Format code"
   ```

2. **Docker Build Issues**
   ```bash
   # Clear Docker cache
   docker system prune -f
   # Rebuild without cache
   docker build --no-cache -t document-matrix .
   ```

3. **Test Failures**
   ```bash
   # Run specific tests
   sbt "testOnly com.example.DocumentSpec"
   # Run with verbose output
   sbt "test -- -oD"
   ```

### Monitoring Health
- Application: `http://localhost:8081/health`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin/admin)

## 🎯 Future Enhancements

### Planned Features
- [ ] Integration testing with Testcontainers
- [ ] Automated dependency updates
- [ ] Performance regression testing
- [ ] Multi-environment configuration
- [ ] Backup and disaster recovery
- [ ] Advanced monitoring dashboards

### Contributing
1. Fork the repository
2. Create feature branch
3. Make changes with tests
4. Submit pull request
5. CI/CD will validate automatically

This pipeline provides a robust, scalable, and secure foundation for the Document Matrix project with modern DevOps practices.
