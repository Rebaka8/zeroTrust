# Multi-stage Dockerfile for Spring Boot 3 + Java 21 Backend
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy dependency definition and source code
COPY pom.xml .
COPY src ./src

# Build production JAR skipping unit tests
RUN mvn clean package -DskipTests -B

# Runtime Image: Lightweight Alpine JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy compiled JAR artifact from build stage
COPY --from=build /app/target/zerotrust-iot-backend-*.jar app.jar

# Render passes dynamic PORT (defaults to 8085 locally)
ENV PORT=8085
ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8085

# Optimized memory flags for Render 512MB RAM tier & dynamic port injection
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:InitialRAMPercentage=40.0", "-Xss512k", "-Dserver.port=${PORT}", "-jar", "app.jar"]
