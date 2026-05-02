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

# Télécharger l'agent OpenTelemetry
RUN wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar -O opentelemetry-javaagent.jar

COPY --from=build /app/target/security-0.0.1-SNAPSHOT.jar app.jar

# 🔥 IMPORTANT - Exclure ByteBuddy de l'override
ENV JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar -Dotel.javaagent.extensions=/app/extensions"
ENV OTEL_INSTRUMENTATION_COMMON_EXPERIMENTAL_CONTROLLED_API_CLASSES_LOADING_ENABLED="true"

# SigNoz configuration
ENV OTEL_RESOURCE_ATTRIBUTES="service.name=leoni-backend"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="https://ingest.us2.signoz.cloud:443"
ENV OTEL_EXPORTER_OTLP_HEADERS="signoz-ingestion-key=${SIGNOZ_INGESTION_KEY}"
ENV OTEL_METRICS_EXPORTER="otlp"
ENV OTEL_LOGS_EXPORTER="otlp"
ENV OTEL_TRACES_EXPORTER="otlp"

EXPOSE 8081

ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=UTC"

# Lancer SANS l'agent en argument (utiliser JAVA_TOOL_OPTIONS à la place)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]