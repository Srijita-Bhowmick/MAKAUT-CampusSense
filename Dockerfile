# ==============================================================================
# Multi-stage Dockerfile for MAKAUT CampusSense on Render / Cloud
# ==============================================================================

# Stage 1: Build Application with Maven & Temurin JDK 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy POM and Maven configuration first
COPY pom.xml ./
COPY .mvn ./.mvn
COPY mvnw ./

# Cache dependencies
RUN mvn dependency:go-offline -B || true

# Copy project source and static assets
COPY src ./src

# Package production executable JAR without running tests during build
RUN mvn clean package -DskipTests -B

# Stage 2: Lightweight Production JRE 21 Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root unprivileged service user
RUN addgroup -S campussense && adduser -S campussense -G campussense

# Copy compiled JAR from builder stage
COPY --from=builder /workspace/target/campussense-*.jar app.jar

# Render dynamic port binding and memory constraints optimized for Free Tier
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=dev
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -Djava.awt.headless=true"

# Assign permissions to service user
RUN chown -R campussense:campussense /app
USER campussense

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
