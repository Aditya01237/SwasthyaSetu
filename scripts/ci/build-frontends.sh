#!/bin/sh
set -eu

for app in swasthya-frontend doctor-frontend
do
  echo "Building $app..."
  (cd "$app" && npm ci && npm run build)
done

echo "Frontend builds complete."
