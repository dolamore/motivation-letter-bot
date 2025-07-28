FROM openjdk:21-slim
EXPOSE 8080
COPY target/MotivationLetterBot-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-XX:MaxRAM=100M", "-jar", "/app.jar"]