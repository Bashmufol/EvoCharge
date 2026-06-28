# EvoCharge Architecture

## Overview

EvoCharge is a cloud-native EV charging intelligence platform that aggregates data from multiple Nigerian charging operators into a unified API and two web applications, all served from one CloudFront URL.

**Live:** https://d8061ggv2y910.cloudfront.net/

## Architecture Diagram

![EvoCharge AWS architecture](evocharge-architecture-diagram.png)

*Single CloudFront distribution: driver app at `/`, operator dashboard at `/operator/`, API at `/api/*`.*

```mermaid
flowchart TB
    subgraph users [Users]
        Driver[EV Driver Browser]
        Operator[Operator Browser]
    end

    subgraph edge [Amazon CloudFront]
        CF[CloudFront Distribution]
        CFF[CloudFront Function SPA rewrite]
    end

    subgraph static [Amazon S3]
        WebBucket[(Web Bucket / and /operator/)]
    end

    subgraph ingress [Load Balancing]
        ALB[Application Load Balancer]
    end

    subgraph network [Amazon VPC]
        ECS[ECS Fargate Spring Boot 4 API]
        ECR[Amazon ECR]
    end

    subgraph data [Data]
        DDBOps[(DynamoDB Operators)]
        DDBSta[(DynamoDB Stations)]
        S3Data[(S3 Data Bucket seed JSON)]
    end

    subgraph intelligence [Intelligence]
        Bedrock[Amazon Bedrock Claude Haiku 4.5]
        Location[Amazon Location Service]
    end

    subgraph events [Eventing]
        EB[Amazon EventBridge 2 min]
        Lambda[AWS Lambda NetworkPulseFn]
    end

    subgraph ops [Operations]
        CW[CloudWatch Logs]
    end

    Driver --> CF
    Operator --> CF
    CF --> WebBucket
    CF -->|/api/*| ALB
    CF --- CFF
    ALB --> ECS
    ECS --> ECR
    ECS --> DDBOps
    ECS --> DDBSta
    ECS --> S3Data
    ECS --> Bedrock
    ECS --> Location
    EB --> Lambda
    Lambda --> ALB
    ECS --> CW
```

## Request Flow

1. **Static pages** — Browser requests `/` or `/operator/`. CloudFront serves React builds from S3. A CloudFront Function rewrites `/operator/` to `index.html` for client-side routing.
2. **API calls** — Browser calls `/api/v1/...` on the same domain. CloudFront proxies to the ALB with caching disabled. ALB routes to a healthy Fargate task.
3. **Data** — API reads operators and stations from DynamoDB. On first boot, seed JSON from the S3 data bucket is loaded into DynamoDB.
4. **Recommendations** — EvoScore ranks stations by distance, availability, wait, reliability, and connector match.
5. **AI advisor** — API invokes Amazon Bedrock (`anthropic.claude-haiku-4-5-20251001-v1:0`) with the user query and top station context. A fallback answer is returned if Bedrock is unavailable.
6. **Network Pulse** — EventBridge triggers Lambda every two minutes. The API rotates station statuses and pushes SSE events to connected driver clients.
7. **Map** — Driver app renders MapLibre tiles from Amazon Location Service.

## CDK Stacks

| Stack | Resources |
|-------|-----------|
| EvoCharge-Network | VPC, 2 AZs, public/private subnets, 1 NAT Gateway |
| EvoCharge-Data | DynamoDB Operators + Stations tables, S3 data bucket |
| EvoCharge-Api | ECR, ECS Fargate, ALB, EventBridge rule, Lambda, CloudWatch log group |
| EvoCharge-Web | S3 web bucket, CloudFront distribution, CloudFront Function |

## Path Routing (single distribution)

| CloudFront path | Origin | Purpose |
|-----------------|--------|---------|
| `/` (default) | S3 web bucket | Driver React app |
| `/operator/*` | S3 web bucket | Operator React app |
| `/api/*` | ALB (HTTP :80) | Spring Boot API |

## Tagging

Compulsory (all resources):

- `aws-apn-id` = `pc:8l8gcn23lmlgammd8572tk6va`
- `event` = `oneWithAI`

Gen AI (Api stack, Bedrock):

- `aws-apn-id` = `pc:a8xnp70u5w0s41039u52e6iuj`

Additional: `project`, `environment`, `managed-by`
