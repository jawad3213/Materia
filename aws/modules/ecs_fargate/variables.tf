# ===================================================================
# AWS ECS Fargate Module - variables.tf
# ===================================================================

variable "name_prefix" {
  description = "Prefix for naming resources (e.g. materia-dev)"
  type        = string
}

variable "aws_region" {
  description = "AWS Region for CloudWatch logging"
  type        = string
  default     = "eu-west-3"
}

variable "cpu" {
  description = "Fargate instance CPU units (256 = 0.25 vCPU, 512 = 0.5 vCPU, 1024 = 1 vCPU)"
  type        = number
  default     = 512
}

variable "memory" {
  description = "Fargate instance memory in MiB (512, 1024, 2048, etc.)"
  type        = number
  default     = 1024
}

variable "desired_count" {
  description = "Number of ECS tasks to run in the service"
  type        = number
  default     = 2
}

variable "container_image" {
  description = "Docker image URI to deploy in ECS"
  type        = string
}

variable "app_port" {
  description = "Application port inside the container"
  type        = number
  default     = 8080
}

variable "private_subnet_ids" {
  description = "List of private subnet IDs for ECS tasks"
  type        = list(string)
}

variable "security_group_ids" {
  description = "List of security group IDs for ECS tasks"
  type        = list(string)
}

variable "target_group_arn" {
  description = "ARN of the ALB target group to register tasks with"
  type        = string
}

variable "db_secret_arn" {
  description = "ARN of the Secrets Manager DB credentials secret"
  type        = string
}

variable "jwt_secret_arn" {
  description = "ARN of the Secrets Manager JWT secret"
  type        = string
}

variable "spring_profiles_active" {
  description = "Spring active profiles"
  type        = string
  default     = "prod"
}

variable "app_messaging_type" {
  description = "Messaging strategy: 'spring' (in-process) or 'kafka'"
  type        = string
  default     = "spring"
}

variable "allowed_origins" {
  description = "CORS allowed origins for the Spring Boot backend"
  type        = string
  default     = "*"
}

variable "log_retention_in_days" {
  description = "CloudWatch log retention in days"
  type        = number
  default     = 30
}

variable "enable_container_insights" {
  description = "Enable ECS CloudWatch Container Insights"
  type        = bool
  default     = false
}

variable "tags" {
  description = "Map of tags to assign to resources"
  type        = map(string)
  default     = {}
}
