# ===================================================================
# Staging Environment - outputs.tf
# ===================================================================

# Presentation Layer
output "cloudfront_domain_name" {
  description = "CloudFront CDN domain name for Frontend React SPA (point Hostinger CNAME here)"
  value       = module.frontend_s3_cloudfront.cloudfront_domain_name
}

output "frontend_s3_bucket" {
  description = "S3 bucket for frontend build artifacts"
  value       = module.frontend_s3_cloudfront.s3_bucket_id
}

# Routing & Load Balancer Layer
output "alb_dns_name" {
  description = "Application Load Balancer DNS name (point Hostinger API CNAME here)"
  value       = module.load_balancer.alb_dns_name
}

# Compute Layer
output "ecs_cluster_name" {
  description = "ECS Cluster Name"
  value       = module.ecs_fargate.cluster_name
}

output "ecs_service_name" {
  description = "ECS Service Name"
  value       = module.ecs_fargate.service_name
}

# Container Registry
output "ecr_repository_url" {
  description = "Amazon ECR Repository URL"
  value       = module.ecr.repository_url
}

# CI/CD Keyless Deploy
output "github_actions_role_arn" {
  description = "IAM Role ARN to assume in GitHub Actions (AWS_ROLE_TO_ASSUME)"
  value       = module.iam_github_actions.role_arn
}

# Data Layer
output "database_endpoint" {
  description = "RDS PostgreSQL master endpoint"
  value       = module.database.db_instance_endpoint
}
