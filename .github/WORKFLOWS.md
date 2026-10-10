# Materia CI/CD Workflows (GitHub Actions)

This directory contains the continuous integration and deployment pipelines of the **Materia** application,
using GitHub Actions and AWS keyless OIDC authentication.

```
push ─► CI ─► CD Staging ─────────────────────────────────────────► CD Production (main only)
        1 Setup          1 Deploy to the staging EC2                 1 Promote the same images
        2 Lint           2 Performance test (k6)                     2 Deploy to the production EC2
        3 Unit tests     3 Security tests (auth + OWASP ZAP)         3 Smoke test
        4 Integration    4 Manual approval ───────────────────────►  4 Monitoring (Prometheus / Grafana)
        5 SonarQube                                                  ↳ rollback if 2-4 fail
        6 Dependency scan
        7 Build images
        8 Push to ECR (staging registry)
```

---

## 📋 Workflows

| Workflow | File | Trigger | What it does |
| :--- | :--- | :--- | :--- |
| **CI** | [ci.yml](workflows/ci.yml) | Pull requests and pushes to `main`, `staging`, `develop`; manual | The 8 stages below; pushes the images to the **staging** ECR registry (pushes only) |
| **CD Staging** | [cd-staging.yml](workflows/cd-staging.yml) | CI succeeded on a push; manual | Deploys to staging, runs the performance and security tests, then (for `main`) waits for the approval and starts CD Production |
| **CD Production** | [cd-production.yml](workflows/cd-production.yml) | Called by CD Staging after the approval; manual (redeploy / rollback, needs an approval) | Promotes the same images, deploys, smoke test, monitoring check, automatic rollback |
| **Deploy to EC2** (reusable) | [deploy-ec2.yml](workflows/deploy-ec2.yml) | Called by both CDs | Ships the stack and its monitoring configuration and deploys through AWS SSM Run Command |

`scripts/ssm-run.sh` runs a command on an instance through SSM and waits for it (used by the deploy and the
monitoring check).

---

## 🔁 CI

Each stage starts only when the previous one passed. Every job starts with its own **checkout** (each runs on a
fresh machine) and sets up Java 21 / Node 22.

| # | Stage | What runs | Fails when |
| :-: | :--- | :--- | :--- |
| 1 | **Setup environment** | Decides the target: pushes to `main`/`staging`/`develop` push their images to staging, pull requests push nothing. Image tag = commit SHA | — |
| 2 | **Lint** | Backend: Checkstyle. Frontend: TypeScript type check, ESLint. Infrastructure: `terraform fmt`/`validate` (bootstrap, staging, prod), `shellcheck` on the deploy scripts | Any Checkstyle violation, a type error, **more** ESLint errors than `frontend/.eslint-baseline`, unformatted or invalid Terraform, a shellcheck finding |
| 3 | **Unit tests** | Backend: every test except `*IT`. Frontend: Vitest with coverage | A failing test |
| 4 | **Integration tests** | Backend `*IT` classes, against PostgreSQL in Docker (Testcontainers) | A failing test |
| 5 | **SonarQube** | Merges unit + integration coverage (JaCoCo), checks the **80% branch-coverage gate**, analyses the code and waits for the quality gate | Coverage below 80% in a gated package, or a red quality gate |
| 6 | **Dependency scan** | Trivy on the libraries **inside the built JAR** and on the lockfiles | A fixable HIGH/CRITICAL vulnerability not accepted in `.trivyignore` |
| 7 | **Build images** | Backend (from the scanned JAR) and frontend images, then a scan of both | Build error, or a fixable HIGH/CRITICAL vulnerability in an image |
| 8 | **Push to Amazon ECR** | `materia-staging-backend:<sha>`, `materia-staging-frontend:<sha>`; skipped for pull requests | AWS login or push error |

## 🚦 CD Staging

| # | Stage | What runs | Fails when |
| :-: | :--- | :--- | :--- |
| 1 | **Deploy** | The commit's images on the staging EC2 instance (with Prometheus, Grafana and the exporters) | The backend does not become healthy (the instance rolls back by itself) |
| 2 | **Performance test** | [k6](../tests/performance/load-test.js): `PERF_VUS` users (default 20) browse the main screens for `PERF_DURATION` (default 2m) | Errors ≥ 1%, checks < 99%, API p95 ≥ 800 ms or p99 ≥ 2 s, any 429 |
| 3 | **Security tests** | [Auth tests](../tests/security/auth-security.mjs) (no session, forged/edited/expired tokens, account enumeration, cookie flags, refresh reuse, sign-out, CORS, headers, brute-force throttling, optional role check) and an [OWASP ZAP](../tests/security/zap/rules.tsv) scan of every API operation | A failed auth check, or a ZAP finding marked `FAIL` (injections, XSS, path traversal…); other findings are reported |
| 4 | **Approval for production** | `main` only: waits for a reviewer of the `production-approval` environment | Rejected |

Results: run summary, plus the `performance-results` and `security-reports` artifacts (ZAP HTML report).

## 🚀 CD Production

| # | Stage | What runs | Fails when |
| :-: | :--- | :--- | :--- |
| 1 | **Promote** | Copies the staging images into `materia-prod-*` **unchanged** (same digest); refuses a release that never reached staging | Missing in staging, or digest mismatch |
| 2 | **Deploy** | The promoted images on the production EC2 instance | Unhealthy backend (the instance rolls back by itself) |
| 3 | **Smoke test** | Checks the running images are the promoted digests, then [the public entry points](../tests/smoke/smoke-test.mjs) (web app, bundle, API, optional sign-in) | A check fails or is slower than 3 s |
| 4 | **Monitoring** | Watches the release in Prometheus for `MONITORING_OBSERVE_SECONDS` (default 300): targets up, no critical alert, 5xx ratio ≤ 5%; prints traffic, latency, memory | Any of these |
| ↩ | **Rollback** | If 3 or 4 fails: redeploys the previous release | — |

To redeploy an earlier release by hand (e.g. a rollback), run **CD Production** with its commit SHA: that run
asks for an approval first.

### Monitoring (Prometheus + Grafana)

Each instance runs Prometheus, Grafana, node-exporter and postgres-exporter next to the application
([aws/deploy/monitoring](../aws/deploy/monitoring)). Prometheus scrapes the backend's actuator on an internal port
(8081), the host and PostgreSQL; alert rules are in `prometheus/alerts.yml`; Grafana opens on the
**Materia - application health** dashboard. Neither is published outside the host — open them through SSM:

```bash
terraform -chdir=aws/environments/prod output -raw grafana_port_forward_command   # then http://localhost:3000
terraform -chdir=aws/environments/prod output -raw grafana_admin_password_command
```

---

## 🔐 Required GitHub configuration

### 1. Environments (Settings → Environments)

| Environment | Variables | Secrets | Protection |
| :--- | :--- | :--- | :--- |
| `staging` | `AWS_DEPLOY_ROLE_ARN`, `APP_URL`, `AWS_REGION` (optional), `PERF_VUS` / `PERF_DURATION` (optional) | `TEST_EMAIL`, `TEST_PASSWORD` (staging account that can read the main screens); `RESTRICTED_EMAIL`, `RESTRICTED_PASSWORD` (optional, non-admin account) | — |
| `production-approval` | — | — | **Required reviewers** (the people who approve releases) |
| `production` | `AWS_DEPLOY_ROLE_ARN`, `APP_URL`, `AWS_REGION` (optional), `MONITORING_OBSERVE_SECONDS` (optional) | `SMOKE_EMAIL`, `SMOKE_PASSWORD` (optional) | Deployment branches: `main` only. No reviewers (the approval is `production-approval`) |

`AWS_DEPLOY_ROLE_ARN` and `APP_URL` are in the Terraform output `github_environment_variables` of each environment.

> [!TIP]
> **No AWS keys:** OIDC gives each job short-lived credentials for `AWS_DEPLOY_ROLE_ARN`. Application secrets
> (database, JWT, Grafana) are in **SSM Parameter Store** (`/materia/<env>/app/*`), created by Terraform and
> written on the instance at deploy time.

### 2. Terraform (once, after pulling this version)

- **prod** `terraform.tfvars`: `promote_from_environment = "staging"` — lets the production role read the staging images.
- **staging** `terraform.tfvars`: `api_rate_limit_per_minute = 6000` — so the performance test is not throttled
  (production keeps 120 requests per minute and per client).
- `terraform apply` in both: creates the Grafana password and the new outputs.

### 3. SonarQube (Settings → Secrets and variables → Actions)

| Name | Kind | Value |
| :--- | :--- | :--- |
| `SONAR_TOKEN` | Repository **secret** | A SonarQube (or SonarCloud) analysis token |
| `SONAR_HOST_URL` | Repository variable | Your server URL |
| `SONAR_PROJECT_KEY` | Repository variable | *(Optional)* project key, default `materia` |
| `SONAR_ORGANIZATION` | Repository variable | *(SonarCloud only)* your organization key |

Until `SONAR_TOKEN` and `SONAR_HOST_URL` are set, CI stage 5 enforces the coverage gate but skips the analysis.

### 4. Branch protection (recommended)

Protect `main`, `staging` and `develop` and require the **CI** checks (`2 · Lint` … `7 · Build images`).

---

## 🧪 Run the stages locally

```bash
# CI
cd backend && ./mvnw checkstyle:check
cd frontend && npm run typecheck && npm run lint:ci
cd backend && ./mvnw test -Dtest='!*IT' -Dsurefire.failIfNoSpecifiedTests=false -Djacoco.destFile=target/jacoco-unit.exec
cd backend && ./mvnw test -Dtest='*IT' -Dsurefire.failIfNoSpecifiedTests=false -Djacoco.destFile=target/jacoco-it.exec
cd backend && ./mvnw jacoco:merge@merge-ci jacoco:report jacoco:check@check

# CD tests, against a running stack (BASE_URL without a trailing /)
k6 run -e BASE_URL=http://localhost -e TEST_EMAIL=... -e TEST_PASSWORD=... tests/performance/load-test.js
BASE_URL=http://localhost TEST_EMAIL=... TEST_PASSWORD=... node tests/security/auth-security.mjs
BASE_URL=http://localhost node tests/smoke/smoke-test.mjs
```
