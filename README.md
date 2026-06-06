# EvoCharge

**Nigeria's EV Charging Intelligence Layer** — built for the [ONE WITH AI Hackathon](https://arthuriteintegrated.com) by Arthurite Integrated (Problem Statement #1).

EvoCharge aggregates charging station data from multiple operators into a single cloud-native platform. EV drivers discover stations, get EvoScore recommendations, and chat with an AI Charge Advisor. Operators and policymakers access utilization analytics and unmet demand insights.

## Features

- **Multi-city coverage** — Lagos (primary), Abuja, and Port Harcourt with city filter
- **Multi-operator aggregation** — EVNetwork NG, ChargePro Africa, VoltLane in one API
- **Interactive map** — MapLibre + Amazon Location Service with live status pins
- **EvoScore recommendations** — weighted scoring: distance, availability, wait, reliability, connector
- **Network Pulse** — real-time status updates via SSE + EventBridge
- **Demand heatmap** — underserved area overlay for investors/policymakers
- **AI Charge Advisor** — Amazon Bedrock natural-language station search
- **Operator dashboard** — KPIs, charts, station table, demand analytics

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 21, Spring Boot 4.0.6 |
| Driver App | React 19, TypeScript, Tailwind CSS v4, Framer Motion, MapLibre |
| Operator Dashboard | React 19, TypeScript, Tailwind CSS v4, Recharts |
| Infrastructure | AWS CDK (Java) |
| Cloud | ECS Fargate, DynamoDB, S3, CloudFront, EventBridge, Bedrock, Location Service |

## Project Structure

```
EvoCharge/
├── apps/driver-web/          # Driver-facing web app
├── apps/operator-dashboard/  # Operator analytics dashboard
├── backend/api/              # Spring Boot REST API
├── infra/cdk/                # AWS CDK infrastructure
├── data/seed/                # Mock data: 44 stations (Lagos, Abuja, Port Harcourt)
└── docs/                     # API contract, SOW, architecture, check-in scripts
```

## Quick Start (Local)

### Prerequisites
- Java 21, Maven 3.9+
- Node.js 20+
- (Optional) Docker

### 1. Start the API

```bash
cd backend/api
mvn spring-boot:run
```

API runs at `http://localhost:8080`. Seed data loads automatically from `data/seed/`.

### 2. Start the Driver App

```bash
cd apps/driver-web
cp .env.example .env
npm install
npm run dev
```

Open `http://localhost:5173`

### 3. Start the Operator Dashboard

```bash
cd apps/operator-dashboard
cp .env.example .env
npm install
npm run dev
```

Open `http://localhost:5174`

### Docker (API only)

```bash
docker compose up --build
```

## AWS Deployment

```bash
cd infra/cdk
mvn package
cdk deploy --all
```

Hackathon tags (`aws-apn-id`, `event`) are applied automatically via CDK; Gen AI tag is set on the Api stack (Bedrock).

Build and push API container to ECR, then update ECS task definition with your image.

Deploy frontends (single URL — driver at `/`, operator at `/operator/`):

```bash
# Driver app → bucket root
cd apps/driver-web
VITE_API_URL= npm run build
aws s3 sync dist/ s3://<WebBucketName>/ --delete --exclude "operator/*"

# Operator dashboard → /operator/ prefix
cd apps/operator-dashboard
VITE_API_URL= npm run build
aws s3 sync dist/ s3://<WebBucketName>/operator/ --delete
```

Open `WebUrl` from CDK outputs (operator: `<WebUrl>/operator/`).

## API Endpoints

See [docs/api-contract.md](docs/api-contract.md).

## Hackathon Deliverables

- [docs/sow.md](docs/sow.md) — Statement of Work
- [docs/architecture.md](docs/architecture.md) — Architecture diagram
- [docs/check-in-scripts.md](docs/check-in-scripts.md) — Progress update scripts

## Team

Built for ONE WITH AI Hackathon — Powering Mobility & EV Ecosystem with AWS.
