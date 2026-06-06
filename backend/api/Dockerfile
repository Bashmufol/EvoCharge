FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build
COPY backend/api/pom.xml ./pom.xml
COPY backend/api/src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
COPY data/seed /app/data/seed
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--evocharge.seed-path=/app/data/seed"]
