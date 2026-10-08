# ===================================================================
# Module: security
# Security group of the application instance: HTTP (and optionally
# HTTPS) in, everything out. No SSH: Session Manager needs no inbound port.
# ===================================================================

resource "aws_security_group" "app" {
  name        = "${var.name}-app-sg"
  description = "Materia application instance"
  vpc_id      = var.vpc_id

  tags = merge(var.tags, { Name = "${var.name}-app-sg" })
}

resource "aws_vpc_security_group_ingress_rule" "http" {
  for_each = toset(var.allowed_http_cidrs)

  security_group_id = aws_security_group.app.id
  description       = "HTTP"
  ip_protocol       = "tcp"
  from_port         = 80
  to_port           = 80
  cidr_ipv4         = each.value
}

resource "aws_vpc_security_group_ingress_rule" "https" {
  for_each = var.enable_https ? toset(var.allowed_http_cidrs) : toset([])

  security_group_id = aws_security_group.app.id
  description       = "HTTPS"
  ip_protocol       = "tcp"
  from_port         = 443
  to_port           = 443
  cidr_ipv4         = each.value
}

resource "aws_vpc_security_group_egress_rule" "all" {
  security_group_id = aws_security_group.app.id
  description       = "Outbound: image pulls, SSM, package updates"
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}
