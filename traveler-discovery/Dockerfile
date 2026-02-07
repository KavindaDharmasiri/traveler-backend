FROM eclipse-temurin:17-jdk-alpine

RUN apk add --no-cache wget

WORKDIR /app

COPY target/*.jar app.jar
COPY startup.sh /app/startup.sh

RUN chmod +x /app/startup.sh

EXPOSE 8761

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["/app/startup.sh"]