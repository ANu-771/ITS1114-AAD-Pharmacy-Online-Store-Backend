# =============================================================
# Stage 1: Build Stage (Maven + OpenJDK 21)
# =============================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy project definition and source code
COPY pom.xml .
COPY src ./src

# Build the executable Spring Boot fat JAR (skip tests for cloud deployment)
RUN mvn clean package -DskipTests -B

# =============================================================
# Stage 2: Runtime Stage (Lightweight Temurin JRE 21 Alpine)
# =============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user and group for security compliance
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built application JAR from the builder stage
COPY --from=builder /build/target/pharmacy_backend-*.jar app.jar

# Ensure proper file ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Render injects the PORT environment variable (default fallback to 8080 for local)
ENV PORT=8080
EXPOSE 8080

# JVM memory management tailored for container limits (e.g. Render 512MB RAM tier)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Start the Spring Boot application binding to Render's dynamic $PORT
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
