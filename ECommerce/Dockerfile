# --- Stage 1: Build Java ---
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# --- Stage 2: Run Java ---
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Copy the JAR from the build stage above
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
