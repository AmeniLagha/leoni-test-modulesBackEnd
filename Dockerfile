# Étape 1: Build avec Maven
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

ENV MAVEN_OPTS="-Dfile.encoding=UTF-8"

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
COPY otel-agent.properties ./otel-agent.properties
RUN rm -f src/main/resources/application-dev.properties

RUN mvn clean package -DskipTests -Pdocker -Dfile.encoding=UTF-8

# Étape 2: Exécution avec JRE
FROM eclipse-temurin:17-jre

WORKDIR /app

RUN mkdir -p /app/uploads

# Copier l'agent OpenTelemetry
COPY --from=build /app/otel-agent.properties ./otel-agent.properties

# Télécharger l'agent OpenTelemetry
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.31.0/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

COPY --from=build /app/target/security-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=UTC -javaagent:/app/opentelemetry-javaagent.jar -Dotel.config=/app/otel-agent.properties"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]