# Build stage: use Maven+JDK image to build the app
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app
COPY . /app

# Build the jar (skip tests to speed up deploy)
RUN ./mvnw clean install -DskipTests

# Run stage: lightweight Java runtime
FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY --from=build /app/target/Personal_Portfolio-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 for Render to connect to
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
