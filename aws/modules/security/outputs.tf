output "app_security_group_id" {
  description = "Security group of the application instance."
  value       = aws_security_group.app.id
}
