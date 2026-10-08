output "vpc_id" {
  description = "ID of the VPC."
  value       = aws_vpc.this.id
}

output "public_subnet_ids" {
  description = "IDs of the public subnets, in the order of public_subnet_cidrs."
  value       = aws_subnet.public[*].id
}
