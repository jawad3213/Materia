variable "name" {
  description = "Name prefix, e.g. materia-staging."
  type        = string
}

variable "instance_type" {
  description = "EC2 instance type. The JVM, PostgreSQL and nginx run on it; 4 GB of RAM is comfortable."
  type        = string
}

variable "architecture" {
  description = "x86_64 (t3, m6i...) or arm64 (t4g, m7g...). Must match the instance type and the images built in CI."
  type        = string
  default     = "x86_64"

  validation {
    condition     = contains(["x86_64", "arm64"], var.architecture)
    error_message = "architecture must be x86_64 or arm64."
  }
}

variable "subnet_id" {
  description = "Public subnet of the instance."
  type        = string
}

variable "security_group_ids" {
  description = "Security groups of the instance."
  type        = list(string)
}

variable "instance_profile_name" {
  description = "IAM instance profile (SSM, ECR pull, parameter read)."
  type        = string
}

variable "root_volume_size" {
  description = "Root volume size in GiB (OS, Docker images, logs)."
  type        = number
  default     = 30
}

variable "data_volume_size" {
  description = "Data volume size in GiB (PostgreSQL)."
  type        = number
  default     = 20
}

variable "swap_size_mb" {
  description = "Swap file size in MiB; 0 disables it."
  type        = number
  default     = 2048
}

variable "compose_version" {
  description = "Docker Compose plugin version installed at boot."
  type        = string
  default     = "2.29.7"
}

variable "app_dir" {
  description = "Directory holding docker-compose.yml, .env and deploy.sh on the host."
  type        = string
  default     = "/opt/materia"
}

variable "detailed_monitoring" {
  description = "1-minute CloudWatch metrics (billed)."
  type        = bool
  default     = false
}

variable "enable_snapshots" {
  description = "Daily snapshots of the data volume."
  type        = bool
  default     = true
}

variable "snapshot_retention_days" {
  description = "How many daily snapshots to keep."
  type        = number
  default     = 7
}

variable "snapshot_time_utc" {
  description = "Time of the daily snapshot (HH:MM, UTC)."
  type        = string
  default     = "02:00"
}

variable "tags" {
  description = "Extra tags. Project and Environment are used by the deploy workflow to find the instance."
  type        = map(string)
  default     = {}
}
