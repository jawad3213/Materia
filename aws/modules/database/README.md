# Database Module

This module provisions a fully-managed AWS RDS PostgreSQL 16 database for the Materia backend:
- **Engine**: PostgreSQL 16.1 with automated minor version upgrades.
- **Storage**: Amazon EBS gp3 with storage auto-scaling up to `max_allocated_storage`.
- **Security**: AWS KMS storage encryption at rest, deployed securely inside the isolated private database subnet group.
- **High Availability**: Multi-AZ standby replica configurable via `multi_az` boolean.

## Resources Created

- `aws_db_instance.postgres`

## Usage Example

```hcl
module "database" {
  source                 = "../../modules/database"
  name_prefix            = "materia-dev"
  db_name                = "materia"
  db_username            = "postgres"
  db_password            = var.db_password
  db_subnet_group_name   = module.networking.db_subnet_group_name
  vpc_security_group_ids = [module.security.database_security_group_id]
  instance_class         = "db.t4g.micro"
  multi_az               = false
  skip_final_snapshot    = true
  tags                   = { Environment = "dev" }
}
```
