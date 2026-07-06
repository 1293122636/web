#!/bin/bash
# Library Full-Stack — Backend Startup Script
# Usage: ./start.sh [dev|prod]

set -e

PROFILE="${1:-dev}"

# Build
echo "==> Building (profile: $PROFILE)..."
./mvnw clean package -DskipTests -q

# Run (JAR, not mvn spring-boot:run — avoids OOM)
echo "==> Starting server on :8080..."
exec java -jar target/library-fullstack-*.jar \
  --spring.profiles.active="$PROFILE"
