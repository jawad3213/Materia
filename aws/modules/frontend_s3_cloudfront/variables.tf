# ===================================================================
# AWS Frontend Module - variables.tf
# ===================================================================

variable "name_prefix" {
  description = "Prefix for naming resources (e.g. materia-dev)"
  type        = string
}

variable "custom_domain_aliases" {
  description = "List of custom domain names (CNAMEs) for the CloudFront distribution"
  type        = list(string)
  default     = []
}

variable "certificate_arn" {
  description = "ACM Certificate ARN for custom domain (must be in us-east-1 for CloudFront)"
  type        = string
  default     = null
}

variable "price_class" {
  description = "CloudFront price class (PriceClass_100, PriceClass_200, PriceClass_All)"
  type        = string
  default     = "PriceClass_100"
}

variable "force_destroy" {
  description = "Whether to allow bucket deletion even if non-empty on terraform destroy"
  type        = bool
  default     = true
}

variable "tags" {
  description = "Map of tags to assign to resources"
  type        = map(string)
  default     = {}
}
