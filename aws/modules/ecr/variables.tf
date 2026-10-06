variable "name" {
  description = "Repository name prefix, e.g. materia-staging (gives materia-staging-backend)."
  type        = string
}

variable "repositories" {
  description = "Image names."
  type        = list(string)
  default     = ["backend", "frontend"]
}

variable "keep_images" {
  description = "How many images to keep per repository (enough to roll back)."
  type        = number
  default     = 15
}

variable "force_delete" {
  description = "Allow destroying a repository that still holds images."
  type        = bool
  default     = false
}

variable "tags" {
  description = "Extra tags."
  type        = map(string)
  default     = {}
}
