# Research: LLM Performance Analytics Platform

**Phase**: 0 — Research & Unknown Resolution  
**Date**: 2026-03-02  
**Feature**: `001-llm-perf-analytics`

All NEEDS CLARIFICATION items from the Technical Context have been resolved below. Each decision records what was chosen, why, and what alternatives were evaluated and rejected.

---

## D-001: Spring Boot Embedded React — Build Strategy

**Decision**: Use `frontend-maven-plugin` (Eirik Sletteberg, v1.15.0) to run `npm ci` and `npm run build` inside the `frontend/` directory during `mvn package`. Vite outputs the production build to `frontend/dist/`, which Maven copies to `target/classes/static/`. Spring Boot serves the compiled SPA from its static resource handler.

**Rationale**: A single `mvn package` (or `mvn install`) produces a self-contained, runnable JAR with no separate npm step. CI/CD is simplified — one tool chain, one artifact. Spring Boot's auto-configuration already serves `src/main/resources/static/**` without any extra configuration.

**SPA Routing Fallback**: A `SpaFallbackController` annotated `@RequestMapping("/{path:^(?!api).*}")` forwards all non-API routes to `/index.html` to support React Router's client-side navigation without 404 errors on deep links.

**Alternatives considered**:
- *Separate frontend Docker service*: Rejected — adds operational complexity (two containers, CORS handling, reverse proxy) for an internal single-tenant tool where simplicity is preferred.
- *Spring Boot serving from a CDN/S3*: Out of scope for v1.
- *Create React App (CRA)*: Rejected — CRA is deprecated; Vite is the current recommended replacement with faster HMR and builds.

---

## D-002: Frontend Build Tool — Vite vs. Webpack

**Decision**: Vite 5.x with `@vitejs/plugin-react` and TypeScript.

**Rationale**: 10–20× faster cold starts vs. webpack in development; native ESM dev server; built-in code-splitting; excellent TypeScript support out of the box; no ejecting. `vite.config.ts` configures a dev-mode proxy (`/api → http://localhost:8080`) so the React dev server can call the Spring Boot backend during local development without CORS issues.

**Alternatives considered**:
- *webpack 5*: Rejected — slower DX, more configuration overhead for no benefit in this project scope.

---

## D-003: UI Component Library — Ant Design vs. Material UI vs. Tailwind CSS

**Decision**: Ant Design (antd) 5.x with CSS-in-JS design tokens.

**Rationale**: Ant Design provides out-of-the-box components that directly map to this feature's requirements — `Table` with server-side filtering/sorting, `RangePicker` for the date-range filter, `Tabs` for comparison view, `Statistic` for metric cards, and `Drawer` for interaction detail — reducing custom UI code significantly. It ships with full TypeScript types and has good WCAG 2.1 AA accessibility support. Its design token system satisfies Constitution Principle III (UX Consistency).

**Alternatives considered**:
- *Material UI (MUI)*: Mature, but heavier bundle and more opinionated Material Design aesthetic.
- *Tailwind CSS + Radix UI*: More flexible but requires building complex components (tables, date pickers) from scratch, which would bloat the task list without adding end-user value.
- *shadcn/ui*: Good option but still requires composing complex data-heavy components; Ant Design wins on time-to-feature for data dashboards.

---

## D-004: Chart Library — Recharts vs. ECharts vs. Victory

**Decision**: Recharts 2.x.

**Rationale**: Recharts is a composable, React-native charting library (MIT, ~24k GitHub stars) with first-class TypeScript support and built-in `ResponsiveContainer`. It covers every chart type required by the spec:
- `<LineChart>` / `<AreaChart>` → interaction volume timeline (FR-010)
- `<BarChart>` → token usage by model (FR-011), iteration comparison (FR-016)
- `<ComposedChart>` with `<Bar>` → latency distribution histogram (FR-012)
- `<RadarChart>` → iteration comparison radar (FR-016)
- Custom `<Cell>` coloring → tool usage frequency (FR-013)

**Alternatives considered**:
- *Apache ECharts (echarts-for-react)*: More powerful for complex visualisations but heavier bundle, less idiomatic React API.
- *Victory*: Smaller community, less active maintenance.
- *Chart.js (react-chartjs-2)*: Not React-native; imperative API creates friction with React's declarative model.

---

## D-005: Data Fetching — TanStack Query vs. Redux vs. SWR

**Decision**: TanStack Query (React Query) 5.x.

**Rationale**: Declarative server state management with automatic caching, background refetching, loading/error states, and pagination support. Eliminates the need for a global store for remote data. `useQuery` and `useMutation` hooks align cleanly with the dashboard (read-heavy) and interaction ingest (write) patterns. The `staleTime` and `gcTime` settings control cache freshness for the dashboard's 30-day default view.

**Alternatives considered**:
- *Redux Toolkit Query*: More boilerplate; RTK is overkill for a single-team internal tool.
- *SWR*: Simpler API but less capable for mutations, pagination, and complex cache invalidation.
- *Plain Axios + useState/useEffect*: No caching, more boilerplate, harder to maintain.

---

## D-006: Gzip Prompt Storage — JPA Converter Approach

**Decision**: A JPA `AttributeConverter<String, byte[]>` named `GzipStringConverter` applies `java.util.zip.GZIPOutputStream` at write time and `GZIPInputStream` at read time. The JPA entity declares the prompt field as `@Column(columnDefinition = "BYTEA") @Convert(converter = GzipStringConverter.class)`. PostgreSQL stores the raw compressed bytes in a `BYTEA` column.

**Rationale**: Fully transparent to all callers — controllers and service methods always work with plain `String`. No special handling in tests (use plain strings). The converter satisfies FR-002 (gzip-compressed, no size limit). BYTEA in PostgreSQL handles arbitrary binary data efficiently; PostgreSQL's TOAST mechanism transparently handles very large values (>8 KB) via out-of-line storage.

**Edge cases handled**:
- `null` prompt: converter returns `null` byte array (nullable column).
- Decompression failure: converter throws `RuntimeException` wrapping `IOException`; Spring's exception handler returns HTTP 500 with a safe error message.
- Very large prompts (100k+ chars): GZIP achieves ~60–80% compression on natural language; TOAST handles the stored bytes.

**Alternatives considered**:
- *Store compressed in application layer, pass byte[] to JPA*: Leaks binary concerns into service layer.
- *PostgreSQL `pg_compress` function*: DB-layer compression is less portable and harder to test.
- *Store as TEXT uncompressed*: Rejected — spec explicitly requires gzip compression.

---

## D-007: API Key Authentication

**Decision**: A Spring Security `OncePerRequestFilter` named `ApiKeyAuthFilter` inspects the `X-API-Key` HTTP header. The expected key is injected from the `APP_API_KEY` environment variable (property: `app.security.api-key`). All `/api/**` paths are secured; the React SPA routes (`/`, `/dashboard`, etc.) are public (served as static files). A development default key is defined in `application.yml` for local use only; Docker Compose injects the real key via environment variable.

**Security considerations**:
- The API key is compared using `MessageDigest.isEqual` (constant-time comparison) to prevent timing attacks.
- The key is never logged or included in error responses.
- `401 Unauthorized` returned for missing or invalid keys (FR-004).

**Rationale**: Stateless, simple, matches the spec's single-key requirement. No session management needed.

**Alternatives considered**:
- *Spring Security HTTP Basic*: Requires Base64 encoding; client ergonomics worse than a header key.
- *JWT*: Full user auth is explicitly out of scope.
- *No auth in dev profile*: Rejected — tests must cover auth behaviour on all paths.

---

## D-008: Cost Estimation Strategy

**Decision**: A `model_pricing` database table (seeded by Flyway `V2__seed_model_pricing.sql`) stores `(model_identifier, input_price_per_million_tokens, output_price_per_million_tokens)`. On ingest, the service looks up pricing for the submitted model identifier and calculates `estimated_cost = (tokens_in / 1_000_000) * input_price + (tokens_out / 1_000_000) * output_price`. If no pricing row exists for the model, `estimated_cost` is stored as `NULL` — **not zero** — to avoid misleading the user.

**Seeded defaults** (representative defaults for common models at time of writing):
| Model | Input $/M tokens | Output $/M tokens |
|-------|-----------------|-------------------|
| gpt-4o | 2.50 | 10.00 |
| gpt-4o-mini | 0.15 | 0.60 |
| gpt-4-turbo | 10.00 | 30.00 |
| claude-3-5-sonnet | 3.00 | 15.00 |
| claude-3-haiku | 0.25 | 1.25 |

**Rationale**: Configurable via DB (operators can INSERT/UPDATE rows); no rebuild needed to update prices. Stored at write time avoids recalculation overhead on every dashboard query. Storing `NULL` for unknown models is honest — operators will notice via the UI that pricing is unset.

**Alternatives considered**:
- *Hard-coded map in application code*: Not configurable without redeployment.
- *External pricing API*: Out of scope; adds network dependency.
- *Calculate on read*: Expensive for aggregation queries across thousands of interactions.

---

## D-009: Derived Metric Storage Strategy

**Decision**: `latency_ms`, `total_tokens`, `tokens_per_second`, and `estimated_cost` are **calculated at ingest time and stored in the `interactions` table**. The service layer performs the calculation before calling the repository `save`.

**Validation rules for derived metrics**:
- `latency_ms = ended_at - started_at` (milliseconds). If `ended_at < started_at`, ingest returns HTTP 422 with a field-level error.
- `total_tokens = tokens_in + tokens_out`.
- `tokens_per_second = (total_tokens / latency_ms) * 1000.0`. If `latency_ms = 0`, store `null` to avoid division by zero.
- `tokens_out = 0` is valid (e.g., interrupted call); stored as-is.

**Rationale**: Enables efficient `ORDER BY latency_ms`, `WHERE latency_ms > threshold`, and `AVG(latency_ms)` aggregations without computed columns or views. Satisfies dashboard performance goal (SC-002, ≤ 3 s for 10k interactions).

---

## D-010: Database Migration — Flyway

**Decision**: Flyway 10.x. Spring Boot auto-configures Flyway when it is on the classpath and `spring.datasource` is configured. Migrations live in `src/main/resources/db/migration/` following the `V{n}__{description}.sql` naming convention.

**Migration plan**:
- `V1__init_schema.sql` — creates all tables, indexes, constraints, and unique indexes.
- `V2__seed_model_pricing.sql` — inserts the default model pricing rows.

Sample data is NOT loaded via Flyway (it's large, environment-specific, and triggered via the API at runtime via FR-020/FR-022).

**Alternatives considered**:
- *Liquibase*: More powerful but heavier XML/YAML format; Flyway's SQL-native approach is faster to write and review.
- *Hibernate `ddl-auto: create-drop`*: Destructive; not suitable for production.

---

## D-011: Hierarchy Auto-Creation on Ingest

**Decision**: When `POST /api/interactions` is received with `projectName`, `experimentName`, `iterationName` fields, the service uses `findOrCreate` logic:
1. Look up Project by `normalised_name` (lowercase, whitespace-normalised). If not found, create it.
2. Look up Experiment by `normalised_name` within the resolved Project. If not found, create it.
3. Look up Iteration by `normalised_name` within the resolved Experiment. If not found, create it.
4. Create the Interaction linked to the resolved Iteration.

All four steps execute within a single `@Transactional` method. Optimistic locking (`@Version`) on Project, Experiment, Iteration prevents duplicate creation under concurrent ingest.

**Conflict handling**: If normalisation reveals a collision (e.g., `" My Project "` vs `"my project"` → same `normalised_name` → same entity), the existing entity is reused — this is expected and correct. A true conflict (attempting to re-use a name that resolves to a *different* existing entity) is not possible given the normalised lookup. The spec's 409 case applies only to explicit create-by-name API calls (`POST /api/projects`) where the name already exists.

**Rationale**: Simplifies API integration — callers do not need to pre-create the hierarchy before posting an interaction. Matches the spec assumption: "Interactions uploaded with names that don't yet exist will automatically create missing levels."

---

## D-012: Docker Compose Architecture

**Decision**:

```yaml
services:
  db:
    image: postgres:16-alpine
    environment: { POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD }
    volumes: [pgdata:/var/lib/postgresql/data]
    healthcheck: pg_isready -U ${POSTGRES_USER}

  app:
    build: .
    environment: { SPRING_DATASOURCE_URL, APP_API_KEY, SPRING_PROFILES_ACTIVE=docker }
    ports: ["8080:8080"]
    depends_on:
      db:
        condition: service_healthy
```

**Dockerfile** uses a multi-stage build:
- Stage 1 (`node:20-alpine`): installs npm deps and runs `npm run build` in `frontend/`.
- Stage 2 (`eclipse-temurin:21-jre-alpine`): copies the frontend build output alongside the Maven-built JAR, then runs `java -jar app.jar`.

Actually, since the `frontend-maven-plugin` handles the frontend build inside Maven, the Dockerfile only needs a single Maven stage + JRE stage:
- Stage 1 (`maven:3.9-eclipse-temurin-21`): runs `mvn package -DskipTests`; Maven invokes the frontend plugin (which downloads Node) inside the container.
- Stage 2 (`eclipse-temurin:21-jre-alpine`): copies the JAR and runs it.

**Rationale**: Maven-first approach means one canonical build command regardless of environment. The `frontend-maven-plugin` downloads the correct Node/npm version specified in the POM, ensuring reproducible builds.

---

## D-013: Performance Baseline Targets

Per Constitution Principle IV, baselines must be established before implementation:

| Metric | Target | Measurement Method |
|--------|--------|--------------------|
| API ingest (POST /api/interactions) p95 | ≤ 200 ms | Spring Boot Actuator + Micrometer → Prometheus; k6 load test (100 VUs) |
| Dashboard load (GET /api/dashboard/summary) p95 | ≤ 200 ms | Same |
| React dashboard LCP | ≤ 2.5 s | Lighthouse CI (Moto G4 simulation) |
| Full dashboard render (10k interactions) | ≤ 3 s | Cypress e2e with performance.now() assertion |
| DB query: interaction list with filters | ≤ 50 ms | PostgreSQL EXPLAIN ANALYZE on test dataset |

Spring Boot Actuator exposes `/actuator/prometheus`; `management.endpoints.web.exposure.include=health,info,prometheus` configured in `application-docker.yml`.

---

## D-014: Sample Data Strategy

**Decision**: Sample data is loaded via `POST /api/admin/sample-data` (FR-022). A `SampleDataLoader` service constructs 3 projects (each with 1+ experiments, 2+ iterations, 20+ interactions per iteration) using deterministic seeded random data covering multiple model identifiers, varied token counts, and tool call patterns.

Loading is idempotent: the service checks for a project named `"Sample Data - LLM Benchmark"` before inserting. If it already exists, it returns `409` with a message and an option (`clearFirst=true` query param) to delete and re-seed (FR-020 / US-5 AC-3).

**Rationale**: Database-level seeding via Flyway would run on every startup, making production contamination possible. API-triggered seeding gives the operator explicit control. The idempotency check satisfies US-5 AC-3.
