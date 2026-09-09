# ECS Fargate Module

This module provisions a containerized compute infrastructure running the Spring Boot backend on **AWS ECS Fargate**:
- **Serverless Compute**: Fully managed container orchestration without managing EC2 instances.
- **Security**: IAM Task Execution Role fetches secrets securely from AWS Secrets Manager directly into container environment variables without exposing plaintext credentials.
- **Network Isolation**: ECS tasks run in private VPC subnets with no public IPs; inbound traffic is strictly permitted from the Application Load Balancer (ALB).
- **Zero Downtime Deployments**: Deployment circuit breaker with automatic rollback on failed container boots.
- **Logging**: Centralized CloudWatch logging with configurable log retention.

## Resources Created

- `aws_ecs_cluster.main`
- `aws_cloudwatch_log_group.app`
- `aws_iam_role.execution` & policy attachments (Secrets Manager access)
- `aws_iam_role.task`
- `aws_ecs_task_definition.app`
- `aws_ecs_service.app`

## Usage Example

```hcl
module "ecs_fargate" {
  source             = "../../modules/ecs_fargate"
  name_prefix        = "materia-dev"
  aws_region         = "eu-west-3"
  container_image    = "${module.ecr.repository_url}:latest"
  cpu                = 512
  memory             = 1024
  desired_count      = 2
  app_port           = 8080
  private_subnet_ids = module.vpc.private_subnet_ids
  security_group_ids = [module.security.ecs_security_group_id]
  target_group_arn   = module.load_balancer.target_group_arn
  db_secret_arn      = module.secrets.db_secret_arn
  jwt_secret_arn     = module.secrets.jwt_secret_arn
  tags               = { Environment = "dev" }
}
```
