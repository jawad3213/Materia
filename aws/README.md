# Materia AWS Cloud-Native Infrastructure (Terraform)

Production-grade, multi-environment Infrastructure as Code (IaC) for deploying the **Materia** application on Amazon Web Services (AWS) using **Terraform**.

---

## 🏛️ Architecture Overview

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│  🖥️ COUCHE PRÉSENTATION                                                                │
│  ├── 🖥️ Frontend React SPA                                                             │
│  ├── 🌐 Amazon CloudFront (CDN - Worldwide Edge Caching)                                │
│  └── 📦 Amazon S3 (Private Asset Bucket with Origin Access Control - OAC)             │
├────────────────────────────────────────────────────────────────────────────────────────┤
│  🔄 COUCHE ROUTING & API GATEWAY                                                       │
│  ├── 🌐 Hostinger DNS (CNAME pointers to CloudFront & ALB)                              │
│  └── 🔄 Application Load Balancer (ALB) (HTTP/HTTPS, Health Checks, Target Group "ip")  │
├────────────────────────────────────────────────────────────────────────────────────────┤
│  ⚙️ COUCHE APPLICATION (MODULAR MONOLITH)                                              │
│  ├── 🚀 Amazon ECS Fargate (Spring Boot 3.2.5 / Java 21)                                │
│  ├── 📨 Spring In-Process Events (Zero AWS MSK cost: APP_MESSAGING_TYPE=spring)        │
│  └── 📋 Amazon CloudWatch Logs (Container Insights & centralized streams)             │
├────────────────────────────────────────────────────────────────────────────────────────┤
│  🗄️ COUCHE DONNÉES                                                                     │
│  └── 🐘 Amazon RDS PostgreSQL 16 (Multi-AZ High Availability)                           │
├────────────────────────────────────────────────────────────────────────────────────────┤
│  🔐 COUCHE SÉCURITÉ & SECRETS                                                          │
│  ├── 🔑 AWS Secrets Manager (Database URL/credentials & JWT Signing Keys)              │
│  └── 🛡️ VPC Security Groups (Least-privilege chained ingress rules)                    │
├────────────────────────────────────────────────────────────────────────────────────────┤
│  🚀 COUCHE CI/CD & REGISTRY                                                            │
│  ├── 🐳 Amazon ECR (Docker Container Registry with automated CVE scan-on-push)         │
│  └── 🐙 GitHub Actions CI/CD (Keyless AWS authentication via IAM OIDC Role)            │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 📁 Repository Structure

```
aws/
│
├── modules/                                  # Shared reusable Terraform modules
│   ├── vpc/                                  # VPC, Internet Gateway, 3-tier Subnets
│   ├── networking/                           # NAT Gateways, Route Tables, DB Subnet Group
│   ├── security/                             # Security Groups (ALB, ECS Tasks, RDS PostgreSQL)
│   ├── database/                             # RDS PostgreSQL 16 (Multi-AZ Standby)
│   ├── secrets/                              # AWS Secrets Manager (DB credentials & JWT secrets)
│   ├── load_balancer/                        # ALB, Listeners (HTTP->HTTPS redirect), Target Group (ip)
│   ├── ecr/                                  # Container Registry with scanning & lifecycle policy
│   ├── ecs_fargate/                          # ECS Cluster, Fargate Service, Task Def, CloudWatch logs
│   ├── frontend_s3_cloudfront/               # S3 Bucket, CloudFront CDN, OAC, SPA 403/404 routing
│   └── iam_github_actions/                   # GitHub Actions OIDC Provider & IAM Deploy Role
│
├── environments/                             # Isolated deployment environments
│   ├── staging/                              # Pre-prod parity: 1 NAT, single-AZ RDS, 2 Fargate tasks
│   └── prod/                                 # HA: 1 NAT, Multi-AZ RDS, 2+ Fargate tasks
│
├── scripts/                                  # Shell automation scripts
│   ├── plan.sh                               # Preview plan for dev/staging/prod
│   ├── deploy.sh                             # Apply plan for dev/staging/prod
│   └── destroy.sh                            # Destroy environment with confirmation
│
├── .gitignore
├── Makefile
└── README.md
```

---

## 🌐 Hostinger DNS Setup

Once your environment is deployed with `terraform apply`, retrieve the endpoints from the outputs:
- `cloudfront_domain_name` (e.g. `d123456789abcdef.cloudfront.net`)
- `alb_dns_name` (e.g. `materia-prod-alb-123456789.eu-west-3.elb.amazonaws.com`)

In your **Hostinger DNS Zone Management**, create the following DNS records:

| Type | Host / Name | Value / Points to | TTL | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **CNAME** | `@` (or `app`) | `d123456789abcdef.cloudfront.net` | 300 | Routes frontend visitors to CloudFront CDN |
| **CNAME** | `api` | `materia-prod-alb-123456789.eu-west-3.elb.amazonaws.com` | 300 | Routes backend API requests to AWS ALB |

> [!TIP]
> If using apex domain (`@`) with Hostinger, check if Hostinger supports CNAME Flattening or ALIAS records. If not, point `app.yourdomain.com` or `www.yourdomain.com` via CNAME to CloudFront, and `api.yourdomain.com` to the ALB.

---

## 🐙 Keyless GitHub Actions CI/CD Pipeline

The `modules/iam_github_actions` module configures AWS OpenID Connect (OIDC). You do **not** need hardcoded `AWS_ACCESS_KEY_ID` or `AWS_SECRET_ACCESS_KEY` secrets in GitHub!

### Example `.github/workflows/deploy.yml`

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main]

permissions:
  id-token: write   # Required for requesting the OIDC JWT token
  contents: read

jobs:
  deploy-backend:
    name: Build & Deploy Backend (ECS Fargate)
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '21'
          cache: 'maven'

      - name: Build JAR with Maven
        run: |
          cd backend
          ./mvnw clean package -DskipTests

      - name: Configure AWS Credentials via OIDC
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: ${{ secrets.AWS_ROLE_TO_ASSUME }} # Output: github_actions_role_arn
          aws-region: eu-west-3

      - name: Log in to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v2

      - name: Build & Push Docker Image
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          ECR_REPOSITORY: materia-prod-backend
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG -t $ECR_REGISTRY/$ECR_REPOSITORY:latest backend/
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest

      - name: Deploy to Amazon ECS
        run: |
          aws ecs update-service \
            --cluster materia-prod-cluster \
            --service materia-prod-service \
            --force-new-deployment

  deploy-frontend:
    name: Build & Deploy Frontend (S3 + CloudFront)
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: 20

      - name: Build Frontend
        run: |
          cd frontend
          npm ci
          npm run build

      - name: Configure AWS Credentials via OIDC
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: ${{ secrets.AWS_ROLE_TO_ASSUME }}
          aws-region: eu-west-3

      - name: Sync build artifacts to S3
        run: |
          aws s3 sync frontend/dist/ s3://${{ secrets.FRONTEND_S3_BUCKET }} --delete

      - name: Invalidate CloudFront CDN Cache
        run: |
          aws cloudfront create-invalidation \
            --distribution-id ${{ secrets.CLOUDFRONT_DISTRIBUTION_ID }} \
            --paths "/*"
```

---

## ⚙️ Environment Sizing & Differences

| Dimension | Staging (`environments/staging`) | Prod (`environments/prod`) |
| :--- | :--- | :--- |
| **Availability Zones** | 2 (`eu-west-3a`, `3b`) | 3 (`eu-west-3a`, `3b`, `3c`) |
| **NAT Gateways** | 1 (shared) | 1 (shared - cost-optimized) |
| **RDS PostgreSQL 16** | Single-AZ (`db.t4g.small`) | Multi-AZ Standby (`db.t4g.medium`) |
| **RDS Backups** | 7 days | 30 days |
| **Deletion Protection**| Disabled | Enabled (RDS & ALB) |
| **ECS Fargate Compute** | 2 tasks (0.5 vCPU / 1GB) | 2+ tasks (1 vCPU / 2GB) |
| **CloudWatch Insights**| Disabled | Enabled |
| **CloudFront Price Class**| `PriceClass_100` | `PriceClass_All` |
| **Messaging Cost** | **$0** (`spring` in-process) | **$0** (`spring` in-process) |

---

## 🚀 Quick Start Guide

### Prerequisites
1. [Terraform CLI](https://developer.hashicorp.com/terraform/downloads) (>= 1.5.0)
2. [AWS CLI](https://aws.amazon.com/cli/) configured (`aws configure`)

### 1. Using Makefile Commands

```bash
cd aws

# Preview changes for staging or prod
make plan-staging
make plan-prod

# Deploy
make apply-staging
make apply-prod

# Destroy
make destroy-staging
```

### 2. Manual Terraform Execution

```bash
cd aws/environments/prod
terraform init
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```
