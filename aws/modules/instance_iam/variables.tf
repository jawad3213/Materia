variable "name" {
  description = "Name prefix, e.g. materia-staging."
  type        = string
}

variable "ecr_repository_arns" {
  description = "Repositories the instance may pull from."
  type        = list(string)
}

variable "parameter_path_arns" {
  description = "Parameter Store ARNs the instance may read (the path and path/*)."
  type        = list(string)
}

variable "tags" {
  description = "Extra tags."
  type        = map(string)
  default     = {}
}
