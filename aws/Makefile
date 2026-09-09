# ===================================================================
# Materia AWS Infrastructure Makefile (Staging & Production)
# ===================================================================

.PHONY: help fmt validate init-staging plan-staging apply-staging destroy-staging init-prod plan-prod apply-prod destroy-prod

help:
	@echo "Materia AWS Terraform Automation"
	@echo ""
	@echo "General Commands:"
	@echo "  make fmt              - Format all Terraform files recursively"
	@echo "  make validate         - Validate syntax across staging and prod"
	@echo ""
	@echo "Staging Environment:"
	@echo "  make init-staging     - Initialize staging environment"
	@echo "  make plan-staging     - Dry-run plan for staging"
	@echo "  make apply-staging    - Apply infrastructure to staging"
	@echo "  make destroy-staging  - Teardown staging infrastructure"
	@echo ""
	@echo "Production Environment:"
	@echo "  make init-prod        - Initialize prod environment"
	@echo "  make plan-prod        - Dry-run plan for prod"
	@echo "  make apply-prod       - Apply infrastructure to prod"
	@echo "  make destroy-prod     - Teardown prod infrastructure"

fmt:
	terraform fmt -recursive .

validate:
	@echo "Validating Staging..."
	cd environments/staging && terraform init -backend=false && terraform validate
	@echo "Validating Prod..."
	cd environments/prod && terraform init -backend=false && terraform validate

# ---- Staging ----
init-staging:
	cd environments/staging && terraform init

plan-staging:
	./scripts/plan.sh staging

apply-staging:
	./scripts/deploy.sh staging

destroy-staging:
	./scripts/destroy.sh staging

# ---- Prod ----
init-prod:
	cd environments/prod && terraform init

plan-prod:
	./scripts/plan.sh prod

apply-prod:
	./scripts/deploy.sh prod

destroy-prod:
	./scripts/destroy.sh prod
