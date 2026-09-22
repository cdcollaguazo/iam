-- bootstrap.sql
-- Initializes the database, roles and permissions for keycloak.

\set ON_ERROR_STOP on


-- Create database if it does not exist
SELECT 'CREATE DATABASE keycloak'
    WHERE NOT EXISTS (
    SELECT FROM pg_database WHERE datname = 'keycloak'
)\gexec


-- Create keycloak role
SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', :'KEYCLOAK_DB_USER', :'KEYCLOAK_DB_PASSWORD')
    WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = :'KEYCLOAK_DB_USER')
\gexec


-- Allow keycloak role to connect to the database
SELECT format('GRANT CONNECT ON DATABASE keycloak TO %I', :'KEYCLOAK_DB_USER')\gexec


-- Connect to the keycloak database
\connect keycloak

-- Make the keycloak role owner of the schema
SELECT format('ALTER SCHEMA public OWNER TO %I', :'KEYCLOAK_DB_USER')\gexec

-- Return to default root database
\connect postgres