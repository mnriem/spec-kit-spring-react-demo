# Quickstart: LLM Performance Analytics Platform

**Feature**: `001-llm-perf-analytics`  
**Stack**: Spring Boot 3.3 (Java 21) · React 18 (Vite) · PostgreSQL 16 · Docker Compose

---

## Prerequisites

| Tool | Minimum Version | Check |
|------|----------------|-------|
| Java (JDK) | 21 | `java -version` |
| Maven | 3.9 | `mvn -version` |
| Node.js | 20 | `node -version` |
| npm | 10 | `npm -version` |
| Docker Desktop | 4.x (Docker Engine 24+) | `docker -version` |
| Docker Compose | 2.x (plugin) | `docker compose version` |

> **Note**: Node / npm are only needed for local frontend development. The Maven build uses `frontend-maven-plugin` to download its own Node/npm version during `mvn package`, so the Docker build has no host Node dependency.

---

## 1. Clone and Configure

```bash
git clone <repo-url> llm-perf-analytics
cd llm-perf-analytics
cp .env.example .env          # Edit APP_API_KEY, POSTGRES_PASSWORD to your preferred values
```

### `.env.example`

```dotenv
POSTGRES_DB=llmanalytics
POSTGRES_USER=llmanalytics
POSTGRES_PASSWORD=changeme
APP_API_KEY=dev-key-change-in-production
```

---

## 2. Start with Docker Compose (recommended)

This is the fastest path to a running system. Docker Compose starts PostgreSQL and the Spring Boot app (which includes the React frontend).

```bash
docker compose up --build
```

- First run: Maven downloads dependencies, builds the React app with Vite, packages the JAR, and starts the JVM (~3–5 minutes on first build; subsequent builds use Docker layer cache).
- Flyway runs migrations automatically on startup (`V1__init_schema.sql`, `V2__seed_model_pricing.sql`).

**Access the application**: http://localhost:8080

**Logs**:

```bash
docker compose logs -f app    # Spring Boot logs
docker compose logs -f db     # PostgreSQL logs
```

**Stop**:

```bash
docker compose down           # stop containers, preserve data volume
docker compose down -v        # stop containers + delete data volume (clean slate)
```

---

## 3. Local Development (hot reload)

For inner-loop development, run the backend and frontend separately with hot reload.

### 3a. Start PostgreSQL via Docker

```bash
docker compose up db -d
```

### 3b. Start the Spring Boot backend

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The backend starts on **http://localhost:8080**. API endpoints available at `/api/**`. Flyway migrations run automatically.

The `dev` profile sets:
- `APP_API_KEY=dev-key-change-in-production` (safe default for local use)
- `spring.jpa.show-sql=false` (set to `true` to debug queries)
- CORS allows `http://localhost:5173` (Vite dev server)

### 3c. Start the React frontend (Vite dev server)

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on **http://localhost:5173** with hot module replacement. Vite proxies `/api/**` requests to `http://localhost:8080`, so the React app can call the Spring Boot backend without CORS issues.

---

## 4. Load Sample Data

Once the application is running, load the pre-built sample dataset:

```bash
# Using curl
curl -X POST http://localhost:8080/api/admin/sample-data \
  -H "X-API-Key: dev-key-change-in-production"

# Expected response: 201 Created with counts of seeded entities
```

Or click **"Load Sample Data"** on the empty-state dashboard screen.

After loading, the dashboard shows:
- 3 projects with multiple experiments and iterations
- 50+ interactions across at least 2 model identifiers
- Pre-configured experiments ready for the comparison view

---

## 5. Ingest an Interaction via REST API

```bash
curl -X POST http://localhost:8080/api/interactions \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-key-change-in-production" \
  -d '{
    "project_name": "My LLM Project",
    "experiment_name": "Baseline Evaluation",
    "iteration_name": "gpt-4o-default",
    "model": "gpt-4o",
    "prompt": "You are a helpful assistant. Answer the following question: What is the capital of France?",
    "tokens_in": 28,
    "tokens_out": 12,
    "started_at": "2026-03-02T10:00:00.000Z",
    "ended_at": "2026-03-02T10:00:01.234Z",
    "tools_called": []
  }'
```

The project, experiment, and iteration are auto-created if they don't exist. The response includes the stored record with all derived metrics (`latency_ms`, `total_tokens`, `tokens_per_second`, `estimated_cost`).

---

## 6. Build a Production JAR

```bash
mvn clean package            # Runs frontend build + Java compilation + tests
java -jar target/llm-perf-analytics-*.jar \
  --spring.profiles.active=docker \
  --app.security.api-key=your-production-key \
  --spring.datasource.url=jdbc:postgresql://your-db-host:5432/llmanalytics
```

---

## 7. Run Tests

```bash
# All backend tests (unit + integration via TestContainers)
mvn test

# Backend unit tests only (fast, no Docker required)
mvn test -Dgroups=unit

# Frontend tests
cd frontend && npm test

# Frontend tests with coverage report
cd frontend && npm run coverage
```

> TestContainers spins up a real PostgreSQL container for integration tests. Docker must be running.

---

## 8. Project Structure Reference

```text
.
├── docker-compose.yml          # app + db services
├── Dockerfile                  # multi-stage: Maven build → JRE runtime
├── pom.xml                     # Maven POM with frontend-maven-plugin
├── src/
│   ├── main/
│   │   ├── java/com/llmanalytics/
│   │   │   ├── api/controller/ # REST controllers
│   │   │   ├── api/dto/        # Request & Response records
│   │   │   ├── config/         # Spring Security, CORS, SPA fallback
│   │   │   ├── domain/model/   # JPA entities
│   │   │   ├── domain/service/ # Business logic
│   │   │   └── infra/          # GzipStringConverter, SampleDataLoader
│   │   └── resources/
│   │       ├── static/         # Built React app (generated by mvn package)
│   │       └── db/migration/   # Flyway SQL migrations
│   └── test/
│       └── java/com/llmanalytics/
│           ├── api/            # MockMVC controller tests
│           ├── service/        # Unit tests
│           └── integration/    # TestContainers integration tests
└── frontend/
    ├── src/
    │   ├── components/         # Shared UI components
    │   ├── pages/              # Dashboard, Comparison, Interactions
    │   ├── services/           # Axios API clients
    │   ├── hooks/              # TanStack Query hooks
    │   └── types/              # TypeScript interfaces
    ├── vite.config.ts
    └── vitest.config.ts
```

---

## 9. Environment Variables Reference

| Variable | Required | Default (dev) | Description |
|----------|----------|---------------|-------------|
| `APP_API_KEY` | Yes | `dev-key-change-in-production` | API key for `X-API-Key` header |
| `POSTGRES_DB` | Yes | `llmanalytics` | PostgreSQL database name |
| `POSTGRES_USER` | Yes | `llmanalytics` | PostgreSQL username |
| `POSTGRES_PASSWORD` | Yes | `changeme` | PostgreSQL password |
| `SPRING_PROFILES_ACTIVE` | No | `dev` | Set to `docker` in containers |
| `SPRING_DATASOURCE_URL` | Docker | auto from compose | JDBC URL (overrides compose default) |
| `JAVA_OPTS` | No | — | JVM options (e.g., `-Xmx512m`) |

---

## 10. Common Troubleshooting

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| `401 Unauthorized` on API calls | Wrong or missing `X-API-Key` header | Check `APP_API_KEY` in `.env` matches the header value |
| `Connection refused` on port 8080 | Spring Boot not started yet | Check `docker compose logs app`; wait for `Started Application` log line |
| Flyway migration error on startup | Schema drift or duplicate migration | Run `docker compose down -v` to reset the DB volume, then `docker compose up` |
| `npm install` fails in Docker build | Stale `node_modules` in Docker cache | Run `docker compose build --no-cache` |
| Dashboard shows no data | No interactions ingested; date range filter | Load sample data via the UI button or `POST /api/admin/sample-data` |
| Vite dev server not proxying | Backend not running on 8080 | Start the Spring Boot backend first: `mvn spring-boot:run` |
