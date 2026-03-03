<!--
SYNC IMPACT REPORT
==================
Version change: (new) → 1.0.0
Added sections:
  - Core Principles (I–IV): Code Quality, Testing Standards,
    User Experience Consistency, Performance Requirements
  - Quality Gates
  - Governance
Modified principles: N/A (initial ratification)
Removed sections: N/A

Templates reviewed:
  ✅ .specify/templates/plan-template.md  — Constitution Check gate aligns with
     all four principles; no structural changes required.
  ✅ .specify/templates/spec-template.md  — UX, performance, and testing fields
     already present in requirements/acceptance sections; no changes required.
  ✅ .specify/templates/tasks-template.md — Testing tasks are optional per
     template; constitution mandates them for every story; implementors MUST
     include test tasks. No template edit needed—governance note added below.

Deferred TODOs: none
-->

# spec-kit-demo-3 Constitution

## Core Principles

### I. Code Quality (NON-NEGOTIABLE)

Every unit of production code MUST be clean, self-documenting, and maintainable
by any team member without prior context.

- Code MUST follow the language/framework's idiomatic conventions and linting rules.
- Functions and methods MUST have a single, clearly stated responsibility
  (Single Responsibility Principle).
- Dead code, commented-out blocks, and TODO comments older than one sprint MUST
  NOT be merged.
- All public interfaces MUST include inline documentation (docstrings, JSDoc, or
  equivalent) before merge.
- Cyclomatic complexity per function MUST NOT exceed 10; refactor if higher.
- Code reviews are MANDATORY for every pull request; self-merges are prohibited.

**Rationale**: Low-quality code compounds over time. Enforcing quality at every
merge prevents technical debt accumulation and reduces onboarding friction.

### II. Testing Standards (NON-NEGOTIABLE)

Test coverage and test quality are first-class deliverables, not afterthoughts.

- Unit tests MUST be written for every public function/method, covering the happy
  path and at least two edge/failure cases.
- Integration tests MUST cover every API contract, database interaction, and
  inter-service boundary.
- End-to-end tests MUST be provided for every user story's primary acceptance
  scenario before that story is considered done.
- Minimum line coverage threshold: **80%** (measured by CI); PRs that drop
  coverage below this threshold MUST NOT be merged.
- Tests MUST be deterministic, isolated (no shared mutable state between tests),
  and executable without network access unless explicitly categorised as
  integration/e2e tests.
- Test pyramid order MUST be respected: unit tests are the majority; integration
  and e2e tests supplement but do not replace unit tests.

**Rationale**: Automated tests are the primary safety net that allows the team to
refactor, scale, and ship with confidence. A project without tests is a project
with undocumented, non-verifiable behaviour.

### III. User Experience Consistency

Every user-facing surface MUST present a coherent, predictable, and accessible
experience.

- All UI components MUST use the project's established design system tokens
  (colours, typography, spacing); ad-hoc styling is prohibited.
- Error messages presented to users MUST be human-readable, actionable, and free
  of internal stack traces or system identifiers.
- REST API responses MUST follow a consistent envelope schema (success shape,
  error shape) across all endpoints—deviations MUST be documented and justified.
- All interactive elements MUST meet WCAG 2.1 Level AA accessibility requirements.
- Navigation patterns, loading states, and empty states MUST be consistent across
  all views/screens.
- Breaking changes to UI flows or API contracts MUST be communicated via
  versioning or deprecation notices before removal.

**Rationale**: Inconsistency erodes user trust and increases support burden.
Predictability is a feature.

### IV. Performance Requirements

The system MUST meet defined performance targets; performance is a correctness
criterion, not an optimisation goal.

- API endpoints MUST respond within **200 ms** at p95 under normal load (defined
  as ≤ 100 concurrent users); endpoints exceeding this require documented
  justification and a remediation plan.
- Frontend pages MUST achieve a Largest Contentful Paint (LCP) ≤ **2.5 s** on a
  simulated mid-tier device (e.g., Moto G4 equivalent in Lighthouse).
- Database queries MUST be reviewed for index usage; N+1 query patterns are
  prohibited in production code paths.
- Performance regression tests MUST be included in CI for any change that touches
  data-access or rendering critical paths.
- Memory and CPU baselines MUST be established during Phase 0 research and
  maintained as acceptance criteria throughout the project.

**Rationale**: Performance degradation that ships to production is expensive to
reverse and directly harms user satisfaction. Treating performance as a
requirement—not an afterthought—prevents costly retrofits.

## Quality Gates

These gates MUST be satisfied before any feature branch is merged to the main
branch:

1. **Lint/format**: Zero linting errors; formatter applied with no diff.
2. **Test suite**: All tests pass; coverage ≥ 80%.
3. **Performance budget**: No API endpoint p95 regression > 20% vs. baseline.
4. **Accessibility scan**: Zero critical or serious WCAG violations reported by
   automated tooling (e.g., axe-core).
5. **Code review**: At least one peer approval; all comments resolved.
6. **Constitution compliance**: Reviewer explicitly confirms each of the four
   principles was considered during implementation.

## Governance

- This constitution supersedes all individual team conventions, wiki pages, and
  informal agreements. In case of conflict, the constitution prevails.
- **Technical decisions** (framework choice, schema design, third-party
  integration, performance trade-offs) MUST be evaluated against all four Core
  Principles before adoption. Decisions that violate a principle require a formal
  exception recorded in the relevant spec or ADR.
- **Implementation choices** (algorithm selection, caching strategy, UI pattern)
  MUST cite which principles they serve and how compliance is demonstrated.
- **Amendment procedure**: Any principle change requires (a) a written proposal
  describing the motivation, (b) team consensus (majority approval), (c) a version
  bump per the semantic versioning policy below, and (d) propagation to all
  dependent templates.
- **Versioning policy**:
  - MAJOR — backward-incompatible governance changes or principle removals.
  - MINOR — new principles, sections, or materially expanded guidance.
  - PATCH — clarifications, wording fixes, and non-semantic refinements.
- **Compliance review**: Constitution adherence MUST be explicitly verified during
  every sprint retrospective and at each feature sign-off.
- The `.specify/memory/constitution.md` file is the single source of truth;
  runtime guidance in `.github/agents/` and `.specify/templates/` derive from it
  and MUST be kept in sync after every amendment.

**Version**: 1.0.0 | **Ratified**: 2026-03-02 | **Last Amended**: 2026-03-02
