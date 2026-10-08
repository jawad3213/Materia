output "repository_urls" {
  description = "Repository URL per image name."
  value       = { for k, repo in aws_ecr_repository.this : k => repo.repository_url }
}

output "repository_arns" {
  description = "Repository ARNs, for IAM policies."
  value       = [for repo in aws_ecr_repository.this : repo.arn]
}

output "registry" {
  description = "Registry host (<account>.dkr.ecr.<region>.amazonaws.com)."
  value       = split("/", values(aws_ecr_repository.this)[0].repository_url)[0]
}
