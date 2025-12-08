#!/bin/bash
set -e

SERVICES=(
  eureka-server
  config-server
  company-service
  user-service
  vacancy_service
  itmo-work-gateway
  application-service
)

echo "=== Building all JAR files ==="
for service in "${SERVICES[@]}"; do
    echo "Building $service..."
    if [ -d "$service" ]; then
        cd "$service"
        if [ -f "gradlew" ]; then
            ./gradlew clean bootJar -x test
        elif [ -f "mvnw" ]; then
            ./mvnw clean package -DskipTests
        fi
        cd ..
    else
        echo "Directory $service not found!"
    fi
done
echo "=== Done ==="