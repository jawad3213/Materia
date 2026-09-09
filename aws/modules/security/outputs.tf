# ===================================================================
# AWS Security Module - outputs.tf
# ===================================================================

output "alb_security_group_id" {
  description = "ID of the Application Load Balancer security group"
  value       = aws_security_group.alb.id
}

output "ecs_security_group_id" {
  description = "ID of the ECS Fargate tasks security group"
  value       = aws_security_group.ecs_tasks.id
}

output "database_security_group_id" {
  description = "ID of the RDS PostgreSQL database security group"
  value       = aws_security_group.database.id
}
