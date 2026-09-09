# ===================================================================
# Staging Environment - providers.tf
# ===================================================================

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Optional: Configure S3 remote backend for Staging
  # backend "s3" {
  #   bucket         = "materia-terraform-state-staging"
  #   key            = "staging/terraform.tfstate"
  #   region         = "us-east-1"
  #   dynamodb_table = "materia-terraform-locks-staging"
  #   encrypt        = true
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "Materia"
      Environment = "staging"
      ManagedBy   = "Terraform"
    }
  }
}
