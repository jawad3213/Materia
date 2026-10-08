# ===================================================================
# Bootstrap - providers.tf
# One-time, account-level resources. This root keeps its state locally
# (it creates the bucket that every other root stores its state in).
# ===================================================================

terraform {
  required_version = ">= 1.10.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project   = var.project
      ManagedBy = "Terraform"
      Stack     = "bootstrap"
    }
  }
}
