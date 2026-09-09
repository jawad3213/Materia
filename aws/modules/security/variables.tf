# ===================================================================
# AWS Security Module - variables.tf
# ===================================================================

variable "name_prefix" {
  description = "Prefix for naming security groups (e.g. materia-staging)"
  type        = string
}

variable "vpc_id" {
  description = "The ID of the VPC"
  type        = string
}

variable "app_port" {
  description = "Port the Spring Boot container listens on (e.g. 8080)"
  type        = number
  default     = 8080
}

variable "alb_ingress_cidrs" {
  description = "CIDR blocks allowed to access the Application Load Balancer"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "tags" {
  description = "Map of tags to assign to resources"
  type        = map(string)
  default     = {}
}
