# ===================================================================
# AWS IAM GitHub Actions OIDC Module - variables.tf
# ===================================================================

variable "name_prefix" {
  description = "Prefix for naming resources (e.g. materia-dev)"
  type        = string
}

variable "github_repo" {
  description = "GitHub repository in 'owner/repo' format (e.g. jawad3213/materia or '*')"
  type        = string
  default     = "*"
}

variable "create_oidc_provider" {
  description = "Whether to create the GitHub OIDC provider (false if already exists in AWS account)"
  type        = bool
  default     = true
}

variable "existing_oidc_provider_arn" {
  description = "Existing GitHub OIDC provider ARN if create_oidc_provider is false"
  type        = string
  default     = null
}

variable "ecr_repository_arn" {
  description = "ARN of the ECR repository allowed for image pushes"
  type        = string
  default     = null
}

variable "ecs_task_execution_role_arn" {
  description = "ARN of the ECS task execution role allowed for iam:PassRole"
  type        = string
  default     = null
}

variable "ecs_task_role_arn" {
  description = "ARN of the ECS task role allowed for iam:PassRole"
  type        = string
  default     = null
}

variable "frontend_s3_bucket_arn" {
  description = "ARN of the frontend S3 bucket for sync"
  type        = string
  default     = null
}

variable "cloudfront_distribution_arn" {
  description = "ARN of the CloudFront distribution for cache invalidation"
  type        = string
  default     = null
}

variable "tags" {
  description = "Map of tags to assign to resources"
  type        = map(string)
  default     = {}
}
