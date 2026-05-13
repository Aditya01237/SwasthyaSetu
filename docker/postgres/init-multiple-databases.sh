#!/bin/sh
set -e

create_database() {
    local database=$1
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        SELECT 'CREATE DATABASE $database'
        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$database')\gexec
EOSQL
}

create_database swasthyasetudb
create_database auth_db
create_database patient_db
create_database appointment_db
create_database hospital_db
