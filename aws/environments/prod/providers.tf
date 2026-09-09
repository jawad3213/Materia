# ===================================================================
# Production Environment - providers.tf
# ===================================================================

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Production S3 remote state storage with DynamoDB state locking
  # backend "s3" {
  #   bucket         = "materia-terraform-state-prod"
  #   key            = "prod/terraform.tfstate"
  #   region         = "us-east-1"
  #   dynamodb_table = "materia-terraform-locks-prod"
  #   encrypt        = true
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "Materia"
      Environment = "prod"
      ManagedBy   = "Terraform"
    }
  }
}
