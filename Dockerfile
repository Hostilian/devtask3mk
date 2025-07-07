# Root Dockerfile - delegates to infrastructure/Dockerfile
# This provides compatibility for CI systems expecting a root-level Dockerfile

# Multi-stage build for smaller final image
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Install sbt
RUN apt-get update && \
    apt-get install -y curl gpg && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | gpg --no-default-keyring --keyring gnupg-ring:/etc/apt/trusted.gpg.d/scalasbt-release.gpg --import && \
    chmod 644 /etc/apt/trusted.gpg.d/scalasbt-release.gpg && \
    apt-get update && \
    apt-get install -y sbt && \
    rm -rf /var/lib/apt/lists/*

# Copy build files first for better caching
COPY build.sbt .
COPY project/ project/

# Download dependencies (cached if build.sbt hasn't changed)
RUN sbt update

# Copy source and build
COPY src/ src/
RUN sbt clean compile package

# Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

# Install curl for health checks
RUN apk add --no-cache curl

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Create directories with proper permissions
RUN mkdir -p /app/logs /app/output && \
    chown -R appuser:appgroup /app

# Copy only the JAR file we need
COPY --from=builder /app/target/scala-3.4.3/devtask3mk_3-1.0.0.jar app.jar

# Entrypoint script for mode selection
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh && chown appuser:appgroup /entrypoint.sh

# Switch to non-root user
USER appuser

# Expose both ports for CLI and server
EXPOSE 8080 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/health || exit 1

ENTRYPOINT ["/entrypoint.sh"]

# Add some metadata
LABEL description="Document Matrix - Functional Programming Showcase"
LABEL version="1.0.0"
LABEL maintainer="hostilian"
LABEL org.opencontainers.image.source="https://github.com/hostilian/devtask3mk"
LABEL org.opencontainers.image.description="Document Matrix application with CLI and Server modes"
LABEL org.opencontainers.image.licenses="MIT"
