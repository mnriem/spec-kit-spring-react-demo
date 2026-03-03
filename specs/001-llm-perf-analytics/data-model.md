# Data Model: LLM Performance Analytics Platform

**Phase**: 1 — Design  
**Date**: 2026-03-02  
**Feature**: `001-llm-perf-analytics`

---

## Entity Overview

```text
Project (1) ──< (n) Experiment (1) ──< (n) Iteration (1) ──< (n) Interaction (1) ──< (n) ToolCall
ModelPricing (independent — used for cost calculation at ingest)
```

---

## Entities

### Project

Top-level namespace. Project names are globally unique (case-insensitive, whitespace-normalised).

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `id` | UUID | PK, generated | `gen_random_uuid()` default |
| `name` | VARCHAR(255) | NOT NULL | Display name as provided by caller |
| `normalised_name` | VARCHAR(255) | NOT NULL, UNIQUE | `lower(trim(regexp_replace(name, '\s+', ' ', 'g')))` — used for uniqueness checks |
| `description` | TEXT | nullable | Optional description |
| `created_at` | TIMESTAMPTZ | NOT NULL, default NOW() | |

**Validation rules**:
- `name` must not be blank.
- `normalised_name` uniqueness enforced via DB unique index and application-layer pre-check (returns HTTP 409 on duplicate).
- Case-insensitive matching: `"My Project"` and `"my project"` resolve to the same entity.

---

### Experiment

A named grouping of related LLM tests within a project. Experiment names are unique within their parent project.

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `id` | UUID | PK, generated | |
| `project_id` | UUID | NOT NULL, FK → Project(id) ON DELETE CASCADE | |
| `name` | VARCHAR(255) | NOT NULL | Display name |
| `normalised_name` | VARCHAR(255) | NOT NULL | Lowercase + whitespace-normalised |
| `description` | TEXT | nullable | |
| `created_at` | TIMESTAMPTZ | NOT NULL, default NOW() | |

**Constraints**:
- `UNIQUE(project_id, normalised_name)` — experiment names unique within a project.

---

### Iteration

A specific configuration variant within an experiment (e.g., model version, prompt strategy). Iteration names are unique within their parent experiment.

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `id` | UUID | PK, generated | |
| `experiment_id` | UUID | NOT NULL, FK → Experiment(id) ON DELETE CASCADE | |
| `name` | VARCHAR(255) | NOT NULL | Display name |
| `normalised_name` | VARCHAR(255) | NOT NULL | Lowercase + whitespace-normalised |
| `description` | TEXT | nullable | |
| `created_at` | TIMESTAMPTZ | NOT NULL, default NOW() | |

**Constraints**:
- `UNIQUE(experiment_id, normalised_name)` — iteration names unique within an experiment.

---

### Interaction

A single LLM call record. All derived metrics are calculated and stored at ingest to support efficient aggregation queries.

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `id` | UUID | PK, generated | |
| `iteration_id` | UUID | NOT NULL, FK → Iteration(id) ON DELETE CASCADE | |
| `model` | VARCHAR(100) | NOT NULL | Model identifier string (e.g., `"gpt-4o"`) |
| `prompt_compressed` | BYTEA | nullable | Gzip-compressed full prompt text (see D-006 in research.md). Nullable for test/partial uploads |
| `response_metadata` | JSONB | nullable | Arbitrary response metadata (finish reason, stop sequence, etc.) |
| `tokens_in` | INTEGER | NOT NULL, ≥ 0 | Prompt token count |
| `tokens_out` | INTEGER | NOT NULL, ≥ 0 | Completion token count (0 for interrupted calls) |
| `total_tokens` | INTEGER | NOT NULL, ≥ 0 | **Derived**: `tokens_in + tokens_out` (stored) |
| `started_at` | TIMESTAMPTZ | NOT NULL | Interaction start timestamp |
| `ended_at` | TIMESTAMPTZ | NOT NULL | Interaction end timestamp |
| `latency_ms` | BIGINT | NOT NULL | **Derived**: `ended_at − started_at` in ms (stored). Must be ≥ 0 |
| `tokens_per_second` | DOUBLE PRECISION | nullable | **Derived**: `(total_tokens / latency_ms) * 1000`. Null if `latency_ms = 0` |
| `estimated_cost` | NUMERIC(12,8) | nullable | **Derived**: calculated from ModelPricing; null if model has no pricing row |
| `created_at` | TIMESTAMPTZ | NOT NULL, default NOW() | Record creation time (differs from `started_at`) |

**Validation rules**:
- `ended_at` MUST be ≥ `started_at`. Violations → HTTP 422 with field-level error.
- `tokens_in` and `tokens_out` MUST be ≥ 0.
- `tokens_out = 0` is valid (interrupted/error call).
- If `latency_ms = 0`, `tokens_per_second` stored as `NULL`.

**JPA mapping notes**:
- `prompt_compressed` uses `@Convert(converter = GzipStringConverter.class)` — caller and service always use `String`; converter handles GZIP transparently.
- `response_metadata` mapped as `String` with `@Column(columnDefinition = "JSONB")` or via a custom JSONB type (e.g., `hibernate-types`).
- List endpoints use a JPA DTO projection interface that **excludes** `prompt_compressed` to avoid loading large BYTEA blobs unnecessarily.

---

### ToolCall

A record of one tool invocation within an interaction. Multiple tool calls per interaction are ordered by `sequence_order`.

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `id` | UUID | PK, generated | |
| `interaction_id` | UUID | NOT NULL, FK → Interaction(id) ON DELETE CASCADE | |
| `tool_name` | VARCHAR(255) | NOT NULL | Name of the tool invoked |
| `input_arguments` | JSONB | nullable | Tool input as a JSON object |
| `output` | JSONB | nullable | Tool output as a JSON object or scalar |
| `sequence_order` | INTEGER | NOT NULL, ≥ 0 | Position of this call within the interaction |
| `called_at` | TIMESTAMPTZ | nullable | Optional: when the tool was invoked |

**Validation rules**:
- `tool_name` must not be blank.
- An empty `tools_called` array in the request body is stored as zero `ToolCall` rows (distinct from omitting the field, which is also zero rows — both are valid per spec).

---

### ModelPricing

Configurable per-model token pricing for cost estimation. Seeded by Flyway; operators can update rows without redeployment.

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `id` | UUID | PK, generated | |
| `model_identifier` | VARCHAR(100) | NOT NULL, UNIQUE | Must match the `model` field on Interaction exactly |
| `input_price_per_million_tokens` | NUMERIC(12,6) | NOT NULL, ≥ 0 | USD per 1M input tokens |
| `output_price_per_million_tokens` | NUMERIC(12,6) | NOT NULL, ≥ 0 | USD per 1M output tokens |
| `effective_from` | DATE | NOT NULL | Pricing effective date (for audit; latest row per model used) |
| `notes` | TEXT | nullable | e.g., `"OpenAI pricing as of 2024-11"` |

**Seeded defaults** (from `V2__seed_model_pricing.sql`):

| Model | Input $/M | Output $/M |
|-------|-----------|------------|
| gpt-4o | 2.50 | 10.00 |
| gpt-4o-mini | 0.15 | 0.60 |
| gpt-4-turbo | 10.00 | 30.00 |
| claude-3-5-sonnet-20241022 | 3.00 | 15.00 |
| claude-3-haiku-20240307 | 0.25 | 1.25 |
| gemini-1.5-pro | 1.25 | 5.00 |
| gemini-1.5-flash | 0.075 | 0.30 |

---

## Relationships

```text
Project
  └── has many Experiments (cascade delete)
        └── each has many Iterations (cascade delete)
              └── each has many Interactions (cascade delete)
                    └── each has many ToolCalls (cascade delete)

ModelPricing
  └── referenced by Interaction.model (no FK — model identifier is a loose coupling)
```

**Cascade behaviour**: Deleting a Project deletes all its Experiments, Iterations, Interactions, and ToolCalls via `ON DELETE CASCADE` foreign keys. This satisfies FR-022 (DELETE /api/projects/{id}).

---

## Indexes

| Table | Index | Type | Purpose |
|-------|-------|------|---------|
| `projects` | `idx_projects_normalised_name` | UNIQUE | Case-insensitive uniqueness lookup |
| `experiments` | `idx_experiments_project_id` | BTREE | FK join |
| `experiments` | `idx_experiments_project_normalised` | UNIQUE (project_id, normalised_name) | Uniqueness within project |
| `iterations` | `idx_iterations_experiment_id` | BTREE | FK join |
| `iterations` | `idx_iterations_experiment_normalised` | UNIQUE (experiment_id, normalised_name) | Uniqueness within experiment |
| `interactions` | `idx_interactions_iteration_id` | BTREE | FK join, per-iteration aggregation |
| `interactions` | `idx_interactions_model` | BTREE | Filter by model (FR-018) |
| `interactions` | `idx_interactions_started_at` | BTREE | Date-range filter (FR-018), default 30-day window |
| `interactions` | `idx_interactions_iteration_started` | BTREE (iteration_id, started_at) | Dashboard aggregation: metrics per iteration in time window |
| `interactions` | `idx_interactions_latency_ms` | BTREE | Filter by latency threshold (FR-018) |
| `tool_calls` | `idx_tool_calls_interaction_id` | BTREE | FK join, tool call list per interaction |
| `model_pricing` | `idx_model_pricing_identifier` | UNIQUE | Cost lookup by model identifier |

---

## State Transitions

Interaction data is **append-only and immutable** once ingested — there are no update operations on Interaction or ToolCall records. Only DELETE is supported (individual interaction or cascade via Project/Experiment/Iteration delete).

Project, Experiment, and Iteration names are fixed at creation (no rename API in v1).

```text
[Not Exists] --POST /api/interactions--> [Persisted]
[Persisted]  --DELETE /api/interactions/{id}--> [Deleted]
[Persisted]  --DELETE /api/projects/{id}--> [Cascade Deleted]
```

---

## Flyway Migration Plan

| Version | File | Contents |
|---------|------|----------|
| V1 | `V1__init_schema.sql` | CREATE TABLE for all 5 entities; all indexes; FK constraints |
| V2 | `V2__seed_model_pricing.sql` | INSERT default pricing rows for 7 models |

Sample data is NOT seeded via Flyway. It is loaded on-demand via `POST /api/admin/sample-data` to keep production databases clean by default.

---

## JPA Entity Design Notes

- All entities use `UUID` primary keys (`@GeneratedValue(strategy = GenerationType.UUID)` on Java 17+ / Hibernate 6).
- `@CreationTimestamp` sets `created_at` automatically (Hibernate annotation).
- `@Version` on Project, Experiment, Iteration entities for optimistic locking during concurrent `findOrCreate` operations (D-011 in research.md).
- `GzipStringConverter` implements `AttributeConverter<String, byte[]>` registered via `@Converter(autoApply = false)`.
- DTO projections for list endpoints declared as JPA interface projections (Spring Data Projections) to avoid loading `prompt_compressed` BYTEA on every list query.
