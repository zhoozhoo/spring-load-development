package ca.zhoozhoo.loaddev.mcp.service;

import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INTERNAL_ERROR;
import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INVALID_REQUEST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import ca.zhoozhoo.loaddev.mcp.dto.RifleDto;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service for managing rifle data operations through the rifles microservice.
 * <p>
 * Provides reactive methods to retrieve rifle information, including individual rifles
 * and collections of rifles. All methods automatically extract and propagate JWT
 * authentication tokens from the reactive security context.
 * <p>
 * This service uses service discovery to locate the rifles-service backend and
 * communicates via WebClient with proper authentication headers.
 * 
 * @author Zhubin Salehi
 * @see ReactiveSecurityContextHolder
 * @see WebClient
 */
@Service
@Log4j2
public class RiflesService {

    @Autowired
    private WebClient webClient;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${service.rifles.name:rifles-service}")
    private String riflesServiceName;

    /**
     * Retrieves all rifles accessible to the authenticated user.
     * <p>
     * Automatically extracts the JWT token from the reactive security context and
     * includes it in the Authorization header when calling the backend rifles-service.
     * <p>
     * Uses service discovery to locate the rifles-service instance dynamically.
     *
     * @return a Flux emitting RifleDto objects for all accessible rifles
     * @throws McpError with INTERNAL_ERROR if service discovery fails
     * @throws McpError with INVALID_REQUEST if authentication fails (401 response)
     */
    public Flux<RifleDto> getRifles() {
        log.debug("RiflesService.getRifles() called");
        
        var instances = discoveryClient.getInstances(riflesServiceName);
        if (instances == null || instances.isEmpty()) {
            log.error("Service {} not found in discovery", riflesServiceName);
            return Flux.error(new McpError(new JSONRPCError(
                    INTERNAL_ERROR, "Service %s not found in discovery".formatted(riflesServiceName),
                    null)));
        }

        String uri = "%s/rifles".formatted(instances.getFirst().getUri());
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
                            .bodyToFlux(RifleDto.class)
                            .onErrorMap(WebClientResponseException.class, _ -> {
                                return new McpError(new JSONRPCError(
                                        INVALID_REQUEST,
                                        "Authentication failed",
                                        null));
                            });
                });
    }

    /**
     * Retrieves a specific rifle by its unique identifier.
     * <p>
     * Automatically extracts the JWT token from the reactive security context and
     * includes it in the Authorization header when calling the backend rifles-service.
     * <p>
     * Uses service discovery to locate the rifles-service instance dynamically.
     *
     * @param id the unique identifier of the rifle to retrieve
     * @return a Mono emitting the RifleDto if found
     * @throws McpError with INTERNAL_ERROR if service discovery fails
     * @throws McpError with INVALID_REQUEST if authentication fails (401 response)
     */
    public Mono<RifleDto> getRifleById(Long id) {
        log.debug("RiflesService.getRifleById({}) called", id);
        
        var instances = discoveryClient.getInstances(riflesServiceName);
        if (instances == null || instances.isEmpty()) {
            log.error("Service {} not found in discovery", riflesServiceName);
            return Mono.error(new McpError(new JSONRPCError(
                    INTERNAL_ERROR, "Service %s not found in discovery".formatted(riflesServiceName),
                    null)));
        }

        String uri = "%s/rifles/%d".formatted(instances.getFirst().getUri(), id);
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
                            .bodyToMono(RifleDto.class)
                            .onErrorMap(WebClientResponseException.class, _ -> {
                                return new McpError(new JSONRPCError(
                                        INVALID_REQUEST,
                                        "Authentication failed",
                                        null));
                            });
                });
    }
}
