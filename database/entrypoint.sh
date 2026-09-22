#!/bin/sh
# Entrypoint script to initialize the database and run migrations

# Script exits if any command fails or any variable is not set
set -eu

# Run bootstrap.sql for keycloak, if needed
export PGPASSWORD="$ROOT_DB_PASSWORD"

DB_EXISTS=$(psql -h "$HOST" -U "$ROOT_DB_USER" -d postgres \
  -tAc "SELECT 1 FROM pg_database WHERE datname='keycloak'")

if [ "$DB_EXISTS" = "1" ]; then
  echo "Database keycloak already exists. Skipping bootstrap..."
else
  echo "Database keycloak doesn't exist. Running bootstrap..."
  psql -h "$HOST" -U "$ROOT_DB_USER" -d postgres \
    -v KEYCLOAK_DB_USER="$KEYCLOAK_DB_USER" \
    -v KEYCLOAK_DB_PASSWORD="$KEYCLOAK_DB_PASSWORD" \
    -f /bootstrap.sql
  echo "keycloak bootstrap completed!"
fi

unset PGPASSWORD
