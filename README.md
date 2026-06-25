# EvoCharge API

REST API and AWS infrastructure for EvoCharge, a charging intelligence platform for Nigeria. The service aggregates station data from multiple operators into one normalized schema, exposes discovery and analytics endpoints, ranks stations with EvoScore, streams live status updates, and powers a natural-language charging advisor through Amazon Bedrock.

## Overview

EvoCharge sits between operator data sources and client applications. It stores operators and stations in DynamoDB (production) or local seed files (development), serves a versioned HTTP API, and runs on AWS using infrastructure defined in CDK.

Seed data includes 48 stations across Lagos, Abuja, and Port Harcourt, representing three operator profiles: EVNetwork NG, ChargePro Africa, and VoltLane.

## API Capabilities

| Area | Description |
|------|-------------|
| Operators and stations | List, filter, search, and fetch station detail by city, operator, status, and connector |
| Nearby search | Find stations within a radius of a latitude and longitude |
| EvoScore | Rank stations by distance, availability, wait time, reliability, and connector match |
| Network Pulse | Server-sent events for live status changes; scheduled pulse via EventBridge and Lambda |
| Analytics | Network summary KPIs and demand-by-area aggregates |
| AI advisor | Natural-language queries via Bedrock, with Mistral API fallback and template fallback |
| Health | Liveness endpoint for load balancer checks |

Full endpoint reference: [docs/api-contract.md](docs/api-contract.md).

## Tech Stack

| Layer | Technology |
|-------|------------|
| API | Java 21, Spring Boot 4.0.6 |
| Infrastructure | AWS CDK (Java) |
| Compute | Amazon ECS on Fargate, Application Load Balancer |
| Data | Amazon DynamoDB, Amazon S3 (seed and data files) |
| AI | Amazon Bedrock |
| Events | Amazon EventBridge, AWS Lambda |
| Networking | Amazon VPC, NAT Gateway |
| Registry and logs | Amazon ECR, Amazon CloudWatch Logs |
| Edge (optional) | Amazon CloudFront (API path proxy via Web stack) |

## Project Structure

```
EvoCharge/
├── backend/api/       # Spring Boot application
├── infra/cdk/         # AWS CDK stacks
├── data/seed/         # Operator and station seed JSON
├── scripts/           # API deployment helpers
└── docs/              # API contract and architecture notes
```

## Local Development

### Prerequisites

- Java 21
- Maven 3.9+
- Docker (optional)

### Run with Maven

```bash
cd backend/api
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. Seed data loads from `data/seed/` on startup.

### Run with Docker

From the repository root:

```bash
docker compose up --build
```

### Configuration

Key settings in `backend/api/src/main/resources/application.properties`:

| Property | Default (local) |
|----------|-----------------|
| `evocharge.storage` | `local` |
| `evocharge.seed-path` | `../../../data/seed` |
| `evocharge.bedrock.model-id` | `anthropic.claude-haiku-4-5-20251001-v1:0` |
| `evocharge.bedrock.enabled` | `true` |
| `evocharge.mistral.enabled` | `true` |
| `evocharge.mistral.api-key` | Set via `MISTRAL_API_KEY` locally or `EVOCHARGE_MISTRAL_API_KEY` on ECS |
| `evocharge.seed.resync-on-startup` | `false` (set `EVOCHARGE_SEED_RESYNC=true` once to reload seed data) |

Production uses the `aws` Spring profile (`application-aws.properties`) with DynamoDB table names and settings supplied by ECS environment variables.

## AWS Infrastructure

Infrastructure is split into four CDK stacks:

| Stack | Purpose |
|-------|---------|
| `EvoCharge-Network` | VPC, public and private subnets across two AZs, NAT Gateway |
| `EvoCharge-Data` | DynamoDB tables (`EvoCharge-Operators`, `EvoCharge-Stations`), S3 data bucket |
| `EvoCharge-Api` | ECR repository, ECS Fargate service, ALB, EventBridge rule, Lambda pulse function, CloudWatch log group |
| `EvoCharge-Web` | CloudFront distribution and S3 bucket for edge delivery (includes `/api/*` proxy to the ALB) |

### Deploy infrastructure

```bash
cd infra/cdk
mvn package
cdk deploy --all
```

Note the `ApiUrl` output from the Api stack (ALB DNS name). Health check path: `/api/v1/health`.

### Deploy the API container

After CDK creates the ECR repository and ECS service, build and push the application image:

**PowerShell (repository root):**

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy-api.ps1
```

**Manual steps:**

```powershell
docker build -f backend/api/Dockerfile -t evocharge-api:latest .

aws ecr get-login-password --region us-east-1 `
  | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

docker tag evocharge-api:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/evocharge-api:latest
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/evocharge-api:latest
```

Register a new ECS task definition revision with the pushed image, then update the service with **Force new deployment**.

Environment variables set by CDK on the Api stack:

| Variable | Purpose |
|----------|---------|
| `SPRING_PROFILES_ACTIVE` | `aws` |
| `EVOCHARGE_STORAGE` | `dynamodb` |
| `EVOCHARGE_DYNAMODB_OPERATORS_TABLE` | Operators table name |
| `EVOCHARGE_DYNAMODB_STATIONS_TABLE` | Stations table name |
| `EVOCHARGE_BEDROCK_ENABLED` | `true` |
| `EVOCHARGE_BEDROCK_MODEL_ID` | `anthropic.claude-haiku-4-5-20251001-v1:0` |
| `EVOCHARGE_MISTRAL_ENABLED` | `true` |
| `EVOCHARGE_MISTRAL_API_KEY` | Your Mistral API key (set in ECS task definition or Secrets Manager) |
| `EVOCHARGE_SEED_RESYNC` | `true` for one deployment to reload updated seed stations |

### IAM permissions (task role)

The ECS task role grants access to:

- Read/write on the DynamoDB operator and station tables
- Read on the S3 data bucket
- `bedrock:InvokeModel`
- Amazon Location Service actions used by the API

## API Base URLs

| Environment | Base URL |
|-------------|----------|
| Local | `http://localhost:8080/api/v1` |
| Production (ALB) | `http://<alb-dns-name>/api/v1` |
| Production (CloudFront) | `https://<distribution-domain>/api/v1` |

Example health check:

```bash
curl https://<your-domain>/api/v1/health
```

## Documentation

| Document | Description |
|----------|-------------|
| [docs/api-contract.md](docs/api-contract.md) | REST API contract and schemas |
| [docs/architecture.md](docs/architecture.md) | AWS architecture and data flow |
| [docs/sow.md](docs/sow.md) | Statement of Work |
| [docs/sow.pdf](docs/sow.pdf) | Statement of Work (PDF) |

## License

Proprietary. All rights reserved.
