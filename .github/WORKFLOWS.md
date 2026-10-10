# Materia CI/CD Workflows (GitHub Actions)

This directory contains the continuous integration and deployment pipelines of the **Materia** application,
using GitHub Actions and AWS keyless OIDC authentication.

---

## 📋 Available Workflows

| Workflow | File | Trigger | What it does |
| :--- | :--- | :--- | :--- |
| **CI** | [ci.yml](workflows/ci.yml) | Pull requests and pushes to `main`, `staging`, `develop`; manual | The 8 stages below. Pushes images to ECR for pushes to `main` / `staging` / `develop` |
| **Deploy Staging** | [deploy-staging.yml](workflows/deploy-staging.yml) | CI succeeded on a push to `staging` or `develop`; manual | Deploys the commit's images to the staging EC2 host |
| **Deploy Production** | [deploy-prod.yml](workflows/deploy-prod.yml) | CI succeeded on a push to `main`; manual | Deploys the commit's images to the production EC2 host |
| **Deploy to EC2** (reusable) | [deploy-ec2.yml](workflows/deploy-ec2.yml) | Called by the two above | Checks the images exist in ECR, then deploys through AWS SSM Run Command |
| **Terraform CI** | [terraform-ci.yml](workflows/terraform-ci.yml) | PRs modifying `aws/**` | `terraform fmt -check`, `terraform validate` (bootstrap, staging, prod), `shellcheck` on `deploy.sh` |

---

## 🔁 The CI pipeline

Each stage starts only when the previous one passed. Every job starts with its own **checkout** (each runs on a
fresh machine) and sets up Java 21 / Node 22.

| # | Stage | What runs | Fails when |
| :-: | :--- | :--- | :--- |
| 1 | **Setup environment** | Decides the target: `main` → production, `staging`/`develop` → staging, pull request → no push. Image tag = commit SHA | — |
| 2 | **Lint** | Backend: Checkstyle (`backend/config/checkstyle/checkstyle.xml`: unused imports, `==` on strings, `equals` without `hashCode`…). Frontend: TypeScript type check, ESLint | Any Checkstyle violation, a type error, or **more** ESLint errors than `frontend/.eslint-baseline` |
| 3 | **Unit tests** | Backend: every test except `*IT` (`mvn test -Dtest='!*IT'`). Frontend: Vitest with coverage | A failing test |
| 4 | **Integration tests** | Backend `*IT` classes, against PostgreSQL in Docker (Testcontainers) | A failing test |
| 5 | **SonarQube** | Merges unit + integration coverage (JaCoCo), checks the **80% branch-coverage gate**, then analyses backend and frontend in SonarQube and waits for its quality gate | Coverage below 80% in a gated package, or a red quality gate |
| 6 | **Dependency scan** | Trivy on the libraries **inside the built JAR** (transitive ones included) and on `frontend/package-lock.json` | A HIGH/CRITICAL vulnerability that has a fix, unless accepted in `.trivyignore` |
| 7 | **Build images** | Builds the backend image (from the scanned JAR) and the frontend image, then scans both images, OS packages included | Build error, or a fixable HIGH/CRITICAL vulnerability in an image |
| 8 | **Push to Amazon ECR** | Pushes `materia-<env>-backend:<sha>` and `materia-<env>-frontend:<sha>`; skipped for pull requests | AWS login or push error |

Then **Deploy Staging / Deploy Production** run automatically and deploy exactly those images: nothing is
rebuilt after the tests. To redeploy an earlier commit (e.g. to roll back), run the deploy workflow by hand with
its commit SHA as `image_tag`.

### Rules the gates enforce

- **Lint, frontend:** the code base had 131 ESLint errors when the gate was introduced. New code cannot add any;
  when you fix some, lower `frontend/.eslint-baseline` (or run `npm run lint:ci -- --update`) so they cannot come back.
- **Tests:** a class whose name ends in `IT` is an integration test (needs the database); everything else is a unit test.
- **Coverage:** the 80% branch gate covers the domain and application packages listed in `backend/pom.xml` (JaCoCo `check`).
- **Vulnerabilities:** an exception goes in `.trivyignore` with a reason and an expiry date (`exp:YYYY-MM-DD`);
  after that date the pipeline fails again, so no exception is forgotten.

### Run the stages locally

```bash
# 2 Lint
cd backend && ./mvnw checkstyle:check
cd frontend && npm run typecheck && npm run lint:ci
# 3 Unit tests
cd backend && ./mvnw test -Dtest='!*IT' -Dsurefire.failIfNoSpecifiedTests=false -Djacoco.destFile=target/jacoco-unit.exec
cd frontend && npm run test:coverage
# 4 Integration tests (Docker must be running)
cd backend && ./mvnw test -Dtest='*IT' -Dsurefire.failIfNoSpecifiedTests=false -Djacoco.destFile=target/jacoco-it.exec
# 5 Coverage gate
cd backend && ./mvnw jacoco:merge@merge-ci jacoco:report jacoco:check@check
```

---

## 🔐 Required GitHub Configuration

Materia uses **keyless AWS IAM OpenID Connect (OIDC)** and **AWS Systems Manager (SSM)**.

### 1. GitHub Environments (Variables)
Navigate to your GitHub repository: **Settings → Environments**, create the two environments:
1. **`staging`**
2. **`production`** *(Note: exact name must be `production`, not `prod`)*

In each environment, under **Environment variables**, add:

| Variable Name | Type | Description | Source |
| :--- | :--- | :--- | :--- |
| `AWS_DEPLOY_ROLE_ARN` | Environment Variable | IAM role ARN for GitHub Actions (push to ECR and deploy) | Terraform output: `github_deploy_role_arn` (or `github_environment_variables.AWS_DEPLOY_ROLE_ARN`) |
| `AWS_REGION` | Environment Variable | *(Optional)* AWS region (defaults to `eu-west-3`) | Terraform output: `github_environment_variables.AWS_REGION` |

The CI **Push to Amazon ECR** stage runs in the same environment as the deploy (the AWS role only trusts jobs
running in it), so it uses these variables too.

> [!TIP]
> **No AWS Keys:** AWS OIDC generates short-lived STS credentials automatically via `AWS_DEPLOY_ROLE_ARN`. You do
> **not** need `AWS_ACCESS_KEY_ID` or `AWS_SECRET_ACCESS_KEY`.
>
> **App Secrets:** Database passwords, JWT secret keys, and API tokens are **not** stored in GitHub. They are
> managed in **AWS SSM Parameter Store** (`/materia/<env>/app/*`) via Terraform
> (`aws/environments/<env>/terraform.tfvars`) and injected directly on the EC2 host during deployment.

### 2. SonarQube
Under **Settings → Secrets and variables → Actions**:

| Name | Kind | Value |
| :--- | :--- | :--- |
| `SONAR_TOKEN` | Repository **secret** | A SonarQube (or SonarCloud) analysis token |
| `SONAR_HOST_URL` | Repository variable | Your server URL, e.g. `https://sonarqube.example.com` or `https://sonarcloud.io` |
| `SONAR_PROJECT_KEY` | Repository variable | *(Optional)* project key, default `materia` |
| `SONAR_ORGANIZATION` | Repository variable | *(SonarCloud only)* your organization key |

Until `SONAR_TOKEN` and `SONAR_HOST_URL` are set, stage 5 still enforces the coverage gate but skips the SonarQube
analysis, with a warning on the run.

### 3. Production Protection Rules
Under **Settings → Environments → production**, enable **Required reviewers** to require approval before images are
pushed to the production registry and before production deployments run.

### 4. Branch protection (recommended)
Under **Settings → Branches**, protect `main`, `staging` and `develop` and require the **CI** checks
(`2 · Lint` … `7 · Build images`) to pass before merging.

---

## 🚀 How Deployments Work

1. **CI** tests, scans and builds the images, and pushes them to ECR tagged with the commit SHA.
2. When CI succeeds on `staging`/`develop` (→ staging) or `main` (→ production), the deploy workflow starts and
   checks that both images of that commit are in ECR.
3. **SSM Execution**: it dispatches `aws/deploy/deploy.sh` to the running EC2 instance via AWS Systems Manager
   (`AWS-RunShellScript`).
4. **Health Check & Auto-Rollback**: the instance pulls the images, fetches the latest app parameters from SSM
   Parameter Store, boots the stack with Docker Compose, and validates the health endpoint. If unhealthy, it
   automatically rolls back to the previous image tag.
