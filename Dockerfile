FROM maven:3.9.6-eclipse-temurin-21
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package

EXPOSE 8080