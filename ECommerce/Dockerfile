FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app


COPY target/*.jar app.jar

# Expose the backend port
EXPOSE 8081

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
