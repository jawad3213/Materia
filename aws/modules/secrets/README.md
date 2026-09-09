# Secrets Manager Module

This module provisions AWS Secrets Manager secrets for secure credential storage:
- **Database Credentials**: Stores PostgreSQL `username`, `password`, `database`, `host`, `port`, and `url` as a JSON secret.
- **JWT Secrets**: Stores `jwt_access_secret` and `jwt_refresh_secret` as a JSON secret.
- **ECS Integration**: Secret ARNs are securely injected into ECS Fargate task definitions without exposing raw passwords in plain text.

## Resources Created

- `aws_secretsmanager_secret.db_credentials`
- `aws_secretsmanager_secret_version.db_credentials`
- `aws_secretsmanager_secret.jwt_secrets`
- `aws_secretsmanager_secret_version.jwt_secrets`

## Usage Example

```hcl
module "secrets" {
  source             = "../../modules/secrets"
  name_prefix        = "materia-dev"
  db_username        = "postgres"
  db_password        = var.db_password
  db_name            = "materia"
  db_host            = module.database.db_instance_address
  db_port            = module.database.db_instance_port
  jwt_access_secret  = var.jwt_access_secret
  jwt_refresh_secret = var.jwt_refresh_secret
  tags               = { Environment = "dev" }
}
```
