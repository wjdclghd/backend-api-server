FROM eclipse-temurin:25-jre

ARG JAR_FILE=build/libs/backend-api-server-0.0.1-SNAPSHOT.jar

WORKDIR /app
COPY ${JAR_FILE} /app/backend-api-server.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/backend-api-server.jar"]
