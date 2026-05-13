#!/bin/sh
set -eu

NAMESPACE="${K8S_NAMESPACE:-swasthya-setu}"
ROLLOUT_TIMEOUT="${K8S_ROLLOUT_TIMEOUT:-360s}"
HEALTH_RETRIES="${HEALTH_RETRIES:-60}"
HEALTH_SLEEP_SECONDS="${HEALTH_SLEEP_SECONDS:-2}"

GATEWAY_FORWARD_PORT="${K8S_GATEWAY_FORWARD_PORT:-28080}"
AUTH_FORWARD_PORT="${K8S_AUTH_FORWARD_PORT:-28081}"
PATIENT_FORWARD_PORT="${K8S_PATIENT_FORWARD_PORT:-28082}"
APPOINTMENT_FORWARD_PORT="${K8S_APPOINTMENT_FORWARD_PORT:-28083}"
HOSPITAL_FORWARD_PORT="${K8S_HOSPITAL_FORWARD_PORT:-28084}"
NOTIFICATION_FORWARD_PORT="${K8S_NOTIFICATION_FORWARD_PORT:-28086}"
BACKEND_FORWARD_PORT="${K8S_BACKEND_FORWARD_PORT:-28090}"
AI_FORWARD_PORT="${K8S_AI_FORWARD_PORT:-28000}"
PATIENT_FRONTEND_FORWARD_PORT="${K8S_PATIENT_FRONTEND_FORWARD_PORT:-25173}"
DOCTOR_FRONTEND_FORWARD_PORT="${K8S_DOCTOR_FRONTEND_FORWARD_PORT:-25174}"

PORT_FORWARD_PIDS=""
PORT_FORWARD_LOG_DIR="$(mktemp -d)"

cleanup() {
  for pid in $PORT_FORWARD_PIDS; do
    kill "$pid" >/dev/null 2>&1 || true
  done
  rm -rf "$PORT_FORWARD_LOG_DIR"
}
trap cleanup EXIT INT TERM

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "$1 is required for Kubernetes health checks."
    exit 1
  fi
}

wait_for_url() {
  name="$1"
  url="$2"
  attempt=1

  while [ "$attempt" -le "$HEALTH_RETRIES" ]; do
    if curl -fsS "$url" >/dev/null; then
      echo "$name is healthy: $url"
      return 0
    fi

    echo "Waiting for $name ($attempt/$HEALTH_RETRIES)..."
    attempt=$((attempt + 1))
    sleep "$HEALTH_SLEEP_SECONDS"
  done

  echo "$name did not become healthy: $url"
  return 1
}

start_port_forward() {
  service="$1"
  local_port="$2"
  target_port="$3"
  log_file="$PORT_FORWARD_LOG_DIR/${service}.log"

  kubectl -n "$NAMESPACE" port-forward "svc/$service" "$local_port:$target_port" >"$log_file" 2>&1 &
  pid="$!"
  PORT_FORWARD_PIDS="$PORT_FORWARD_PIDS $pid"
  sleep 1
}

require_command kubectl
require_command curl

echo "Waiting for Kubernetes workloads in namespace: $NAMESPACE"

kubectl -n "$NAMESPACE" rollout status statefulset/postgres --timeout="$ROLLOUT_TIMEOUT"

for deployment in \
  redis \
  rabbitmq \
  mailpit \
  ai-service \
  backend \
  auth-service \
  hospital-service \
  patient-service \
  appointment-service \
  notification-service \
  api-gateway \
  swasthya-frontend \
  doctor-frontend
do
  kubectl -n "$NAMESPACE" rollout status "deployment/$deployment" --timeout="$ROLLOUT_TIMEOUT"
done

echo "Opening temporary service port-forwards..."
start_port_forward ai-service "$AI_FORWARD_PORT" 8000
start_port_forward backend "$BACKEND_FORWARD_PORT" 8090
start_port_forward auth-service "$AUTH_FORWARD_PORT" 8081
start_port_forward patient-service "$PATIENT_FORWARD_PORT" 8082
start_port_forward appointment-service "$APPOINTMENT_FORWARD_PORT" 8083
start_port_forward hospital-service "$HOSPITAL_FORWARD_PORT" 8084
start_port_forward notification-service "$NOTIFICATION_FORWARD_PORT" 8086
start_port_forward api-gateway "$GATEWAY_FORWARD_PORT" 8080
start_port_forward swasthya-frontend "$PATIENT_FRONTEND_FORWARD_PORT" 80
start_port_forward doctor-frontend "$DOCTOR_FRONTEND_FORWARD_PORT" 80

wait_for_url "AI service" "http://localhost:$AI_FORWARD_PORT/health"
wait_for_url "Backend" "http://localhost:$BACKEND_FORWARD_PORT/actuator/health"
wait_for_url "Auth service" "http://localhost:$AUTH_FORWARD_PORT/actuator/health"
wait_for_url "Patient service" "http://localhost:$PATIENT_FORWARD_PORT/actuator/health"
wait_for_url "Appointment service" "http://localhost:$APPOINTMENT_FORWARD_PORT/actuator/health"
wait_for_url "Hospital service" "http://localhost:$HOSPITAL_FORWARD_PORT/actuator/health"
wait_for_url "Notification service" "http://localhost:$NOTIFICATION_FORWARD_PORT/actuator/health"
wait_for_url "Gateway" "http://localhost:$GATEWAY_FORWARD_PORT/actuator/health"
wait_for_url "Patient frontend" "http://localhost:$PATIENT_FRONTEND_FORWARD_PORT/patient/"
wait_for_url "Doctor frontend" "http://localhost:$DOCTOR_FRONTEND_FORWARD_PORT/doctor/"

echo "Kubernetes health checks passed."
