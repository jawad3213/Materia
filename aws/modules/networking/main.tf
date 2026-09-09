# ===================================================================
# AWS Networking Module - main.tf
# ===================================================================

locals {
  nat_count = var.enable_nat_gateway ? (var.single_nat_gateway ? 1 : length(var.public_subnet_ids)) : 0
}

# -------------------------------------------------------------------
# Elastic IPs for NAT Gateways
# -------------------------------------------------------------------
resource "aws_eip" "nat" {
  count  = local.nat_count
  domain = "vpc"

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-nat-eip-${count.index + 1}"
  })
}

# -------------------------------------------------------------------
# NAT Gateways
# -------------------------------------------------------------------
resource "aws_nat_gateway" "main" {
  count         = local.nat_count
  allocation_id = aws_eip.nat[count.index].id
  subnet_id     = var.public_subnet_ids[count.index]

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-nat-gw-${count.index + 1}"
  })
}

# -------------------------------------------------------------------
# Public Route Table (0.0.0.0/0 -> IGW)
# -------------------------------------------------------------------
resource "aws_route_table" "public" {
  vpc_id = var.vpc_id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = var.internet_gateway_id
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-public-rt"
    Tier = "public"
  })
}

resource "aws_route_table_association" "public" {
  count          = length(var.public_subnet_ids)
  subnet_id      = var.public_subnet_ids[count.index]
  route_table_id = aws_route_table.public.id
}

# -------------------------------------------------------------------
# Private App Route Tables (0.0.0.0/0 -> NAT Gateway)
# -------------------------------------------------------------------
resource "aws_route_table" "private_app" {
  count  = length(var.private_app_subnet_ids)
  vpc_id = var.vpc_id

  dynamic "route" {
    for_each = var.enable_nat_gateway ? [1] : []
    content {
      cidr_block     = "0.0.0.0/0"
      nat_gateway_id = var.single_nat_gateway ? aws_nat_gateway.main[0].id : aws_nat_gateway.main[count.index].id
    }
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-private-app-rt-${count.index + 1}"
    Tier = "private-app"
  })
}

resource "aws_route_table_association" "private_app" {
  count          = length(var.private_app_subnet_ids)
  subnet_id      = var.private_app_subnet_ids[count.index]
  route_table_id = aws_route_table.private_app[count.index].id
}

# -------------------------------------------------------------------
# Private Database Route Table (Isolated, internal routing only)
# -------------------------------------------------------------------
resource "aws_route_table" "private_db" {
  vpc_id = var.vpc_id

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-private-db-rt"
    Tier = "private-db"
  })
}

resource "aws_route_table_association" "private_db" {
  count          = length(var.private_db_subnet_ids)
  subnet_id      = var.private_db_subnet_ids[count.index]
  route_table_id = aws_route_table.private_db.id
}

# -------------------------------------------------------------------
# Database Subnet Group for RDS
# -------------------------------------------------------------------
resource "aws_db_subnet_group" "main" {
  name        = "${var.name_prefix}-db-subnet-group"
  description = "Database subnet group for ${var.name_prefix}"
  subnet_ids  = var.private_db_subnet_ids

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-db-subnet-group"
  })
}
