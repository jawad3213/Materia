# ===================================================================
# Module: ec2
# The application host: Amazon Linux 2023 with Docker + Compose,
# reachable only through its security group (HTTP) and SSM (admin,
# deploys). PostgreSQL data lives on a separate encrypted EBS volume
# mounted at /data, so replacing the instance keeps the database.
# ===================================================================

data "aws_ssm_parameter" "al2023" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-${var.architecture}"
}

data "aws_subnet" "this" {
  id = var.subnet_id
}

locals {
  data_device = "/dev/sdf"
}

# -------------------------------------------------------------------
# Data volume (PostgreSQL)
# -------------------------------------------------------------------
resource "aws_ebs_volume" "data" {
  availability_zone = data.aws_subnet.this.availability_zone
  size              = var.data_volume_size
  type              = "gp3"
  encrypted         = true

  tags = merge(var.tags, {
    Name     = "${var.name}-data"
    Snapshot = "${var.name}-data"
  })
}

# -------------------------------------------------------------------
# Instance
# -------------------------------------------------------------------
resource "aws_instance" "app" {
  ami                    = data.aws_ssm_parameter.al2023.value
  instance_type          = var.instance_type
  subnet_id              = var.subnet_id
  vpc_security_group_ids = var.security_group_ids
  iam_instance_profile   = var.instance_profile_name
  monitoring             = var.detailed_monitoring
  ebs_optimized          = true

  user_data = templatefile("${path.module}/templates/user_data.sh.tftpl", {
    app_dir         = var.app_dir
    compose_version = var.compose_version
    data_volume_id  = aws_ebs_volume.data.id
    swap_size_mb    = var.swap_size_mb
    architecture    = var.architecture == "arm64" ? "aarch64" : "x86_64"
  })

  # IMDSv2 only.
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }

  root_block_device {
    volume_type           = "gp3"
    volume_size           = var.root_volume_size
    encrypted             = true
    delete_on_termination = true
  }

  tags = merge(var.tags, { Name = "${var.name}-app", Role = "app" })

  # A newer AMI or bootstrap script must not silently replace a running host;
  # taint or replace it deliberately (terraform apply -replace=...).
  lifecycle {
    ignore_changes = [ami, user_data]
  }
}

resource "aws_volume_attachment" "data" {
  device_name = local.data_device
  volume_id   = aws_ebs_volume.data.id
  instance_id = aws_instance.app.id
}

resource "aws_eip" "app" {
  domain   = "vpc"
  instance = aws_instance.app.id

  tags = merge(var.tags, { Name = "${var.name}-eip" })
}

# -------------------------------------------------------------------
# Daily snapshots of the data volume (Data Lifecycle Manager)
# -------------------------------------------------------------------
data "aws_iam_policy_document" "dlm_assume" {
  count = var.enable_snapshots ? 1 : 0

  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["dlm.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "dlm" {
  count = var.enable_snapshots ? 1 : 0

  name               = "${var.name}-dlm-role"
  assume_role_policy = data.aws_iam_policy_document.dlm_assume[0].json

  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "dlm" {
  count = var.enable_snapshots ? 1 : 0

  role       = aws_iam_role.dlm[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSDataLifecycleManagerServiceRole"
}

resource "aws_dlm_lifecycle_policy" "data" {
  count = var.enable_snapshots ? 1 : 0

  description        = "${var.name} daily data volume snapshots"
  execution_role_arn = aws_iam_role.dlm[0].arn
  state              = "ENABLED"

  policy_details {
    resource_types = ["VOLUME"]
    target_tags    = { Snapshot = "${var.name}-data" }

    schedule {
      name      = "daily"
      copy_tags = true

      create_rule {
        interval      = 24
        interval_unit = "HOURS"
        times         = [var.snapshot_time_utc]
      }

      retain_rule {
        count = var.snapshot_retention_days
      }
    }
  }

  tags = var.tags
}
