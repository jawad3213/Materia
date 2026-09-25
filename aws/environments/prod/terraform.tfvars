# ===================================================================
# Production Environment - terraform.tfvars
# ===================================================================

aws_region  = "eu-west-3"
environment = "prod"

# Network Configuration (3 AZs for full redundancy)
vpc_cidr                 = "10.2.0.0/16"
availability_zones       = ["eu-west-3a", "eu-west-3b", "eu-west-3c"]
public_subnet_cidrs      = ["10.2.1.0/24", "10.2.2.0/24", "10.2.3.0/24"]
private_app_subnet_cidrs = ["10.2.11.0/24", "10.2.12.0/24", "10.2.13.0/24"]
private_db_subnet_cidrs  = ["10.2.21.0/24", "10.2.22.0/24", "10.2.23.0/24"]

# Application Port
app_port = 8080

# Database Configuration (Production Multi-AZ)
db_name           = "materia"
db_username       = "postgres"
db_password       = "ProdMasterStrongSecret2026!#"
db_instance_class = "db.t4g.medium"

# Secrets Manager (JWT)
jwt_access_secret  = "bWF0ZXJpYS1wcm9kLWFjY2Vzcy10b2tlbi1zZWNyZXQta2V5LTIwMjY="
jwt_refresh_secret = "bWF0ZXJpYS1wcm9kLXJlZnJlc2gtdG9rZW4tc2VjcmV0LWtleS0yMDI2="

# ECS Fargate Compute (High Availability 1 vCPU / 2GB RAM per replica)
ecs_cpu           = 1024
ecs_memory        = 2048
ecs_desired_count = 2

# Custom Domain (Optional: Uncomment & configure with your Hostinger domain)
# custom_domain_aliases      = ["app.materia.com"]
# cloudfront_certificate_arn = "arn:aws:acm:us-east-1:123456789012:certificate/your-cert-id"
# certificate_arn            = "arn:aws:acm:eu-west-3:123456789012:certificate/your-alb-cert-id"

# CI/CD GitHub Actions
github_repo = "*"
