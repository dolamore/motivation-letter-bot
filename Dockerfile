FROM openjdk:21
EXPOSE 8080
COPY target/MotivationLetterBot-0.0.1-SNAPSHOT.jar app.jar
ENV POSRGRES_SCHEMA "motivation_letter_bot_schema"
ENTRYPOINT ["java","-XX:MaxRAM=100M", "-jar", "/app.jar"]