-- Enable the pgvector extension for vector similarity search.
-- This runs automatically on first database creation via docker-entrypoint-initdb.d.
CREATE EXTENSION IF NOT EXISTS vector;
