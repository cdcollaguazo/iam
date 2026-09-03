# IAM
Central Identity and Access Management (IAM) service, powered by `Keycloak`.

---

## 1. Responsibilities

- Define and manage realm configuration.
- Provide authentication and authorization capabilities.
- Externalize identity concerns from the applications.
- Ensure consistent and reproducible setup across environments.

---

## 2. Project Structure

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

---

## 5. Infrastructure
Cloud infrastructure is managed using AWS CDK.
Main components:
- VPC
- PostgreSQL
- ECS/Fargate
- Application Load Balancer
- ACM
- Route 53

Public endpoint: https://auth.cdcollaguazo.com
