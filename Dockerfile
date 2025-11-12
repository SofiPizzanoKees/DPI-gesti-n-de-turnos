# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -Pprod

# Stage 2: Run  
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

EXPOSE 8080

# Usar variables de entorno para el perfil
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]