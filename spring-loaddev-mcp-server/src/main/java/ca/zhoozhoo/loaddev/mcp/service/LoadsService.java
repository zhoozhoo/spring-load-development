package ca.zhoozhoo.loaddev.mcp.service;

import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INTERNAL_ERROR;
import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INVALID_PARAMS;
import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INVALID_REQUEST;
import static java.lang.String.format;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LoadsService {

    @Autowired
    private WebClient webClient;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${service.loads.name:loads-service}")
    private String loadsServiceName;

    /**
     * Retrieves all loads from the loads service.
     *
     * @return A Flux emitting LoadDto objects, or error with:
     *         - McpError(INTERNAL_ERROR) if service discovery fails
     *         - McpError(INVALID_REQUEST) if authentication fails
     */
    public Flux<LoadDto> getLoads() {
        var instances = discoveryClient.getInstances(loadsServiceName);
        if (instances == null || instances.isEmpty()) {
            return Flux.error(new McpError(new JSONRPCError(
                    INTERNAL_ERROR, format("Service %s not found in discovery", loadsServiceName),
                    null)));
        }

        String uri = instances.get(0).getUri().toString() + "/loads";

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMapMany(auth -> {
                    String token = ((Jwt) auth.getCredentials()).getTokenValue();

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
     * Retrieves a single load by its ID from the loads service.
     * Returns a Mono that emits the LoadDto or an error.
     *
     * @param id The ID of the load to retrieve
     * @return A Mono emitting the LoadDto, or error with:
     *         - McpError(INTERNAL_ERROR) if service discovery fails
     *         - McpError(INVALID_REQUEST) if authentication fails
     *         - McpError(INVALID_PARAMS) if load is not found
     */
    public Mono<LoadDto> getLoadById(Long id) {
        var instances = discoveryClient.getInstances(loadsServiceName);
        if (instances == null || instances.isEmpty()) {
            return Mono.error(new McpError(new JSONRPCError(
                    INTERNAL_ERROR, format("Service %s not found in discovery", loadsServiceName),
                    null)));
        }

        String uri = instances.get(0).getUri().toString() + "/loads/" + id;

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    String token = ((Jwt) auth.getCredentials()).getTokenValue();

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
                                            "Load not found with ID: " + id,
                                            null));
                                }
                                return e;
                            });
                });
    }

    /**
     * Fetches statistics for a specific load from the loads service.
     *
     * @param id the ID of the load to retrieve statistics for
     * @return a Flux emitting GroupStatisticsDto objects, or error with:
     *         - McpError(INTERNAL_ERROR) if service discovery fails
     *         - McpError(INVALID_REQUEST) if authentication fails
     *         - McpError(INVALID_PARAMS) if load is not found
     */
    public Flux<GroupDto> getGroupsByLoadId(Long id) {
        var instances = discoveryClient.getInstances(loadsServiceName);
        if (instances == null || instances.isEmpty()) {
            return Flux.error(new McpError(new JSONRPCError(
                    INTERNAL_ERROR, format("Service %s not found in discovery", loadsServiceName),
                    null)));
        }

        String uri = instances.get(0).getUri().toString() + "/loads/" + id + "/statistics";

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMapMany(auth -> {
                    String token = ((Jwt) auth.getCredentials()).getTokenValue();

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
                                            "Load not found with ID: " + id,
                                            null));
                                }
                                return e;
                            });
                });
    }
}
