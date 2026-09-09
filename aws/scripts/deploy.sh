#!/usr/bin/env bash
# ===================================================================
# Terraform Deploy Script for Materia AWS Infrastructure
# Usage: ./deploy.sh [dev|staging|prod]
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
echo "🚀 Deploying Terraform Infrastructure for Environment: [${ENV}]"
echo "==================================================================="

cd "$TARGET_DIR"

terraform init -upgrade
terraform apply -var-file="terraform.tfvars" -auto-approve

echo "==================================================================="
echo "✅ Deployment completed successfully for [${ENV}]!"
echo "==================================================================="
terraform output
