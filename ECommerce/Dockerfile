# Use a lightweight JDK 17 image
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR file from the target directory
# (Note: Jenkins 'mvnw' build creates this file)
COPY target/*.jar app.jar

# Expose the backend port
EXPOSE 8081

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
