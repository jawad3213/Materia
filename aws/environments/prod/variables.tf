# ===================================================================
# Production Environment - variables.tf
# ===================================================================

variable "aws_region" {
  description = "AWS Region to deploy resources into"
  type        = string
  default     = "eu-west-3"
}

variable "environment" {
  description = "Environment identifier"
  type        = string
  default     = "prod"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.2.0.0/16"
}

variable "availability_zones" {
  description = "Availability zones for subnets (min 3 for production high availability)"
  type        = list(string)
  default     = ["eu-west-3a", "eu-west-3b", "eu-west-3c"]
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets"
  type        = list(string)
  default     = ["10.2.1.0/24", "10.2.2.0/24", "10.2.3.0/24"]
}

variable "private_app_subnet_cidrs" {
  description = "CIDR blocks for private app subnets"
  type        = list(string)
  default     = ["10.2.11.0/24", "10.2.12.0/24", "10.2.13.0/24"]
}

variable "private_db_subnet_cidrs" {
  description = "CIDR blocks for private db subnets"
  type        = list(string)
  default     = ["10.2.21.0/24", "10.2.22.0/24", "10.2.23.0/24"]
}

variable "app_port" {
  description = "Port the application backend listens on"
  type        = number
  default     = 8080
}

# -------------------------------------------------------------------
# Database Variables (Production Multi-AZ)
# -------------------------------------------------------------------
variable "db_name" {
  description = "Database name"
  type        = string
  default     = "materia"
}

variable "db_username" {
  description = "Database master username"
  type        = string
  default     = "postgres"
}

variable "db_password" {
  description = "Database master password"
  type        = string
  sensitive   = true
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t4g.medium"
}

# -------------------------------------------------------------------
# Application Secrets
# -------------------------------------------------------------------
variable "jwt_access_secret" {
  description = "JWT Access Token Secret Key"
  type        = string
  sensitive   = true
}

variable "jwt_refresh_secret" {
  description = "JWT Refresh Token Secret Key"
  type        = string
  sensitive   = true
}

# -------------------------------------------------------------------
# ECS Fargate Compute Variables
# -------------------------------------------------------------------
variable "ecs_cpu" {
  description = "Fargate CPU units (1024 = 1 vCPU)"
  type        = number
  default     = 1024
}

variable "ecs_memory" {
  description = "Fargate Memory in MiB (2048 = 2GB RAM)"
  type        = number
  default     = 2048
}

variable "ecs_desired_count" {
  description = "Desired number of ECS task replicas for high availability"
  type        = number
  default     = 2
}

variable "container_image" {
  description = "Initial Docker container image (defaults to public nginx placeholder before CI/CD push)"
  type        = string
  default     = "public.ecr.aws/nginx/nginx:alpine"
}

# -------------------------------------------------------------------
# Networking & Routing Variables (ALB & CloudFront)
# -------------------------------------------------------------------
variable "certificate_arn" {
  description = "ARN of ACM SSL certificate for ALB HTTPS listener (optional)"
  type        = string
  default     = null
}

variable "custom_domain_aliases" {
  description = "Custom domains for CloudFront CDN (e.g. app.materia.com)"
  type        = list(string)
  default     = []
}

variable "cloudfront_certificate_arn" {
  description = "ACM SSL Certificate for CloudFront (must be in us-east-1)"
  type        = string
  default     = null
}

# -------------------------------------------------------------------
# CI/CD GitHub Actions Variables
# -------------------------------------------------------------------
variable "github_repo" {
  description = "GitHub repository for OIDC authentication (e.g. jawad3213/materia)"
  type        = string
  default     = "*"
}
