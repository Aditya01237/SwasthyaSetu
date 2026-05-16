#!/bin/sh

NS="${K8S_NAMESPACE:-swasthya-setu}"

cleanup() {
  echo ""
  echo "Stopping SwasthyaSetu local port-forwards..."
  jobs -p | xargs -r kill 2>/dev/null || true
}

start_port_forward() {
  SERVICE_NAME="$1"
  LOCAL_PORT="$2"
  SERVICE_PORT="$3"
  LABEL="$4"
  URL="$5"

  if kubectl get svc "$SERVICE_NAME" -n "$NS" >/dev/null 2>&1; then
    echo "$LABEL: $URL"
    kubectl port-forward -n "$NS" "svc/$SERVICE_NAME" "$LOCAL_PORT:$SERVICE_PORT" >/tmp/swasthya-${SERVICE_NAME}.log 2>&1 &
  else
    echo "$LABEL: service/$SERVICE_NAME not found, skipping"
  fi
}

trap cleanup INT TERM EXIT

echo "Starting SwasthyaSetu local port-forwards..."
echo "Namespace: $NS"
echo ""

start_port_forward "swasthya-frontend" "3004" "80"   "Patient Frontend" "http://localhost:3004/patient/"
start_port_forward "doctor-frontend"   "3003" "80"   "Doctor Frontend " "http://localhost:3003/doctor/"
start_port_forward "api-gateway"       "8081" "8080" "API Gateway     " "http://localhost:8081"
start_port_forward "prometheus"        "9090" "9090" "Prometheus      " "http://localhost:9090"
start_port_forward "grafana"           "3000" "3000" "Grafana         " "http://localhost:3000  admin/admin"
start_port_forward "kibana"            "5601" "5601" "Kibana          " "http://localhost:5601"

echo ""
echo "Keep this terminal open."
echo "Press CTRL+C to stop all port-forwards."
echo ""

wait
