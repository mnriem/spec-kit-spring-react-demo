-- V1: Initial schema for LLM Performance Analytics Platform

-- Projects
CREATE TABLE projects (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    normalised_name VARCHAR(255) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_projects_normalised_name ON projects (normalised_name);

-- Experiments
CREATE TABLE experiments (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    project_id      UUID        NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    normalised_name VARCHAR(255) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_experiments_project_id ON experiments (project_id);
CREATE UNIQUE INDEX idx_experiments_project_normalised ON experiments (project_id, normalised_name);

-- Iterations
CREATE TABLE iterations (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    experiment_id   UUID        NOT NULL REFERENCES experiments(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    normalised_name VARCHAR(255) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_iterations_experiment_id ON iterations (experiment_id);
CREATE UNIQUE INDEX idx_iterations_experiment_normalised ON iterations (experiment_id, normalised_name);

-- Interactions
CREATE TABLE interactions (
    id                 UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    iteration_id       UUID           NOT NULL REFERENCES iterations(id) ON DELETE CASCADE,
    model              VARCHAR(100)   NOT NULL,
    prompt_compressed  BYTEA,
    response_metadata  JSONB,
    tokens_in          INTEGER        NOT NULL CHECK (tokens_in >= 0),
    tokens_out         INTEGER        NOT NULL CHECK (tokens_out >= 0),
    total_tokens       INTEGER        NOT NULL CHECK (total_tokens >= 0),
    started_at         TIMESTAMPTZ    NOT NULL,
    ended_at           TIMESTAMPTZ    NOT NULL,
    latency_ms         BIGINT         NOT NULL CHECK (latency_ms >= 0),
    tokens_per_second  DOUBLE PRECISION,
    estimated_cost     NUMERIC(12,8),
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_interactions_iteration_id ON interactions (iteration_id);
CREATE INDEX idx_interactions_model ON interactions (model);
CREATE INDEX idx_interactions_started_at ON interactions (started_at);
CREATE INDEX idx_interactions_iteration_started ON interactions (iteration_id, started_at);
CREATE INDEX idx_interactions_latency_ms ON interactions (latency_ms);

-- Tool Calls
CREATE TABLE tool_calls (
    id               UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    interaction_id   UUID        NOT NULL REFERENCES interactions(id) ON DELETE CASCADE,
    tool_name        VARCHAR(255) NOT NULL,
    input_arguments  JSONB,
    output           JSONB,
    sequence_order   INTEGER     NOT NULL CHECK (sequence_order >= 0),
    called_at        TIMESTAMPTZ
);

CREATE INDEX idx_tool_calls_interaction_id ON tool_calls (interaction_id);
