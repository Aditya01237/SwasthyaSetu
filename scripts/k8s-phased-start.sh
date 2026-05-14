#!/bin/sh
# k8s-phased-start.sh
# Starts SwasthyaSetu services in dependency order to avoid resource spikes.
# Usage: sh scripts/k8s-phased-start.sh
set -eu

NS="swasthya-setu"
KUBECTL="kubectl --request-timeout=20s"

# ─── helpers ────────────────────────────────────────────────────────────────

log()  { printf '\n\033[1;34m[PHASE] %s\033[0m\n' "$1"; }
ok()   { printf '  \033[1;32m✓ %s\033[0m\n' "$1"; }
info() { printf '  \033[0;37m→ %s\033[0m\n' "$1"; }
err()  { printf '  \033[1;31m✗ %s\033[0m\n' "$1"; }

# Wait for a deployment/statefulset to have at least 1 ready pod
wait_ready() {
  kind="$1"   # deployment | statefulset
  name="$2"
  max_wait="${3:-180}"   # seconds
  elapsed=0
  info "Waiting for $name to be ready (max ${max_wait}s)..."
  while [ "$elapsed" -lt "$max_wait" ]; do
    ready=$($KUBECTL get "$kind" "$name" -n "$NS" \
      -o jsonpath='{.status.readyReplicas}' 2>/dev/null || echo "0")
    ready="${ready:-0}"
    if [ "$ready" -ge 1 ]; then
      ok "$name is READY ($ready replica(s) up)"
      return 0
    fi
    sleep 5
    elapsed=$((elapsed + 5))
    printf '.'
  done
  err "$name did NOT become ready within ${max_wait}s"
  $KUBECTL describe "$kind" "$name" -n "$NS" 2>/dev/null | tail -20 || true
  return 1
}

# Scale a deployment to N replicas and wait for it to be ready
scale_and_wait() {
  name="$1"
  replicas="${2:-1}"
  max_wait="${3:-180}"
  info "Scaling $name to $replicas..."
  $KUBECTL scale deployment "$name" -n "$NS" --replicas="$replicas" 2>/dev/null || true
  wait_ready deployment "$name" "$max_wait"
}

# ─── Phase 0: Reset everything to 0 replicas ────────────────────────────────
log "Phase 0 — Scaling ALL app deployments to 0 (clean slate)"
for dep in api-gateway auth-service hospital-service appointment-service \
           patient-service notification-service ai-service backend \
           swasthya-frontend doctor-frontend; do
  $KUBECTL scale deployment "$dep" -n "$NS" --replicas=0 2>/dev/null && info "scaled $dep → 0" || info "skipped $dep (not found)"
done
ok "All app deployments at 0 replicas"

# ─── Phase 1: Infrastructure ────────────────────────────────────────────────
log "Phase 1 — Infrastructure (postgres, redis, rabbitmq, mailpit)"

# Postgres is a StatefulSet
info "Ensuring postgres StatefulSet has 1 replica..."
$KUBECTL scale statefulset postgres -n "$NS" --replicas=1 2>/dev/null || true
wait_ready statefulset postgres 180

# Redis
scale_and_wait redis 1 120

# RabbitMQ (needs longer — AMQP broker boots slowly)
scale_and_wait rabbitmq 1 240

# Mailpit (lightweight)
scale_and_wait mailpit 1 60

ok "Infrastructure layer healthy"

# ─── Phase 2: Core Auth + Patient services ──────────────────────────────────
log "Phase 2 — Core services (auth-service, patient-service)"
scale_and_wait auth-service      1 240
scale_and_wait patient-service   1 240
ok "Core services healthy"

# ─── Phase 3: Domain services ───────────────────────────────────────────────
log "Phase 3 — Domain services (hospital, appointment, notification)"
scale_and_wait hospital-service     1 240
scale_and_wait appointment-service  1 240
scale_and_wait notification-service 1 240
ok "Domain services healthy"

# ─── Phase 4: Gateway + AI ──────────────────────────────────────────────────
log "Phase 4 — API Gateway + AI service"
scale_and_wait api-gateway 1 180
scale_and_wait ai-service  1 120
ok "Gateway and AI healthy"

# ─── Phase 5: Frontends ─────────────────────────────────────────────────────
log "Phase 5 — Frontends (swasthya-frontend, doctor-frontend)"
scale_and_wait swasthya-frontend 1 60
scale_and_wait doctor-frontend   1 60
ok "Frontends healthy"

# ─── Final status ───────────────────────────────────────────────────────────
log "All phases complete — final pod status:"
$KUBECTL get pods -n "$NS" 2>/dev/null

printf '\n\033[1;32m✅ SwasthyaSetu is fully up on Kubernetes!\033[0m\n'
printf '\nRun the following to access the app:\n'
printf '  minikube tunnel           (in a separate terminal)\n'
printf '  Patient portal : http://localhost/patient/\n'
printf '  Doctor portal  : http://localhost/doctor/\n'
printf '  API Gateway    : http://localhost/api/\n\n'
