# ===================================================================
# Environment root - variables.tf (identical in staging and prod)
# ===================================================================

# --- General --------------------------------------------------------
variable "aws_region" {
  description = "AWS region of the environment."
  type        = string
  default     = "eu-west-3"
}

variable "project" {
  description = "Project name: prefixes resource names and the parameter path."
  type        = string
  default     = "materia"
}

variable "environment" {
  description = "staging or prod."
  type        = string

  validation {
    condition     = contains(["staging", "prod"], var.environment)
    error_message = "environment must be staging or prod."
  }
}

# --- Network ----------------------------------------------------------
variable "vpc_cidr" {
  description = "CIDR block of the VPC."
  type        = string
}

variable "public_subnet_cidrs" {
  description = "Public subnets; the instance goes in the first one."
  type        = list(string)
}

variable "allowed_http_cidrs" {
  description = "IPv4 ranges allowed to open the application. Restrict staging to your office/VPN if you can."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "enable_https" {
  description = "Open port 443 (once TLS is terminated on the host)."
  type        = bool
  default     = false
}

# --- Instance ---------------------------------------------------------
variable "instance_type" {
  description = "EC2 instance type."
  type        = string
}

variable "architecture" {
  description = "x86_64 or arm64; must match instance_type and the images built in CI."
  type        = string
  default     = "x86_64"
}

variable "root_volume_size" {
  description = "Root volume size in GiB."
  type        = number
  default     = 30
}

variable "data_volume_size" {
  description = "PostgreSQL data volume size in GiB."
  type        = number
  default     = 20
}

variable "swap_size_mb" {
  description = "Swap file size in MiB."
  type        = number
  default     = 2048
}

variable "detailed_monitoring" {
  description = "1-minute CloudWatch metrics."
  type        = bool
  default     = false
}

variable "enable_snapshots" {
  description = "Daily snapshots of the data volume."
  type        = bool
  default     = true
}

variable "snapshot_retention_days" {
  description = "Daily snapshots kept."
  type        = number
  default     = 7
}

# --- Images -----------------------------------------------------------
variable "ecr_keep_images" {
  description = "Images kept per repository."
  type        = number
  default     = 15
}

variable "ecr_force_delete" {
  description = "Allow terraform destroy to delete repositories that still contain images."
  type        = bool
  default     = false
}

# --- GitHub OIDC ------------------------------------------------------
variable "github_oidc_provider_arn" {
  description = "Output github_oidc_provider_arn of aws/bootstrap."
  type        = string
}

variable "github_repository" {
  description = "owner/name of the repository that deploys this environment."
  type        = string
}

variable "github_environment" {
  description = "GitHub environment of the deploy job (staging / production)."
  type        = string
}

# --- Application ------------------------------------------------------
variable "spring_profile" {
  description = "Spring profile of the backend (staging or prod)."
  type        = string
}

variable "app_url" {
  description = "Public URL of the application, e.g. https://app.example.com. Empty: http://<elastic ip>."
  type        = string
  default     = ""
}

variable "refresh_cookie_secure" {
  description = "Mark the refresh-token cookie Secure. Requires HTTPS; keep false while the site is served over plain HTTP."
  type        = bool
  default     = false
}

variable "contact_email" {
  description = "Contact e-mail shown in the API documentation."
  type        = string
  default     = "support@example.com"
}

variable "app_config" {
  description = "Extra or overriding non-secret backend settings (environment variable => value), e.g. SPRING_MAIL_HOST."
  type        = map(string)
  default     = {}
}

variable "extra_secrets" {
  description = "Extra secrets (environment variable => value), e.g. SPRING_MAIL_PASSWORD, EXCHANGE_API_KEY. Stored as SecureString."
  type        = map(string)
  default     = {}
  sensitive   = true
}
