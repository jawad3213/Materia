# ===================================================================
# AWS Database Module - outputs.tf
# ===================================================================

output "db_instance_id" {
  description = "The RDS instance ID"
  value       = aws_db_instance.postgres.id
}

output "db_instance_arn" {
  description = "The ARN of the RDS instance"
  value       = aws_db_instance.postgres.arn
}

output "db_instance_endpoint" {
  description = "Connection endpoint in host:port format"
  value       = aws_db_instance.postgres.endpoint
}

output "db_instance_address" {
  description = "Host address of the database"
  value       = aws_db_instance.postgres.address
}

output "db_instance_port" {
  description = "Port number of the database"
  value       = aws_db_instance.postgres.port
}

output "db_name" {
  description = "Database name"
  value       = aws_db_instance.postgres.db_name
}

output "db_username" {
  description = "Master database username"
  value       = aws_db_instance.postgres.username
}
