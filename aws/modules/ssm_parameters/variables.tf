variable "path" {
  description = "Parameter path without a trailing slash, e.g. /materia/staging/app."
  type        = string

  validation {
    condition     = startswith(var.path, "/") && !endswith(var.path, "/")
    error_message = "path must start with / and must not end with /."
  }
}

variable "parameters" {
  description = "Non-secret settings: environment variable name => value."
  type        = map(string)
  default     = {}
}

variable "secure_parameters" {
  description = "Secrets: environment variable name => value. Stored as SecureString."
  type        = map(string)
  default     = {}
  sensitive   = true
}

variable "tags" {
  description = "Extra tags."
  type        = map(string)
  default     = {}
}
