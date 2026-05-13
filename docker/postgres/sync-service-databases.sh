#!/bin/sh
set -eu

DB_HOST="${POSTGRES_HOST:-localhost}"
DB_PORT="${POSTGRES_PORT:-5432}"
DB_USER="${POSTGRES_USER:-myuser}"
export PGPASSWORD="${POSTGRES_PASSWORD:-myuser}"

SOURCE_DB="${SOURCE_DB:-swasthyasetudb}"
AUTH_DB="${AUTH_DB:-auth_db}"
PATIENT_DB="${PATIENT_DB:-patient_db}"
APPOINTMENT_DB="${APPOINTMENT_DB:-appointment_db}"
HOSPITAL_DB="${HOSPITAL_DB:-hospital_db}"

psql_db() {
  db="$1"
  shift
  psql -v ON_ERROR_STOP=1 --host "$DB_HOST" --port "$DB_PORT" --username "$DB_USER" --dbname "$db" "$@"
}

database_exists() {
  db="$1"
  result="$(psql -v ON_ERROR_STOP=1 --host "$DB_HOST" --port "$DB_PORT" --username "$DB_USER" --dbname postgres --tuples-only --no-align -c "SELECT 1 FROM pg_database WHERE datname = '$db'")"
  [ "$result" = "1" ]
}

table_exists() {
  db="$1"
  table="$2"
  result="$(psql_db "$db" --tuples-only --no-align -c "SELECT to_regclass('public.$table') IS NOT NULL")"
  [ "$result" = "t" ]
}

existing_table_list() {
  db="$1"
  shift
  list=""
  for table in "$@"; do
    if table_exists "$db" "$table"; then
      list="$list, public.$table"
    fi
  done
  printf '%s' "${list#, }"
}

truncate_target_tables() {
  db="$1"
  shift
  tables="$(existing_table_list "$db" "$@")"
  if [ -n "$tables" ]; then
    echo "Resetting $db tables: $tables"
    psql_db "$db" -c "TRUNCATE TABLE $tables RESTART IDENTITY CASCADE;"
  fi
}

copy_table() {
  target_db="$1"
  table="$2"

  if ! table_exists "$SOURCE_DB" "$table"; then
    echo "Skipping $table for $target_db: source table not found"
    return
  fi

  if ! table_exists "$target_db" "$table"; then
    echo "Skipping $table for $target_db: target table not found yet"
    return
  fi

  echo "Copying $SOURCE_DB.public.$table -> $target_db.public.$table"
  pg_dump \
    --host "$DB_HOST" \
    --port "$DB_PORT" \
    --username "$DB_USER" \
    --data-only \
    --column-inserts \
    --table "public.$table" \
    "$SOURCE_DB" | psql_db "$target_db" >/dev/null
}

reset_sequences() {
  db="$1"
  psql_db "$db" <<'SQL'
DO $$
DECLARE
  rec record;
BEGIN
  FOR rec IN
    SELECT
      quote_ident(table_schema) || '.' || quote_ident(table_name) AS table_name,
      quote_ident(column_name) AS column_name,
      pg_get_serial_sequence(quote_ident(table_schema) || '.' || quote_ident(table_name), column_name) AS sequence_name
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND pg_get_serial_sequence(quote_ident(table_schema) || '.' || quote_ident(table_name), column_name) IS NOT NULL
  LOOP
    EXECUTE format(
      'SELECT setval(%L, COALESCE((SELECT MAX(%s) FROM %s), 1), EXISTS (SELECT 1 FROM %s))',
      rec.sequence_name,
      rec.column_name,
      rec.table_name,
      rec.table_name
    );
  END LOOP;
END $$;
SQL
}

sync_database() {
  target_db="$1"
  shift

  if ! database_exists "$target_db"; then
    echo "Skipping $target_db: database does not exist"
    return
  fi

  if [ "$target_db" = "$SOURCE_DB" ]; then
    echo "Skipping $target_db: target is the source database"
    return
  fi

  truncate_target_tables "$target_db" "$@"

  for table in "$@"; do
    copy_table "$target_db" "$table"
  done

  reset_sequences "$target_db"
}

if ! database_exists "$SOURCE_DB"; then
  echo "Source database $SOURCE_DB does not exist"
  exit 1
fi

sync_database "$AUTH_DB" \
  hospitals \
  doctors \
  patients \
  auth_otp_verification

sync_database "$HOSPITAL_DB" \
  hospitals \
  hospital_images \
  hospital_services \
  hospital_specializations \
  doctors

sync_database "$APPOINTMENT_DB" \
  hospitals \
  doctors \
  patients \
  appointments \
  qr_tokens \
  audit_logs \
  medical_records \
  medicines

sync_database "$PATIENT_DB" \
  hospitals \
  doctors \
  patients \
  appointments \
  audit_logs \
  medical_records \
  medicines

echo "Service database sync complete."
