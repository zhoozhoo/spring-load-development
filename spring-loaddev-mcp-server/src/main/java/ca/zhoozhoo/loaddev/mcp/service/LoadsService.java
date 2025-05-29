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
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import ca.zhoozhoo.loaddev.mcp.dto.GroupStatisticsDto;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
import reactor.core.publisher.Flux;

@Service
public class LoadsService {

    @Autowired
    private WebClient webClient;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${service.loads.name:loads-service}")
    private String loadsServiceName;

    /**
     * Fetches statistics for a specific load from the loads service.
     *
     * @param auth the authentication object containing user credentials
     * @param id   the ID of the load to retrieve statistics for
     * @return a Flux of GroupStatisticsDto objects
     * @throws McpError with INTERNAL_ERROR code if reactive context is missing
     * @throws McpError with INVALID_REQUEST code if authentication fails
     * @throws McpError with INVALID_PARAMS code if load is not found
     */
    public Flux<GroupStatisticsDto> fetchLoadStatistics(Authentication auth, Long id) {
        var instances = discoveryClient.getInstances(loadsServiceName);
        if (instances == null || instances.isEmpty()) {
            return Flux.error(new McpError(new JSONRPCError(
                    INTERNAL_ERROR, format("Service %s not found in discovery", loadsServiceName),
                    null)));
        }

        String uri = instances.get(0).getUri().toString() + "/loads/" + id + "/statistics";
        String token = ((Jwt) auth.getCredentials()).getTokenValue();

        return webClient
                .get()
                .uri(uri)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToFlux(GroupStatisticsDto.class)
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
    }
}
