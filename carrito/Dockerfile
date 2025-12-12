# Multi-stage build for LUNARi Carrito Service
# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy both pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build with dependencies download (retry on failure)
RUN mvn clean package -DskipTests -B || \
    (sleep 5 && mvn clean package -DskipTests -B)

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user for security
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser

# Copy JAR from build stage
COPY --from=build /app/target/lunari-cart-api-*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# JVM optimization for containerized environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
