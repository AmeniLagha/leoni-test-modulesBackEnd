# 🧪 Leoni Test Modules — Backend

Application de gestion du cycle de vie des modules de test industriels (Leoni). API REST Spring Boot avec pipeline DevOps complet.

![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?logo=github-actions&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Hub-0db7ed?logo=docker&logoColor=white)
![Deploy](https://img.shields.io/badge/Deploy-Clever_Cloud-F5A623?logo=cloud&logoColor=white)
![Monitoring](https://img.shields.io/badge/Monitoring-SigNoz-blueviolet)
![Java](https://img.shields.io/badge/Java-17-red?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-6DB33F?logo=springboot&logoColor=white)

---

## 📋 Présentation

Ce dépôt contient le **backend** de l'application Leoni Test Modules, une solution web dédiée à la gestion et au suivi du cycle de vie des modules de test. Il expose une API REST sécurisée consommée par le [frontend Angular](#).

---

## 🏗️ Stack technique

| Catégorie | Technologie |
|---|---|
| Langage | Java 17 |
| Framework | Spring Boot 3 |
| Sécurité | Spring Security / JWT |
| Base de données | PostgreSQL |
| Build | Maven 3.9 |
| Conteneurisation | Docker (multi-stage build) |
| Registry | Docker Hub |
| Observabilité | OpenTelemetry Agent + SigNoz Cloud |
| Déploiement | Clever Cloud |

---

## 🔁 Pipeline CI/CD

Le pipeline GitHub Actions se déclenche sur chaque push ou PR vers `main`/`master` et s'exécute en 4 étapes séquentielles :

```
Build & Tests (JUnit + JaCoCo)
        ↓
Tests E2E (Selenium + Cucumber)
        ↓
Docker Build & Push (Docker Hub)
        ↓
Deploy (Clever Cloud) + Health Check
```

---

## 🧪 Stratégie de tests

| Type | Outil | Couverture | Rapport |
|---|---|---|---|
| Unitaires | JUnit 5 | Logique métier | JaCoCo (artifact CI) |
| Intégration | Spring Test | Couche API / DB | Surefire |
| E2E | Selenium + Cucumber | Login, Register, ChargeSheet | Cucumber HTML |
| API manuelle | Postman | Tous les endpoints | Collection exportée |

---

## 📦 Lancer le projet en local

### Prérequis

- Java 17+
- Maven 3.9+
- PostgreSQL (ou Docker)

### Avec Maven

```bash
git clone https://github.com/AmeniLagha/leoni-test-modulesBackEnd.git
cd leoni-test-modulesBackEnd
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Avec Docker

```bash
docker build -t leoni-backend .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SIGNOZ_ACCESS_TOKEN=votre_token \
  leoni-backend
```

---

## 📡 Monitoring — SigNoz

Le backend est instrumenté avec l'agent **OpenTelemetry** et envoie automatiquement vers SigNoz Cloud :

- **Traces** : suivi des requêtes HTTP end-to-end
- **Métriques** : JVM, CPU, temps de réponse
- **Logs** : corrélés avec les traces

La configuration se fait via les variables d'environnement (injectées par Clever Cloud au runtime).

---

## 🔐 Variables d'environnement requises

| Variable | Description |
|---|---|
| `SIGNOZ_ACCESS_TOKEN` | Token d'accès SigNoz Cloud |
| `SIGNOZ_ENDPOINT` | URL d'ingestion OTLP |
| `FRONTEND_URL` | URL du frontend (tests Selenium) |
| `BACKEND_URL` | URL de l'API déployée (health check) |
| `DOCKER_USERNAME` | Identifiant Docker Hub |
| `DOCKER_PASSWORD` | Token Docker Hub |
| `CLEVER_CLOUD_APP_ID` | ID de l'app Clever Cloud |
| `CLEVER_CLOUD_SECRET` | Secret de redéploiement |

---

## 📁 Structure du projet

```
src/
├── main/
│   ├── java/
│   │   └── ...          # Controllers, Services, Repositories
│   └── resources/
│       ├── application.properties
│       ├── application-dev.properties
│       └── application-docker.properties
└── test/
    └── java/
        ├── unit/         # Tests JUnit
        ├── integration/  # Tests Spring
        └── e2e/          # Tests Selenium/Cucumber
```

---

## 🔗 Repos liés

- [leoni-test-modulesFrontEnd](#) — Frontend Angular

---

*Projet académique — Cycle de vie des modules de test · Spring Boot · DevOps*
