package ca.zhoozhoo.loaddev.mcp.service;

import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INTERNAL_ERROR;
import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INVALID_PARAMS;
import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INVALID_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import ca.zhoozhoo.loaddev.mcp.dto.GroupDto;
import ca.zhoozhoo.loaddev.mcp.dto.LoadDto;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service for managing load data operations through the loads microservice.
 * <p>
 * Provides reactive methods to retrieve load information, including individual loads,
 * collections of loads, and associated group statistics. All methods automatically
 * extract and propagate JWT authentication tokens from the reactive security context.
 * <p>
 * This service uses service discovery to locate the loads-service backend and
 * communicates via WebClient with proper authentication headers.
 * 
 * @author Zhubin Salehi
 * @see ReactiveSecurityContextHolder
 * @see WebClient
 */
@Service
@Log4j2
public class LoadsService {

    @Autowired
    private WebClient webClient;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${service.loads.name:loads-service}")
    private String loadsServiceName;

    /**
     * Retrieves all loads accessible to the authenticated user.
     * <p>
     * Automatically extracts the JWT token from the reactive security context and
     * includes it in the Authorization header when calling the backend loads-service.
     * <p>
     * Uses service discovery to locate the loads-service instance dynamically.
     *
     * @return a Flux emitting LoadDto objects for all accessible loads
     * @throws McpError with INTERNAL_ERROR if service discovery fails
     * @throws McpError with INVALID_REQUEST if authentication fails (401 response)
     */
    public Flux<LoadDto> getLoads() {
        log.debug("LoadsService.getLoads() called");
        
        var instances = discoveryClient.getInstances(loadsServiceName);
        if (instances == null || instances.isEmpty()) {
            log.error("Service {} not found in discovery", loadsServiceName);
            return Flux.error(new McpError(new JSONRPCError(
                    INTERNAL_ERROR, "Service %s not found in discovery".formatted(loadsServiceName),
                    null)));
        }

        String uri = "%s/loads".formatted(instances.getFirst().getUri());
        log.debug("Target URI: {}", uri);

        return ReactiveSecurityContextHolder.getContext()
                .doOnNext(ctx -> {
                    log.debug("ReactiveSecurityContextHolder returned context: {}", ctx);
                    log.debug("Authentication present: {}", ctx.getAuthentication() != null);
                    if (ctx.getAuthentication() != null) {
                        log.debug("Authentication type: {}", ctx.getAuthentication().getClass().getName());
                        log.debug("Is authenticated: {}", ctx.getAuthentication().isAuthenticated());
                        log.debug("Principal: {}", ctx.getAuthentication().getPrincipal());
                    }
                })
                .map(SecurityContext::getAuthentication)
                .flatMapMany(auth -> {
                    log.debug("Extracting token from authentication");
                    String token = ((Jwt) auth.getCredentials()).getTokenValue();
                    log.debug("Token extracted (first 20 chars): {}...", token.substring(0, Math.min(20, token.length())));

                    return webClient
                            .get()
                            .uri(uri)
                            .headers(h -> h.setBearerAuth(token))
                            .retrieve()
                            .bodyToFlux(LoadDto.class)
                            .onErrorMap(WebClientResponseException.class, e -> {
                                if (e.getStatusCode() == UNAUTHORIZED) {
                                    return new McpError(new JSONRPCError(
                                            INVALID_REQUEST,
                                            "Authentication failed",
                                            null));
                                }
                                return e;
                            });
                });
    }

    /**
     * Retrieves a specific load by its unique identifier.
     * <p>
     * Automatically extracts the JWT token from the reactive security context and
     * includes it in the Authorization header when calling the backend loads-service.
     * <p>
     * Uses service discovery to locate the loads-service instance dynamically.
     *
     * @param id the unique identifier of the load to retrieve
     * @return a Mono emitting the LoadDto if found
     * @throws McpError with INTERNAL_ERROR if service discovery fails
     * @throws McpError with INVALID_REQUEST if authentication fails (401 response)
     * @throws McpError with INVALID_PARAMS if the load is not found (404 response)
     */
    public Mono<LoadDto> getLoadById(Long id) {
        log.debug("LoadsService.getLoadById({}) called", id);
        
        var instances = discoveryClient.getInstances(loadsServiceName);
        if (instances == null || instances.isEmpty()) {
            log.error("Service {} not found in discovery", loadsServiceName);
            return Mono.error(new McpError(new JSONRPCError(
                    INTERNAL_ERROR, "Service %s not found in discovery".formatted(loadsServiceName),
                    null)));
        }

        String uri = "%s/loads/%d".formatted(instances.getFirst().getUri(), id);
        log.debug("Target URI: {}", uri);

        return ReactiveSecurityContextHolder.getContext()
                .doOnNext(ctx -> {
                    log.debug("ReactiveSecurityContextHolder returned context: {}", ctx);
                    log.debug("Authentication present: {}", ctx.getAuthentication() != null);
                })
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    log.debug("Extracting token from authentication");
                    String token = ((Jwt) auth.getCredentials()).getTokenValue();
                    log.debug("Token extracted (first 20 chars): {}...", token.substring(0, Math.min(20, token.length())));

                    return webClient
                            .get()
                            .uri(uri)
                            .headers(h -> h.setBearerAuth(token))
                            .retrieve()
                            .bodyToMono(LoadDto.class)
                            .onErrorMap(WebClientResponseException.class, e -> {
                                if (e.getStatusCode() == UNAUTHORIZED) {
                                    return new McpError(new JSONRPCError(
                                            INVALID_REQUEST,
                                            "Authentication failed",
                                            null));
                                }
                                if (e.getStatusCode() == NOT_FOUND) {
                                    return new McpError(new JSONRPCError(
                                            INVALID_PARAMS,
                                            "Load not found with ID: %d".formatted(id),
                                            null));
                                }
                                return e;
                            });
                });
    }

    /**
     * Retrieves group statistics for a specific load.
     * <p>
     * Fetches shooting group data and statistics associated with the specified load ID.
     * Automatically extracts the JWT token from the reactive security context and
     * includes it in the Authorization header when calling the backend loads-service.
     * <p>
     * Uses service discovery to locate the loads-service instance dynamically.
     *
     * @param id the unique identifier of the load to retrieve statistics for
     * @return a Flux emitting GroupDto objects containing group statistics
     * @throws McpError with INTERNAL_ERROR if service discovery fails
     * @throws McpError with INVALID_REQUEST if authentication fails (401 response)
     * @throws McpError with INVALID_PARAMS if the load is not found (404 response)
     */
    public Flux<GroupDto> getGroupsByLoadId(Long id) {
        log.debug("LoadsService.getGroupsByLoadId({}) called", id);
        
        var instances = discoveryClient.getInstances(loadsServiceName);
        if (instances == null || instances.isEmpty()) {
            log.error("Service {} not found in discovery", loadsServiceName);
            return Flux.error(new McpError(new JSONRPCError(
                    INTERNAL_ERROR, "Service %s not found in discovery".formatted(loadsServiceName),
                    null)));
        }

        String uri = "%s/loads/%d/statistics".formatted(instances.getFirst().getUri(), id);
        log.debug("Target URI: {}", uri);

        return ReactiveSecurityContextHolder.getContext()
                .doOnNext(ctx -> {
                    log.debug("ReactiveSecurityContextHolder returned context: {}", ctx);
                    log.debug("Authentication present: {}", ctx.getAuthentication() != null);
                })
                .map(SecurityContext::getAuthentication)
                .flatMapMany(auth -> {
                    log.debug("Extracting token from authentication");
                    String token = ((Jwt) auth.getCredentials()).getTokenValue();
                    log.debug("Token extracted (first 20 chars): {}...", token.substring(0, Math.min(20, token.length())));

                    return webClient
                            .get()
                            .uri(uri)
                            .headers(h -> h.setBearerAuth(token))
                            .retrieve()
                            .bodyToFlux(GroupDto.class)
                            .onErrorMap(WebClientResponseException.class, e -> {
                                if (e.getStatusCode() == UNAUTHORIZED) {
                                    return new McpError(new JSONRPCError(
                                            INVALID_REQUEST,
                                            "Authentication failed",
                                            null));
                                }
                                if (e.getStatusCode() == NOT_FOUND) {
                                    return new McpError(new JSONRPCError(
                                            INVALID_PARAMS,
                                            "Load not found with ID: %d".formatted(id),
                                            null));
                                }
                                return e;
                            });
                });
    }
}
