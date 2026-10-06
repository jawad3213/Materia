# ===================================================================
# Bootstrap - outputs.tf
# ===================================================================

output "state_bucket_name" {
  description = "Put this in environments/<env>/backend.hcl as `bucket`."
  value       = aws_s3_bucket.state.bucket
}

output "github_oidc_provider_arn" {
  description = "Put this in environments/<env>/terraform.tfvars as `github_oidc_provider_arn`."
  value       = var.create_github_oidc_provider ? aws_iam_openid_connect_provider.github[0].arn : data.aws_iam_openid_connect_provider.github[0].arn
}
