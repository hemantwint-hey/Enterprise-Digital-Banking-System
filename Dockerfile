# =========================================================================
# Multi-Stage Dockerfile for Enterprise Digital Banking System (Day 66)
# =========================================================================

# -------------------------------------------------------------------------
# Stage 1: Build the Application
# -------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# 1. Install dos2unix to ensure Maven wrapper line endings work on Linux containers
RUN apk add --no-cache dos2unix

# 2. Copy Maven wrapper and POM first to leverage Docker layer caching for dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN dos2unix mvnw && chmod +x mvnw

# 3. Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B || true

# 4. Copy source code and build the production executable jar
COPY src/ ./src/
RUN ./mvnw clean package -DskipTests -B

# -------------------------------------------------------------------------
# Stage 2: Minimal, Secure Runtime Image
# -------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# 1. Create a dedicated non-root service user for banking security compliance
RUN addgroup -S banking && adduser -S bankingUser -G banking

# 2. Copy the compiled JAR artifact from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# 3. Change file ownership to non-root user
RUN chown -R bankingUser:banking /app

# 4. Switch to non-root user
USER bankingUser

# 5. Expose default Spring Boot HTTP port
EXPOSE 8080

# 6. Container healthcheck using Spring Boot Actuator Liveness Probe (Day 63)
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health/liveness || exit 1

# 7. JVM Production Flags:
# - UseContainerSupport: respects Docker memory & CPU limits
# - MaxRAMPercentage=75.0: caps heap at 75% of container RAM
# - java.security.egd: non-blocking entropy source for fast startup
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", \
            "app.jar"]

