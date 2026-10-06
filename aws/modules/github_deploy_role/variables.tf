variable "name" {
  description = "Name prefix, e.g. materia-staging."
  type        = string
}

variable "oidc_provider_arn" {
  description = "ARN of the token.actions.githubusercontent.com provider (output of aws/bootstrap)."
  type        = string
}

variable "github_repository" {
  description = "Repository allowed to assume the role, as owner/name."
  type        = string

  validation {
    condition     = can(regex("^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$", var.github_repository))
    error_message = "github_repository must look like owner/name."
  }
}

variable "github_environment" {
  description = "GitHub environment the deploy job runs in (the job must declare `environment:`)."
  type        = string
}

variable "ecr_repository_arns" {
  description = "Repositories the workflow may push to."
  type        = list(string)
}

variable "instance_arn" {
  description = "Instance the workflow may run the deploy command on."
  type        = string
}

variable "tags" {
  description = "Extra tags."
  type        = map(string)
  default     = {}
}
