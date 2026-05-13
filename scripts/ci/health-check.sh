#!/bin/sh
set -eu

RETRIES="${HEALTH_RETRIES:-60}"
SLEEP_SECONDS="${HEALTH_SLEEP_SECONDS:-2}"

check_url() {
  name="$1"
  url="$2"
  attempt=1

  while [ "$attempt" -le "$RETRIES" ]; do
    if curl -fsS "$url" >/dev/null; then
      echo "$name is healthy: $url"
      return 0
    fi

    echo "Waiting for $name ($attempt/$RETRIES)..."
    attempt=$((attempt + 1))
    sleep "$SLEEP_SECONDS"
  done

  echo "$name did not become healthy: $url"
  return 1
}

check_url "AI service" "http://localhost:8000/health"
check_url "Gateway" "http://localhost:8080/actuator/health"
check_url "Auth service" "http://localhost:8081/actuator/health"
check_url "Patient service" "http://localhost:8082/actuator/health"
check_url "Appointment service" "http://localhost:8083/actuator/health"
check_url "Hospital service" "http://localhost:8084/actuator/health"
check_url "Notification service" "http://localhost:8086/actuator/health"
check_url "Backend" "http://localhost:8090/actuator/health"
check_url "Patient frontend" "http://localhost:5173/patient/"
check_url "Doctor frontend" "http://localhost:5174/doctor/"
