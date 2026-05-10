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

# Récupérer le token depuis les build-args
ARG SIGNOZ_ACCESS_TOKEN
ARG SIGNOZ_ENDPOINT

# Créer le fichier de configuration OTEL avec la bonne clé
RUN echo "otel.service.name=leoni-backend" > /app/otel-agent.properties && \
    echo "otel.exporter.otlp.endpoint=https://ingest.us2.signoz.cloud:443" >> /app/otel-agent.properties && \
    echo "otel.exporter.otlp.headers=signoz-access-token=${SIGNOZ_ACCESS_TOKEN}" >> /app/otel-agent.properties && \
    echo "otel.exporter.otlp.protocol=http/protobuf" >> /app/otel-agent.properties && \
    echo "otel.traces.exporter=otlp" >> /app/otel-agent.properties && \
    echo "otel.metrics.exporter=otlp" >> /app/otel-agent.properties && \
    echo "otel.logs.exporter=otlp" >> /app/otel-agent.properties

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.31.0/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
COPY --from=build /app/target/security-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=UTC -javaagent:/app/opentelemetry-javaagent.jar -Dotel.javaagent.configuration-file=/app/otel-agent.properties"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]