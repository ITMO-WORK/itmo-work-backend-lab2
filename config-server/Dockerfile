FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 9991
ENTRYPOINT ["java", "-jar", "app.jar"]