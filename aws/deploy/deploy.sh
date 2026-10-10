#!/bin/bash
# ===================================================================
# Materia deploy script, run on the EC2 host as root by SSM Run Command.
#
# Required environment: PROJECT, ENVIRONMENT, AWS_REGION, REGISTRY, IMAGE_TAG
# Optional:             APP_DIR (default /opt/materia)
#
# 1. writes .env from Parameter Store (/$PROJECT/$ENVIRONMENT/app/*)
# 2. pulls the images tagged IMAGE_TAG and restarts the stack, with
#    the monitoring (Prometheus, Grafana) configuration in ./monitoring
# 3. waits for the backend health check; on failure rolls back to the
#    previously deployed tag and exits non-zero
# 4. prints the lines the deploy workflow reads:
#      DEPLOYED_IMAGE <service> <repository@digest>
#      PREVIOUS_TAG <tag deployed before this one>
# ===================================================================
set -euo pipefail

: "${PROJECT:?}" "${ENVIRONMENT:?}" "${AWS_REGION:?}" "${REGISTRY:?}" "${IMAGE_TAG:?}"
APP_DIR="${APP_DIR:-/opt/materia}"
PARAM_PATH="/${PROJECT}/${ENVIRONMENT}/app"

cd "$APP_DIR"
umask 077

log() { echo "[deploy $(date -u +%H:%M:%S)] $*"; }

write_env() {
  local tag="$1" tmp
  tmp="$(mktemp "$APP_DIR/.env.XXXXXX")"
  aws ssm get-parameters-by-path \
    --region "$AWS_REGION" \
    --path "$PARAM_PATH" \
    --recursive \
    --with-decryption \
    --query 'Parameters[].[Name,Value]' \
    --output text |
    while IFS=$'\t' read -r name value; do
      printf '%s=%s\n' "${name##*/}" "$value"
    done >"$tmp"

  if ! grep -q '^POSTGRES_PASSWORD=' "$tmp"; then
    rm -f "$tmp"
    log "No parameters found under $PARAM_PATH"
    exit 1
  fi

  # Grafana's admin password: from Parameter Store when Terraform created it, else one generated on this host
  if ! grep -q '^GRAFANA_ADMIN_PASSWORD=' "$tmp"; then
    [ -s .grafana-admin-password ] || openssl rand -hex 24 >.grafana-admin-password
    printf 'GRAFANA_ADMIN_PASSWORD=%s\n' "$(cat .grafana-admin-password)" >>"$tmp"
    log "GRAFANA_ADMIN_PASSWORD is not in Parameter Store: using the one in $APP_DIR/.grafana-admin-password"
  fi

  {
    printf 'REGISTRY=%s\n' "$REGISTRY"
    printf 'PROJECT=%s\n' "$PROJECT"
    printf 'ENVIRONMENT=%s\n' "$ENVIRONMENT"
    printf 'IMAGE_TAG=%s\n' "$tag"
  } >>"$tmp"
  mv "$tmp" .env
  log "Wrote .env ($(wc -l <.env) variables)"
}

backend_health() {
  local id
  id="$(docker compose ps -q backend)"
  [ -n "$id" ] && docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' "$id"
}

wait_healthy() {
  for _ in $(seq 1 60); do
    case "$(backend_health || true)" in
      healthy) return 0 ;;
      unhealthy) return 1 ;;
    esac
    sleep 5
  done
  return 1
}

start() {
  local tag="$1"
  write_env "$tag"
  docker compose pull --quiet
  docker compose up -d --remove-orphans
  # Configuration files are mounted, not baked in: make the running monitoring read the ones just shipped
  docker compose kill -s SIGHUP prometheus >/dev/null 2>&1 || true
  docker compose restart grafana >/dev/null 2>&1 || true
}

report_images() {
  local service id image
  for service in backend frontend; do
    id="$(docker compose ps -q "$service")"
    [ -n "$id" ] || continue
    image="$(docker inspect --format '{{.Image}}' "$id")"
    echo "DEPLOYED_IMAGE $service $(docker image inspect --format '{{index .RepoDigests 0}}' "$image" 2>/dev/null || echo unknown)"
  done
}

[ -f .bootstrapped ] || { log "Host bootstrap has not finished yet"; exit 1; }

# Attachment files: owned by the backend container user (uid 10001, see backend/Dockerfile)
install -d -m 750 -o 10001 -g 10001 /data/attachments
# Monitoring data, owned by the users the images run as (Prometheus: nobody, Grafana: 472)
install -d -m 750 -o 65534 -g 65534 /data/prometheus
install -d -m 750 -o 472 -g 0 /data/grafana
[ -f monitoring/prometheus/prometheus.yml ] || { log "Monitoring configuration missing in $APP_DIR/monitoring"; exit 1; }

log "Logging in to $REGISTRY"
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$REGISTRY" >/dev/null

PREVIOUS_TAG="$(cat .deployed-tag 2>/dev/null || true)"
log "Deploying $IMAGE_TAG (previous: ${PREVIOUS_TAG:-none})"
start "$IMAGE_TAG"

if wait_healthy; then
  echo "$IMAGE_TAG" >.deployed-tag
  if [ -n "$PREVIOUS_TAG" ] && [ "$PREVIOUS_TAG" != "$IMAGE_TAG" ]; then
    echo "$PREVIOUS_TAG" >.previous-tag
  fi
  docker image prune -af --filter "until=168h" >/dev/null || true
  docker compose ps
  report_images
  echo "PREVIOUS_TAG $(cat .previous-tag 2>/dev/null || true)"
  log "Deploy of $IMAGE_TAG succeeded"
  exit 0
fi

log "Backend did not become healthy. Last logs:"
docker compose logs --tail 150 backend || true

if [ -n "$PREVIOUS_TAG" ] && [ "$PREVIOUS_TAG" != "$IMAGE_TAG" ]; then
  log "Rolling back to $PREVIOUS_TAG"
  start "$PREVIOUS_TAG"
  if wait_healthy; then
    log "Rollback to $PREVIOUS_TAG succeeded"
  else
    log "Rollback to $PREVIOUS_TAG is not healthy either"
  fi
fi
exit 1
