package ca.zhoozhoo.loaddev.api.security;

import static java.time.Duration.ofMillis;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.annotation.JsonProperty;

import ca.zhoozhoo.loaddev.api.config.CacheConfiguration;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Service responsible for exchanging OAuth2 access tokens for UMA permission tokens.
 * 
 * <p>This service encapsulates the logic for communicating with Keycloak's token endpoint
 * to obtain User-Managed Access (UMA) permission tokens. These tokens contain resource-specific
 * permissions that can be used for fine-grained authorization decisions.</p>
 * 
 * <p><b>UMA Token Exchange Process:</b></p>
 * <ol>
 *   <li>Receives a standard OAuth2 access token</li>
 *   <li>Constructs a token exchange request with grant type {@code urn:ietf:params:oauth:grant-type:uma-ticket}</li>
 *   <li>Calls Keycloak's token endpoint with client credentials via the Keycloak WebClient</li>
 *   <li>Returns the permission token containing resource permissions</li>
 * </ol>
 * 
 * <p>The service includes automatic retry logic for transient failures, proper error handling,
 * and Spring Cache integration for caching permission tokens.</p>
 * 
 * <p><b>OpenTelemetry Observability:</b></p>
 * <p>This service benefits from automatic OpenTelemetry instrumentation through the injected
 * {@code keycloakWebClient}, which is configured with an {@link io.micrometer.observation.ObservationRegistry}.
 * Each token exchange operation generates observability data:</p>
 * <ul>
 *   <li><b>Distributed Traces:</b> HTTP client spans are created for each Keycloak call, showing
 *       request duration, response status, and correlation with parent gateway spans. Trace context
 *       is propagated using W3C Trace Context headers (traceparent, tracestate).</li>
 *   <li><b>Metrics:</b> Request count, duration histograms, error rates, and retry attempts are
 *       automatically collected and exported to OpenTelemetry collectors.</li>
 *   <li><b>Error Tracking:</b> Failed token exchanges include exception details in span events,
 *       enabling quick diagnosis of authentication issues.</li>
 *   <li><b>Cache Hit/Miss Observability:</b> Spring Cache operations are observable through
 *       Micrometer's cache metrics, showing cache effectiveness for token reuse.</li>
 * </ul>
 * 
 * @author Zhubin Salehi
 * @see <a href="https://docs.kantarainitiative.org/uma/wg/rec-oauth-uma-grant-2.0.html">UMA 2.0 Grant</a>
 * @see CacheConfiguration
 * @see SecurityConfiguration#keycloakWebClient
 */
@Service
@Log4j2
public class UmaTokenExchangeService {

    private static final String UMA_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:uma-ticket";

    private static final int MAX_RETRY_ATTEMPTS = 2;

    private static final Duration RETRY_DELAY = ofMillis(100);

    @Qualifier("keycloakWebClient")
    @Autowired
    private WebClient keycloakWebClient;

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.registration.api-gateway.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.api-gateway.client-secret}")
    private String clientSecret;

    /**
     * Exchanges a standard OAuth2 access token for a UMA permission token.
     * 
     * <p>The permission token includes resource-specific permissions that have been
     * granted to the user based on Keycloak's authorization policies.</p>
     * 
     * <p>This method uses Spring Cache abstraction with {@code @Cacheable} annotation.
     * When caching is enabled, permission tokens are automatically cached using the
     * access token as the key. Cached tokens are returned immediately without making
     * a network call to Keycloak.</p>
     * 
     * <p><b>OpenTelemetry Tracing:</b></p>
     * <p>The WebClient call to Keycloak's token endpoint automatically creates a child span
     * in the distributed trace. This span includes:</p>
     * <ul>
     *   <li>HTTP method, URL, and status code</li>
     *   <li>Request/response timing</li>
     *   <li>Error details if the exchange fails</li>
     *   <li>Retry attempt counts and timing</li>
     * </ul>
     * <p>Trace context is propagated to Keycloak via W3C Trace Context headers, enabling
     * end-to-end trace correlation across the gateway and authorization server.</p>
     * 
     * @param accessToken the OAuth2 access token to exchange (used as cache key)
     * @return a Mono emitting the permission token, or an error if exchange fails
     */
    @Cacheable(value = CacheConfiguration.UMA_TOKEN_CACHE, key = "#accessToken", unless = "#result == null")
    public Mono<UmaPermissionToken> exchangeForPermissionToken(String accessToken) {
        log.debug("Performing UMA token exchange with Keycloak");

        return keycloakWebClient.post()
                .uri(tokenUri)
                .header(AUTHORIZATION, "Bearer %s".formatted(accessToken))
                .contentType(APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(buildTokenExchangeRequest()))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .map(this::mapToPermissionToken)
                .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, RETRY_DELAY)
                        .filter(this::isRetryableError)
                        .doBeforeRetry(signal -> log.debug("Retrying token exchange, attempt: {}", 
                                signal.totalRetries() + 1)))
                .doOnSuccess(token -> log.debug("Successfully exchanged token for UMA permission token"))
                .doOnError(e -> log.error("Failed to exchange token: {}", e.getMessage()))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

    /**
     * Builds the form data required for UMA token exchange.
     * 
     * @return MultiValueMap containing the token exchange request parameters
     */
    private MultiValueMap<String, String> buildTokenExchangeRequest() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", UMA_GRANT_TYPE);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("audience", clientId);
        formData.add("scope", "openid");

        return formData;
    }

    /**
     * Maps the token response to a UmaPermissionToken domain object.
     * 
     * @param response the raw token response from Keycloak
     * @return a UmaPermissionToken containing the access token and metadata
     */
    private UmaPermissionToken mapToPermissionToken(TokenResponse response) {
        return new UmaPermissionToken(
                response.accessToken(),
                response.tokenType(),
                response.expiresIn(),
                response.scope()
        );
    }

    /**
     * Determines if an error is retryable based on its type.
     * 
     * @param throwable the error to check
     * @return true if the error should trigger a retry
     */
    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException wcre) {
            int statusCode = wcre.getStatusCode().value();
            // Retry on 5xx server errors and 429 rate limiting
            return statusCode >= 500 || statusCode == 429;
        }

        return false;
    }

    /**
     * Maps WebClient exceptions to more descriptive domain exceptions.
     * 
     * @param exception the WebClient exception
     * @return a TokenExchangeException with contextual information
     */
    private TokenExchangeException mapWebClientException(WebClientResponseException exception) {
        var message = switch (exception.getStatusCode().value()) {
            case 400 -> "Invalid token exchange request: %s".formatted(exception.getResponseBodyAsString());
            case 401 -> "Authentication failed during token exchange";
            case 403 -> "Client not authorized for UMA token exchange";
            case 429 -> "Rate limit exceeded for token exchange";
            default -> "Token exchange failed: %s".formatted(exception.getMessage());
        };
        
        return new TokenExchangeException(message, exception);
    }

    /**
     * Response object for token endpoint.
     * Jackson automatically maps snake_case JSON fields to camelCase record components.
     */
    private record TokenResponse(
        
            @JsonProperty("access_token") 
            String accessToken,
            
            @JsonProperty("token_type") 
            String tokenType,
            
            @JsonProperty("expires_in") 
            Integer expiresIn,
            
            String scope
    ) {
    }
}
