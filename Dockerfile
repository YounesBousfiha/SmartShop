# ===== Stage 1 — Build JAR =====
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests


# ===== Stage 2 — Create custom JRE =====
FROM eclipse-temurin:21-jdk-alpine AS jre-builder

RUN jlink \
    --add-modules java.base,java.logging,java.naming,java.sql,java.management,java.security.sasl,java.xml,java.instrument \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --compress=2 \
    --output /custom-jre


# ===== Stage 3 — Runtime =====
FROM alpine:3.20

WORKDIR /app

# Copy custom runtime (ONLY THIS)
COPY --from=jre-builder /custom-jre /opt/jre

# Copy jar
COPY --from=build /app/target/*.jar app.jar

ENV PATH="/opt/jre/bin:${PATH}"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
