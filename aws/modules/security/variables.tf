variable "name" {
  description = "Name prefix, e.g. materia-staging."
  type        = string
}

variable "vpc_id" {
  description = "VPC of the security group."
  type        = string
}

variable "allowed_http_cidrs" {
  description = "IPv4 ranges allowed to reach the application on port 80 (and 443 when HTTPS is enabled)."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "enable_https" {
  description = "Also open port 443."
  type        = bool
  default     = false
}

variable "tags" {
  description = "Extra tags."
  type        = map(string)
  default     = {}
}
