# ===================================================================
# AWS Secrets Manager Module - main.tf
# ===================================================================

# -------------------------------------------------------------------
# Database Credentials Secret
# -------------------------------------------------------------------
resource "aws_secretsmanager_secret" "db_credentials" {
  name                    = "${var.name_prefix}-db-credentials"
  description             = "Database master credentials for ${var.name_prefix}"
  recovery_window_in_days = var.recovery_window_in_days

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-db-credentials"
  })
}

resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = var.db_username
    password = var.db_password
    database = var.db_name
    host     = var.db_host
    port     = var.db_port
    url      = "jdbc:postgresql://${var.db_host}:${var.db_port}/${var.db_name}"
  })
}

# -------------------------------------------------------------------
# Application JWT Secrets
# -------------------------------------------------------------------
resource "aws_secretsmanager_secret" "jwt_secrets" {
  name                    = "${var.name_prefix}-jwt-secrets"
  description             = "JWT signing secrets for ${var.name_prefix}"
  recovery_window_in_days = var.recovery_window_in_days

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-jwt-secrets"
  })
}

resource "aws_secretsmanager_secret_version" "jwt_secrets" {
  secret_id = aws_secretsmanager_secret.jwt_secrets.id
  secret_string = jsonencode({
    jwt_access_secret  = var.jwt_access_secret
    jwt_refresh_secret = var.jwt_refresh_secret
  })
}
