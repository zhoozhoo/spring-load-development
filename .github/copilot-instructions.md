# Copilot Instructions

This document provides essential knowledge for AI agents to be productive in this codebase.

## Architecture Overview

This is a **microservices-based load development management system** built with Spring Cloud. The system manages reloading data (ammunition development) with user isolation and full observability.

### Service Boundaries
- **API Gateway** (`spring-loaddev-api-gateway`) - OAuth2 authentication, UMA token exchange with Keycloak, routes to services
- **Loads Service** (`spring-loaddev-loads-service`) - Manages loads, groups, and shots; calculates ballistic statistics
- **Components Service** (`spring-loaddev-components-service`) - Manages reloading components (bullets, powder, primers, cases)
- **Rifles Service** (`spring-loaddev-rifles-service`) - Manages rifle configurations
- **MCP Server** (`spring-loaddev-mcp-server`) - Model Context Protocol server for Spring AI integration with tool and resource capabilities; provides async communication via SSE at `/sse` and `/mcp/messages` endpoints
- **Config Server** (`spring-loaddev-config-server`) - Centralized configuration from `spring-load-development-config` git repo (Docker/local only)
- **Discovery Server** (`spring-loaddev-discovery-server`) - Eureka service registry (Docker/local only)

**Note:** On Kubernetes, services use **Kubernetes native service discovery** (DNS-based) and **ConfigMaps** instead of Config Server and Discovery Server.

### Data Flow & Security Pattern
All services use **Keycloak UMA (User-Managed Access)** for authorization:
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

## Technology Stack

- **Java 25** with modern features (`var`, static imports, records)
- **Spring Boot 3.5.6** + **Spring Cloud 2025.0.0**
- **Spring Cloud Kubernetes** for native Kubernetes integration (ConfigMap access, service discovery)
- **WebFlux** for reactive REST APIs (no blocking code)
- **R2DBC** with PostgreSQL for reactive database access
- **Spring AI 1.1.0-M3** for MCP server
- **MapStruct 1.6.3** for DTO mapping (see `GroupStatisticsMapper`)
- **TestContainers** for integration tests with real database instances (PostgreSQL, R2DBC)
- **Resilience4J** circuit breaker in API Gateway
- **OpenTelemetry** for observability (traces to Tempo, logs to Loki, metrics to Prometheus)

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
- Services discover each other via Kubernetes DNS (e.g., `http://spring-loaddev-loads-service.spring-load-development.svc.cluster.local`)
- No Eureka server needed (disabled with `spring.cloud.discovery.enabled=false` in kubernetes profile)
- Service mesh handles load balancing automatically

## Critical Patterns

### 1. User Isolation Pattern
Every entity has `ownerId` field (JSON ignored, auto-populated):
```java
@Table(name = "loads")
public record Load(
    @Id Long id,
    @JsonIgnore @Column String ownerId,
    // ...
) {}
```

Controllers use `@CurrentUser` to inject authenticated user ID:
```java
public Flux<Load> getAllLoads(@Parameter(hidden = true) @CurrentUser String userId) {
    return loadRepository.findAllByOwnerId(userId);
}
```

Custom resolver extracts from JWT: `CurrentUserMethodArgumentResolver` → registers in `WebConfig.addArgumentResolvers()`

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
          - name: loads-service-config  # Service-specific ConfigMap
      discovery:
        all-namespaces: true  # Use K8s DNS for service discovery
```

**Spring Cloud Kubernetes Integration:** The `spring-cloud-starter-kubernetes-client-all` dependency enables direct ConfigMap access through the Kubernetes API without requiring volume mounts. ConfigMaps are automatically loaded and can be refreshed dynamically. The only volume mount needed is for `log4j2.yaml` logging configuration, as it requires file-system access for the logging framework.

**Config Server Setup:** The Config Server fetches configurations from the `spring-load-development-config` repository, allowing centralized management of properties files for all services (`loads-service.yml`, `components-service.yml`, etc.).

### 3. Testing Pattern
**Unit Testing with TestContainers:** All services that require database access (Loads Service, Components Service, Rifles Service) use **TestContainers** to spin up real PostgreSQL instances for integration tests. This ensures tests run against actual database behavior without requiring manual setup.

Use `WebTestClient` + `mockJwt()` for controller tests:
```java
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class LoadsControllerTest {
    var jwt = mockJwt().jwt(token -> token.claim("sub", userId));
    webTestClient.mutateWith(jwt).get().uri("/loads")...
}
```

TestContainers dependencies are configured in each service's `pom.xml`:
- `org.testcontainers:junit-jupiter` - JUnit 5 integration
- `org.testcontainers:postgresql` - PostgreSQL container
- `org.testcontainers:r2dbc` - R2DBC TestContainers support

The PostgreSQL container is automatically started before tests and stopped after, providing isolated test environments.

### 4. MapStruct Usage
Interface-based mappers with Spring component model for DTO conversions:
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
    @Autowired
    private GroupStatisticsMapper groupStatisticsMapper;
    
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

## Build & Development Workflow

### Build Project
```bash
# Requires Java 25 + Maven 4.0.0-rc-4
mvn clean package
```

### Run Locally (3 options)
**Option 1 - Docker Compose (recommended):**
```bash
mvn clean package
docker-compose --env-file .env up -d
```

**Option 2 - Kubernetes/Helm:**
```bash
helm install spring-load-development ./helm/spring-load-development -n spring-load-development --create-namespace
```

**Option 3 - Manual Services (for debugging):**
Start infrastructure: `docker-compose up -d postgres keycloak grafana loki tempo prometheus otel-collector`
Then start services in order: Config → Discovery → API Gateway → Microservices

### Testing
Use VS Code REST Client with files in `test/` directory:
1. Run `test/loads-service.http` to authenticate and get token
2. Token stored in `http-client.env.json` automatically
3. Requests auto-inject token

Load test data: `test/load_test_data.sh`

### CI/CD
GitHub Actions workflow (`.github/workflows/ci.yml`) runs on push/PR:
- Java 25 + Maven 4.0.0-rc-4
- Full `mvn package` build

## Project Conventions

### Code Style
- **Static imports** for constants/utilities:
  ```java
  import static org.springframework.http.HttpStatus.CREATED;
  import static org.springframework.http.HttpStatus.NOT_FOUND;
  import static org.springframework.http.ResponseEntity.ok;
  import static org.springframework.http.ResponseEntity.status;
  ```
- **`var`** keyword where type is obvious:
  ```java
  var userId = randomUUID().toString();
  var jwt = mockJwt().jwt(token -> token.claim("sub", userId));
  ```
- **Records** for immutable models/DTOs:
  ```java
  @Table(name = "loads")
  @LoadMeasurement
  public record Load(
      @Id Long id,
      @JsonIgnore @Column String ownerId,
      @NotBlank(message = "Name is required") @Column("name") String name,
      @Positive(message = "Bullet weight must be positive") 
      @Column("bullet_weight") Double bulletWeight
      // ...
  ) {}
  ```
- **Reactive types**: Return `Mono<T>` or `Flux<T>`, never call `.block()` in production code:
  ```java
  // Good: Returns reactive type
  public Flux<Load> getAllLoads(String userId) {
      return loadRepository.findAllByOwnerId(userId);
  }
  
  // Good: Reactive composition
  return loadRepository.findByIdAndOwnerId(id, userId)
      .map(load -> ok(load))
      .defaultIfEmpty(notFound().build());
  ```

### Package Structure
```
ca.zhoozhoo.loaddev.<service>/
├── config/        # Spring configuration
├── dao/           # R2DBC repositories
├── dto/           # Data transfer objects
├── mapper/        # MapStruct interfaces
├── model/         # Entity records with @Table
├── security/      # Auth helpers (@CurrentUser)
├── service/       # Business logic (@Service)
├── validation/    # Custom validators
└── web/           # Controllers (@RestController)
```

### Naming
- Services: `{Entity}Service` (e.g., `LoadsService`)
- Repositories: `{Entity}Repository` (e.g., `LoadRepository extends R2dbcRepository`)
- Controllers: `{Entity}Controller` with `@RequestMapping("/{entities}")`
- Mappers: `{Entity}Mapper` with `@Mapper(componentModel = "spring")`

### Observability
OpenTelemetry auto-instruments all services:
- **Traces** → Tempo (port 4319)
- **Logs** → Loki (port 3100)
- **Metrics** → Prometheus (port 9091)
- **Dashboard** → Grafana (port 3000)

No manual instrumentation needed in business code.

### External Dependencies
**All Environments:**
- **PostgreSQL** (R2DBC driver) - port 5432, reactive database access
- **Keycloak** - OAuth2/OIDC/UMA provider, port 7080, manages authentication and authorization

**Docker/Local Only:**
- **Eureka Discovery Server** - port 8761, service registry (replaced by K8s DNS in Kubernetes)
- **Config Server** - port 8888, pulls configs from `spring-load-development-config` git repo (replaced by ConfigMaps in Kubernetes)

**Observability Stack (All Environments):**
- **OpenTelemetry Collector** - port 4318 (HTTP), 4317 (gRPC)
- **Tempo** - Distributed tracing backend, port 3200
- **Loki** - Log aggregation, port 3100
- **Prometheus** - Metrics collection, port 9091
- **Grafana** - Unified dashboard, port 3000

## Key Files Reference
- `pom.xml` - Multi-module parent with version properties
- `docker-compose.yml` - Full stack orchestration
- `helm/spring-load-development/` - Kubernetes deployment charts
- `.env` - Environment variables for Docker Compose (versions, ports)
- `test/` - API integration tests (`.http` files for REST Client)
- Each service's `application.yml` - Service-specific bootstrap config
