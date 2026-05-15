#!/bin/sh

NS="swasthya-setu"

echo "Starting SwasthyaSetu local port-forwards..."
echo "Patient: http://localhost:3004/patient/"
echo "Doctor : http://localhost:3003/doctor/"
echo "API    : http://localhost:8081"

kubectl port-forward svc/swasthya-frontend 3004:80 -n "$NS" &
kubectl port-forward svc/doctor-frontend 3003:80 -n "$NS" &
kubectl port-forward svc/api-gateway 8081:8080 -n "$NS" &

wait
