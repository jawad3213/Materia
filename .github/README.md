# Materia CI/CD Workflows (GitHub Actions)

This directory contains automated continuous integration and deployment pipelines for the **Materia** application using GitHub Actions and AWS keyless OIDC authentication.

---

## 📋 Available Workflows

| Workflow | File | Trigger | Target Environment | Actions Performed |
| :--- | :--- | :--- | :--- | :--- |
| **Deploy Staging** | [deploy-staging.yml](file:///.github/workflows/deploy-staging.yml) | Push to `staging`, `develop`, or Manual dispatch | Staging | • Build JAR with Maven & JDK 21<br>• Push Docker image to ECR<br>• Deploy to ECS Fargate<br>• Build React bundle with Vite<br>• Sync to S3 & invalidate CloudFront |
| **Deploy Production** | [deploy-prod.yml](file:///.github/workflows/deploy-prod.yml) | Push to `main` or Manual dispatch | Production | • Build JAR with Maven & JDK 21<br>• Push Docker image to ECR<br>• Deploy to ECS Fargate<br>• Build React bundle with Vite<br>• Sync to S3 & invalidate CloudFront |
| **Terraform CI** | [terraform-ci.yml](file:///.github/workflows/terraform-ci.yml) | PRs modifying `aws/**` | Staging & Prod | • `terraform fmt -check`<br>• `terraform validate` across staging & prod |

---

## 🔐 Required GitHub Repository Secrets

Navigate to your GitHub repository: **Settings → Secrets and variables → Actions**, and add the following repository secrets:

| Secret Name | Description | Where to Find in Terraform |
| :--- | :--- | :--- |
| `AWS_ROLE_TO_ASSUME` | ARN of the IAM Role created by `modules/iam_github_actions` | Terraform output: `github_actions_role_arn` |
| `STAGING_FRONTEND_S3_BUCKET` | *(Optional)* Name of the staging S3 bucket | Default: `materia-staging-frontend` |
| `STAGING_CLOUDFRONT_DISTRIBUTION_ID` | CloudFront Distribution ID for staging | AWS CloudFront Console |
| `STAGING_VITE_API_BASE_URL` | *(Optional)* Staging backend API URL for Vite build | Default: `https://api-staging.yourdomain.com/api/v1` |
| `PROD_FRONTEND_S3_BUCKET` | *(Optional)* Name of the production S3 bucket | Default: `materia-prod-frontend` |
| `PROD_CLOUDFRONT_DISTRIBUTION_ID` | CloudFront Distribution ID for production | AWS CloudFront Console |
| `PROD_VITE_API_BASE_URL` | *(Optional)* Production backend API URL for Vite build | Default: `https://api.yourdomain.com/api/v1` |

> [!TIP]
> **No AWS Access Keys Needed**: Because we use AWS IAM OpenID Connect (OIDC), you do **not** need to store permanent `AWS_ACCESS_KEY_ID` or `AWS_SECRET_ACCESS_KEY` credentials in GitHub. The workflow requests a short-lived token from AWS Security Token Service (STS) securely.

---

## 🚀 How Deployments Work

### Automatic Trigger
- When you push to the **`staging`** branch, the `deploy-staging.yml` workflow automatically runs.
- When you merge/push to the **`main`** branch, the `deploy-prod.yml` workflow automatically runs.

### Manual Trigger
You can deploy manually at any time from GitHub:
1. Go to the **Actions** tab in your GitHub repository.
2. Select **Deploy to Staging** or **Deploy to Production**.
3. Click **Run workflow**, choose the branch, and select whether to deploy Backend, Frontend, or both.
