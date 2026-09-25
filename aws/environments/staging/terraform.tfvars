# ===================================================================
# Staging Environment - terraform.tfvars
# ===================================================================

aws_region  = "eu-west-3"
environment = "staging"

# Network Configuration
vpc_cidr                 = "10.1.0.0/16"
availability_zones       = ["eu-west-3a", "eu-west-3b"]
public_subnet_cidrs      = ["10.1.1.0/24", "10.1.2.0/24"]
private_app_subnet_cidrs = ["10.1.11.0/24", "10.1.12.0/24"]
private_db_subnet_cidrs  = ["10.1.21.0/24", "10.1.22.0/24"]

# Application Port
app_port = 8080

# Database Configuration (Staging)
db_name           = "materia"
db_username       = "postgres"
db_password       = "StagingSecretPass2026!"
db_instance_class = "db.t4g.small"

# Secrets Manager (JWT)
jwt_access_secret  = "bWF0ZXJpYS1zdGFnaW5nLWFjY2Vzcy10b2tlbi1zZWNyZXQta2V5LTIwMjY="
jwt_refresh_secret = "bWF0ZXJpYS1zdGFnaW5nLXJlZnJlc2gtdG9rZW4tc2VjcmV0LWtleS0yMDI2="

# ECS Fargate Compute
ecs_cpu           = 512
ecs_memory        = 1024
ecs_desired_count = 2

# CI/CD GitHub Actions
github_repo = "*"
