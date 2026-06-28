# EvoCharge API

REST API and AWS infrastructure for EvoCharge, a charging intelligence platform for Nigeria. The service aggregates station data from multiple operators into one normalized schema, exposes discovery and analytics endpoints, ranks stations with EvoScore, streams live status updates, and powers a natural-language charging advisor through Amazon Bedrock.

## Overview

EvoCharge sits between operator data sources and client applications. Locally it loads seed JSON into in-memory stores. In AWS it uses DynamoDB tables defined in CDK.

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
| Data | Amazon DynamoDB, Amazon S3 |
| AI | Amazon Bedrock |
| Events | Amazon EventBridge, AWS Lambda |
| Networking | Amazon VPC, NAT Gateway |
| Edge | Amazon CloudFront (API path proxy via Web stack) |

## Project Structure

```
EvoCharge/
├── backend/api/       # Spring Boot application
├── infra/cdk/         # AWS CDK stacks (Network, Data, Api, Web)
├── data/seed/         # Operator and station seed JSON
├── apps/              # Driver and operator frontends (separate packages)
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
| `evocharge.mistral.api-key` | Set via `MISTRAL_API_KEY` environment variable |
| `evocharge.seed.resync-on-startup` | `false` |

Set `MISTRAL_API_KEY` for the AI advisor Mistral fallback. Bedrock calls require AWS credentials with `bedrock:InvokeModel` when `evocharge.bedrock.enabled=true`.

Production uses the `aws` Spring profile (`application-aws.properties`) with DynamoDB table names supplied by ECS environment variables.

### API base URL (local)

```
http://localhost:8080/api/v1
```

Example health check:

```bash
curl http://localhost:8080/api/v1/health
```

## AWS Infrastructure (CDK)

Infrastructure is split into four stacks:

| Stack | Purpose |
|-------|---------|
| `EvoCharge-Network` | VPC, public and private subnets across two AZs, NAT Gateway |
| `EvoCharge-Data` | DynamoDB tables, S3 data bucket, ECR repository |
| `EvoCharge-Api` | ECS Fargate service, ALB, EventBridge pulse rule, CloudWatch logs |
| `EvoCharge-Web` | CloudFront distribution and S3 bucket; `/api/*` proxies to the ALB |

### Synthesize and deploy

```bash
cd infra/cdk
mvn package
cdk synth
cdk deploy --all
```

Before deploying `EvoCharge-Api`, build and push the API image to the ECR repository created by `EvoCharge-Data`:

```bash
# From repository root
docker build -f backend/api/Dockerfile -t evocharge-api:latest .

aws ecr get-login-password --region us-east-1 \
  | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

docker tag evocharge-api:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/evocharge-api:latest
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/evocharge-api:latest
```

Health check path: `/api/v1/health`. The Web stack serves frontends from S3 and routes API traffic to the ALB over HTTP (`OriginProtocolPolicy.HTTP_ONLY`).

Environment variables set on the ECS task by CDK:

| Variable | Purpose |
|----------|---------|
| `SPRING_PROFILES_ACTIVE` | `aws` |
| `EVOCHARGE_STORAGE` | `dynamodb` |
| `EVOCHARGE_DYNAMODB_OPERATORS_TABLE` | Operators table name |
| `EVOCHARGE_DYNAMODB_STATIONS_TABLE` | Stations table name |
| `EVOCHARGE_BEDROCK_ENABLED` | `true` |
| `EVOCHARGE_BEDROCK_MODEL_ID` | Bedrock model ID |
| `EVOCHARGE_MISTRAL_ENABLED` | `true` |

Set `EVOCHARGE_MISTRAL_API_KEY` on the task definition for advisor Mistral fallback. Set `EVOCHARGE_SEED_RESYNC=true` for one deployment to reload seed data into DynamoDB.

## Documentation

| Document | Description |
|----------|-------------|
| [docs/api-contract.md](docs/api-contract.md) | REST API contract and schemas |
| [docs/architecture.md](docs/architecture.md) | AWS architecture and data flow |

## License

Proprietary. All rights reserved.
