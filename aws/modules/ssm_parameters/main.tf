# ===================================================================
# Module: ssm_parameters
# The application's environment, stored in SSM Parameter Store under
# one path (e.g. /materia/staging/app/<VARIABLE>). Plain settings are
# String parameters, secrets are SecureString (encrypted with the
# aws/ssm KMS key). The instance reads the whole path at deploy time
# and writes it to the docker compose .env file.
# ===================================================================

data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

resource "aws_ssm_parameter" "plain" {
  for_each = var.parameters

  name        = "${var.path}/${each.key}"
  description = "Materia setting ${each.key}"
  type        = "String"
  tier        = "Standard"
  value       = each.value

  tags = var.tags
}

resource "aws_ssm_parameter" "secure" {
  # Keys are not secret, only values; nonsensitive() lets them drive for_each.
  for_each = nonsensitive(toset(keys(var.secure_parameters)))

  name        = "${var.path}/${each.key}"
  description = "Materia secret ${each.key}"
  type        = "SecureString"
  tier        = "Standard"
  value       = var.secure_parameters[each.key]

  tags = var.tags
}
