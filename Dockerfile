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

RUN mkdir -p /app/uploads && chmod 777 /app/uploads

# ✅ Créer un volume pour les uploads
VOLUME /app/uploads

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.31.0/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
COPY --from=build /app/target/security-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
# Ajouter après EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1
  
ENV SPRING_PROFILES_ACTIVE=docker
# ✅ Le token vient des variables d'environnement Clever Cloud au runtime
ENTRYPOINT ["sh", "-c", "java -Dfile.encoding=UTF-8 -Duser.timezone=UTC -javaagent:/app/opentelemetry-javaagent.jar -Dotel.service.name=leoni-backendv2 -Dotel.exporter.otlp.endpoint=https://ingest.us2.signoz.cloud:443 -Dotel.exporter.otlp.headers=signoz-access-token=${SIGNOZ_ACCESS_TOKEN} -Dotel.exporter.otlp.protocol=http/protobuf -Dotel.traces.exporter=otlp -Dotel.metrics.exporter=otlp -Dotel.logs.exporter=otlp -jar app.jar"]
