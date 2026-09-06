# ===================================================================
# Multi-stage Dockerfile for Materia Spring Boot Backend (Java 21)
# ===================================================================

# -------------------------------------------------------------------
# Stage 1: Build Application
# -------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests

# -------------------------------------------------------------------
# Stage 2: Minimal Production JRE Runtime
# -------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy executable jar from builder stage
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
