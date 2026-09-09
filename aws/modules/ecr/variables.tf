# ===================================================================
# AWS ECR Module - variables.tf
# ===================================================================

variable "name_prefix" {
  description = "Prefix for naming resources (e.g. materia-dev)"
  type        = string
}

variable "image_tag_mutability" {
  description = "Tag mutability setting for repository (MUTABLE or IMMUTABLE)"
  type        = string
  default     = "MUTABLE"
}

variable "scan_on_push" {
  description = "Indicates whether images are scanned after being pushed to repository"
  type        = bool
  default     = true
}

variable "max_image_count" {
  description = "Number of tagged images to keep in ECR lifecycle policy"
  type        = number
  default     = 15
}

variable "tags" {
  description = "Map of tags to assign to resources"
  type        = map(string)
  default     = {}
}
