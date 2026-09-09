# ECR Module

This module provisions an Amazon Elastic Container Registry (ECR) repository for storing Docker container images of the Spring Boot backend:
- **Vulnerability Scanning**: Automated CVE security scanning on every image push (`scan_on_push = true`).
- **Cost Optimization**: Lifecycle policy automatically purges untagged images older than 7 days and retains only the most recent N container images.
- **Encryption**: Server-side encryption enabled at rest.

## Resources Created

- `aws_ecr_repository.app`
- `aws_ecr_lifecycle_policy.app`

## Usage Example

```hcl
module "ecr" {
  source          = "../../modules/ecr"
  name_prefix     = "materia-dev"
  max_image_count = 15
  tags            = { Environment = "dev" }
}
```
