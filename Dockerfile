
FROM maven:3.9.5-eclipse-temurin-21 AS builder


WORKDIR /app


COPY pom.xml .
COPY src ./src

RUN mvn clean package -Dmaven.test.skip=true


FROM openjdk:24


WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8443

ENTRYPOINT ["java", "-jar", "app.jar"]
