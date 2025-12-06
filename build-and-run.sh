#!/bin/bash

echo "=== Building all JAR files ==="
for service in eureka-server config-server application-service company-service user-service vacancy_service itmo-work-gateway; do
    echo "Building $service..."
    if [ -d "$service" ]; then
        cd "$service"
        if [ -f "gradlew" ]; then
            ./gradlew clean bootJar --no-daemon -x test
        elif [ -f "mvnw" ]; then
            ./mvnw clean package -DskipTests
        fi
        cd ..
    else
        echo "Directory $service not found!"
    fi
done

echo "=== Building Docker images ==="

docker-compose build

echo "=== Starting services ==="

docker-compose up -d

echo "=== Done! ==="
