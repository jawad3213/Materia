output "instance_profile_name" {
  description = "Instance profile to attach to the application instance."
  value       = aws_iam_instance_profile.instance.name
}

output "role_arn" {
  description = "ARN of the instance role."
  value       = aws_iam_role.instance.arn
}
