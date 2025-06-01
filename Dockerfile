# ---------- Stage 1: Build ----------
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Copy Gradle files separately to leverage Docker cache
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Pre-fetch dependencies
RUN gradle build --no-daemon || return 0

# Copy full source code
COPY . .

# Build the application
RUN gradle bootJar --no-daemon

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy built fat JAR from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]