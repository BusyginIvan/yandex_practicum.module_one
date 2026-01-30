# ===== build stage =====
FROM gradle:8.6-jdk21 AS build
WORKDIR /app
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src
RUN ./gradlew clean bootJar -x test

# ===== runtime stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
RUN mkdir -p /var/blog/images
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
