# IAM GitHub Actions OIDC Module

This module provisions keyless AWS authentication for GitHub Actions CI/CD using **OpenID Connect (OIDC)**:
- **No Long-Lived Credentials**: Eliminates hardcoded `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` secrets from GitHub.
- **Least-Privilege Role**: Grants scoped permissions to:
  - Authenticate and push Docker images to Amazon ECR.
  - Deploy new revisions and update services on Amazon ECS Fargate.
  - Sync React frontend build artifacts to Amazon S3.
  - Invalidate Amazon CloudFront CDN edge caches.
- **Repository Bound**: Only GitHub Actions workflows executing from your specific repository can assume this role.

## Resources Created

- `aws_iam_openid_connect_provider.github` (Conditional)
- `aws_iam_role.github_actions`
- `aws_iam_policy.deploy`
- `aws_iam_role_policy_attachment.deploy`

## Usage in GitHub Actions Workflow

```yaml
- name: Configure AWS Credentials via OIDC
  uses: aws-actions/configure-aws-credentials@v4
  with:
    role-to-assume: ${{ secrets.AWS_ROLE_TO_ASSUME }} # Output: module.iam_github_actions.role_arn
    aws-region: eu-west-3
```
