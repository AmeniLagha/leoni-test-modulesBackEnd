# Étape 1: Build avec Maven
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

ENV MAVEN_OPTS="-Dfile.encoding=UTF-8"

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN rm -f src/main/resources/application-dev.properties

RUN mvn clean package -DskipTests -Pdocker -Dfile.encoding=UTF-8

# Étape 2: Exécution avec JRE
FROM eclipse-temurin:17-jre

WORKDIR /app

RUN mkdir -p /app/uploads

# Télécharger l'agent OpenTelemetry pour SigNoz
RUN wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar -O opentelemetry-javaagent.jar

COPY --from=build /app/target/security-0.0.1-SNAPSHOT.jar app.jar

# SigNoz configuration
ENV OTEL_RESOURCE_ATTRIBUTES="service.name=leoni-backend"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="https://ingest.us2.signoz.cloud:443"
# L'ingestion key sera injectée via GitHub Secret
ENV OTEL_EXPORTER_OTLP_HEADERS="signoz-ingestion-key=${SIGNOZ_INGESTION_KEY}"
ENV OTEL_METRICS_EXPORTER="none"
ENV OTEL_LOGS_EXPORTER="none"

# 🔥 Exposer le bon port (8081)
EXPOSE 8081

ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=UTC"

# Lancer avec l'agent OpenTelemetry
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -javaagent:/app/opentelemetry-javaagent.jar -jar app.jar"]