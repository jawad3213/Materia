# Security Module

This module implements AWS Security Groups applying the principle of least privilege:
- **ALB Security Group**: Accepts HTTP (80) and HTTPS (443) traffic from the public internet.
- **ECS Tasks Security Group**: Accepts inbound traffic on application port (8080) **strictly from the ALB Security Group**. Never allows direct public ingress.
- **Database Security Group**: Accepts PostgreSQL traffic on port 5432 **strictly from the ECS Tasks Security Group**. Never allows external ingress.

## Resources Created

- `aws_security_group.alb`
- `aws_security_group.ecs_tasks`
- `aws_security_group.database`

## Usage Example

```hcl
module "security" {
  source            = "../../modules/security"
  name_prefix       = "materia-staging"
  vpc_id            = module.vpc.vpc_id
  app_port          = 8080
  alb_ingress_cidrs = ["0.0.0.0/0"]
  tags              = { Environment = "staging" }
}
```
