# ===================================================================
# AWS Secrets Manager Module - outputs.tf
# ===================================================================

output "db_secret_arn" {
  description = "ARN of the Database credentials secret"
  value       = aws_secretsmanager_secret.db_credentials.arn
}

output "db_secret_name" {
  description = "Name of the Database credentials secret"
  value       = aws_secretsmanager_secret.db_credentials.name
}

output "jwt_secret_arn" {
  description = "ARN of the JWT credentials secret"
  value       = aws_secretsmanager_secret.jwt_secrets.arn
}

output "jwt_secret_name" {
  description = "Name of the JWT credentials secret"
  value       = aws_secretsmanager_secret.jwt_secrets.name
}
