# ===================================================================
# AWS Load Balancer Module - variables.tf
# ===================================================================

variable "name_prefix" {
  description = "Prefix for naming resources (e.g. materia-dev)"
  type        = string
}

variable "vpc_id" {
  description = "The ID of the VPC"
  type        = string
}

variable "public_subnet_ids" {
  description = "List of public subnet IDs for the ALB"
  type        = list(string)
}

variable "security_group_ids" {
  description = "List of security group IDs for the ALB"
  type        = list(string)
}

variable "app_port" {
  description = "Backend application port for target group forwarding"
  type        = number
  default     = 8080
}

variable "health_check_path" {
  description = "Path for ALB health checks"
  type        = string
  default     = "/actuator/health"
}

variable "target_type" {
  description = "Target type for the target group (ip for Fargate, instance for EC2)"
  type        = string
  default     = "ip"
}

variable "certificate_arn" {
  description = "ARN of ACM SSL certificate for HTTPS listener (optional)"
  type        = string
  default     = null
}

variable "enable_deletion_protection" {
  description = "Protect the ALB against accidental deletion"
  type        = bool
  default     = false
}

variable "tags" {
  description = "Map of tags to assign to resources"
  type        = map(string)
  default     = {}
}
