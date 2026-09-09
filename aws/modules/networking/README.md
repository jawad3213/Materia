# Networking Module

This module manages routing, NAT Gateways, Elastic IPs, and database subnet grouping for the VPC architecture:
- **Public Routing**: Direct internet access via the Internet Gateway.
- **Private App Routing**: Configurable egress via AWS NAT Gateway (`single_nat_gateway = true` for dev/staging cost savings, `false` for multi-AZ production redundancy).
- **Database Routing & Subnet Group**: Isolated routing with no direct internet access, creating the `aws_db_subnet_group` required by Amazon RDS.

## Resources Created

- `aws_eip` (NAT allocation)
- `aws_nat_gateway`
- `aws_route_table` (Public, Private App, Private Database)
- `aws_route_table_association`
- `aws_db_subnet_group`

## Usage Example

```hcl
module "networking" {
  source                 = "../../modules/networking"
  name_prefix            = "materia-dev"
  vpc_id                 = module.vpc.vpc_id
  internet_gateway_id    = module.vpc.internet_gateway_id
  public_subnet_ids      = module.vpc.public_subnet_ids
  private_app_subnet_ids = module.vpc.private_app_subnet_ids
  private_db_subnet_ids  = module.vpc.private_db_subnet_ids
  enable_nat_gateway     = true
  single_nat_gateway     = true
  tags                   = { Environment = "dev" }
}
```
