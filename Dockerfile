FROM openjdk:21-slim
RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*
EXPOSE 8080
COPY target/MotivationLetterBot-0.0.1-SNAPSHOT.jar app.jar
COPY wait-for-schema.sh /wait-for-schema.sh
RUN chmod +x /wait-for-schema.sh
ENV POSRGRES_SCHEMA "motivation_letter_bot_schema"
ENTRYPOINT ["/wait-for-schema.sh"]
