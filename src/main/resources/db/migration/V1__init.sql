CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE knowledge_document (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    source      VARCHAR(500),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
