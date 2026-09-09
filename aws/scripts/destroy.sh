#!/usr/bin/env bash
# ===================================================================
# Terraform Destroy Script for Materia AWS Infrastructure
# Usage: ./destroy.sh [dev|staging|prod]
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

echo "⚠️  WARNING: You are about to DESTROY the [${ENV}] environment!"
echo "This will terminate instances, load balancers, and databases."
read -rp "Are you absolutely sure you want to proceed? (Type '${ENV}' to confirm): " CONFIRMATION

if [[ "$CONFIRMATION" != "$ENV" ]]; then
  echo "❌ Operation cancelled. Confirmation mismatch."
  exit 1
fi

echo "==================================================================="
echo "💥 Destroying Terraform Infrastructure for Environment: [${ENV}]"
echo "==================================================================="

cd "$TARGET_DIR"

terraform destroy -var-file="terraform.tfvars" -auto-approve

echo "==================================================================="
echo "✅ Environment [${ENV}] destroyed successfully."
echo "==================================================================="
