FROM openjdk:21-jdk-slim

LABEL maintainer="Yatrik Fleet Platform"
LABEL description="Identity Service for Yatrik Fleet Platform"
LABEL version="1.0.0"

# Set working directory
WORKDIR /app

# Install required system dependencies
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    netcat \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Set Java security properties for better TLS/SSL support
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Djava.net.preferIPv4Stack=true -Dspring.profiles.active=docker"

# Copy Maven dependencies (build stage optimization)
COPY identity-service/target/dependencies/* ./

# Copy application source code
COPY identity-service/src ./src

# Copy application configuration
COPY identity-service/src/main/resources/application.yml ./src/main/resources/

# Copy migration files
COPY identity-service/src/main/resources/db ./src/main/resources/db

# Create necessary directories
RUN mkdir -p logs

# Set file permissions
RUN chmod +x /app/identity-service.jar 2>/dev/null || true

# Create a non-root user for security
RUN useradd -m -u 1001 -s /bin/bash appuser && \
    chown -R appuser:appuser /app
USER appuser

# Health check script
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Expose application port
EXPOSE 8081

# Default command
CMD ["sh", "-c", "java $JAVA_OPTS -jar identity-service.jar --server.port=8081"]