# EvoCharge

**Nigeria's EV Charging Intelligence Layer** — built for the [ONE WITH AI Hackathon](https://arthuriteintegrated.com) by Arthurite Integrated (Problem Statement #1).

EvoCharge aggregates charging station data from multiple operators into a single cloud-native platform. EV drivers discover stations, get EvoScore recommendations, and chat with an AI Charge Advisor powered by Amazon Bedrock. Operators and policymakers access utilization analytics and unmet demand insights.

## Live Demo

| Surface | URL |
|---------|-----|
| Driver app | https://d8061ggv2y910.cloudfront.net/ |
| Operator dashboard | https://d8061ggv2y910.cloudfront.net/operator/ |
| API health | https://d8061ggv2y910.cloudfront.net/api/v1/health |

All three run on a **single CloudFront distribution**. The driver header links to the operator dashboard; the operator sidebar links back to the driver app.

## Features

- **Multi-city coverage** — Lagos (primary), Abuja, and Port Harcourt with city filter
- **Multi-operator aggregation** — EVNetwork NG, ChargePro Africa, and VoltLane in one API
- **Interactive map** — MapLibre + Amazon Location Service with live status pins
- **EvoScore recommendations** — weighted scoring: distance, availability, wait, reliability, connector
- **Network Pulse** — real-time status updates via SSE + EventBridge + Lambda
- **Demand heatmap** — underserved area overlay for investors and policymakers
- **AI Charge Advisor** — Amazon Bedrock (Claude Haiku 4.5) natural-language station search
- **Operator dashboard** — KPIs, charts, station table, demand analytics
- **Cross-app navigation** — switch between driver and operator views from one domain

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 21, Spring Boot 4.0.6 |
| Driver app | React 19, TypeScript, Tailwind CSS v4, Framer Motion, MapLibre |
| Operator dashboard | React 19, TypeScript, Tailwind CSS v4, Recharts |
| Infrastructure | AWS CDK (Java) |
| Cloud | ECS Fargate, ALB, ECR, DynamoDB, S3, CloudFront, EventBridge, Lambda, Bedrock, Location Service, CloudWatch |

## Project Structure

```
EvoCharge/
├── apps/driver-web/          # Driver-facing web app
├── apps/operator-dashboard/  # Operator analytics dashboard
├── backend/api/              # Spring Boot REST API
├── infra/cdk/                # AWS CDK infrastructure (4 stacks)
├── data/seed/                # 44 stations across Lagos, Abuja, Port Harcourt
├── scripts/                  # Deployment helpers
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

From the repo root:

```bash
docker compose up --build
```

## AWS Deployment

Infrastructure is defined in four CDK stacks: **Network**, **Data**, **Api**, and **Web**.

```bash
cd infra/cdk
mvn package
cdk deploy --all
```

Hackathon tags (`aws-apn-id`, `event`) are applied automatically via CDK. The Gen AI partner tag is set on the Api stack (Bedrock).

### Deploy the API (Docker + ECR + ECS)

The CDK Api stack creates the ECR repository and ECS service. After the first `cdk deploy`, build and push the real API image:

**PowerShell (repo root):**

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy-api.ps1
```

Or manually:

```powershell
docker build -f backend/api/Dockerfile -t evocharge-api:latest .
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
docker tag evocharge-api:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/evocharge-api:latest
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/evocharge-api:latest
```

Then create a new ECS task definition revision pointing at the ECR image and **Update service** with **Force new deployment**.

Bedrock model (configured in `application.properties` and ECS env):

`anthropic.claude-haiku-4-5-20251001-v1:0`

### Deploy the Frontends (single CloudFront URL)

Get `WebBucketName` and `WebUrl` from CDK outputs after deploying `EvoCharge-Web`.

**PowerShell:**

```powershell
$env:VITE_API_URL = "https://<your-cloudfront-domain>"

cd apps/driver-web
npm run build
aws s3 sync dist/ s3://<WebBucketName>/ --delete --exclude "operator/*"

cd ..\operator-dashboard
npm run build
aws s3 sync dist/ s3://<WebBucketName>/operator/ --delete

aws cloudfront create-invalidation --distribution-id <DistributionId> --paths "/*"
```

Set `VITE_API_URL` to your full CloudFront HTTPS URL (not empty). An empty value falls back to `localhost:8080` in production builds.

| Path | App |
|------|-----|
| `/` | Driver web app |
| `/operator/` | Operator dashboard |
| `/api/*` | Proxied to ALB → ECS Fargate API |

## API Endpoints

See [docs/api-contract.md](docs/api-contract.md).

Production base URL: `https://d8061ggv2y910.cloudfront.net/api/v1`

## Documentation

| Document | Description |
|----------|-------------|
| [docs/sow.md](docs/sow.md) | Statement of Work (markdown) |
| [docs/sow.pdf](docs/sow.pdf) | Statement of Work with architecture diagram (submission PDF) |
| [docs/architecture.md](docs/architecture.md) | Architecture overview and data flow |
| [docs/api-contract.md](docs/api-contract.md) | REST API contract |
| [docs/check-in-scripts.md](docs/check-in-scripts.md) | Hackathon progress scripts |

## Team

Built for ONE WITH AI Hackathon — Powering Mobility & EV Ecosystem with AWS.

| Role | Name |
|------|------|
| Team lead + cloud engineer | Bashir |
| Driver app | AbdulSamad |
| Operator dashboard | Abdullateef |
| Backend API | Abdulroheem |
