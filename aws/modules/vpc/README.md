# VPC Module

This module provisions an AWS Virtual Private Cloud (VPC) with a 3-tier subnet architecture:
- **Public Subnets**: Internet-facing (ALB, NAT Gateways).
- **Private App Subnets**: Internal tier for application servers / container instances.
- **Private Database Subnets**: Isolated tier for relational databases (AWS RDS PostgreSQL).

## Resources Created

- `aws_vpc`
- `aws_internet_gateway`
- `aws_subnet` (Public)
- `aws_subnet` (Private App)
- `aws_subnet` (Private Database)


