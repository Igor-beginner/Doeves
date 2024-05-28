FROM openjdk:latest

WORKDIR /app

COPY target/doeves-0.0.1-SNAPSHOT.jar backend.jar

EXPOSE 8080

ENV DB_HOST=localhost
ENV DB_NAME=postgres
ENV DB_PASSWORD=postgres
ENV SPRING_PROFILES_ACTIVE=dev

ENTRYPOINT ["java", "-jar", "backend.jar"]