# Feature Specification: LLM Performance Analytics Platform

**Feature Branch**: `001-llm-perf-analytics`  
**Created**: 2026-03-02  
**Status**: Draft  
**Input**: User description: "Create a web-based platform that can be used to capture performance analysis for LLM interactions / chats using prompt sent, tokens in, tokens out, time started, time ended, tools called, etceteras. Make it possible to store different iterations and make it possible to show graphs and all the other relevant ways that one can determine performance. Include a REST API so results can be uploaded. Include sample data so we can see how it would look like."

## Overview

A web-based analytics platform that collects, stores, and visualises performance data from LLM (Large Language Model) interactions. Teams running LLM-powered systems can upload interaction records—including prompts, token counts, latency, and tool calls—via a REST API or through the web UI, then explore that data through dashboards, charts, and side-by-side iteration comparisons to understand and improve the performance of their AI systems.

## Clarifications

### Session 2026-03-02

- Q: How long should interaction records be retained before automatic deletion? → A: Keep indefinitely — no automatic deletion; manual deletion only.
- Q: What are the scoping rules for experiment and iteration name uniqueness? → A: Interactions are grouped by a unique project name; the UI reflects the full hierarchy: Project → Experiment → Iteration → Interaction. Project names are globally unique.
- Q: Should the REST API enforce a request rate limit to prevent flooding? → A: No rate limiting — accept all requests unconditionally.
- Q: What time window should the dashboard show by default? → A: Last 30 days by default; user can select a different range via a date-range picker.
- Q: How should prompt text be stored? → A: Always store the full prompt text with no size limit, compressed with gzip to minimise storage footprint.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ingest Interaction Data via REST API (Priority: P1)

An automated system (CI pipeline, integration harness, or LLM wrapper library) sends a POST request to the platform after each LLM interaction, uploading the captured metrics. This is the core data ingestion path and must work as a standalone capability before any UI is built.

**Why this priority**: Without reliable data ingestion, nothing else has value. This is the foundation of the platform.

**Independent Test**: Can be fully tested by sending a POST request with a valid interaction payload and verifying the data is stored and returned via a GET request. No UI required.

**Acceptance Scenarios**:

1. **Given** a valid interaction payload (prompt, tokens_in, tokens_out, started_at, ended_at, model, tools_called), **When** a POST request is made to `/api/interactions`, **Then** the system stores the record and returns a 201 response with the created record including an assigned ID.
2. **Given** an incomplete payload missing required fields, **When** a POST request is made, **Then** the system returns a 422 response with a clear error listing missing fields.
3. **Given** an invalid API key, **When** a POST request is made, **Then** the system returns a 401 response.
4. **Given** a payload with optional fields omitted (e.g., no tools called), **When** posted, **Then** the record is saved successfully with default/null values for omitted optional fields.

---

### User Story 2 - View Performance Dashboard (Priority: P2)

An engineer opens the web dashboard to get an immediate overview of LLM performance across all recorded interactions: aggregate token usage, average latency, most-used models, and recent activity.

**Why this priority**: The dashboard is the primary interface. Once data is being ingested, users need a way to see it without writing queries.

**Independent Test**: Can be fully tested by loading the dashboard against pre-seeded sample data and verifying that metric cards and charts render with correct values.

**Acceptance Scenarios**:

1. **Given** sample data is loaded, **When** a user opens the dashboard, **Then** they see summary cards showing total interactions, average latency, total tokens consumed, and total estimated cost.
2. **Given** interactions from multiple models are stored, **When** the dashboard loads, **Then** a chart displays token usage grouped by model.
3. **Given** interactions spanning multiple days, **When** the dashboard loads, **Then** a timeline chart shows interaction volume over time.
4. **Given** no data is ingested yet, **When** the dashboard loads, **Then** it displays an empty-state message with guidance on how to upload data or load sample data.

---

### User Story 3 - Compare Iterations Side-by-Side (Priority: P3)

A prompt engineer runs the same prompt with different models or configuration settings across multiple named iterations (e.g., "gpt-4o-baseline", "gpt-4o-concise-prompt") and uses the platform to compare their performance metrics side-by-side.

**Why this priority**: Iteration comparison is the core analytical workflow for prompt engineering and model evaluation.

**Independent Test**: Can be fully tested by creating two iterations with seeded data and navigating to the comparison view to verify metrics are shown side by side.

**Acceptance Scenarios**:

1. **Given** two or more iterations in an experiment, **When** a user selects them in the comparison view, **Then** a table shows each iteration's average latency, average tokens in/out, tool call frequency, and estimated cost.
2. **Given** the comparison view is active, **When** a user looks at the charts, **Then** bar/radar charts visually highlight the differences between the selected iterations.
3. **Given** an experiment with a single iteration, **When** a user opens the comparison view, **Then** the system notifies them that at least two iterations are needed and offers to add another.
4. **Given** iterations with different numbers of runs, **When** compared, **Then** the platform normalises metrics (averages) so the comparison is meaningful.

---

### User Story 4 - Explore Interaction Details and Filter Records (Priority: P4)

A developer needs to investigate a specific slow or expensive interaction. They browse the interactions list, filter by model, date range, or experiment, and drill into a single interaction to see the full prompt, response metadata, tool invocation log, and all raw metrics.

**Why this priority**: Detailed inspection enables root-cause analysis and quality control.

**Independent Test**: Can be fully tested by filtering the interactions list by model name and verifying only matching records appear, then clicking one to check all fields are displayed.

**Acceptance Scenarios**:

1. **Given** a list of interactions, **When** a user filters by model name, **Then** only interactions for that model are shown.
2. **Given** a list of interactions, **When** a user filters by date range, **Then** only interactions within that range are shown.
3. **Given** an interaction in the list, **When** a user clicks on it, **Then** a detail view shows: full prompt text, full response metadata, all captured metrics, tool calls with names and arguments, and calculated derived metrics (latency, tokens/sec).
4. **Given** an interaction with multiple tool calls, **When** the detail view is open, **Then** each tool call is listed with its name, input arguments, and output.

---

### User Story 5 - Load and Explore Sample Data (Priority: P5)

A new user wants to see the platform in action without integrating their own systems first. They load the included sample dataset with a single action and the dashboard immediately displays meaningful charts and records.

**Why this priority**: Sample data is essential for onboarding, demos, and testing the platform without needing a live integration.

**Independent Test**: Can be fully tested by triggering the sample data load from the UI or API and verifying charts, dashboards, and comparison views are populated with representative data.

**Acceptance Scenarios**:

1. **Given** an empty platform, **When** a user selects "Load Sample Data" from the dashboard, **Then** the platform seeds at least 3 experiments with at least 2 iterations each, containing varied models, token counts, and tool call patterns.
2. **Given** sample data is loaded, **When** the user navigates to the comparison view, **Then** at least one pre-configured experiment is ready for comparison without additional setup.
3. **Given** sample data is already present, **When** the user selects "Load Sample Data" again, **Then** the system warns about duplication and offers an option to clear existing data first.

---

### Edge Cases

- What happens when a payload contains tokens_out of 0 (e.g., the model was interrupted or errored)?
- What happens when `ended_at` is before `started_at` — invalid latency?
- How does the system handle very large prompts (e.g., 100k characters)?
- What if `tools_called` is an empty array vs. absent entirely?
- How does the comparison view behave when one iteration has 1 run and another has 1,000?
- What happens when the database is empty and a comparison is requested?

## Requirements *(mandatory)*

### Functional Requirements

**Data Ingestion**

- **FR-001**: System MUST expose a REST API endpoint to accept LLM interaction records via HTTP POST.
- **FR-002**: System MUST capture the following fields per interaction: full prompt text (stored gzip-compressed, no size limit), response summary or metadata, tokens in (prompt tokens), tokens out (completion tokens), interaction start timestamp, interaction end timestamp, model identifier, list of tools called (name, inputs, outputs), experiment name, and iteration label.
- **FR-003**: System MUST calculate and store derived metrics on ingest: latency (ended_at minus started_at in ms), total tokens (tokens_in + tokens_out), and tokens per second.
- **FR-004**: System MUST authenticate REST API requests using an API key passed in the request header.
- **FR-005**: System MUST validate incoming payloads and return structured error responses for invalid data.

**Data Organisation**

- **FR-006**: System MUST support a top-level **Project** entity with a globally unique name. Project names must be unique across the platform (case-insensitive, whitespace-normalised).
- **FR-006b**: System MUST support grouping interactions into named **Experiments** within a project. Experiment names must be unique within their parent project.
- **FR-007**: System MUST support grouping interactions within an experiment into named **Iterations** (a specific configuration, prompt version, or model variant under test). Iteration names must be unique within their parent experiment.
- **FR-008**: System MUST allow multiple runs (individual interactions) within each iteration to support averaging and statistical analysis.
- **FR-008b**: The web UI MUST reflect the four-level hierarchy: Project → Experiment → Iteration → Interaction. Navigation and filtering must be scoped to the selected project.

**Dashboard & Visualisation**

- **FR-009**: System MUST provide a web-based dashboard displaying: total interaction count, average latency, total tokens consumed, and estimated cost — scoped to the active project and the selected time window.
- **FR-009b**: The dashboard MUST default to showing data for the **last 30 days**. Users MUST be able to change the time window via a date-range picker; the entire dashboard (all metric cards and charts) MUST update to reflect the selected range without a full page reload.
- **FR-010**: System MUST display a timeline chart showing interaction volume over time.
- **FR-011**: System MUST display a token usage chart broken down by model.
- **FR-012**: System MUST display a latency distribution chart (e.g., histogram or box plot).
- **FR-013**: System MUST display a tool usage frequency chart showing which tools are most commonly called.

**Iteration Comparison**

- **FR-014**: System MUST provide an iteration comparison view allowing users to select 2 or more iterations side-by-side.
- **FR-015**: The comparison view MUST display normalised metrics per iteration: average latency, average tokens in/out, tool call rate, and estimated cost per run.
- **FR-016**: The comparison view MUST include visual charts (bar or radar) highlighting differences between selected iterations.

**Interaction Exploration**

- **FR-017**: System MUST provide a searchable, filterable list of all interactions with columns for: timestamp, model, experiment, iteration, latency, total tokens, and tool call count.
- **FR-018**: System MUST support filtering by: model, experiment, iteration, date range, and latency threshold.
- **FR-019**: System MUST provide a detail view for each interaction showing all captured and derived fields, full prompt text, and a log of all tool calls.

**Sample Data**

- **FR-020**: System MUST include a mechanism to load a pre-built sample dataset covering at least 3 experiments, 2 iterations each, across at least 2 different model identifiers, with varied tool call patterns.
- **FR-021**: System MUST include at least 50 sample interaction records to make charts and comparisons meaningful.

**API**

- **FR-022**: REST API MUST provide endpoints for: creating/listing projects (POST, GET), retrieving/deleting a project and all its data (GET, DELETE), creating interactions (POST), listing interactions (GET), retrieving a single interaction (GET), deleting an interaction (DELETE), listing experiments by project (GET), listing iterations by experiment (GET), deleting an experiment (DELETE), and triggering sample data load (POST).
- **FR-023**: API MUST return JSON responses conforming to a documented schema.

### Key Entities

- **Project**: Top-level grouping with a globally unique name (case-insensitive). Owns all experiments beneath it. Has a name, description, and creation date.
- **Experiment**: A named grouping of related LLM tests within a project (e.g., "Customer Support Bot v2 Evaluation"). Name is unique within its project.
- **Iteration**: A named configuration variant within an experiment (e.g., "gpt-4o-with-tools", "claude-3-concise"). Name is unique within its parent experiment.
- **Interaction**: A single LLM call record containing all raw and derived metrics. Belongs to one iteration.
- **ToolCall**: A record of one tool invocation within an interaction. Contains tool name, input arguments, and output. Belongs to one interaction.
- **SampleDataset**: A pre-defined collection of projects, experiments, iterations, and interactions used to populate the platform for demonstration.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: An automated system can upload an LLM interaction record via REST API and receive a success acknowledgement in under 1 second under normal load.
- **SC-002**: The dashboard loads and displays all metric cards and charts in under 3 seconds for datasets of up to 10,000 interactions.
- **SC-003**: Users can compare 2 or more iterations side-by-side in less than 30 seconds from arriving at the platform, using only sample data.
- **SC-004**: All mandatory interaction fields (prompt, tokens in/out, latency, model, tools) are captured, stored, and visible in the detail view without data loss.
- **SC-005**: A new user loading the sample dataset sees a fully populated dashboard — with at least 4 meaningful charts and comparison-ready experiments — within 10 seconds of triggering the load action.
- **SC-006**: 100% of API requests with invalid payloads receive a structured error response with actionable field-level details.
- **SC-007**: The platform supports filtering interactions by any combination of model, date range, and experiment without requiring a page reload.

## Assumptions

- Estimated cost will be calculated using configurable, per-model token pricing rates (sane defaults provided for common models); exact billing integration is out of scope.
- "Response text" is not required to be stored in full by default (it may be very large); only response metadata (finish reason, token count) is captured unless the caller explicitly includes response text in the payload.
- **Prompt text storage**: The full prompt text is always stored with no size limit. It is compressed using gzip at write time and decompressed transparently at read time; callers and the UI always deal with plain text.
- API key management (creation, rotation, revocation) is a single static key configured at deployment time for the initial version; multi-user auth is out of scope.
- The REST API does not enforce rate limiting; all authenticated requests are accepted regardless of volume. Operators are responsible for infrastructure-level throttling if needed.
- The platform is intended for internal engineering teams; public-facing access controls and multi-tenancy are out of scope.
- Interactions uploaded with a project/experiment/iteration name that does not yet exist will automatically create the missing levels in the hierarchy (project → experiment → iteration).
- Project names are globally unique using case-insensitive, whitespace-normalised matching. Experiment names are unique within a project; iteration names are unique within an experiment. Duplicate name conflicts on ingest return a 409 response.
- **Data retention**: Interaction records are kept indefinitely; there is no automatic deletion or expiry. Storage growth must be managed manually by the operator.

## Out of Scope

- Real-time streaming of LLM interactions (batch/after-the-fact upload only).
- Direct integration with LLM providers (OpenAI, Anthropic, etc.); callers are responsible for wrapping their own calls.
- Multi-tenant access control and user management beyond a single API key.
- Long-term data archiving or data export to external BI tools in this iteration.
- Billing reconciliation against actual provider invoices.
