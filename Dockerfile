# Multi-stage Dockerfile for Document Matrix Application
# Supports both Scala backend (primary) and Node.js frontend
# Designed for multi-platform builds (linux/amd64, linux/arm64)

# Stage 1: Node.js build stage for frontend (if needed)
FROM --platform=$BUILDPLATFORM node:18-alpine AS frontend-builder

WORKDIR /app/frontend

# Copy frontend package files and install dependencies
COPY package*.json ./
COPY frontend/ ./frontend/

# Build frontend if package.json exists and has build scripts
RUN if [ -f package.json ]; then \
        npm ci --only=production && \
        npm run build || echo "Frontend build skipped - no build script"; \
    else \
        echo "No frontend package.json found, skipping frontend build"; \
    fi

# Stage 2: Scala/SBT build stage for backend (primary application)
FROM eclipse-temurin:21-jdk AS scala-builder

WORKDIR /app

# Install sbt using the approach that works in the infrastructure/Dockerfile
RUN apt-get update && \
    apt-get install -y curl ca-certificates && \
    apt-get install -y gnupg && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 && \
    apt-get update && \
    apt-get install -y sbt && \
    rm -rf /var/lib/apt/lists/*

# Copy SBT build configuration first for better Docker layer caching
# Dependencies will only re-download if these files change
COPY build.sbt .
COPY project/ project/

# Download and cache dependencies (most time-consuming step)
RUN sbt update

# Copy all source code for compilation
# The build.sbt configures custom source directories:
# - Compile / scalaSource := baseDirectory.value / "backend" / "src" / "main" / "scala"
# - Also includes: backend/core, backend/apps/transport-api, src/ (root)
COPY src/ src/
COPY backend/ backend/
COPY core/ core/
COPY apps/ apps/

# Compile and package the application
# This creates target/scala-3.4.3/devtask3mk-assembly-1.0.0.jar or similar
RUN sbt clean compile package

# Stage 3: Production runtime image
FROM eclipse-temurin:21-jre-alpine AS runtime

# Install utilities for health checks and troubleshooting
RUN apk add --no-cache curl

WORKDIR /app

# Create non-root user for security best practices
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Create application directories with proper permissions
RUN mkdir -p /app/logs /app/output /app/data && \
    chown -R appuser:appgroup /app

# Copy the compiled JAR from the Scala builder stage
# The exact JAR name comes from build.sbt: name := "devtask3mk"
COPY --from=scala-builder /app/target/scala-3.4.3/devtask3mk_3-1.0.0.jar app.jar

# Copy frontend build artifacts if they exist
COPY --from=frontend-builder /app/frontend/build/ ./static/ 2>/dev/null || echo "No frontend build to copy"

# Copy the entrypoint script for application mode selection
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh && chown appuser:appgroup /entrypoint.sh

# Switch to non-root user before running application
USER appuser

# Expose ports for both CLI monitoring and server mode
# 8080: Primary application server port
# 8081: Health check and management port
EXPOSE 8080 8081

# Health check endpoint - attempts server mode health check, falls back gracefully
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/health || curl -f http://localhost:8080/health || exit 1

# Set default environment variables
ENV MODE=cli
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Use entrypoint script to handle both server and CLI modes
ENTRYPOINT ["/entrypoint.sh"]

# Container metadata for documentation and registry
LABEL description="Document Matrix - Multi-language development platform with Scala backend and modern web frontend"
LABEL version="1.0.0"
LABEL maintainer="hostilian"
LABEL org.opencontainers.image.source="https://github.com/hostilian/devtask3mk"
LABEL org.opencontainers.image.description="Document Matrix application supporting both CLI and server modes with functional programming showcase"
LABEL org.opencontainers.image.licenses="MIT"
LABEL org.opencontainers.image.documentation="https://github.com/hostilian/devtask3mk/blob/main/README.md"

# Build-time arguments for multi-platform support
ARG BUILDPLATFORM
ARG TARGETPLATFORM
LABEL org.opencontainers.image.platform=$TARGETPLATFORM