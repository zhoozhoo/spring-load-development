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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RiflesService {

    @Autowired
    private WebClient webClient;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${service.rifles.name:rifles-service}")
    private String riflesServiceName;

    /**
     * Retrieves all rifles from the rifles service.
     *
     * @return A Flux emitting Rifle objects, or error with:
     *         - McpError(INTERNAL_ERROR) if service discovery fails
     *         - McpError(INVALID_REQUEST) if authentication fails
     */
    public Flux<RifleDto> getRifles() {
        var instances = discoveryClient.getInstances(riflesServiceName);
        if (instances == null || instances.isEmpty()) {
            return Flux.error(new McpError(new JSONRPCError(
                    INTERNAL_ERROR, String.format("Service %s not found in discovery", riflesServiceName),
                    null)));
        }

        String uri = instances.get(0).getUri().toString() + "/rifles";

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMapMany(auth -> {
                    String token = ((Jwt) auth.getCredentials()).getTokenValue();

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
     * Retrieves a single rifle by its ID from the rifles service.
     * Returns a Mono that emits the RifleDto or an error.
     *
     * @param id The ID of the rifle to retrieve
     * @return A Mono emitting the RifleDto, or error with:
     *         - McpError(INTERNAL_ERROR) if service discovery fails
     *         - McpError(INVALID_REQUEST) if authentication fails
     */
    public Mono<RifleDto> getRifleById(Long id) {
        var instances = discoveryClient.getInstances(riflesServiceName);
        if (instances == null || instances.isEmpty()) {
            return Mono.error(new McpError(new JSONRPCError(
                    INTERNAL_ERROR, String.format("Service %s not found in discovery", riflesServiceName),
                    null)));
        }

        String uri = instances.get(0).getUri().toString() + "/rifles/" + id;

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    String token = ((Jwt) auth.getCredentials()).getTokenValue();

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
