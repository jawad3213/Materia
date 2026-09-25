# ===================================================================
# Production Environment - main.tf
# ===================================================================

locals {
  name_prefix = "materia-${var.environment}"
  common_tags = {
    Environment = var.environment
    Project     = "Materia"
    ManagedBy   = "Terraform"
  }
}

# 1. VPC Module (3 Availability Zones for Prod High Availability)
module "vpc" {
  source = "../../modules/vpc"

  name_prefix              = local.name_prefix
  cidr_block               = var.vpc_cidr
  availability_zones       = var.availability_zones
  public_subnet_cidrs      = var.public_subnet_cidrs
  private_app_subnet_cidrs = var.private_app_subnet_cidrs
  private_db_subnet_cidrs  = var.private_db_subnet_cidrs
  tags                     = local.common_tags
}

# 2. Networking Module (Single NAT Gateway for cost optimization)
module "networking" {
  source = "../../modules/networking"

  name_prefix            = local.name_prefix
  vpc_id                 = module.vpc.vpc_id
  internet_gateway_id    = module.vpc.internet_gateway_id
  public_subnet_ids      = module.vpc.public_subnet_ids
  private_app_subnet_ids = module.vpc.private_app_subnet_ids
  private_db_subnet_ids  = module.vpc.private_db_subnet_ids
  enable_nat_gateway     = true
  single_nat_gateway     = true # Cost-optimized: 1 shared NAT Gateway across all AZs
  tags                   = local.common_tags
}

# 3. Security Module
module "security" {
  source = "../../modules/security"

  name_prefix       = local.name_prefix
  vpc_id            = module.vpc.vpc_id
  app_port          = var.app_port
  alb_ingress_cidrs = ["0.0.0.0/0"]
  tags              = local.common_tags
}

# 4. Database Module (Production Multi-AZ RDS PostgreSQL 16)
module "database" {
  source = "../../modules/database"

  name_prefix             = local.name_prefix
  db_name                 = var.db_name
  db_username             = var.db_username
  db_password             = var.db_password
  db_subnet_group_name    = module.networking.db_subnet_group_name
  vpc_security_group_ids  = [module.security.database_security_group_id]
  instance_class          = var.db_instance_class
  allocated_storage       = 50
  max_allocated_storage   = 500
  multi_az                = true
  backup_retention_period = 30
  deletion_protection     = true
  skip_final_snapshot     = false
  tags                    = local.common_tags
}

# 5. Secrets Manager Module (Database & JWT Credentials)
module "secrets" {
  source = "../../modules/secrets"

  name_prefix             = local.name_prefix
  db_username             = var.db_username
  db_password             = var.db_password
  db_name                 = var.db_name
  db_host                 = module.database.db_instance_address
  db_port                 = module.database.db_instance_port
  jwt_access_secret       = var.jwt_access_secret
  jwt_refresh_secret      = var.jwt_refresh_secret
  recovery_window_in_days = 7 # Protection against accidental deletion in prod
  tags                    = local.common_tags
}

# 6. Load Balancer Module (With deletion protection & SSL support)
module "load_balancer" {
  source = "../../modules/load_balancer"

  name_prefix                = local.name_prefix
  vpc_id                     = module.vpc.vpc_id
  public_subnet_ids          = module.vpc.public_subnet_ids
  security_group_ids         = [module.security.alb_security_group_id]
  app_port                   = var.app_port
  health_check_path          = "/actuator/health"
  target_type                = "ip"
  certificate_arn            = var.certificate_arn
  enable_deletion_protection = true
  tags                       = local.common_tags
}

# 7. ECR Module (Container Registry with CVE scanning)
module "ecr" {
  source          = "../../modules/ecr"
  name_prefix     = local.name_prefix
  max_image_count = 30
  tags            = local.common_tags
}

# 8. ECS Fargate Compute Module (Spring Boot Backend Modular Monolith)
module "ecs_fargate" {
  source = "../../modules/ecs_fargate"

  name_prefix               = local.name_prefix
  aws_region                = var.aws_region
  cpu                       = var.ecs_cpu
  memory                    = var.ecs_memory
  desired_count             = var.ecs_desired_count
  container_image           = var.container_image != null && var.container_image != "" ? var.container_image : "${module.ecr.repository_url}:latest"
  app_port                  = var.app_port
  private_subnet_ids        = module.vpc.private_app_subnet_ids
  security_group_ids        = [module.security.ecs_security_group_id]
  target_group_arn          = module.load_balancer.target_group_arn
  db_secret_arn             = module.secrets.db_secret_arn
  jwt_secret_arn            = module.secrets.jwt_secret_arn
  spring_profiles_active    = var.environment
  app_messaging_type        = "spring" # In-process event publisher ($0 messaging cost!)
  enable_container_insights = true
  tags                      = local.common_tags
}

# 9. Presentation Layer: Frontend S3 + CloudFront CDN Module
module "frontend_s3_cloudfront" {
  source = "../../modules/frontend_s3_cloudfront"

  name_prefix           = local.name_prefix
  custom_domain_aliases = var.custom_domain_aliases
  certificate_arn       = var.cloudfront_certificate_arn
  price_class           = "PriceClass_All" # Worldwide lowest latency edge caching
  force_destroy         = false            # Protect prod assets from accidental wipe
  tags                  = local.common_tags
}

# 10. CI/CD: IAM Role for GitHub Actions (Keyless OIDC)
module "iam_github_actions" {
  source = "../../modules/iam_github_actions"

  name_prefix                 = local.name_prefix
  github_repo                 = var.github_repo
  create_oidc_provider        = var.create_oidc_provider
  ecr_repository_arn          = module.ecr.repository_arn
  ecs_task_execution_role_arn = module.ecs_fargate.execution_role_arn
  ecs_task_role_arn           = module.ecs_fargate.task_role_arn
  frontend_s3_bucket_arn      = module.frontend_s3_cloudfront.s3_bucket_arn
  cloudfront_distribution_arn = module.frontend_s3_cloudfront.cloudfront_distribution_arn
  tags                        = local.common_tags
}
