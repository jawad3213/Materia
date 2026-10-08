# ===================================================================
# Environment root - main.tf (identical in staging and prod; the
# differences live in terraform.tfvars and the backend key)
#
#   GitHub Actions --OIDC--> deploy role --ECR push--> images
#                                       \--SSM Run Command--> EC2
#   EC2 (docker compose: frontend nginx :80 -> backend -> postgres)
#     reads its .env from Parameter Store /<project>/<env>/app/*
# ===================================================================

locals {
  name = "${var.project}-${var.environment}"

  tags = {
    Project     = var.project
    Environment = var.environment
  }

  app_url = var.app_url != "" ? trimsuffix(var.app_url, "/") : "http://${module.ec2.public_ip}"

  # Settings the backend needs and that have no default in application.properties.
  # Anything in var.app_config overrides these.
  default_app_config = {
    SPRING_PROFILES_ACTIVE                  = var.spring_profile
    POSTGRES_DB                             = "materia"
    POSTGRES_USER                           = "materia"
    SPRING_DATASOURCE_DRIVER_CLASS_NAME     = "org.postgresql.Driver"
    SPRING_JPA_HIBERNATE_DIALECT            = "org.hibernate.dialect.PostgreSQLDialect"
    SPRING_JPA_DEFAULT_SCHEMA               = "public"
    SPRING_FLYWAY_DEFAULT_SCHEMA            = "public"
    SPRING_JPA_BATCH_SIZE                   = "50"
    SPRING_JPA_ORDER_INSERTS                = "true"
    SPRING_JPA_ORDER_UPDATES                = "true"
    APP_MESSAGING_TYPE                      = "spring"
    SPRING_KAFKA_CONSUMER_GROUP_ID          = "materia-${var.environment}"
    SPRING_KAFKA_CONSUMER_AUTO_OFFSET_RESET = "earliest"
    SPRING_KAFKA_LISTENER_AUTO_STARTUP      = "false"
    JWT_EXPIRATION_MS                       = "900000"
    JWT_REFRESH_EXPIRATION_MS               = "604800000"
    SPRINGDOC_API_DOCS_PATH                 = "/v3/api-docs"
    SPRINGDOC_SWAGGER_UI_PATH               = "/swagger-ui.html"
    SPRINGDOC_INFO_TITLE                    = "Materia API"
    SPRINGDOC_INFO_DESCRIPTION              = "Materia procure-to-pay API (${var.environment})"
    SPRINGDOC_INFO_VERSION                  = "1.0.0"
    SPRINGDOC_INFO_CONTACT_NAME             = "Materia"
    SPRINGDOC_INFO_CONTACT_EMAIL            = var.contact_email
    APP_CORS_ALLOWED_ORIGINS                = local.app_url
    APP_LOGIN_URL                           = "${local.app_url}/login"
    APP_RESET_PASSWORD_URL                  = "${local.app_url}/reset-password"
    APP_AUTH_REFRESH_COOKIE_SECURE          = tostring(var.refresh_cookie_secure)
    JAVA_TOOL_OPTIONS                       = "-XX:MaxRAMPercentage=60"
  }
}

# -------------------------------------------------------------------
# Network and firewall
# -------------------------------------------------------------------
module "network" {
  source = "../../modules/network"

  name                = local.name
  vpc_cidr            = var.vpc_cidr
  public_subnet_cidrs = var.public_subnet_cidrs
  tags                = local.tags
}

module "security" {
  source = "../../modules/security"

  name               = local.name
  vpc_id             = module.network.vpc_id
  allowed_http_cidrs = var.allowed_http_cidrs
  enable_https       = var.enable_https
  tags               = local.tags
}

# -------------------------------------------------------------------
# Images
# -------------------------------------------------------------------
module "ecr" {
  source = "../../modules/ecr"

  name         = local.name
  keep_images  = var.ecr_keep_images
  force_delete = var.ecr_force_delete
  tags         = local.tags
}

# -------------------------------------------------------------------
# Application configuration and secrets (Parameter Store)
# -------------------------------------------------------------------
resource "random_password" "postgres" {
  length  = 32
  special = false # the value goes through a docker compose .env file
}

# The backend base64-decodes its JWT keys (HMAC-SHA, 512 bits).
resource "random_bytes" "jwt_access" {
  length = 64
}

resource "random_bytes" "jwt_refresh" {
  length = 64
}

module "app_parameters" {
  source = "../../modules/ssm_parameters"

  path       = "/${var.project}/${var.environment}/app"
  parameters = merge(local.default_app_config, var.app_config)
  secure_parameters = merge(
    {
      POSTGRES_PASSWORD  = random_password.postgres.result
      JWT_ACCESS_SECRET  = random_bytes.jwt_access.base64
      JWT_REFRESH_SECRET = random_bytes.jwt_refresh.base64
    },
    var.extra_secrets,
  )
  tags = local.tags
}

# -------------------------------------------------------------------
# Application host
# -------------------------------------------------------------------
module "instance_iam" {
  source = "../../modules/instance_iam"

  name                = local.name
  ecr_repository_arns = module.ecr.repository_arns
  parameter_path_arns = module.app_parameters.path_arns
  tags                = local.tags
}

module "ec2" {
  source = "../../modules/ec2"

  name                    = local.name
  instance_type           = var.instance_type
  architecture            = var.architecture
  subnet_id               = module.network.public_subnet_ids[0]
  security_group_ids      = [module.security.app_security_group_id]
  instance_profile_name   = module.instance_iam.instance_profile_name
  root_volume_size        = var.root_volume_size
  data_volume_size        = var.data_volume_size
  swap_size_mb            = var.swap_size_mb
  detailed_monitoring     = var.detailed_monitoring
  enable_snapshots        = var.enable_snapshots
  snapshot_retention_days = var.snapshot_retention_days
  tags                    = local.tags
}

# -------------------------------------------------------------------
# GitHub Actions deploys (OIDC -> ECR push + SSM Run Command)
# -------------------------------------------------------------------
module "github_deploy_role" {
  source = "../../modules/github_deploy_role"

  name                = local.name
  oidc_provider_arn   = var.github_oidc_provider_arn
  github_repository   = var.github_repository
  github_environment  = var.github_environment
  ecr_repository_arns = module.ecr.repository_arns
  instance_arn        = module.ec2.instance_arn
  tags                = local.tags
}
