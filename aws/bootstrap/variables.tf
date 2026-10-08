# ===================================================================
# Bootstrap - variables.tf
# ===================================================================

variable "aws_region" {
  description = "AWS region for the state bucket."
  type        = string
  default     = "eu-west-3"
}

variable "project" {
  description = "Project name, used in tags."
  type        = string
  default     = "materia"
}

variable "state_bucket_name" {
  description = "Globally unique name of the S3 bucket that stores the staging and prod Terraform state."
  type        = string
}

variable "state_version_retention_days" {
  description = "How long old versions of a state file are kept."
  type        = number
  default     = 90
}

variable "create_github_oidc_provider" {
  description = "Create the GitHub Actions OIDC provider. Set to false if the account already has one; it is then looked up."
  type        = bool
  default     = true
}
