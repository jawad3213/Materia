# ===================================================================
# AWS Secrets Manager Module - variables.tf
# ===================================================================

variable "name_prefix" {
  description = "Prefix for naming resources (e.g. materia-dev)"
  type        = string
}

variable "db_username" {
  description = "Database master username"
  type        = string
}

variable "db_password" {
  description = "Database master password"
  type        = string
  sensitive   = true
}

variable "db_name" {
  description = "Database name"
  type        = string
}

variable "db_host" {
  description = "Database host address"
  type        = string
}

variable "db_port" {
  description = "Database port number"
  type        = number
  default     = 5432
}

variable "jwt_access_secret" {
  description = "JWT Access Token Secret Key"
  type        = string
  sensitive   = true
}

variable "jwt_refresh_secret" {
  description = "JWT Refresh Token Secret Key"
  type        = string
  sensitive   = true
}

variable "recovery_window_in_days" {
  description = "Number of days AWS Secrets Manager waits before deleting a secret (0 allows immediate deletion on terraform destroy)"
  type        = number
  default     = 0
}

variable "tags" {
  description = "Map of tags to assign to resources"
  type        = map(string)
  default     = {}
}
