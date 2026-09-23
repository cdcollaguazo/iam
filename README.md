# IAM

Identity and access management service for the `cdcollaguazo` platform.

This project provides authentication and authorization using **Keycloak**, together with the infrastructure and deployment steps required to run and configure it.

---

## 1. Responsibilities

- Define and manage realm configuration.
- Provide authentication and authorization capabilities.
- Externalize identity concerns from the applications.
- Ensure consistent and reproducible setup across environments.

---

## 2. Architecture

```text
CloudFront
    |
    | /auth/*
    v
Shared ALB
    |
    v
Keycloak Target Group
    |
    v
ECS Keycloak Service
    |
    v
PostgreSQL
```

The application imports shared AWS references from SSM Parameter Store and owns its ECS service, Target Group, ALB Listener Rule, secrets, and IAM-specific deployment resources.

Keycloak configuration is applied after the service is available.

---

## 3. Project Structure

```
iam/
├── .github/                # GitHub workflows and CI/CD configuration
├── infra/                  # Infrastructure as Code (IaC)
├── realm/                  # Realm configuration
├── spi/                    # Custom extension for identity synchronization
├── docker-compose.yml      # Local infrastructure services
└── Dockerfile              # Container definition for execution

```

---

## 3. Local Development
Central Identity and Access Management (IAM) service, powered by `Keycloak`. 

- Docker
- Docker Compose

Start the local environment:

```
docker compose up -d
```

Keycloak will be available at http://localhost:8080

---

## 4. Realm configuration
The configuration is managed with Configuration as Code (CoC) using `keycloak-config-cli`, which allows to apply
changes effectively in Keycloak systems. It has been integrated in the docker-compose.yml file and in the pipeline 
for reproducible configurations. For more information about the tool see: https://github.com/adorsys/keycloak-config-cli
