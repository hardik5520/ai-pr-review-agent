# ── Stage 1: Build ────────────────────────────────────────────────────────────
# Use the official Maven image with Java 21 to compile and package the app.
# We call this stage "build" so we can reference it later.
FROM maven:3.9-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project descriptor first.
# Docker caches each layer — copying pom.xml separately means dependencies
# are only re-downloaded when pom.xml changes, not on every code change.
COPY pom.xml .

# Download all dependencies (cached layer if pom.xml hasn't changed)
RUN mvn dependency:go-offline -q

# Now copy the actual source code
COPY src ./src

# Build the JAR, skipping tests (tests need a running DB which we don't have here)
RUN mvn package -DskipTests -q

# ── Stage 2: Run ──────────────────────────────────────────────────────────────
# Use a smaller JRE-only image for the final container — no Maven, no source code.
# This keeps the image lean (JDK is ~600MB, JRE is ~200MB).
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy only the built JAR from the build stage — nothing else
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 so Docker knows the container listens on it
EXPOSE 8080

# Command to run when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
