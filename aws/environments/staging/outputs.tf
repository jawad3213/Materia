# ===================================================================
# Environment root - outputs.tf (identical in staging and prod)
# ===================================================================

output "app_url" {
  description = "Where the application is served."
  value       = local.app_url
}

output "instance_id" {
  description = "Application instance."
  value       = module.ec2.instance_id
}

output "public_ip" {
  description = "Elastic IP; point your DNS record here."
  value       = module.ec2.public_ip
}

output "ecr_repository_urls" {
  description = "Image repositories (backend, frontend)."
  value       = module.ecr.repository_urls
}

output "ecr_registry" {
  description = "ECR registry host."
  value       = module.ecr.registry
}

output "github_deploy_role_arn" {
  description = "Set as the AWS_DEPLOY_ROLE_ARN variable of the GitHub environment."
  value       = module.github_deploy_role.role_arn
}

output "ssm_parameter_path" {
  description = "Parameter Store path holding the application environment."
  value       = module.app_parameters.path
}

output "ssm_session_command" {
  description = "Open a shell on the instance (no SSH needed)."
  value       = "aws ssm start-session --region ${var.aws_region} --target ${module.ec2.instance_id}"
}

output "github_environment_variables" {
  description = "Variables to define in the GitHub environment used by the deploy workflow."
  value = {
    AWS_REGION          = var.aws_region
    AWS_DEPLOY_ROLE_ARN = module.github_deploy_role.role_arn
  }
}
