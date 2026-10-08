output "instance_id" {
  description = "ID of the application instance."
  value       = aws_instance.app.id
}

output "instance_arn" {
  description = "ARN of the application instance (scopes the SSM deploy permission)."
  value       = aws_instance.app.arn
}

output "public_ip" {
  description = "Elastic IP of the instance."
  value       = aws_eip.app.public_ip
}

output "public_dns" {
  description = "Public DNS name of the Elastic IP."
  value       = aws_eip.app.public_dns
}

output "data_volume_id" {
  description = "EBS volume holding the PostgreSQL data."
  value       = aws_ebs_volume.data.id
}
