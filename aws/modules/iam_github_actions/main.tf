# ===================================================================
# AWS IAM GitHub Actions OIDC Module - main.tf
# ===================================================================

# -------------------------------------------------------------------
# GitHub OIDC Provider (Conditionally created if not already in AWS account)
# -------------------------------------------------------------------
data "aws_caller_identity" "current" {}

resource "aws_iam_openid_connect_provider" "github" {
  count = var.create_oidc_provider ? 1 : 0

  url            = "https://token.actions.githubusercontent.com"
  client_id_list = ["sts.amazonaws.com"]
  thumbprint_list = [
    "6938fd4d98bab03faadb97b34396831e3780aea1",
    "1c58a3a8518e8759bf075b76b750d4f8d264494f"
  ]

  tags = merge(var.tags, {
    Name = "github-actions-oidc"
  })
}

locals {
  account_id        = data.aws_caller_identity.current.account_id
  default_oidc_arn  = "arn:aws:iam://${local.account_id}:oidc-provider/token.actions.githubusercontent.com"
  oidc_provider_arn = var.create_oidc_provider ? aws_iam_openid_connect_provider.github[0].arn : coalesce(var.existing_oidc_provider_arn, local.default_oidc_arn)
}

# -------------------------------------------------------------------
# IAM Role for GitHub Actions (Assumed via OIDC - No static AWS keys needed!)
# -------------------------------------------------------------------
resource "aws_iam_role" "github_actions" {
  name = "${var.name_prefix}-github-actions-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = local.oidc_provider_arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringLike = {
            "token.actions.githubusercontent.com:sub" = "repo:${var.github_repo}:*"
          }
          StringEquals = {
            "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
          }
        }
      }
    ]
  })

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-github-actions-role"
  })
}

# -------------------------------------------------------------------
# CI/CD Deployment Policy (ECR push, ECS service update, S3 + CloudFront)
# -------------------------------------------------------------------
resource "aws_iam_policy" "deploy" {
  name        = "${var.name_prefix}-github-actions-deploy-policy"
  description = "Allows GitHub Actions CI/CD to push to ECR, update ECS, and deploy frontend to S3/CloudFront"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      # ECR Authentication Token
      {
        Sid    = "ECRAuthToken"
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken"
        ]
        Resource = "*"
      },
      # ECR Image Push/Pull
      {
        Sid    = "ECRPushPull"
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload"
        ]
        Resource = var.ecr_repository_arn != null ? var.ecr_repository_arn : "*"
      },
      # ECS Deployment
      {
        Sid    = "ECSDeployment"
        Effect = "Allow"
        Action = [
          "ecs:DescribeServices",
          "ecs:UpdateService",
          "ecs:DescribeTaskDefinition",
          "ecs:RegisterTaskDefinition"
        ]
        Resource = "*"
      },
      # IAM PassRole for ECS Task Execution
      {
        Sid    = "IAMPassRole"
        Effect = "Allow"
        Action = "iam:PassRole"
        Resource = [
          var.ecs_task_execution_role_arn != null ? var.ecs_task_execution_role_arn : "*",
          var.ecs_task_role_arn != null ? var.ecs_task_role_arn : "*"
        ]
      },
      # S3 Frontend Upload
      {
        Sid    = "S3FrontendDeploy"
        Effect = "Allow"
        Action = [
          "s3:PutObject",
          "s3:GetObject",
          "s3:ListBucket",
          "s3:DeleteObject"
        ]
        Resource = [
          var.frontend_s3_bucket_arn != null ? var.frontend_s3_bucket_arn : "*",
          var.frontend_s3_bucket_arn != null ? "${var.frontend_s3_bucket_arn}/*" : "*"
        ]
      },
      # CloudFront Cache Invalidation
      {
        Sid    = "CloudFrontInvalidation"
        Effect = "Allow"
        Action = [
          "cloudfront:CreateInvalidation",
          "cloudfront:GetInvalidation"
        ]
        Resource = var.cloudfront_distribution_arn != null ? var.cloudfront_distribution_arn : "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "deploy" {
  role       = aws_iam_role.github_actions.name
  policy_arn = aws_iam_policy.deploy.arn
}
