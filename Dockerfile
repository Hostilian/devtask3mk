# Multi-stage build for smaller final image
FROM sbtscala/scala-sbt:openjdk-21_1.10.3_3.4.3 AS builder

WORKDIR /app

# Copy build files first for better caching
COPY build.sbt .
COPY project/ project/

# Download dependencies (cached if build.sbt hasn't changed)
RUN sbt update

# Copy source and build
COPY src/ src/
RUN sbt package

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the JAR file we need
COPY --from=builder /app/target/scala-3.4.3/document-matrix_3-1.0.0.jar app.jar

# Default to CLI, but allow override  
ENTRYPOINT ["java", "-cp", "app.jar", "com.example.Cli"]

# Expose port for server mode
EXPOSE 8080

# Add some metadata
LABEL description="Document Matrix - Functional Programming Showcase"
LABEL version="1.0.0"
