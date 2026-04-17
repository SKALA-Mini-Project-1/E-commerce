FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src src

RUN chmod +x mvnw && ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

ENV JAVA_OPTS="-XX:InitialRAMPercentage=25.0 -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

RUN groupadd --system spring \
    && useradd --system --gid spring --uid 10001 spring

COPY --from=builder /workspace/target/*.jar /app/app.jar

RUN chown 10001:10001 /app/app.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
