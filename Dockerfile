FROM openjdk:17-jdk-slim

WORKDIR /app

COPY /target/api-gateway-0.0.1-SNAPSHOT.jar /app/api-gateway.jar

EXPOSE 9191

CMD ["java", "-jar", "api-gateway.jar"]
