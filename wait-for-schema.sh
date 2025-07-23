#!/bin/sh
set -e

# Wait for Postgres to be available
until pg_isready -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER"; do
  echo "Waiting for Postgres..."
  sleep 2
done

# Wait for schema to exist
until psql "host=$POSTGRES_HOST port=$POSTGRES_PORT user=$POSTGRES_USER password=$POSTGRES_PASSWORD dbname=$POSTGRES_DB" -tc "SELECT schema_name FROM information_schema.schemata WHERE schema_name = '$POSTGRES_SCHEMA';" | grep -q "$POSTGRES_SCHEMA"; do
  echo "Waiting for schema $POSTGRES_SCHEMA to be created..."
  sleep 2
done

echo "Schema $POSTGRES_SCHEMA exists. Starting app..."
exec java -XX:MaxRAM=100M -jar /app.jar
