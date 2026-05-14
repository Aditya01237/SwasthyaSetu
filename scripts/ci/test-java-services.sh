#!/bin/sh
set -eu

for service in \
  services/api-gateway \
  services/auth-service \
  services/hospital-service \
  services/appointment-service \
  services/patient-service \
  services/notification-service \
  services/backend
do
  echo "Testing $service..."
  (cd "$service" && mvn -q test)
done

echo "Java service tests complete."
