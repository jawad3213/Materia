# Load Balancer Module

This module deploys an AWS Application Load Balancer (ALB) to route public traffic to the backend application instances:
- **Public Entrypoint**: Deployed across public subnets with an assigned Security Group.
- **Health Checks**: Actively verifies instances via HTTP health checks (e.g. `/actuator/health` or `/`).
- **Target Group**: Forwards traffic to EC2 Auto Scaling Group instances with 30-second deregistration delay for zero-downtime rolling updates.

## Resources Created

- `aws_lb.main`
- `aws_lb_target_group.app`
- `aws_lb_listener.http`

## Usage Example

```hcl
module "load_balancer" {
  source             = "../../modules/load_balancer"
  name_prefix        = "materia-dev"
  vpc_id             = module.vpc.vpc_id
  public_subnet_ids  = module.vpc.public_subnet_ids
  security_group_ids = [module.security.alb_security_group_id]
  app_port           = 8080
  health_check_path  = "/actuator/health"
  tags               = { Environment = "dev" }
}
```
