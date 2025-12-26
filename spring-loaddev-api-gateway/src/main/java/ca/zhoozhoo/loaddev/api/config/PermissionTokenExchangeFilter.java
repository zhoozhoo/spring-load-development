package ca.zhoozhoo.loaddev.api.config;

import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import ca.zhoozhoo.loaddev.api.security.UmaTokenExchangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * Web filter that handles the exchange of user tokens for permission tokens in a Keycloak-secured application.
 * 
 * <p>This filter intercepts incoming requests, extracts the original Bearer token from the Authorization
 * header, and delegates to {@link UmaTokenExchangeService} to exchange it for a permission token using 
 * Keycloak's UMA (User-Managed Access) token exchange endpoint. The new permission token, which contains 
 * resource-specific permissions, is then stored in the exchange attributes for use by downstream filters 
 * and services.</p>
 * 
 * <p>The filter runs with {@code @Order(0)} to ensure it executes early in the filter chain, before
 * security filters that may need the permission token.</p>
 * 
 * <p><b>Token Exchange Flow:</b></p>
 * <ol>
 *   <li>Extract Bearer token from Authorization header</li>
 *   <li>Delegate to UmaTokenExchangeService for token exchange</li>
 *   <li>Receive permission token with resource permissions</li>
 *   <li>Store permission token in exchange attributes</li>
 *   <li>Continue filter chain with enhanced permissions</li>
 * </ol>
 * 
 * <p>If token exchange fails, the filter logs the error and continues with the original token,
 * allowing the request to proceed without enhanced permissions.</p>
 * 
 * @author Zhubin Salehi
 * @see TokenForwardingGatewayFilterFactory
 * @see UmaTokenExchangeService
 */
@Component
@Order(0)
@RequiredArgsConstructor
@Log4j2
public class PermissionTokenExchangeFilter implements WebFilter {

    private final UmaTokenExchangeService tokenExchangeService;

    /**
     * Main filter method that processes each incoming request.
     * It extracts the original token, exchanges it for a permission token via the service,
     * and stores the new token in the exchange attributes.
     * 
     * <p>This filter skips token exchange for actuator endpoints to avoid polluting logs
     * with health check requests from Kubernetes probes.</p>
     *
     * @param exchange the current server exchange
     * @param chain the filter chain
     * @return Mono<Void> representing the completion of the filter operation
     */
    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        var request = Objects.requireNonNull(exchange.getRequest(), "request must not be null");
        var path = request.getPath().value();
        
        // Skip token exchange for actuator endpoints (health checks, metrics, etc.)
        if (shouldSkipTokenExchange(path)) {
            return chain.filter(exchange);
        }
        
        var originalToken = extractToken(request);

        if (originalToken == null) {
            log.debug("No Bearer token found, skipping permission token exchange");
            return chain.filter(exchange);
        }

        log.debug("Processing request to: {} - exchanging for permission token", path);

        return tokenExchangeService.exchangeForPermissionToken(originalToken)
                .doOnNext(permissionToken -> {
                    log.debug("Successfully obtained UMA permission token");
                    // Store the permission token access value in exchange attributes
                    exchange.getAttributes().put("permission_token", permissionToken.accessToken());
                })
                .then(chain.filter(exchange))
                .onErrorResume(e -> {
                    log.error("Failed to exchange token for path {}: {}", 
                            request.getPath(), e.getMessage());
                    log.debug("Proceeding with original token after exchange failure", e);
                    
                    return chain.filter(exchange);
                });
    }

    /**
     * Determines if token exchange should be skipped for the given path.
     * Skips actuator endpoints, Swagger UI, and OpenAPI documentation paths
     * to avoid polluting logs with health check and monitoring requests.
     * 
     * @param path the request path
     * @return true if token exchange should be skipped, false otherwise
     */
    private boolean shouldSkipTokenExchange(String path) {
        return path.startsWith("/actuator/") 
            || path.startsWith("/swagger-ui") 
            || path.startsWith("/v3/api-docs");
    }

    /**
     * Extracts the Bearer token from the Authorization header.
     * 
     * @param request the incoming server request
     * @return the token string without the "Bearer " prefix, or null if not present
     */
    private String extractToken(@NonNull ServerHttpRequest request) {
        var auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }
}