FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/traveler-core-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8084

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]