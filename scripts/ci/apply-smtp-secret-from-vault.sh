#!/bin/sh
set -eu

: "${VAULT_ADDR:?VAULT_ADDR is required}"
: "${VAULT_TOKEN:?VAULT_TOKEN is required}"

NS="${K8S_NAMESPACE:-swasthya-setu}"
VAULT_PATH="${SMTP_VAULT_PATH:-secret/swasthya-setu/smtp}"

echo "Reading SMTP secrets from Vault path: $VAULT_PATH"

SPRING_MAIL_USERNAME="$(vault kv get -field=SPRING_MAIL_USERNAME "$VAULT_PATH")"
SPRING_MAIL_PASSWORD="$(vault kv get -field=SPRING_MAIL_PASSWORD "$VAULT_PATH")"
SPRING_MAIL_HOST="$(vault kv get -field=SPRING_MAIL_HOST "$VAULT_PATH")"
SPRING_MAIL_PORT="$(vault kv get -field=SPRING_MAIL_PORT "$VAULT_PATH")"
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH="$(vault kv get -field=SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH "$VAULT_PATH")"
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE="$(vault kv get -field=SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE "$VAULT_PATH")"
APP_NOTIFICATION_FROM="$(vault kv get -field=APP_NOTIFICATION_FROM "$VAULT_PATH")"

kubectl create secret generic smtp-secret \
  --from-literal=SPRING_MAIL_USERNAME="$SPRING_MAIL_USERNAME" \
  --from-literal=SPRING_MAIL_PASSWORD="$SPRING_MAIL_PASSWORD" \
  --from-literal=SPRING_MAIL_HOST="$SPRING_MAIL_HOST" \
  --from-literal=SPRING_MAIL_PORT="$SPRING_MAIL_PORT" \
  --from-literal=SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH="$SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH" \
  --from-literal=SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE="$SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE" \
  --from-literal=APP_NOTIFICATION_FROM="$APP_NOTIFICATION_FROM" \
  -n "$NS" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl set env deployment/notification-service \
  --from=secret/smtp-secret \
  -n "$NS"

echo "SMTP secret applied to notification-service"