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

COPY --from=build /app/target/security-0.0.1-SNAPSHOT.jar app.jar

# 🔥 Exposer le bon port (8081)
EXPOSE 8081

ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=UTC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]