# Copilot Instructions for Spring Load Development

## Project Context

This repository contains a multi-module Spring Boot 4 / Spring Cloud 2025 microservices project for load development and ballistic data management. The app is organized as a parent Maven build with multiple service modules, shared libraries, and a Helm-based Kubernetes deployment.

## Architecture Notes

- The runtime source of truth for Kubernetes configuration is the Helm chart, especially:
  - `helm/spring-load-development/values.yaml`
  - `helm/spring-load-development/charts/reloading/values.yaml`
  - `helm/spring-load-development/charts/reloading/templates/configmaps.yaml`
- Local Docker-based development uses `docker-compose.yml` and the root `.env` file.
- The `spring-load-development-config` repository is a legacy/external config reference, not the main operational config for this project.
- The app exposes a gateway-first API model: API Gateway routes to Loads, Rifles, Components, and MCP services.
- Domain events are published to Apache Kafka for cross-service integration. A single-broker KRaft Kafka runs via `docker-compose.yml` locally and via the `helm/spring-load-development/charts/kafka` subchart in Kubernetes.

## Event-Driven Integration

- The `shared` module provides a reactive, opt-in event-publishing abstraction: `DomainEvent`, `DomainEventPublisher`, `KafkaDomainEventPublisher`, and `KafkaEventPublisherAutoConfiguration` (package `ca.zhoozhoo.loaddev.common.events` / `common.autoconfigure`).
- A service opts in simply by adding the `reactor-kafka` dependency; the auto-configuration activates via `@ConditionalOnClass(KafkaSender.class)`.
- Publishing uses a **transactional outbox pattern**, not a direct Kafka call from the write path: `shared`'s `ca.zhoozhoo.loaddev.common.events.outbox` package (`OutboxEvent`, `OutboxEventStore`, `R2dbcOutboxEventStore`, `OutboxRelay`, `OutboxAutoConfiguration`) persists events to an `outbox_events` table in the *same database transaction* as the triggering write (service methods that publish events are `@Transactional`), then a scheduled `OutboxRelay` relays pending rows to Kafka via `DomainEventPublisher`, marking them published or recording a retryable failed attempt. This guarantees at-least-once delivery even across Kafka outages or restarts.
- `loads-service`, `rifles-service`, and `components-service` (Case/Primer/Projectile/Propellant) all publish events this way; `mcp-server` has a first consumer (`ComponentEventConsumer`) for the component topics.
- When adding event publishing to a new service, follow the same pattern: a small `<Entity>EventPublisher` component per service that calls `OutboxEventStore.enqueue(topic, event)` (not `DomainEventPublisher.publish` directly), injected into the relevant service class whose create/update/delete methods are `@Transactional`, publishing to a topic named `<entity>-events`. Add an `outbox_events` table to that service's `schema.sql` (copy from an existing service's schema).
- IMPORTANT: never add `@EnableR2dbcRepositories`/`@EnableJpaRepositories`/etc. to a `shared`-module auto-configuration, even scoped to a specific package — this silently disables Spring Boot's automatic repository scanning for the *entire* host application. Use `R2dbcEntityTemplate` (as `R2dbcOutboxEventStore`/`OutboxRelay` do) instead of a Spring Data repository interface for library-owned tables.
- Kafka bootstrap servers are configured via `loaddev.events.kafka.bootstrap-servers`, sourced from `KAFKA_BOOTSTRAP_SERVERS` (Docker Compose) or `LOADDEV_EVENTS_KAFKA_BOOTSTRAPSERVERS` (Helm, injected via the `reloading.microservice.commonEnv` helper).

## Domain Guidance

- Treat this as a real-world load-development / shooting-data domain application.
- Preserve the business terminology: loads, groups, shots, rifle configuration, projectiles, primers, propellants, and cases.
- Keep measurement values in JSR-385 quantity shapes where the codebase expects them.
- Prefer domain-driven naming and keep service responsibilities cleanly separated.

## Coding Expectations

- Use the newest Java features where they add clarity and correctness, while keeping code readable and maintainable.
- Prefer modern Java language features and standard library improvements that fit the project and are supported by the toolchain.
- Use `var` where it improves readability and reduces ceremony, especially for local variables with obvious inferred types.
- Minimize the number of method-local variables and assignments; prefer direct expressions, concise intermediate steps, and clear single-purpose logic.
- Follow Spring Boot conventions and favor constructor injection.
- Keep controllers thin and business logic in service classes.
- Use `@ConfigurationProperties` patterns sparingly but consistently when configuration is typed.
- Keep OAuth2 / Keycloak and API Gateway integration secure and explicit.
- Use reactive, non-blocking patterns consistently for both database access and web handling.
- Prefer Spring WebFlux and reactive repositories / data access patterns instead of blocking I/O where the stack already supports it.
- Avoid introducing synchronous blocking patterns in request handling or persistence flows unless there is a strong, explicit reason.
- Add validation and clear error handling for input and API contracts.

## Documentation Expectations

- Keep README files aligned with the actual project topology and deployment path.
- When changing deployment or configuration behavior, update the relevant README and the Helm values/config maps together.
- Favor concise, accurate documentation over marketing wording.
- Include validation steps and startup order when a new service or dependency is introduced.

## Build and Verification

- Prefer the Maven wrapper for Java builds:
  - `./mvnw clean test`
  - `./mvnw package`
- For chart verification, use Helm when relevant:
  - `helm dependency update helm/spring-load-development`
  - `helm lint helm/spring-load-development`
  - `helm lint helm/spring-load-development/charts/<subchart>` for a specific subchart (e.g. `kafka`, `reloading`)
  - `helm template spring-load-dev helm/spring-load-development --debug --dry-run`
- If you modify documentation only, validate that links and commands still reflect the repo structure.

## Files to Check Before Making Changes

Before making deployment or architecture changes, review the most relevant files and keep them in sync:

- `pom.xml`
- `docker-compose.yml`
- `.env`
- `helm/spring-load-development/values.yaml`
- `helm/spring-load-development/charts/reloading/values.yaml`
- `helm/spring-load-development/charts/reloading/templates/configmaps.yaml`
- `helm/spring-load-development/charts/kafka/values.yaml`
- `README.adoc`
- `mcp-server/README.adoc`
- `test/README.adoc`
- `docs/enhancement-roadmap.md`

## Implementation Preferences

- Prefer small, targeted changes over broad refactors.
- Preserve backward compatibility unless the task explicitly requires a breaking change.
- If a change affects service configuration, update the chart templates and documentation together.
- Avoid introducing hardcoded values when the project already has chart values or environment overrides.
