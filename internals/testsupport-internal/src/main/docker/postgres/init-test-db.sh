#!/bin/sh
set -e

# Create test user and database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER jdbc_test_login;
    CREATE DATABASE jdbc_test_db;
    GRANT ALL PRIVILEGES ON DATABASE jdbc_test_db TO jdbc_test_login;
EOSQL

# Enable ltree extension on test database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -d jdbc_test_db <<-EOSQL
    CREATE EXTENSION ltree;
EOSQL
