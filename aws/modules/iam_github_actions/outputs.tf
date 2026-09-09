# ===================================================================
# AWS IAM GitHub Actions OIDC Module - outputs.tf
# ===================================================================

output "role_arn" {
  description = "ARN of the IAM role to assume in GitHub Actions (aws-actions/configure-aws-credentials)"
  value       = aws_iam_role.github_actions.arn
}

output "role_name" {
  description = "Name of the IAM role for GitHub Actions"
  value       = aws_iam_role.github_actions.name
}

output "oidc_provider_arn" {
  description = "ARN of the GitHub OIDC provider"
  value       = local.oidc_provider_arn
}
