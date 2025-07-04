# 🚀 Document Matrix CI/CD Pipeline

Welcome to the **Document Matrix** CI/CD pipeline! This pipeline ensures your code is always formatted, tested, secure, and ready for production or development. Below you'll find a clear, modern overview of all pipeline stages, Docker features, and developer tips.

---

## 🛠️ Pipeline Stages

| Stage                | Description                                      | Key Tools/Features                |
|----------------------|--------------------------------------------------|-----------------------------------|
| 🧹 Format & Lint     | Auto-formats and lints all Scala code            | `scalafmt`, SBT                   |
| 🧪 Build & Test      | Compiles and runs all tests, generates reports   | SBT, ScalaTest, ZIO Test          |
| 🔒 Security Scan     | Checks dependencies for vulnerabilities          | `sbt-dependency-check`            |
| 🐳 Docker Build      | Builds and pushes multi-arch Docker images       | Docker Buildx, GitHub Container   |
| 🚦 Performance Test  | Runs benchmarks and performance checks           | JMH, custom scripts               |
| 🚀 Deploy            | Deploys to staging/production (auto/manual)      | Docker Compose, GitHub Actions    |

---

## 🐳 Docker Features

- **Multi-Stage Build**: Small, secure images using builder/runtime split
- **Health Checks**: Built-in HTTP health endpoints
- **Non-root User**: Runs as a secure, non-root user
- **Alpine Runtime**: Minimal, fast, and secure
- **Monitoring**: Prometheus & Grafana ready
- **Modes**: `MODE=server` (HTTP API), `MODE=cli` (interactive CLI)

**Example Docker Compose Services:**
```yaml
services:
  document-matrix-server:
    image: ghcr.io/hostilian/devtask3mk:latest
    ports: ["8081:8081"]
    environment:
      - MODE=server
  document-matrix-cli:
    image: ghcr.io/hostilian/devtask3mk:latest
    environment:
      - MODE=cli
```

---

## ⚡ Caching & Speed

- **SBT Dependency Caching**: Faster builds
- **Docker Layer Caching**: Efficient image rebuilds
- **Coursier Cache**: Optimized Scala dependency resolution

---

## 🔍 Security & Quality

- **Dependency Scanning**: Automated CVE checks
- **Code Formatting**: Auto-fix and commit on PRs
- **Test Coverage**: Reports generated for every build

---

## 📈 Monitoring & Observability

- **Prometheus**: Metrics endpoint for all services
- **Grafana**: Pre-configured dashboards
- **Health Endpoints**: `/health` for liveness/readiness

---

## 📝 Developer Quick Reference

- **Format code:** `make format`
- **Run tests:** `make test`
- **Build Docker:** `make docker-build`
- **Start all services:** `make up`
- **View logs:** `make logs`
- **Check security:** `make security-check`

---

## 📦 Release & Deployment

- **Semantic Versioning**: Tags trigger releases
- **Auto Docker Publish**: On every main branch/tag push
- **Staging/Production Deploys**: Automated via GitHub Actions

---

> 💡 **Tip:** All pipeline steps are visible in GitHub Actions. For troubleshooting, check the logs for each stage.

---

For more details, see the [Makefile](Makefile), [Dockerfile](Dockerfile), and [docker-compose.yml](docker-compose.yml).

```dockerfile
# Builder stage for compilation
FROM sbtscala/scala-sbt:1.8.2-openjdk-21 AS builder
# Runtime stage for minimal final image
FROM eclipse-temurin:21-jre-alpine AS runtime
```
