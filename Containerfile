# syntax=docker/dockerfile:1

# ---- Builder stage ----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /project

# Resolve dependencies first for better layer caching.
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# Build and run the full test suite inside the image.
COPY src ./src
RUN mvn -B -q clean package -DskipTests=false

# ---- Runtime stage ----------------------------------------------------------
FROM ghcr.io/kelleyblackmore/rhel9-hardened-base:latest

# OCI image metadata
LABEL org.opencontainers.image.title="SecureLedger" \
      org.opencontainers.image.description="Secure task & audit REST API on a hardened RHEL9 base" \
      org.opencontainers.image.source="https://github.com/kelleyblackmore/rhel9-app-full" \
      org.opencontainers.image.licenses="Apache-2.0" \
      org.opencontainers.image.vendor="kelleyblackmore"

# Install a JRE as root, then drop privileges again.
USER 0
RUN dnf -y update \
    && dnf -y install --setopt=install_weak_deps=0 java-21-openjdk-headless \
    && dnf -y clean all \
    && rm -rf /var/cache/dnf

# Application directory owned by the non-root runtime user (uid 10001, group 0).
RUN mkdir -p /app/data \
    && chown -R 10001:0 /app \
    && chmod -R g=u /app

COPY --from=build /project/target/app.jar /app/app.jar

ENV DB_PATH=/app/data/secureledger.db \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080
WORKDIR /app
USER 10001

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
