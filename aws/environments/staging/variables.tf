# ===================================================================
# Staging Environment - variables.tf
# ===================================================================

variable "aws_region" {
  description = "AWS Region to deploy resources into"
  type        = string
  default     = "eu-west-3"
}

variable "environment" {
  description = "Environment identifier"
  type        = string
  default     = "staging"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.1.0.0/16"
}

variable "availability_zones" {
  description = "Availability zones for subnets"
  type        = list(string)
  default     = ["eu-west-3a", "eu-west-3b"]
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets"
  type        = list(string)
  default     = ["10.1.1.0/24", "10.1.2.0/24"]
}

variable "private_app_subnet_cidrs" {
  description = "CIDR blocks for private app subnets"
  type        = list(string)
  default     = ["10.1.11.0/24", "10.1.12.0/24"]
}

variable "private_db_subnet_cidrs" {
  description = "CIDR blocks for private db subnets"
  type        = list(string)
  default     = ["10.1.21.0/24", "10.1.22.0/24"]
}

variable "app_port" {
  description = "Port the application backend listens on"
  type        = number
  default     = 8080
}

# -------------------------------------------------------------------
# Database Variables
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
  default     = "db.t4g.small"
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
  description = "Fargate CPU units (256, 512, 1024)"
  type        = number
  default     = 512
}

variable "ecs_memory" {
  description = "Fargate Memory in MiB (512, 1024, 2048)"
  type        = number
  default     = 1024
}

variable "ecs_desired_count" {
  description = "Desired number of ECS task replicas"
  type        = number
  default     = 2
}

variable "container_image" {
  description = "Initial Docker container image (defaults to public nginx placeholder before CI/CD push)"
  type        = string
  default     = "public.ecr.aws/nginx/nginx:alpine"
}

# -------------------------------------------------------------------
# Networking & Routing Variables
# -------------------------------------------------------------------
variable "certificate_arn" {
  description = "ARN of ACM SSL certificate for ALB HTTPS listener (optional)"
  type        = string
  default     = null
}

variable "custom_domain_aliases" {
  description = "Custom domains for CloudFront CDN"
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
  description = "GitHub repository for OIDC authentication (e.g. user/materia)"
  type        = string
  default     = "*"
}
