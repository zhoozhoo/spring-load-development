package ca.zhoozhoo.loaddev.api.config;

import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * Web filter that handles the exchange of user tokens for permission tokens in a Keycloak-secured application.
 * 
 * <p>This filter intercepts incoming requests, extracts the original Bearer token from the Authorization
 * header, and exchanges it for a permission token using Keycloak's UMA (User-Managed Access) token exchange
 * endpoint. The new permission token, which contains resource-specific permissions, is then stored in the
 * exchange attributes for use by downstream filters and services.</p>
 * 
 * <p>The filter runs with {@code @Order(0)} to ensure it executes early in the filter chain, before
 * security filters that may need the permission token.</p>
 * 
 * <p><b>Token Exchange Flow:</b></p>
 * <ol>
 *   <li>Extract Bearer token from Authorization header</li>
 *   <li>Call Keycloak token endpoint with UMA grant type</li>
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
 */
@Component
@Order(0)
@Log4j2
public class PermissionTokenExchangeFilter implements WebFilter {

    @Qualifier("keycloakWebClient")
    @Autowired
    private WebClient webClient;
    
    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.registration.api-gateway.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.api-gateway.client-secret}")
    private String clientSecret;

    /**
     * Main filter method that processes each incoming request.
     * It extracts the original token, exchanges it for a permission token,
     * and stores the new token in the exchange attributes.
     *
     * @param exchange the current server exchange
     * @param chain the filter chain
     * @return Mono<Void> representing the completion of the filter operation
     */
    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        var request = Objects.requireNonNull(exchange.getRequest(), "request must not be null");
        var originalToken = extractToken(request);

        if (originalToken == null) {
            return chain.filter(exchange);
        }

        log.debug("Processing request to: {}", request.getPath());

        return getPermissionToken(originalToken)
                .map(newToken -> {
                    log.debug("Successfully obtained new permission token");
                    // Store the token in the exchange attributes
                    exchange.getAttributes().put("permission_token", newToken);
                    return exchange;
                })
                .flatMap(chain::filter)
                .onErrorResume(e -> {
                    log.error("Error during token exchange, proceeding with original token", e);
                    return chain.filter(exchange);
                });
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

    /**
     * Performs the token exchange with Keycloak server using UMA grant type.
     * Makes a POST request to exchange the original access token for a permission token
     * that contains resource-specific permissions.
     *
     * @param originalToken the original Bearer token to be exchanged
     * @return Mono<String> containing the new permission token, or error if exchange fails
     * @throws NullPointerException if originalToken is null
     * @throws WebClientResponseException if the token exchange request fails
     */
    @NonNull
    private Mono<String> getPermissionToken(@NonNull String originalToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "urn:ietf:params:oauth:grant-type:uma-ticket");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("audience", clientId);
        formData.add("scope", "openid");

        log.debug("Requesting permission token with form data: {}", formData);

        return webClient.post()
                .uri(tokenUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(originalToken))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(response -> log.debug("Received token response: {}", response))
                .map(response -> response.get("access_token").asText())
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("Error exchanging token: {} - Response body: {}",
                            e.getMessage(), e.getResponseBodyAsString());
                    return Mono.error(e);
                })
                .doOnError(e -> log.error("Failed to exchange token", e));
    }
}