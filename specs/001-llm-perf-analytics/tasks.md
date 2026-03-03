# Tasks: LLM Performance Analytics Platform

**Input**: Design documents from `/specs/001-llm-perf-analytics/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/openapi.yaml ✅, quickstart.md ✅

**Tests**: Included per project constitution (Principle II: Testing Standards) — JUnit 5 + Mockito + TestContainers for backend; Vitest + React Testing Library + axe-core for frontend.

**Organization**: Tasks grouped by user story (P1 → P5) to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no pending dependencies within phase)
- **[Story]**: User story label (US1–US5) — required for US phases only
- Exact file paths are included in every task description

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project scaffolding — Maven POM, frontend tooling, Docker infrastructure, Spring Boot entry point

- [x] T001 Create `pom.xml` at repo root with Spring Boot 3.3.x parent, Java 21, all backend dependencies (Spring Web MVC, Data JPA, Security, Actuator, Validation, Flyway 10.x, HikariCP, Lombok, MapStruct 1.6, hibernate-types for JSONB) and `frontend-maven-plugin` wired to `frontend/` build
- [x] T002 [P] Create frontend tooling files: `frontend/package.json` (React 18, Vite 5, TypeScript, Ant Design 5.x, Recharts 2.x, TanStack Query 5.x, Axios 1.x, Vitest, React Testing Library 14, axe-core), `frontend/vite.config.ts` (proxy `/api` → `localhost:8080`), `frontend/tsconfig.json`, `frontend/vitest.config.ts`
- [x] T003 [P] Create `Dockerfile` (multi-stage: Node 20 build + Eclipse Temurin 21 JRE runtime), `docker-compose.yml` (services: `app` + `db` PostgreSQL 16), `.env.example` (`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `APP_API_KEY`)
- [x] T004 [P] Create Spring Boot entry point `src/main/java/com/llmanalytics/LlmAnalyticsApplication.java`, `src/main/resources/application.yml` (HikariCP defaults, Flyway, dev API key), `src/main/resources/application-docker.yml` (reads env vars for DB + API key)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T005 Create Flyway migration `src/main/resources/db/migration/V1__init_schema.sql` with all tables (`projects`, `experiments`, `iterations`, `interactions`, `tool_calls`) and all indexes defined in data-model.md (unique normalised_name indexes, composite iteration+started_at index, model + latency + started_at individual indexes)
- [x] T006 [P] Create Flyway migration `src/main/resources/db/migration/V2__seed_model_pricing.sql` with `model_pricing` table DDL and INSERT rows for all 7 seeded models (gpt-4o, gpt-4o-mini, gpt-4-turbo, claude-3-5-sonnet-20241022, claude-3-haiku-20240307, gemini-1.5-pro, gemini-1.5-flash)
- [x] T007 [P] Create `GzipStringConverter` (`AttributeConverter<String, byte[]>`) with `GZIPOutputStream`/`GZIPInputStream` at `src/main/java/com/llmanalytics/infra/persistence/GzipStringConverter.java`; handle null input and wrap `IOException` as `RuntimeException`
- [x] T008 [P] Create JPA entity `Project` with Lombok `@Data`/`@Builder`, UUID PK, `name`, `normalised_name` (UNIQUE), `description`, `created_at` at `src/main/java/com/llmanalytics/domain/model/Project.java`
- [x] T009 [P] Create JPA entity `Experiment` with UUID PK, `project_id` FK (ON DELETE CASCADE), `name`, `normalised_name`, `description`, `created_at`; UNIQUE constraint on `(project_id, normalised_name)` at `src/main/java/com/llmanalytics/domain/model/Experiment.java`
- [x] T010 [P] Create JPA entity `Iteration` with UUID PK, `experiment_id` FK (ON DELETE CASCADE), `name`, `normalised_name`, `description`, `created_at`; UNIQUE constraint on `(experiment_id, normalised_name)` at `src/main/java/com/llmanalytics/domain/model/Iteration.java`
- [x] T011 Create JPA entity `Interaction` with all fields from data-model.md; map `prompt_compressed` as `BYTEA` with `@Convert(converter = GzipStringConverter.class)`; map `response_metadata` as JSONB `String`; all derived fields (`latency_ms`, `total_tokens`, `tokens_per_second`, `estimated_cost`) stored at `src/main/java/com/llmanalytics/domain/model/Interaction.java` (depends on T007)
- [x] T012 [P] Create JPA entity `ToolCall` with UUID PK, `interaction_id` FK (ON DELETE CASCADE), `tool_name`, `input_arguments` (JSONB), `output` (JSONB), `sequence_order`, `called_at` at `src/main/java/com/llmanalytics/domain/model/ToolCall.java`
- [x] T013 [P] Create JPA entity `ModelPricing` with UUID PK, `model_identifier` (UNIQUE), `input_price_per_million_tokens`, `output_price_per_million_tokens`, `effective_from`, `notes` at `src/main/java/com/llmanalytics/domain/model/ModelPricing.java`
- [x] T014 Create Spring Data JPA repositories — `ProjectRepository`, `ExperimentRepository`, `IterationRepository`, `InteractionRepository`, `ToolCallRepository`, `ModelPricingRepository` — with named finder methods needed by services at `src/main/java/com/llmanalytics/domain/repository/` (depends on T008–T013)
- [x] T015 Create `ApiKeyAuthFilter` (`OncePerRequestFilter`, constant-time `MessageDigest.isEqual` comparison, key from `app.security.api-key` property) and `SecurityConfig` (secure `/api/**`, permit SPA routes) at `src/main/java/com/llmanalytics/config/`
- [x] T016 [P] Create `GlobalExceptionHandler` (`@ControllerAdvice`) handling `MethodArgumentNotValidException` (422), `DuplicateNameException` (409), `EntityNotFoundException` (404), and generic `Exception` (500); create Java record types `SuccessEnvelope`, `ErrorEnvelope`, `FieldError` at `src/main/java/com/llmanalytics/api/`
- [x] T017 [P] Create `SpaFallbackController` with `@RequestMapping("/{path:^(?!api).*}")` forwarding to `/index.html` at `src/main/java/com/llmanalytics/config/SpaFallbackController.java`
- [x] T018 [P] Create React app skeleton: `frontend/src/main.tsx` (render root), `frontend/src/App.tsx` (React Router `BrowserRouter` with placeholder routes for `/`, `/comparison`, `/interactions`), `frontend/src/components/Layout.tsx` (Ant Design `Layout` with side navigation and top bar)
- [x] T019 [P] Create TypeScript interfaces mirroring all API DTOs from `contracts/openapi.yaml` including `ProjectResponse`, `ExperimentResponse`, `IterationResponse`, `InteractionSummaryResponse`, `InteractionDetailResponse`, `ToolCallResponse`, `DashboardSummaryResponse`, `IterationMetrics`, `PagedResponse`, `SuccessEnvelope`, `ErrorEnvelope` at `frontend/src/types/api.ts`
- [x] T020 [P] Create Axios base client with `X-API-Key` header injected from Vite env var `VITE_API_KEY`; typed response unwrapper for `SuccessEnvelope` and `ErrorEnvelope` at `frontend/src/services/apiClient.ts`
- [x] T085 [P] Create `ProjectSelector` component (Ant Design `Select` with search, populated by `GET /api/projects`) and store the active project ID in React context (`ProjectContext`) at `frontend/src/components/ProjectSelector.tsx` and `frontend/src/context/ProjectContext.tsx`; wire into `frontend/src/components/Layout.tsx` top bar so all pages (Dashboard, Comparison, Interactions) can consume `useProjectContext()` to scope their queries — required by FR-008b (navigation scoped to selected project)

**Checkpoint**: Foundation complete — entities, migrations, security, error handling, and React skeleton ready. All user story phases can now begin.

---

## Phase 3: User Story 1 — Ingest Interaction Data via REST API (Priority: P1) 🎯 MVP

**Goal**: Automated systems can POST interaction records to `/api/interactions`; the platform stores them with all derived metrics and exposes them via GET. Full Project → Experiment → Iteration hierarchy is auto-created on ingest.

**Independent Test**: `POST /api/interactions` with a valid payload returns 201 and the stored record (with `latency_ms`, `total_tokens`, `estimated_cost` computed). `GET /api/interactions/{id}` returns the same record with decompressed prompt. No UI required.

### Tests for User Story 1 (REQUIRED per constitution — Principle II) ✅

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T021 [P] [US1] Create unit tests for `InteractionService` covering ingest happy path, `ended_at < started_at` (422), `tokens_out = 0` (valid), null prompt (valid), model with no pricing (null cost) at `src/test/java/com/llmanalytics/service/InteractionServiceTest.java`
- [x] T022 [P] [US1] Create unit tests for `ProjectService` covering findOrCreate (new + existing), normalised name collision (409), and experiment/iteration delegation at `src/test/java/com/llmanalytics/service/ProjectServiceTest.java`
- [x] T023 [P] [US1] Create MockMVC controller tests for `POST /api/interactions` (valid → 201, missing fields → 422, invalid key → 401, `ended_at` before `started_at` → 422) and `GET /api/projects` (200 list) at `src/test/java/com/llmanalytics/api/InteractionControllerTest.java`
- [x] T024 [P] [US1] Create MockMVC tests for `GET/POST /api/projects`, `GET/DELETE /api/projects/{id}`, `GET /api/projects/{id}/experiments`, `GET/DELETE /api/experiments/{id}`, `GET /api/experiments/{id}/iterations` covering 200/201/204/404/409 responses at `src/test/java/com/llmanalytics/api/ProjectControllerTest.java`
- [x] T025 [US1] Create TestContainers full-stack integration test: start PostgreSQL, run Flyway, POST an interaction (new hierarchy auto-created), GET by ID, verify all derived fields and decompressed prompt, DELETE and verify 404 at `src/test/java/com/llmanalytics/integration/IngestIntegrationTest.java`

### Implementation for User Story 1

- [x] T026 [P] [US1] Create request and response DTOs as Java records: `CreateInteractionRequest` (with Bean Validation annotations), `InteractionSummaryResponse`, `InteractionDetailResponse`, `ToolCallRequest`, `ToolCallResponse`, `CreateProjectRequest`, `ProjectResponse`, `ExperimentResponse`, `IterationResponse` at `src/main/java/com/llmanalytics/api/dto/`
- [x] T027 [P] [US1] Create custom `@EndedAtAfterStartedAt` constraint annotation and `EndedAtValidator` (`ConstraintValidator`) that checks `ended_at >= started_at` at `src/main/java/com/llmanalytics/api/validation/`
- [x] T028 [P] [US1] Create MapStruct mappers for `Project → ProjectResponse`, `Experiment → ExperimentResponse`, `Iteration → IterationResponse`, `Interaction → InteractionSummaryResponse/InteractionDetailResponse`, `ToolCall → ToolCallResponse` at `src/main/java/com/llmanalytics/api/dto/mapper/`
- [x] T029 [US1] Implement `ProjectService`: `findOrCreate(name, description)` with whitespace-normalised uniqueness check (throws `DuplicateNameException` on standalone create conflict); `findById`, `findAll`, `deleteById` at `src/main/java/com/llmanalytics/domain/service/ProjectService.java`
- [x] T030 [US1] Implement `ExperimentService`: `findOrCreate(projectId, name)` within project; `findById`, `findByProjectId`, `deleteById` at `src/main/java/com/llmanalytics/domain/service/ExperimentService.java`
- [x] T031 [US1] Implement `IterationService`: `findOrCreate(experimentId, name)`; `findById`, `findByExperimentId` at `src/main/java/com/llmanalytics/domain/service/IterationService.java`
- [x] T032 [US1] Implement `InteractionService`: `ingest(CreateInteractionRequest)` — resolve/create hierarchy via ProjectService/ExperimentService/IterationService → compute `latency_ms`, `total_tokens`, `tokens_per_second` (null if `latency_ms=0`), `estimated_cost` (null if no pricing row) → persist `Interaction` + `ToolCall` list → return `InteractionDetailResponse` at `src/main/java/com/llmanalytics/domain/service/InteractionService.java`
- [x] T033 [US1] Implement `InteractionController`: `POST /api/interactions` (→ `InteractionService.ingest`), `GET /api/interactions/{id}` (detail with prompt + tool calls), `DELETE /api/interactions/{id}` at `src/main/java/com/llmanalytics/api/controller/InteractionController.java`
- [x] T034 [US1] Implement `ProjectController`: `GET /api/projects` (paginated), `POST /api/projects`, `GET /api/projects/{id}`, `DELETE /api/projects/{id}` at `src/main/java/com/llmanalytics/api/controller/ProjectController.java`
- [x] T035 [US1] Implement `ExperimentController`: `GET /api/projects/{projectId}/experiments`, `DELETE /api/experiments/{id}`; `IterationController`: `GET /api/experiments/{experimentId}/iterations` at `src/main/java/com/llmanalytics/api/controller/`

**Checkpoint**: User Story 1 fully functional — POST an interaction via curl per `quickstart.md` step 5 and verify 201 + derived metrics

---

## Phase 4: User Story 2 — View Performance Dashboard (Priority: P2)

**Goal**: Engineers open the web dashboard and immediately see total interactions, average latency, total tokens, estimated cost, and 4 charts (timeline, token usage by model, latency distribution, tool usage frequency) for the last 30 days. Empty state guides toward data loading.

**Independent Test**: Load the dashboard against seeded sample data; verify 4 metric cards and 4 charts render without error. Change the date-range picker; verify all cards and charts update without page reload.

### Tests for User Story 2 (REQUIRED per constitution — Principle II) ✅

- [x] T036 [P] [US2] Create unit tests for `DashboardService` covering summary metrics (with + without date filters), empty dataset (all-zero/null), timeline bucketing, token-by-model aggregation at `src/test/java/com/llmanalytics/service/DashboardServiceTest.java`
- [x] T037 [P] [US2] Create MockMVC tests for all 5 dashboard endpoints (`/api/dashboard/summary`, `/timeline`, `/tokens-by-model`, `/latency-distribution`, `/tool-usage`) with and without project/date filters at `src/test/java/com/llmanalytics/api/DashboardControllerTest.java`
- [x] T038 [P] [US2] Create Vitest + React Testing Library tests for `Dashboard` page: renders metric cards from mocked API data, shows empty state when API returns zero interactions, RangePicker change triggers query refetch; run axe-core assertion for WCAG 2.1 AA at `frontend/src/pages/Dashboard/Dashboard.test.tsx`
- [x] T081 [P] [US2] Create Playwright E2E test covering US2 primary acceptance scenario: start Docker Compose, seed sample data via `POST /api/admin/sample-data`, navigate to `localhost:8080`, assert 4 metric cards are visible with non-zero values, assert 4 charts render, change RangePicker to last 7 days, assert cards update without page reload at `e2e/dashboard.spec.ts`

### Implementation for User Story 2

- [x] T039 [P] [US2] Create backend response record types: `TimelineDataPoint`, `TokensByModelEntry`, `LatencyBucketEntry`, `ToolUsageEntry` at `src/main/java/com/llmanalytics/api/dto/`
- [x] T040 [US2] Add JPQL aggregate queries to `InteractionRepository` (summary metrics, timeline `DATE_TRUNC` by granularity, tokens grouped by model, latency histogram bucket counts) and to `ToolCallRepository` (tool frequency grouped by `tool_name`) at `src/main/java/com/llmanalytics/domain/repository/`
- [x] T041 [US2] Implement `DashboardService` orchestrating all 5 aggregate queries; default `from = now - 30 days`, `to = now`; scoped to `projectId` when provided at `src/main/java/com/llmanalytics/domain/service/DashboardService.java`
- [x] T042 [US2] Implement `DashboardController`: `GET /api/dashboard/summary`, `GET /api/dashboard/timeline` (with `granularity` param), `GET /api/dashboard/tokens-by-model`, `GET /api/dashboard/latency-distribution` (with `buckets` param), `GET /api/dashboard/tool-usage` (with `limit` param) at `src/main/java/com/llmanalytics/api/controller/DashboardController.java`
- [x] T043 [P] [US2] Create dashboard API service calling all 5 endpoints with typed responses at `frontend/src/services/dashboardService.ts`
- [x] T044 [P] [US2] Create reusable Recharts wrapper components: `TimelineChart` (`AreaChart`), `TokensByModelChart` (`BarChart`), `LatencyDistributionChart` (histogram `BarChart`), `ToolUsageChart` (`BarChart` with `Cell` coloring) — all wrapped in `ResponsiveContainer` at `frontend/src/components/charts/`
- [x] T045 [P] [US2] Create shared UI components: `MetricCard` (Ant Design `Statistic`), `EmptyState` (guidance + sample data CTA), `LoadingSkeleton` (Ant Design `Skeleton`) at `frontend/src/components/`
- [x] T046 [US2] Implement `useDashboard` TanStack Query hook managing all 5 dashboard queries, accepting `{ projectId?, from, to }` params; invalidates on date range change at `frontend/src/hooks/useDashboard.ts`
- [x] T047 [US2] Implement `Dashboard` page: Ant Design `DatePicker.RangePicker` (default: last 30 days), 4 `MetricCard` components, 4 chart components from T044, `EmptyState` when no data, `LoadingSkeleton` during load; register `/` route in `frontend/src/App.tsx` at `frontend/src/pages/Dashboard/Dashboard.tsx`

**Checkpoint**: Dashboard renders correctly against sample data. Date range picker updates all cards and charts.

---

## Phase 5: User Story 3 — Compare Iterations Side-by-Side (Priority: P3)

**Goal**: Prompt engineers select 2+ iterations and see normalised metrics (avg latency, avg tokens in/out, tool call rate, estimated cost per run) in a table and bar/radar charts highlighting differences.

**Independent Test**: Select any 2 iterations from pre-seeded data in the Comparison view; verify the metrics table and both charts render correct averaged values. Verify single-iteration warning is shown when only 1 iteration is available.

### Tests for User Story 3 (REQUIRED per constitution — Principle II) ✅

- [x] T048 [P] [US3] Create unit tests for `ComparisonService` covering 2-iteration comparison, normalised averages, missing pricing (null cost), and request with < 2 iterations (400) at `src/test/java/com/llmanalytics/service/ComparisonServiceTest.java`
- [x] T049 [P] [US3] Create MockMVC tests for `GET /api/comparison` with 2 valid IDs (200 + metrics), 1 ID (400), unknown ID (404) at `src/test/java/com/llmanalytics/api/ComparisonControllerTest.java`
- [x] T050 [P] [US3] Create Vitest + React Testing Library tests for `Comparison` page: renders metrics table with mocked data, shows single-iteration warning, axe-core WCAG assertion at `frontend/src/pages/Comparison/Comparison.test.tsx`
- [x] T082 [P] [US3] Create Playwright E2E test covering US3 primary acceptance scenario: with sample data loaded, navigate to `/comparison`, select any 2 pre-seeded iterations, assert metrics table appears with `avg_latency_ms` and `avg_tokens_in` columns populated, assert bar chart and radar chart render; assert single-iteration warning shows when only 1 iteration selected at `e2e/comparison.spec.ts`

### Implementation for User Story 3

- [x] T051 [P] [US3] Verify `IterationMetrics` DTO covers all fields from `openapi.yaml` (`avg_latency_ms`, `avg_tokens_in`, `avg_tokens_out`, `avg_total_tokens`, `tool_call_rate`, `avg_estimated_cost`, `interaction_count`) at `src/main/java/com/llmanalytics/api/dto/IterationMetrics.java`
- [x] T052 [US3] Add JPQL aggregate queries to `InteractionRepository` for per-iteration `AVG` metrics and to `ToolCallRepository` for per-interaction tool call count average at `src/main/java/com/llmanalytics/domain/repository/`
- [x] T053 [US3] Implement `ComparisonService`: accept list of iteration IDs (min 2, else throw), fetch aggregate metrics per iteration, return list of `IterationMetrics` at `src/main/java/com/llmanalytics/domain/service/ComparisonService.java`
- [x] T054 [US3] Implement `ComparisonController`: `GET /api/comparison` with `iterationIds` query param array (min 2); return 400 on < 2 IDs at `src/main/java/com/llmanalytics/api/controller/ComparisonController.java`
- [x] T055 [P] [US3] Create comparison API service calling `GET /api/comparison` with typed `IterationMetrics[]` response at `frontend/src/services/comparisonService.ts`
- [x] T056 [P] [US3] Implement `useComparison` TanStack Query hook accepting selected iteration IDs; disabled when fewer than 2 IDs selected at `frontend/src/hooks/useComparison.ts`
- [x] T057 [US3] Implement `Comparison` page: cascading Ant Design `Select` for Project → Experiment → Iteration multi-select, metrics summary `Table`, `BarChart` comparing avg latency/tokens per iteration, `RadarChart` for multi-metric comparison, alert when < 2 iterations available; register `/comparison` route in `frontend/src/App.tsx` at `frontend/src/pages/Comparison/Comparison.tsx`

**Checkpoint**: Side-by-side comparison of 2+ pre-seeded iterations renders correct normalised metrics and both charts.

---

## Phase 6: User Story 4 — Explore Interaction Details and Filter Records (Priority: P4)

**Goal**: Developers browse a filterable, paginated list of all interactions and drill into any one to see full prompt text, all metrics, and tool call log.

**Independent Test**: Filter the interaction list by model name (e.g., "gpt-4o"); only matching rows appear. Click one; the detail drawer shows the decompressed full prompt, all derived metrics, and each tool call with name + arguments + output.

### Tests for User Story 4 (REQUIRED per constitution — Principle II) ✅

- [x] T058 [P] [US4] Create unit tests for filtering + pagination logic in `InteractionQueryService`: model filter, date range filter, latency range filter, combined filters, empty result at `src/test/java/com/llmanalytics/service/InteractionQueryServiceTest.java`
- [x] T059 [P] [US4] Create MockMVC tests for `GET /api/interactions` with each supported filter param (model, from/to, minLatencyMs, maxLatencyMs, iterationId) and pagination params; also `GET /api/interactions/{id}` detail with tool calls at `src/test/java/com/llmanalytics/api/InteractionListControllerTest.java`
- [x] T060 [P] [US4] Create Vitest + React Testing Library tests for `InteractionsList` page (filter toolbar renders, table shows mocked data, pagination) and `InteractionDetailDrawer` (all fields visible, tool calls listed, axe-core WCAG check) at `frontend/src/pages/Interactions/Interactions.test.tsx`
- [x] T083 [P] [US4] Create Playwright E2E test covering US4 primary acceptance scenario: with sample data loaded, navigate to `/interactions`, filter by model name "gpt-4o", assert only gpt-4o rows visible in table, click first row, assert detail drawer opens with prompt text, `latency_ms`, and tool call list populated at `e2e/interactions.spec.ts`

### Implementation for User Story 4

- [x] T061 [P] [US4] Create `InteractionFilterParams` record (model, experimentId, iterationId, from, to, minLatencyMs, maxLatencyMs, page, size, sort) as a `@ParameterObject` Spring request param binding object at `src/main/java/com/llmanalytics/api/dto/InteractionFilterParams.java`
- [x] T062 [US4] Add JPA `Specification<Interaction>` or dynamic JPQL building in `InteractionRepository` for the multi-predicate filtered + sorted paginated list; use DTO projection interface `InteractionSummaryProjection` that excludes `prompt_compressed` at `src/main/java/com/llmanalytics/domain/repository/InteractionRepository.java`
- [x] T063 [US4] Implement `InteractionQueryService`: `findAll(InteractionFilterParams)` → `Page<InteractionSummaryResponse>` (projection, no BYTEA loaded); `findById(UUID)` → `InteractionDetailResponse` (with decompressed prompt + tool calls) at `src/main/java/com/llmanalytics/domain/service/InteractionQueryService.java`
- [x] T064 [US4] Extend `InteractionController` with `GET /api/interactions` accepting all filter params (bound via `InteractionFilterParams`), returning `PagedResponse<InteractionSummaryResponse>`; `GET /api/interactions/{id}` returning `InteractionDetailResponse` at `src/main/java/com/llmanalytics/api/controller/InteractionController.java`
- [x] T065 [P] [US4] Create interactions API service: `listInteractions(params)` (typed paged response), `getInteraction(id)` at `frontend/src/services/interactionsService.ts`
- [x] T066 [P] [US4] Implement `useInteractions` TanStack Query hook with `keepPreviousData` for smooth pagination, accepting `InteractionFilterState` at `frontend/src/hooks/useInteractions.ts`
- [x] T067 [US4] Implement `InteractionsList` page: Ant Design `Table` (columns: timestamp, model, experiment, iteration, latency, total tokens, tool call count), filter toolbar (model `Select`, date `RangePicker`, latency range inputs, experiment `Select`), server-side sorting and pagination; clicking a row opens `InteractionDetailDrawer`; register `/interactions` route in `frontend/src/App.tsx` at `frontend/src/pages/Interactions/InteractionsList.tsx`
- [x] T068 [US4] Implement `InteractionDetailDrawer` (Ant Design `Drawer`): displays all metric fields in a `Descriptions` block, full decompressed prompt in a `pre`/`code` block, `response_metadata` JSON, tool calls in a nested `Table` (name, input_arguments, output) at `frontend/src/pages/Interactions/InteractionDetailDrawer.tsx`

**Checkpoint**: Filter by model → only matching rows shown. Click row → drawer shows full prompt and tool calls.

---

## Phase 7: User Story 5 — Load and Explore Sample Data (Priority: P5)

**Goal**: A new user triggers "Load Sample Data" from the empty-state dashboard and immediately sees a fully populated system: 3+ projects, 3+ experiments with 2+ iterations each, 50+ varied interactions across ≥ 2 model identifiers — all ready for dashboard, comparison, and exploration views.

**Independent Test**: Call `POST /api/admin/sample-data` on a clean system; response body contains counts (≥ 3 projects, ≥ 50 interactions). Navigate to comparison view; a pre-seeded experiment with 2+ iterations is selectable. Call the endpoint again on populated data; response warns about duplication.

### Tests for User Story 5 (REQUIRED per constitution — Principle II) ✅

- [x] T069 [P] [US5] Create unit tests for `SampleDataLoader`: verify seeding counts (projects ≥ 3, experiments ≥ 3, iterations ≥ 6, interactions ≥ 50), duplicate detection logic (returns warning when data exists) at `src/test/java/com/llmanalytics/service/SampleDataLoaderTest.java`
- [x] T070 [P] [US5] Create MockMVC test for `POST /api/admin/sample-data` (201 + `SampleDataResponse` counts on empty system; 200 + duplication warning message on populated system; 401 on missing key) at `src/test/java/com/llmanalytics/api/AdminControllerTest.java`
- [x] T071 [P] [US5] Create Vitest tests for the `EmptyState` component's "Load Sample Data" button: mocked API call fires on click, loading spinner shown, success triggers dashboard data refetch at `frontend/src/pages/Dashboard/Dashboard.test.tsx` (extend existing file)
- [x] T084 [P] [US5] Create Playwright E2E test covering US5 primary acceptance scenario: start with empty system, click "Load Sample Data" button from the empty-state dashboard, assert Ant Design success notification appears, assert at least 4 metric cards show non-zero values, navigate to `/comparison` and assert at least one experiment with 2+ iterations is selectable at `e2e/sampledata.spec.ts`

### Implementation for User Story 5

- [x] T072 [US5] Implement `SampleDataLoader` service creating 3 projects (e.g., "Customer Support Bot", "Code Review Assistant", "Document Summarizer"), 1+ experiment each with 2+ named iterations, and 50+ `Interaction` records with varied models (gpt-4o, gpt-4o-mini, claude-3-5-sonnet-20241022), varied token counts, timestamps spread across the last 30 days, and varied tool call patterns; check for existing data flag and return warning without re-seeding at `src/main/java/com/llmanalytics/infra/seed/SampleDataLoader.java`
- [x] T073 [US5] Implement `AdminController`: `POST /api/admin/sample-data` calls `SampleDataLoader`, returns 201 + `SampleDataResponse` (counts) on clean load, returns 200 + warning message if data already present at `src/main/java/com/llmanalytics/api/controller/AdminController.java`
- [x] T074 [US5] Add "Load Sample Data" button and duplicate-detection confirmation `Modal` to the `EmptyState` component; on success display Ant Design `notification.success` and invalidate all dashboard queries via TanStack Query client at `frontend/src/components/EmptyState.tsx` (update) and `frontend/src/pages/Dashboard/Dashboard.tsx` (wire handler)

**Checkpoint**: Fresh system → "Load Sample Data" → dashboard, comparison, and interaction list all show meaningful populated data.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Performance tuning, code quality tooling, and final end-to-end validation across all user stories

- [x] T075 [P] Add HikariCP pool tuning (`minimum-idle: 5`, `maximum-pool-size: 20`, `connection-timeout: 30s`) and Spring Boot Actuator health check endpoint config to `src/main/resources/application.yml` and `src/main/resources/application-docker.yml`
- [x] T076 [P] Configure Vite `manualChunks` code-splitting (vendor bundle for Ant Design + Recharts) and wrap all page imports in `React.lazy()` + `<Suspense>` in `frontend/src/App.tsx`; verify Lighthouse LCP ≤ 2.5 s budget in `frontend/vite.config.ts`
- [x] T077 [P] Add SpotBugs Maven plugin (`spotbugs-maven-plugin`) and Checkstyle plugin (`maven-checkstyle-plugin`) with `checkstyle.xml` config (cyclomatic complexity ≤ 10 rule) to `pom.xml`; bind to `verify` lifecycle phase
- [x] T078 [P] Configure ESLint (`@typescript-eslint`, `eslint-plugin-react-hooks`, `eslint-plugin-jsx-a11y`) and Prettier for frontend; add `lint` and `format` scripts to `frontend/package.json`; create `frontend/.eslintrc.json` and `frontend/.prettierrc.json`
- [x] T080 [P] Create k6 load test script at `k6/ingest-load-test.js` targeting `POST /api/interactions` and all 5 `GET /api/dashboard/*` endpoints at 100 VUs (30 s ramp + 60 s sustain); assert p95 ≤ 1 s for ingest, p95 ≤ 200 ms for all dashboard reads; configure a CI workflow step (`.github/workflows/perf.yml`) to run k6 against a Docker Compose test environment and fail the build on any threshold breach — satisfies Constitution Principle IV performance regression CI requirement
- [x] T079 Run end-to-end quickstart validation per `quickstart.md`: `docker compose up --build` → access `localhost:8080` → `POST /api/admin/sample-data` → verify dashboard charts render → navigate to comparison view with 2 iterations → navigate to interactions list and open detail drawer → confirm all acceptance scenarios from spec.md pass

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 completion — BLOCKS all user story phases
- **Phases 3–7 (User Stories)**: All depend on Phase 2 completion
  - Stories are independent of each other and can proceed in priority order (P1 → P2 → P3 → P4 → P5) or in parallel if team capacity allows
- **Phase 8 (Polish)**: Depends on all desired user stories being complete before T079 validation

### User Story Dependencies

| Story | Depends On | Notes |
|-------|-----------|-------|
| US1 (P1 — Ingest) | Phase 2 only | Pure backend; no UI. Foundation for all data |
| US2 (P2 — Dashboard) | Phase 2 only | Backend queries independent of US1 controllers; shares entities |
| US3 (P3 — Comparison) | Phase 2 only | Can be built and tested independently using seeded data |
| US4 (P4 — Exploration) | Phase 2 only | Interaction list + detail; reuses `InteractionRepository` queries |
| US5 (P5 — Sample Data) | US1 (ingest path) | `SampleDataLoader` calls `InteractionService.ingest` internally |

### Within Each User Story

1. Tests (T0xx) → write first, verify they FAIL
2. DTOs + Mappers (parallel) → before services
3. Services → before controllers
4. Backend complete → start frontend in parallel
5. API service + hook (parallel) → before page component
6. Page component → register route in `App.tsx`

---

## Parallel Execution Examples

### Phase 2 Parallel Opportunities

```
T005 (V1 schema)
T006 [P] (V2 pricing)     ──┐
T007 [P] (GzipConverter)  ──┤
T008 [P] (Project entity) ──┤ All can be written simultaneously
T009 [P] (Experiment)     ──┤
T010 [P] (Iteration)      ──┤
T012 [P] (ToolCall)       ──┤
T013 [P] (ModelPricing)   ──┘
T011 (Interaction)        → after T007 (GzipConverter)
T014 (Repositories)       → after T008–T013
T015 / T016 / T017 [P]   → in parallel (security, error handler, SPA fallback)
T018 / T019 / T020 [P]   → in parallel (React skeleton, types, Axios client)
```

### Phase 3 (US1) Parallel Opportunities

```
T021 [P] (InteractionServiceTest)   ──┐
T022 [P] (ProjectServiceTest)       ──┤ Write all tests in parallel
T023 [P] (InteractionControllerTest)──┤
T024 [P] (ProjectControllerTest)    ──┘
T025 (IngestIntegrationTest)        → after T021–T024

T026 [P] (DTOs)    ──┐
T027 [P] (Validation)──┤ All parallel (different files)
T028 [P] (Mappers) ──┘
T029 → T030 → T031 → T032 (service chain, sequential)
T033 / T034 / T035 (controllers, parallel once services done)
```

### Phase 4 (US2) Parallel Opportunities

```
T036 [P] (DashboardServiceTest)    ──┐
T037 [P] (DashboardControllerTest) ──┤ Write in parallel
T038 [P] (Dashboard.test.tsx)      ──┘
T039 [P] (DTOs) → T040 → T041 → T042 (backend chain)
T043 [P] (API service) ──┐
T044 [P] (Charts)       ──┤ Frontend parallel while backend is in progress
T045 [P] (UI components)──┘
T046 → T047 (hook then page, sequential)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (~4 tasks)
2. Complete Phase 2: Foundational (~16 tasks)
3. Complete Phase 3: User Story 1 (~15 tasks)
4. **STOP AND VALIDATE**: `curl -X POST http://localhost:8080/api/interactions` per quickstart.md step 5
5. Deploy/demo the standalone ingest API if ready

### Incremental Delivery

| Step | Phase | Capability Added | Validation |
|------|-------|-----------------|-----------|
| 1 | 1 + 2 | Infrastructure ready | `mvn package` succeeds |
| 2 | 3 (US1) | REST ingest API | Post interaction via curl, verify 201 + derived metrics |
| 3 | 4 (US2) | Performance dashboard | Dashboard loads with 4 charts from sample data |
| 4 | 5 (US3) | Iteration comparison | Select 2 iterations, verify side-by-side metrics |
| 5 | 6 (US4) | Interaction explorer | Filter by model, open detail drawer |
| 6 | 7 (US5) | One-click sample data | "Load Sample Data" populates entire platform |
| 7 | 8 | Hardened production build | Docker Compose end-to-end quickstart passes |

### Parallel Team Strategy

With 3 developers after Phase 2 completion:
- **Developer A**: US1 (ingest backend) → US5 (sample data)
- **Developer B**: US2 (dashboard backend + frontend)
- **Developer C**: US3 (comparison) → US4 (exploration)

---

## Summary

| Phase | Tasks | Scope |
|-------|-------|-------|
| 1 — Setup | T001–T004 | Maven POM, frontend tooling, Docker, Spring Boot app skeleton |
| 2 — Foundational | T005–T020 | DB schema, JPA entities, repositories, security, error handling, React skeleton |
| 3 — US1 (P1 MVP) | T021–T035 | Full ingest REST API + project hierarchy auto-create |
| 4 — US2 (P2) | T036–T047 | Performance dashboard + 5 backend query endpoints + React Dashboard page |
| 5 — US3 (P3) | T048–T057 | Iteration comparison backend + React Comparison page |
| 6 — US4 (P4) | T058–T068 | Interaction list/filter backend + React Interactions page + detail drawer |
| 7 — US5 (P5) | T069–T074 | Sample data seed service + admin endpoint + UI button |
| 8 — Polish | T075–T080 | Performance tuning, code quality plugins, ESLint, perf regression CI, end-to-end validation |
| **E2E additions** | T081–T084 | Playwright E2E per US2–US5 (added to phases 4–7 respectively) |
| **Phase 2 addition** | T085 | ProjectSelector component + ProjectContext (added to Phase 2) |
| **Total** | **85 tasks** | |

- **Parallel opportunities**: 40+ tasks marked `[P]` — parallelisable within their phase
- **MVP scope**: Complete Phases 1–3 (T001–T035) for a fully functional ingest API
- **Suggested first milestone**: Phase 1 + Phase 2 + Phase 3 (T001–T035) — deployable REST API without UI
