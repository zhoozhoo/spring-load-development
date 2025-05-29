/**
 * Tool implementation for load-related operations.
 * Provides AI-enabled tools for retrieving load information while maintaining
 * reactive context and security propagation.
 * 
 * <p>All tools in this class follow a consistent error handling pattern:
 * <ul>
 *   <li>Errors are wrapped in {@link McpError} with appropriate JSON-RPC error codes</li>
 *   <li>Missing reactive context results in INTERNAL_ERROR code</li>
 *   <li>Authentication failures result in INVALID_REQUEST code</li>
 *   <li>Invalid load IDs result in INVALID_PARAMS code</li>
 *   <li>Other errors result in INTERNAL_ERROR code</li>
 * </ul>
 */
package ca.zhoozhoo.loaddev.mcp.tools;

import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INTERNAL_ERROR;
import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INVALID_PARAMS;
import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INVALID_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static reactor.core.publisher.Mono.just;

import java.time.Duration;
import java.util.List;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import ca.zhoozhoo.loaddev.mcp.config.McpToolRegistrationConfig.ReactiveContextHolder;
import ca.zhoozhoo.loaddev.mcp.dto.GroupStatisticsDto;
import ca.zhoozhoo.loaddev.mcp.dto.LoadDto;
import ca.zhoozhoo.loaddev.mcp.service.LoadsService;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;
import reactor.util.retry.Retry;

@Service
@Log4j2
@RequiredArgsConstructor
public class LoadsTools {

    @Autowired
    private WebClient webClient;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private LoadsService loadsService;

    @Value("${service.loads.name:loads-service}")
    private String loadsServiceName;

    /**
     * Retrieves a specific load by its ID.
     * Ensures reactive context propagation and proper error handling.
     *
     * @param id      ID of the load to retrieve
     * @param context Tool execution context
     * @return The requested load
     * @throws McpError with INTERNAL_ERROR code if reactive context is missing
     * @throws McpError with INVALID_REQUEST code if authentication fails
     * @throws McpError with INVALID_PARAMS code if load is not found
     */
    @Tool(description = "Find a specific load by its unique identifier", name = "getLoadById")
    public LoadDto getLoadById(
            @ToolParam(description = "Numeric ID of the load to retrieve", required = true) Long id,
            ToolContext context) {
        log.debug("Retrieving load with ID: {}", id);

        if (id == null || id <= 0) {
            throw new McpError(new JSONRPCError(
                    INVALID_PARAMS,
                    "Load ID must be a positive number",
                    null));
        }

        var reactiveContext = getReactiveContext();

        try {
            return just(id)
                    .flatMap(loadId -> getLoadById(loadId))
                    .contextWrite(ctx -> ctx.putAll(reactiveContext))
                    .doOnSuccess(load -> log.debug("Successfully retrieved load: {}", load))
                    .doOnError(IllegalArgumentException.class,
                            e -> log.debug("Load not found with ID {}: {}", id, e.getMessage()))
                    .doOnError(SecurityException.class,
                            e -> log.error("Authentication error retrieving load {}: {}", id, e.getMessage()))
                    .doOnError(e -> log.error("Error retrieving load {}: {}", id, e.getMessage()))
                    .block();
        } catch (Exception e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw new McpError(new JSONRPCError(
                        INVALID_PARAMS,
                        "Load not found: " + e.getCause().getMessage(),
                        null));
            }
            throw handleException("Failed to retrieve load", e);
        }
    }

    /**
     * Retrieves a single load by its ID from the loads service.
     * 
     * @param id The ID of the load to retrieve
     * @return A Mono of LoadDto object
     */
    private Mono<LoadDto> getLoadById(Long id) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    var instances = discoveryClient.getInstances(loadsServiceName);
                    if (instances == null || instances.isEmpty()) {
                        return Mono.error(new IllegalStateException(
                                String.format("Service %s not found in discovery", loadsServiceName)));
                    }

                    return webClient
                            .get()
                            .uri(instances.get(0).getUri().toString() + "/loads/" + id)
                            .headers(h -> h.setBearerAuth(((Jwt) auth.getCredentials()).getTokenValue()))
                            .retrieve()
                            .bodyToMono(LoadDto.class)
                            .onErrorMap(WebClientResponseException.class,
                                    e -> e.getStatusCode() == UNAUTHORIZED
                                            ? new SecurityException("Authentication failed", e)
                                            : e.getStatusCode() == NOT_FOUND
                                                    ? new IllegalArgumentException("Load not found with ID: " + id)
                                                    : e)
                            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                                    .filter(throwable -> !(throwable instanceof SecurityException)));
                });
    }

    /**
     * Retrieves all loads accessible to the current user.
     * Ensures reactive context propagation and proper error handling.
     *
     * @param context Tool execution context
     * @return List of all accessible loads
     * @throws McpError with INTERNAL_ERROR code if reactive context is missing
     * @throws McpError with INVALID_REQUEST code if authentication fails
     */
    @Tool(description = "Retrieve all available loads in the system", name = "getLoads")
    public List<LoadDto> getLoads(ToolContext context) {
        log.debug("Retrieving all loads");
        var reactiveContext = getReactiveContext();

        try {
            return getLoads()
                    .contextWrite(ctx -> ctx.putAll(reactiveContext))
                    .collectList()
                    .doOnSuccess(list -> log.debug("Successfully retrieved {} loads", list.size()))
                    .doOnError(SecurityException.class,
                            e -> log.error("Authentication error retrieving loads: {}", e.getMessage()))
                    .doOnError(e -> log.error("Error retrieving loads: {}", e.getMessage()))
                    .block();
        } catch (Exception e) {
            if (e.getCause() instanceof SecurityException) {
                throw new McpError(new JSONRPCError(
                        INVALID_REQUEST,
                        "Authentication failed while retrieving loads: " + e.getMessage(),
                        null));
            }
            throw handleException("Failed to retrieve loads", e);
        }
    }

    /**
     * Retrieves all loads from the loads service.
     * 
     * @return A Flux of LoadDto objects
     */
    private Flux<LoadDto> getLoads() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMapMany(auth -> {
                    var instances = discoveryClient.getInstances(loadsServiceName);
                    if (instances == null || instances.isEmpty()) {
                        return Flux.error(new IllegalStateException(
                                String.format("Service %s not found in discovery", loadsServiceName)));
                    }

                    return webClient
                            .get()
                            .uri(instances.get(0).getUri().toString() + "/loads")
                            .headers(h -> h.setBearerAuth(((Jwt) auth.getCredentials()).getTokenValue()))
                            .retrieve()
                            .bodyToFlux(LoadDto.class)
                            .onErrorMap(WebClientResponseException.class,
                                    e -> e.getStatusCode() == UNAUTHORIZED
                                            ? new SecurityException("Authentication failed", e)
                                            : e)
                            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                                    .filter(throwable -> !(throwable instanceof SecurityException)));
                });
    }

    /**
     * Retrieves statistics for a specific load.
     * Ensures reactive context propagation and proper error handling.
     *
     * @param id      ID of the load to retrieve statistics for
     * @param context Tool execution context
     * @return List of group statistics for the load
     * @throws McpError with INTERNAL_ERROR code if reactive context is missing
     * @throws McpError with INVALID_REQUEST code if authentication fails
     * @throws McpError with INVALID_PARAMS code if load is not found
     */
    @Tool(description = "Get statistics for a specific load", name = "getLoadStatistics")
    public List<GroupStatisticsDto> getLoadStatistics(
            @ToolParam(description = "Numeric ID of the load", required = true) Long id, ToolContext context) {
        log.debug("Retrieving statistics for load ID: {}", id);

        if (id == null || id <= 0) {
            throw new McpError(new JSONRPCError(
                    INVALID_PARAMS,
                    "Load ID must be a positive number",
                    null));
        }

        Mono<List<GroupStatisticsDto>> statsMono = ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMapMany(auth -> loadsService.fetchLoadStatistics(auth, id))
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                    .filter(throwable -> !(throwable instanceof McpError)))
            .contextWrite(ctx -> ctx.putAll(getReactiveContext()))
            .collectList()
            .doOnSuccess(stats -> log.debug("Retrieved {} statistics for load {}", stats.size(), id));

        return statsMono.block();
    }

    /**
     * Retrieves the reactive context from thread-local storage.
     *
     * @return The current reactive context
     * @throws McpError with INTERNAL_ERROR code if no reactive context is available
     */
    private ContextView getReactiveContext() {
        var reactiveContext = ReactiveContextHolder.reactiveContext.get();
        if (reactiveContext == null) {
            throw new McpError(new JSONRPCError(
                    INTERNAL_ERROR,
                    "No reactive context available",
                    null));
        }

        return reactiveContext;
    }

    /**
     * Handles exceptions by mapping them to appropriate McpError with JSON-RPC
     * error codes.
     * Special handling for authentication and state-related errors.
     *
     * @param message Base error message
     * @param e       Original exception
     * @return McpError with appropriate JSON-RPC error code
     */
    private McpError handleException(String message, Exception e) {
        if (e instanceof IllegalStateException) {
            return new McpError(new JSONRPCError(
                    INTERNAL_ERROR,
                    e.getMessage(),
                    null));
        }
        if (e.getCause() instanceof SecurityException) {
            return new McpError(new JSONRPCError(
                    INVALID_REQUEST,
                    "Authentication failed: " + e.getMessage(),
                    null));
        }
        log.error(message, e);
        return new McpError(new JSONRPCError(
                INTERNAL_ERROR,
                message + ": " + e.getMessage(),
                null));
    }
}
