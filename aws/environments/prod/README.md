# Production Environment

This environment provisions the full production-grade AWS infrastructure for Materia:
- **Presentation Layer**: React SPA hosted in private S3 bucket, served via CloudFront CDN with worldwide edge caching (`PriceClass_All`), OAC origin lockdown, and SPA routing fallback.
- **Routing Layer**: High Availability Application Load Balancer with deletion protection and SSL termination.
- **Compute Layer**: Amazon ECS Fargate running the Spring Boot modular monolith with CloudWatch Container Insights, zero MSK cost via in-process Spring Events, and automated circuit breaker rollbacks.
- **Data Layer**: Amazon RDS PostgreSQL 16 Multi-AZ with automatic failover and 30-day automated backups.
- **Secrets Management**: AWS Secrets Manager storing database credentials and JWT secret keys with accidental deletion protection.
- **CI/CD**: Keyless GitHub Actions deployment via IAM OIDC role.

## Quick Start

```bash
cd aws/environments/prod
terraform init
terraform plan
terraform apply
```
