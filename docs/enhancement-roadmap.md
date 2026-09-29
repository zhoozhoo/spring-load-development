# Enhancement and Improvement Roadmap

## Executive summary

This project is already strong in several areas: a clear multi-service layout, Spring WebFlux and R2DBC usage, gateway-first routing, OAuth2 security, and a mature observability stack. The biggest opportunities are not to add more framework layers, but to refine the architecture, simplify repetition, and move more of the codebase toward modern Java and reactive design patterns.

The recommended direction is:

- keep the reactive, non-blocking foundation
- modernize Java usage where it improves clarity and correctness
- reduce repeated service/controller patterns through shared abstractions
- strengthen resilience, testing, and developer ergonomics
- add stronger domain boundaries and event-driven patterns for future growth

---

## 1. Architecture improvements

### 1.1 Standardize service boundaries and shared contracts

The project already separates services by domain, which is good. However, several services appear to have repeated CRUD patterns and nearly identical controller/service shapes.

Suggested improvements:

- create reusable base patterns for:
  - reactive CRUD service interfaces
  - domain-specific validation rules
  - pagination and user-scoping logic
  - standard response wrappers or error envelopes
- define a shared contract library for stable request/response DTOs
- clearly separate:
  - domain models
  - API DTOs
  - persistence entities
  - MCP or external integration DTOs

This reduces copy-paste drift and makes changes across the stack safer.

### 1.2 Move toward a more explicit ports-and-adapters structure

The current design is close to a clean domain-driven layout, but it can be made more explicit.

Recommended pattern:

- domain module or package for business logic and invariants
- application service layer for orchestration
- persistence adapter layer for R2DBC repositories
- API adapter layer for controllers and gateway integration
- external adapter layer for Keycloak, MCP, and telemetry

This makes it easier to test the domain logic without pulling in infrastructure concerns.

### 1.3 Use domain events for cross-service actions

The project is already a good candidate for event-driven integration because it has multiple services with clear ownership boundaries.

**Status: implemented, with a transactional outbox.** Apache Kafka is now part of the stack:

- `shared` provides a reusable, opt-in event-publishing abstraction (`DomainEvent`, `DomainEventPublisher`, `KafkaDomainEventPublisher`, `KafkaEventPublisherAutoConfiguration`), activated only when `reactor-kafka` is on a service's classpath.
- `shared` also provides a transactional outbox (`ca.zhoozhoo.loaddev.common.events.outbox`: `OutboxEvent`, `OutboxEventStore`, `R2dbcOutboxEventStore`, `OutboxRelay`, `OutboxAutoConfiguration`). Each `<Entity>EventPublisher` now enqueues events to an `outbox_events` table instead of calling Kafka directly, and the enqueue happens in the *same database transaction* (`@Transactional`) as the triggering aggregate write. A scheduled `OutboxRelay` then relays pending rows to Kafka via the existing `DomainEventPublisher`, marking them published or recording a retryable failed attempt (up to a configurable `maxAttempts` before giving up). This gives at-least-once delivery even across Kafka outages or service restarts, instead of the earlier "publish inline, log and drop on failure" behavior.
- `loads-service`, `rifles-service`, and `components-service` (Case/Primer/Projectile/Propellant) all publish domain events following this pattern, to `loads-events`, `rifles-events`, `cases-events`, `primers-events`, `projectiles-events`, and `propellants-events` topics respectively.
- `mcp-server` has a first consumer: `ComponentEventConsumer` subscribes to the four component topics via `reactor-kafka`'s `KafkaReceiver` and materializes a proof-of-concept, in-memory, deduplicated read model.
- A single-broker KRaft-mode Kafka is available in both `docker-compose.yml` (local dev) and the Helm chart (`charts/kafka` subchart, its own `kafka` namespace).

Potential event use cases:

- load created / updated / deleted (done)
- rifle profile changed (done)
- component search index refresh (consumer proof-of-concept done; not yet a real index)
- analytics or reporting snapshots
- audit trail generation

Remaining next steps:

- promote the `mcp-server` in-memory read model to a durable, shared store (it is currently lost on restart and inconsistent across replicas)
- add consumers for `loads-events` and `rifles-events` to prove the pattern beyond components
- add a dead-letter topic or alerting for outbox rows that exceed `maxAttempts` and are marked `FAILED`
- keep event handlers idempotent and retry-safe as more consumers are added

This helps the system evolve from CRUD-only to workflow-driven architecture.

### 1.4 Add query and read-side optimization patterns

The project has rich read use cases, especially statistics and search. These are ideal for CQRS-style read models.

Suggested read-side improvements:

- create purpose-built read models for statistics, summaries, and analytics
- keep write models separate from read models when the read path becomes heavier
- precompute/aggregate common load statistics instead of re-deriving them on every request
- add caching strategies for hot queries such as frequently accessed rifles, load summaries, and component metadata

### 1.5 Strengthen cross-service consistency and contract validation

This is especially important given the MCP gateway integration.

Recommendations:

- enforce contract tests between services and the gateway
- validate JSON schemas for selected DTOs
- maintain explicit API compatibility policies for versioned endpoints
- add regression checks for auth propagation and route behavior

---

## 2. Java modernisation

### 2.1 Use modern Java features where they improve clarity

The project is already using Java 26 and modern Spring Boot 4. That is a strong foundation. The next step is to adopt new Java features selectively and intentionally.

Recommended candidates:

- `record` types for DTOs and immutable value objects
- `sealed` classes or interfaces for domain states and result types
- pattern matching for `switch` when branching on domain-specific states
- `when`-style modern switch expressions where the code becomes more readable
- `var` for local variables where readability is preserved
- `List.copyOf`, `Set.copyOf`, `Map.copyOf` for defensive immutable structures
- `StringTemplate`/text blocks when supported and beneficial for generated output or SQL/JSON builders

Important note: use modern features when they simplify code, not just because they are available.

### 2.2 Prefer structured concurrency for independent work

The MCP server already demonstrates parallel retrieval patterns with `Mono.zip()`, which is a good starting point. This can be pushed further with structured concurrency patterns and clearer decomposition.

Good candidates:

- loading load details by combining independent fetches
- collecting metrics and metadata concurrently
- running multiple service lookups in a single workflow

This reduces nested callback logic and makes failure handling more explicit.

### 2.3 Use more explicit result types for business outcomes

Some business logic is likely returning bare entities or optional values. For large domain logic, consider introducing explicit result types such as:

- `Success<T>` / `Failure`
- `DomainResult<T>`
- `ValidationResult`
- sealed result hierarchies for success, validation failure, not found, domain conflict

This improves correctness and makes error handling easier to reason about, especially in MCP and gateway flows.

### 2.4 Lean into virtual threads selectively

Virtual threads are excellent for blocking I/O boundaries, especially for external service calls or legacy integration points.

Use carefully for:

- blocking adapters or client libraries
- migration bridges
- background processing or job runners
- non-reactive integration bridges

Keep the core service boundary reactive where the stack already supports it.

### 2.5 Improve request-context handling

This project already has strong security and observability config. The next upgrade is to make request context propagation more explicit and safer.

Recommended patterns:

- `ScopedValue` for request-local data where appropriate
- well-defined context propagation for trace IDs and user identity
- explicit propagation of tenant or auth context across service calls
- clear boundaries between user context and technical context

---

## 3. Reactive and non-blocking patterns

### 3.1 Keep the core reactive model, but reduce accidental blocking

The project already uses Reactors and WebFlux correctly. The main concern is accidental blocking introduced by helper methods, serialization paths, or legacy libraries.

Suggested improvements:

- audit blocking calls in the service pipeline
- avoid `block()`, `toFuture()`, or synchronous wrappers unless there is a clear boundary
- avoid mixing blocking and reactive code inside the same path
- keep database, security, and gateway layers fully reactive end to end

### 3.2 Use richer reactive composition patterns

The codebase would benefit from more consistent use of:

- `switchIfEmpty` for missing-resource handling
- `then`, `zipWhen`, and `flatMap` patterns with clear business meaning
- central operator composition for common business rules
- `onErrorResume` for predictable failure translation at service boundaries

This makes the reactive code easier to follow and reduces scattered error handling.

### 3.3 Add backpressure-aware processing and pagination strategy

A system with search and aggregation endpoints can benefit from strong pageable and stream-aware patterns.

Recommendations:

- standardize pagination metadata consistently across services
- add sorting strategy rules and validation
- use bounded stream processing for heavy queries and searches
- document expected backpressure behavior for large result sets

---

## 4. Resilience and operational quality

### 4.1 Strengthen resilience patterns

The project already includes gateway routing and security. Add stronger production protections:

- retry with exponential backoff for transient service failures
- circuit breakers at the service boundary and downstream integration points
- timeouts and budgets for external calls
- bulkhead patterns for heavy calls
- idempotent write operations

### 4.2 Improve health and readiness semantics

Current readiness/liveness patterns are solid, but they can be made more domain-aware.

Suggested upgrades:

- separate readiness from dependency health
- include database availability and downstream dependency health in readiness checks
- expose a clear operational status model for each service
- pipeline startup ordering and dependency health reporting more explicitly

### 4.3 Standardize observability conventions

The observability stack is already good. The next step is to make it more standardized across all services.

Actions:

- standardize metrics naming conventions
- standardize log structure across services
- define business-level tags for user, resource type, and domain action
- create common dashboards for request latency, db latency, dependency errors, and auth failures
- add alert thresholds for key service degradation

---

## 5. Security and access patterns

### 5.1 Tighten least-privilege boundaries

The project uses Keycloak and OAuth2 well. Continue to improve the model:

- enforce finer-grained authorities by resource and action
- validate ownership checks at the service boundary consistently
- centralize authorization rules for cross-service access
- add audit logging for sensitive writes

### 5.2 Add explicit write-idempotency policy

For high-traffic and AI-driven interactions, writes should be idempotent where appropriate.

Examples:

- idempotency keys for create operations
- deduplication of repeated tool invocations from MCP clients
- audit of repeated create calls for the same payload

---

## 6. Testing strategy improvements

### 6.1 Increase architecture-level tests

The project already has good service-level tests in places. Expand to:

- contract tests for gateway routing and API compatibility
- reactive integration tests with real database setup where possible
- security tests covering auth propagation and role checks
- failure-mode tests for downstream dependency outages

### 6.2 Add property-based tests for calculations and validation

This domain naturally involves measurements, units, and quantities. Property-based tests would be particularly valuable.

Examples:

- quantity conversion consistency
- domain validation edge cases
- pagination and search result invariants
- serialization round-trips for units and measures

### 6.3 Expand test observability and mutation checks

For a domain-heavy system, add:

- deterministic tests around JSR-385 quantity conversions
- mutation coverage for validation rules
- regression tests for service authorization patterns

---

## 7. Data and persistence improvements

### 7.1 Add stronger auditing and ownership metadata

The domain is data-heavy and user-scoped. Add:

- created/updated timestamps
- created-by/updated-by metadata
- soft delete handling
- optimistic locking for concurrent updates

This greatly improves operating safety.

### 7.2 Introduce clearer persistence boundaries

The use of R2DBC is a good fit, but persistence implementation details are probably spreading through service logic. Recommended improvement:

- keep repository logic narrow and specific
- use custom repository methods for complex query behavior
- move composition logic into service or query modules
- keep persistence concerns away from API-level DTO decisions

### 7.3 Consider supporting more query/reporting models

For long-term scale, a read-optimized reporting layer may be beneficial for:

- per-rifle load trend analysis
- component search analytics
- group statistics and historical comparisons
- common query patterns that are expensive under live transactional paths

---

## 8. MCP and AI integration improvements

### 8.1 Expand the MCP tool model

The MCP server already supports tool-based interactions, which is a strong pattern. Continue with:

- structured resource definitions for load and rifle entities
- prompt templates for common user workflows
- contextual tool selection based on domain intent
- shared result-format conventions for summaries and detail payloads

### 8.2 Add typed tool orchestration

The project is already doing parallel lookups for details. Expand that into a more formal orchestration layer:

- a typed tool workflow abstraction
- result composition with validation and error mapping
- clear boundaries between tool input validation and downstream service calls

This will help as the AI layer grows in scope.

---

## 9. Suggested implementation order

### Phase 1: Quick wins

1. standardize shared DTO and validation rules
2. add stronger error envelopes and consistent API responses
3. tighten observability and metric naming
4. audit blocking calls in the reactive flow

### Phase 2: Architectural hardening

1. introduce explicit domain/application/persistence boundaries
2. add event-driven integration patterns for notable domain actions — **done**: Kafka-backed publishing with a transactional outbox implemented for Loads, Rifles, and Components (see section 1.3); a `mcp-server` consumer exists for the component topics. Remaining: durable/shared read model, consumers for `loads-events`/`rifles-events`, dead-letter handling for the relay
3. improve write idempotency and audit metadata
4. create stronger contract tests between gateway and services

### Phase 3: Java modernization and scalability

1. convert DTO-heavy classes to records where appropriate
2. adopt sealed result types for business outcomes
3. introduce structured concurrency patterns for parallel workflows
4. evaluate virtual threads for blocking integration boundaries

### Phase 4: Next-level platform evolution

1. CQRS read models for analytics-heavy queries
2. caching strategy for top read paths
3. broader AI tool and resource model
4. operational dashboards and service-level SLO policies

---

## 10. Final recommendation

The strongest next step is not a rewrite. It is a controlled modernization effort focused on the project’s already good base:

- keep WebFlux and R2DBC as the foundation
- modernize Java usage gradually and deliberately
- centralize reusable patterns and contracts
- strengthen resilience, observability, and typing
- introduce event-driven and read-optimized patterns for the next growth phase

If this project continues to grow, the combination of reactive architecture, domain-driven service separation, and modern Java features will keep it maintainable, scalable, and easier to evolve.
