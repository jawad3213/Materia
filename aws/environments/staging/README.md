# Staging Environment

This environment provisions the staging infrastructure for Materia on AWS:
- **Presentation Layer**: React SPA hosted in private S3 bucket and served worldwide via CloudFront CDN.
- **API Routing**: Application Load Balancer (ALB) pointing to ECS Fargate backend.
- **Compute Layer**: Amazon ECS Fargate running the Spring Boot modular monolith (with in-process Spring Events, zero MSK cost).
- **Data Layer**: Amazon RDS PostgreSQL 16.
- **Secrets Management**: AWS Secrets Manager storing database credentials and JWT secret keys.
- **CI/CD**: Keyless GitHub Actions deployment via IAM OIDC role.

## Quick Start

```bash
cd aws/environments/staging
terraform init
terraform plan
terraform apply
```
