FROM openjdk:latest

WORKDIR /app

COPY target/doeves.jar backend.jar

EXPOSE 8080

ENV DB_HOST=localhost
ENV DB_NAME=postgres
ENV DB_PASSWORD=postgres
ENV SPRING_PROFILES_ACTIVE=dev
ENV jwt.secret.key=gavno_123456789_zalupa_123456789_penis_123456789_her_123456789_davalka_123456789

ENTRYPOINT ["java", "-jar", "backend.jar"]