#!/usr/bin/env bash
# ===================================================================
# Terraform Plan Script for Materia AWS Infrastructure
# Usage: ./plan.sh [dev|staging|prod]
# ===================================================================

set -euo pipefail

ENV="${1:-}"

if [[ -z "$ENV" ]]; then
  echo "❌ Error: Environment not specified."
  echo "Usage: $0 [staging|prod]"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET_DIR="${SCRIPT_DIR}/../environments/${ENV}"

if [[ ! -d "$TARGET_DIR" ]]; then
  echo "❌ Error: Environment directory not found: $TARGET_DIR"
  echo "Valid options: staging, prod"
  exit 1
fi

echo "==================================================================="
echo "🔍 Running Terraform Plan for Environment: [${ENV}]"
echo "==================================================================="

cd "$TARGET_DIR"

terraform init -upgrade
terraform plan -var-file="terraform.tfvars"
