# Multi-stage Dockerfile for Spring Boot 3 + Java 21 Backend
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/zerotrust-iot-backend-*.jar app.jar

ENV SERVER_PORT=8085
ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
