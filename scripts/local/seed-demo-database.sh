#!/bin/sh
# scripts/local/seed-demo-database.sh
# Seed SwasthyaSetu demo database after fresh Minikube/Postgres setup.
# Usage: sh scripts/local/seed-demo-database.sh

set -eu

NS="${K8S_NAMESPACE:-swasthya-setu}"
POSTGRES_POD="${POSTGRES_POD:-postgres-0}"
POSTGRES_USER="${POSTGRES_USER:-myuser}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-myuser}"
SOURCE_DB="${SOURCE_DB:-swasthyasetudb}"

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
DUMMY_SQL="$ROOT_DIR/dummy_data.sql"
SYNC_SCRIPT="$ROOT_DIR/docker/postgres/sync-service-databases.sh"

echo "Using namespace: $NS"
echo "Using Postgres pod: $POSTGRES_POD"
echo "Using source DB: $SOURCE_DB"

if [ ! -f "$DUMMY_SQL" ]; then
  echo "ERROR: dummy_data.sql not found at $DUMMY_SQL"
  exit 1
fi

if [ ! -f "$SYNC_SCRIPT" ]; then
  echo "ERROR: sync-service-databases.sh not found at $SYNC_SCRIPT"
  exit 1
fi

echo ""
echo "Step 1: Waiting for Postgres pod..."
kubectl wait --for=condition=Ready pod/"$POSTGRES_POD" -n "$NS" --timeout=180s

echo ""
echo "Step 2: Scaling down DB-dependent services to avoid too many Postgres connections..."
kubectl scale deployment \
  backend \
  auth-service \
  patient-service \
  appointment-service \
  hospital-service \
  -n "$NS" \
  --replicas=0 || true

echo ""
echo "Step 3: Waiting for DB-dependent pods to terminate..."
sleep 10

echo ""
echo "Step 4: Restarting Postgres to clear old connections..."
kubectl delete pod "$POSTGRES_POD" -n "$NS"

echo ""
echo "Step 5: Waiting for Postgres to become ready again..."
kubectl wait --for=condition=Ready pod/"$POSTGRES_POD" -n "$NS" --timeout=240s

echo ""
echo "Step 6: Copying dummy_data.sql into Postgres pod..."
kubectl cp "$DUMMY_SQL" "$NS/$POSTGRES_POD:/tmp/dummy_data.sql"

echo ""
echo "Step 7: Loading dummy data into $SOURCE_DB..."
kubectl exec -n "$NS" "$POSTGRES_POD" -- \
  psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$SOURCE_DB" -f /tmp/dummy_data.sql

echo ""
echo "Step 8: Copying sync-service-databases.sh into Postgres pod..."
kubectl cp "$SYNC_SCRIPT" "$NS/$POSTGRES_POD:/tmp/sync-service-databases.sh"

echo ""
echo "Step 9: Syncing data from $SOURCE_DB to service databases..."
kubectl exec -n "$NS" "$POSTGRES_POD" -- sh -c "
chmod +x /tmp/sync-service-databases.sh
POSTGRES_HOST=localhost \
POSTGRES_PORT=5432 \
POSTGRES_USER=$POSTGRES_USER \
POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
SOURCE_DB=$SOURCE_DB \
/tmp/sync-service-databases.sh
"

echo ""
echo "Step 10: Starting DB-dependent services again..."
kubectl scale deployment \
  backend \
  auth-service \
  patient-service \
  appointment-service \
  hospital-service \
  -n "$NS" \
  --replicas=1

echo ""
echo "Step 11: Waiting for services to become ready..."
kubectl rollout status deployment/backend -n "$NS" --timeout=300s || true
kubectl rollout status deployment/auth-service -n "$NS" --timeout=300s || true
kubectl rollout status deployment/patient-service -n "$NS" --timeout=300s || true
kubectl rollout status deployment/appointment-service -n "$NS" --timeout=300s || true
kubectl rollout status deployment/hospital-service -n "$NS" --timeout=300s || true

echo ""
echo "Database seed and sync completed."
echo ""
echo "Demo UHID:"
echo "UHID-987654321"
echo ""
echo "Test OTP:"
echo "curl -i --max-time 30 -X POST http://localhost:8081/api/auth/send-otp \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{\"uhid\":\"UHID-987654321\"}'"
