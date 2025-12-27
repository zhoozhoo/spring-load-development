# Copilot Instructions

This document provides essential knowledge for AI agents to be productive in this codebase.

## Architecture Overview

This is a **microservices-based load development management system** built with Spring Cloud. The system manages ammunition reloading data with user isolation, full observability, and AI integration via Model Context Protocol.

### Service Boundaries
- **API Gateway** (`spring-loaddev-api-gateway`) - Single entry point; OAuth2 authentication, UMA token exchange with Keycloak, reactive routing with circuit breakers
- **Loads Service** (`loads-service`) - Manages loads, groups, and shots; calculates ballistic statistics using JSR-385 quantities
- **Components Service** (`spring-loaddev-components-service`) - Manages reloading components (bullets, powder, primers, cases) with measurements
- **Rifles Service** (`rifles-service`) - Manages rifle configurations including barrel specs and rifling parameters
- **MCP Server** (`spring-loaddev-mcp-server`) - Model Context Protocol server with SSE support at `/sse`; integrates Spring AI for GitHub Copilot
- **Config Server** (`config-server`) - Centralized configuration from `spring-load-development-config` git repo (Docker/local only)
- **Discovery Server** (`discovery-server`) - Eureka service registry (Docker/local only)

**Deployment-Specific Patterns:**
- **Docker/Local:** Uses Config Server + Eureka Discovery + Spring Cloud LoadBalancer
- **Kubernetes:** Uses ConfigMaps + Kubernetes DNS service discovery (Config Server and Discovery Server disabled via profiles)

### Data Flow & Security Pattern
All services enforce **user isolation** via Keycloak UMA (User-Managed Access):
1. User authenticates via API Gateway with OAuth2/OIDC
2. Gateway exchanges user token for UMA permission token via `PermissionTokenExchangeFilter`:
   ```java
   // API Gateway filter exchanges tokens
   MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
   formData.add("grant_type", "urn:ietf:params:oauth:grant-type:uma-ticket");
   formData.add("audience", clientId);
   ```
3. Services verify ownership via `@CurrentUser` annotation extracting JWT subject:
   ```java
   // Controller method
   public Flux<Load> getAllLoads(@Parameter(hidden = true) @CurrentUser String userId) {
       return loadRepository.findAllByOwnerId(userId);
   }
   ```
4. R2DBC repositories filter data by `ownerId`:
   ```java
   public interface LoadRepository extends R2dbcRepository<Load, Long> {
       Flux<Load> findAllByOwnerId(String ownerId);
       Mono<Load> findByIdAndOwnerId(Long id, String ownerId);
   }
   ```

### Measurement System
The project uses **JSR-385 (javax.measure)** for type-safe physical measurements with custom Jackson serialization:
```java
// Entity with typed measurements
@JsonSerialize(using = QuantitySerializer.class)
@JsonDeserialize(using = QuantityDeserializer.class)
@NotNull(message = "Bullet weight is required")
Quantity<Mass> bulletWeight,

// JSON representation
{ "value": 175.5, "unit": "grain" }
```

Common units from UCUM:
- Mass: `grain`, `g` (gram)
- Length: `in` (inch), `mm`, `thou` (thousandth of inch)
- Speed: `ft_i/s` (feet per second), `m/s`

## Technology Stack

- **Java 25** with modern features (`var`, static imports, records, pattern matching)
- **Spring Boot 4.0.0** + **Spring Cloud 2025.1.0**
- **Spring Cloud Kubernetes** for native Kubernetes integration (ConfigMap access, service discovery)
- **WebFlux** for reactive REST APIs (never `.block()` in production code)
- **R2DBC** with PostgreSQL for reactive database access
- **Spring AI 1.1.1** for MCP server integration
- **MapStruct 1.6.3** for DTO mapping with Spring component model
- **TestContainers** for integration tests with real PostgreSQL instances
- **Resilience4J** circuit breakers in API Gateway routes
- **OpenTelemetry 1.57.0** for observability (traces to Tempo, logs to Loki, metrics to Prometheus)
- **Maven 4.0+** (requires 4.0.0-rc-4 or later)

## Integration Points

### Service-to-Service Communication
**Non-Kubernetes (Docker/Local):** API Gateway uses **Spring Cloud LoadBalancer** with Eureka service discovery:
```java
@Configuration
public class SecurityConfiguration {
    @Bean
    public WebClient webClient(ReactorLoadBalancerExchangeFilterFunction lbFunction) {
        return WebClient.builder().filter(lbFunction).build();
    }
}
```
Services register with Eureka and call via service names: `http://loads-service/loads`

**Kubernetes:** Services use **Kubernetes native DNS-based service discovery**:
- Services discover each other via Kubernetes DNS (e.g., `http://loads-service.reloading.svc.cluster.local:8080`)
- No Eureka server needed (disabled with `spring.cloud.discovery.enabled=false` in kubernetes profile)
- Service mesh handles load balancing automatically

### API Gateway Routes (Spring Cloud Gateway)
Routes defined in ConfigMap (K8s) or `api-gateway.yml` (Docker/local):
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: loads-service
          uri: http://loads-service:8080  # Service name in Docker/Local
          # or http://loads-service.reloading.svc.cluster.local:8080 in K8s
          predicates:
            - Path=/api/loads/**
          filters:
            - TokenForwarding  # Custom filter for UMA token exchange
            - TokenRelay=
            - name: CircuitBreaker
              args:
                name: loadsCircuitBreaker
            - Retry=1
```

Each route includes:
- Circuit breaker for fault tolerance
- Token forwarding for authentication propagation
- Path-based routing with `/api/{service}/**` pattern

## Critical Patterns

### 1. User Isolation Pattern
Every entity has `ownerId` field (JSON ignored, auto-populated):
```java
@Table(name = "loads")
public record Load(
    @Id Long id,
    @JsonIgnore @Column("owner_id") String ownerId,
    @NotBlank(message = "Name is required") String name,
    // ...
) {}
```

Controllers use `@CurrentUser` to inject authenticated user ID from JWT:
```java
public Flux<Load> getAllLoads(@Parameter(hidden = true) @CurrentUser String userId) {
    return loadRepository.findAllByOwnerId(userId);
}
```

Custom resolver extracts from JWT subject claim: `CurrentUserMethodArgumentResolver` registered in `WebConfig.addArgumentResolvers()`

**CRITICAL:** All database queries MUST filter by `ownerId` to enforce user isolation:
- Use `findAllByOwnerId(String ownerId)` for collections
- Use `findByIdAndOwnerId(Long id, String ownerId)` for single entities
- Never expose repositories directly to controllers without user filtering

### 2. Configuration Management
**Non-Kubernetes (Docker/Local):** Services load config from Config Server which pulls from `spring-load-development-config` git repository:
```yaml
# application.yml (default profile)
spring:
  application:
    name: loads-service
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888/}
```

**Kubernetes:** Services use native Kubernetes ConfigMaps accessed via **spring-cloud-kubernetes** library:
```yaml
# application.yml (kubernetes profile)
spring:
  config:
    activate:
      on-profile: kubernetes
    import: "kubernetes:"
  cloud:
    config:
      enabled: false  # Disable Config Server
    kubernetes:
      config:
        sources:
          - name: common-config       # Shared ConfigMap
          - name: loads-service       # Service-specific ConfigMap
      discovery:
        all-namespaces: true  # Use K8s DNS for service discovery
```

**Spring Cloud Kubernetes Integration:** The `spring-cloud-starter-kubernetes-client-all` dependency enables direct ConfigMap access through the Kubernetes API without requiring volume mounts. ConfigMaps are automatically loaded and can be refreshed dynamically. The only volume mount needed is for `log4j2.yaml` logging configuration.

**Config Server Setup:** The Config Server fetches configurations from the `spring-load-development-config` repository, allowing centralized management of properties files for all services.

### 3. Testing Pattern
**Integration Testing with TestContainers:** All services with database dependencies use **TestContainers** to spin up real PostgreSQL instances with R2DBC support. No mocks for database layer.

Use `WebTestClient` + `mockJwt()` for controller tests:
```java
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class LoadsControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    void shouldGetAllLoads() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));
        
        webTestClient.mutateWith(jwt)
            .get().uri("/loads")
            .exchange()
            .expectStatus().isOk();
    }
}
```

**TestContainers Dependencies:** Configured in each service's `pom.xml`:
- `org.testcontainers:junit-jupiter` - JUnit 5 integration
- `org.testcontainers:postgresql` - PostgreSQL container
- `org.testcontainers:r2dbc` - R2DBC TestContainers support

**Test Profile Configuration:** Each service has `application-test.yml` with R2DBC test configuration pointing to TestContainers PostgreSQL instance.

### 4. MapStruct Usage
Interface-based mappers with Spring component model for DTO conversions (never manual mapping):
```java
@Mapper(componentModel = "spring")
public interface GroupStatisticsMapper {
    @Mapping(source = "group.date", target = "date")
    @Mapping(source = "group.powderCharge", target = "powderCharge")
    @Mapping(source = "group.targetRange", target = "targetRange")
    @Mapping(source = "group.groupSize", target = "groupSize")
    GroupStatisticsDto toDto(GroupStatistics statistics);
    
    @Mapping(source = "velocity", target = "velocity")
    ShotDto shotToShotDto(Shot shot);
}
```

Usage in service layer (MapStruct auto-injects as Spring bean):
```java
@Service
public class LoadsService {
    private final GroupStatisticsMapper groupStatisticsMapper;
    
    public Mono<GroupStatisticsDto> getGroupStatistics(Long groupId, String userId) {
        return groupRepository.findByIdAndOwnerId(groupId, userId)
            .flatMap(group -> shotRepository.findByGroupIdAndOwnerId(groupId, userId)
                .collectList()
                .map(shots -> buildGroupStatistics(group, shots)))
            .map(groupStatisticsMapper::toDto);  // Mapper converts entity to DTO
    }
}
```

Maven compiler plugin configured with `-Amapstruct.defaultComponentModel=spring` to auto-generate Spring-managed implementations.

### 5. Reactive Programming Pattern
**NEVER block in reactive chains** - all database and HTTP calls return `Mono<T>` or `Flux<T>`:
```java
// GOOD: Reactive composition
return loadRepository.findByIdAndOwnerId(id, userId)
    .map(load -> ok(load))
    .defaultIfEmpty(notFound().build());

// GOOD: FlatMap for nested async operations
return loadRepository.findByIdAndOwnerId(id, userId)
    .flatMap(load -> groupRepository.findAllByLoadIdAndOwnerId(id, userId)
        .collectList()
        .map(groups -> new LoadWithGroups(load, groups)));

// BAD: Never call .block() in production code
var load = loadRepository.findById(id).block();  // NEVER DO THIS

// BAD: Don't use blocking JDBC or synchronous APIs
var result = jdbcTemplate.queryForObject(...);  // Use R2DBC instead
```

**Controller return types:** Always return reactive types:
- `Mono<ResponseEntity<T>>` for single items
- `Flux<T>` for collections
- Use `ResponseEntity.ok()`, `.notFound()`, `.status(CREATED)` for HTTP responses

## Build & Development Workflow

### Prerequisites
- **Java 25** (JDK 25 required)
- **Maven 4.0+** (minimum 4.0.0-rc-4)
- **Docker** and **Docker Compose** for local infrastructure
- **kubectl** and **Helm 3** for Kubernetes deployment

### Build Project
```bash
# Full build with tests
mvn clean package

# Build without tests (faster)
mvn clean package -DskipTests

# Build specific service
cd loads-service && mvn clean package
```

### Run Locally (3 options)

**Option 1 - Docker Compose (recommended for full stack):**
```bash
# Build all services first
mvn clean package

# Start entire stack (infrastructure + services)
docker-compose --env-file .env up -d

# View logs
docker-compose logs -f api-gateway

# Stop all services
docker-compose down
```

**Option 2 - Kubernetes/Helm (production-like environment):**
```bash
# Install full stack
helm install spring-load-development ./helm/spring-load-development \
  -n spring-load-development --create-namespace

# Access via port-forward
kubectl port-forward -n reloading svc/api-gateway 8080:8080

# Access via NodePort (Docker Desktop)
# API Gateway: http://localhost:30090
# Grafana: http://localhost:30000
# Keycloak: http://localhost:30080

# Uninstall
helm uninstall spring-load-development -n spring-load-development
```

**Option 3 - Manual Services (for debugging individual services):**
```bash
# 1. Start infrastructure only
docker-compose up -d postgres keycloak grafana loki tempo prometheus otel-collector

# 2. Start Config Server (port 8888)
java -jar config-server/target/onfig-server-*.jar

# 3. Start Discovery Server (port 8761)
java -jar discovery-server/target/discovery-server-*.jar

# 4. Start API Gateway (port 8080)
java -jar spring-loaddev-api-gateway/target/spring-loaddev-api-gateway-*.jar

# 5. Start microservices (distinct ports)
java -Dserver.port=8081 -jar loads-service/target/loads-service-*.jar
java -Dserver.port=8082 -jar rifles-service/target/rifles-service-*.jar
java -Dserver.port=8083 -jar spring-loaddev-components-service/target/spring-loaddev-components-service-*.jar
java -Dserver.port=8084 -jar spring-loaddev-mcp-server/target/spring-loaddev-mcp-server-*.jar
```

### Testing

**VS Code REST Client (recommended):**
Use `.http` files in `test/` directory with VS Code REST Client extension:
1. Open `test/loads-service.http`
2. Click "Send Request" on authentication call (credentials in `test/http-client.env.json`)
3. Token stored automatically and injected into subsequent requests
4. Test all CRUD operations

**Load Test Data:**
```bash
./test/load_test_data.sh
```

**Run Unit/Integration Tests:**
```bash
# All tests (includes TestContainers)
mvn test

# Specific service
cd loads-service && mvn test

# Single test class
mvn test -Dtest=LoadsControllerTest
```

### Common Development Tasks

**Add New Microservice:**
1. Create new Maven module in `pom.xml` under `<subprojects>`
2. Add to Docker Compose in `docker-compose.yml`
3. Add Helm chart in `helm/spring-load-development/charts/`
4. Follow existing service structure (see `loads-service` as template)

**Database Migrations:**
- Schema files in `src/main/resources/schema.sql` for each service
- Liquibase/Flyway NOT used - prefer R2DBC native schema initialization
- Each service has its own database schema (loads, rifles, components)

**Add New Route to API Gateway:**
Edit `spring-load-development-config/api-gateway.yml`:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: new-service
          uri: http://new-service:8080
          predicates:
            - Path=/api/new/**
          filters:
            - TokenForwarding
            - TokenRelay=
            - name: CircuitBreaker
              args:
                name: newServiceCircuitBreaker
```

**View Observability Data:**
- **Grafana:** http://localhost:3000 (Docker) or http://localhost:30000 (K8s) - anonymous access enabled
- **Traces:** Navigate to "Explore" → Select "Tempo" → Search traces by service name
- **Logs:** Navigate to "Explore" → Select "Loki" → Query with `{service_name="loads-service"}`
- **Metrics:** Navigate to "Explore" → Select "Prometheus" → Query with `http_server_requests_seconds_count{application="loads-service"}`

### CI/CD
GitHub Actions workflow (`.github/workflows/ci.yml`) runs on push/PR:
- Java 25 + Maven 4.0.0-rc-5
- Full `mvn clean verify` build (includes SpotBugs, JaCoCo coverage)
- TestContainers tests execute automatically

**Release Process:**
```bash
# Release plugin updates versions across all modules and Helm charts
mvn release:prepare
mvn release:perform
```

## Project Conventions

### Code Style
- **Java 25 features:** Use modern language features (records, pattern matching, switch expressions, `var`)
- **Static imports** for constants and utilities to reduce noise:
  ```java
  import static org.springframework.http.HttpStatus.CREATED;
  import static org.springframework.http.HttpStatus.NOT_FOUND;
  import static org.springframework.http.ResponseEntity.ok;
  import static org.springframework.http.ResponseEntity.status;
  ```
- **Static imports in tests** for assertion methods, test utilities, and frequently used constants:
  ```java
  import static org.junit.jupiter.api.Assertions.assertEquals;
  import static org.junit.jupiter.api.Assertions.assertNotNull;
  import static org.mapstruct.factory.Mappers.getMapper;
  import static systems.uom.ucum.UCUM.GRAIN;
  import static tech.units.indriya.quantity.Quantities.getQuantity;
  ```
- **`var` keyword** - Use when type is obvious from right-hand side (never for method return types, parameters, or fields):
  ```java
  var userId = randomUUID().toString();
  var jwt = mockJwt().jwt(token -> token.claim("sub", userId));
  var dto = mapper.toDto(statistics);
  var shots = List.of(shot1, shot2, shot3);
  ```
- **Eliminate single-use variables** - Inline expressions used only once:
  ```java
  // BAD: Single-use variable
  var statistics = createValidGroupStatistics();
  var dto = mapper.toDto(statistics);
  
  // GOOD: Inline the expression
  var dto = mapper.toDto(createValidGroupStatistics());
  
  // EXCEPTION: Keep variable if used multiple times or improves readability
  var dto = mapper.toDto(statistics);
  assertNotNull(dto);
  assertEquals(expectedDate, dto.date());  // 'dto' used multiple times
  ```
- **Records** for immutable models/DTOs (all entities are records):
  ```java
  @Table(name = "loads")
  public record Load(
      @Id Long id,
      @JsonIgnore @Column("owner_id") String ownerId,
      @NotBlank(message = "Name is required") String name,
      @JsonSerialize(using = QuantitySerializer.class)
      @JsonDeserialize(using = QuantityDeserializer.class)
      @Positive(message = "Bullet weight must be positive") 
      Quantity<Mass> bulletWeight
      // ...
  ) {}
  ```
- **Reactive types:** Always return `Mono<T>` or `Flux<T>`, never call `.block()` in production code
- **JavaDoc:** Document all public APIs with clear descriptions; include `@author` tag
- **Validation:** Use Bean Validation annotations (`@NotBlank`, `@Positive`, `@NotNull`, etc.) on record fields

### Package Structure
Standard package organization for all services:
```
ca.zhoozhoo.loaddev.<service>/
├── config/        # Spring configuration classes (@Configuration)
├── dao/           # R2DBC repositories (extends R2dbcRepository)
├── dto/           # Data transfer objects (records)
├── mapper/        # MapStruct interfaces (@Mapper)
├── model/         # Entity records with @Table annotation
├── security/      # Auth helpers (@CurrentUser annotation)
├── service/       # Business logic (@Service)
├── validation/    # Custom validators (Bean Validation)
└── web/           # Controllers (@RestController)
```

### Naming Conventions
- **Services:** `{Entity}Service` (e.g., `LoadsService`, `RiflesService`)
- **Repositories:** `{Entity}Repository extends R2dbcRepository<Entity, Long>`
- **Controllers:** `{Entity}Controller` with `@RequestMapping("/{entities}")`
- **Mappers:** `{Entity}Mapper` with `@Mapper(componentModel = "spring")`
- **DTOs:** `{Entity}Dto` as records (e.g., `LoadDto`, `GroupStatisticsDto`)
- **Test classes:** `{Class}Test` for unit tests, `{Class}IntegrationTest` for integration tests

### Observability
OpenTelemetry auto-instruments all services - **no manual instrumentation required**:
- **Traces** → Tempo (OTLP endpoint: `http://otel-collector:4317`)
- **Logs** → Loki (via OTLP logs)
- **Metrics** → Prometheus (scraped from actuator endpoints)
- **Dashboard** → Grafana (http://localhost:3000)

**OpenTelemetry Configuration:**
- Auto-configured via `spring-boot-starter-actuator` and OpenTelemetry Java agent
- Environment variable `OTEL_EXPORTER_OTLP_ENDPOINT` points to collector
- Service name automatically set from `spring.application.name`
- No manual spans or metrics needed in business code

**Custom Metrics Configuration:**
API Gateway excludes `SimpleMetricsExportAutoConfiguration` to prevent duplicate registries:
```java
@SpringBootApplication(excludeName = {
    "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration"
})
```

### External Dependencies

**All Environments:**
- **PostgreSQL 18** - Port 5432, separate schemas per service
- **Keycloak 26.4** - Port 7080 (Docker) or 30080 (K8s), OAuth2/OIDC/UMA provider

**Docker/Local Only:**
- **Eureka Discovery Server** - Port 8761
- **Config Server** - Port 8888, pulls from `spring-load-development-config` git repo

**Kubernetes Only:**
- **ConfigMaps** - Replace Config Server
- **Kubernetes DNS** - Replace Eureka Discovery

**Observability Stack (All Environments):**
- **OpenTelemetry Collector** - Port 4318 (HTTP), 4317 (gRPC)
- **Tempo 2.9.0** - Distributed tracing backend
- **Loki 3.6.3** - Log aggregation
- **Prometheus v3.8.1** - Metrics collection
- **Grafana 12.3.1** - Unified dashboard

### Multi-Module Maven Structure
Parent POM defines:
- Shared dependencies (Spring Boot, Spring Cloud, OpenTelemetry)
- Common build plugins (SpotBugs, JaCoCo, Maven compiler)
- Version properties for all dependencies
- Release plugin configuration

Each service module:
- Inherits from parent POM
- Defines only service-specific dependencies
- Shares common configuration (logging, OpenTelemetry, security)

## Key Files Reference
- `pom.xml` - Parent POM with version properties and shared plugins
- `docker-compose.yml` - Full stack orchestration for local development
- `helm/spring-load-development/` - Production Kubernetes deployment
- `helm/spring-load-development/charts/reloading/` - Microservices subchart
- `.env` - Environment variables for Docker Compose (versions, ports)
- `test/` - API integration tests (`.http` files for REST Client)
- `test/http-client.env.json` - Test credentials and configuration
- `spring-load-development-config/` - External config repo (separate repository)
- `.github/workflows/ci.yml` - GitHub Actions CI pipeline
- `.github/instructions/` - Java upgrade guides (11→17, 17→21, 21→25)

## Domain-Specific Notes

### Load Development Terminology
- **Load:** Complete ammunition recipe (powder, bullet, primer, case measurements)
- **Group:** Collection of shots fired with the same load at a target
- **Shot:** Individual projectile measurement (velocity, impact point)
- **Rifle:** Firearm configuration (caliber, barrel length, twist rate)
- **Component:** Individual reloading part (bullet, powder, primer, case)

### Measurement Units (JSR-385)
All physical measurements use typed quantities with custom Jackson serialization:
- **Mass:** `grain` (grains), `g` (grams)
- **Length:** `in` (inches), `mm` (millimeters), `thou` (thousandths of inch)
- **Speed:** `ft_i/s` (feet per second), `m/s` (meters per second)

Example entity field:
```java
@JsonSerialize(using = QuantitySerializer.class)
@JsonDeserialize(using = QuantityDeserializer.class)
@NotNull(message = "Bullet weight is required")
Quantity<Mass> bulletWeight,
```

JSON representation:
```json
{
  "bulletWeight": { "value": 175.5, "unit": "grain" }
}
```

Common imports for measurements:
```java
import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Length;
import javax.measure.quantity.Speed;
import static systems.uom.ucum.UCUM.GRAIN;
import static tech.units.indriya.quantity.Quantities.getQuantity;
```
