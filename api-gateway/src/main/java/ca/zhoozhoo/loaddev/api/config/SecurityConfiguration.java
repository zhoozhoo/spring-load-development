package ca.zhoozhoo.loaddev.api.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.config.HttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

import io.micrometer.observation.ObservationRegistry;

/**
 * Security configuration class for the API Gateway using Spring WebFlux Security.
 * 
 * <p>This configuration defines the security policies and components for the reactive web application,
 * including OAuth2 login, JWT-based resource server authentication, bearer token propagation,
 * and specialized WebClient configurations for service-to-service communication.</p>
 * 
 * <p><b>Key Security Features:</b></p>
 * <ul>
 *   <li><b>Public Endpoints:</b> Actuator health checks, Swagger UI, and OpenAPI documentation
 *       are accessible without authentication</li>
 *   <li><b>Protected Endpoints:</b> All other endpoints require OAuth2 authentication</li>
 *   <li><b>OAuth2 Login:</b> Supports user authentication through OAuth2 providers (Keycloak)</li>
 *   <li><b>Resource Server:</b> Validates JWT tokens for API access</li>
 *   <li><b>Token Propagation:</b> Automatically forwards bearer tokens to downstream services</li>
 *   <li><b>CSRF:</b> Disabled for stateless API operations</li>
 * </ul>
 * 
 * <p><b>WebClient Configurations:</b></p>
 * <ul>
 *   <li><b>Standard WebClient:</b> For load-balanced service discovery calls</li>
 *   <li><b>OAuth2 Client WebClient:</b> For OAuth2 client credentials flow</li>
 *   <li><b>Keycloak WebClient:</b> Dedicated client for Keycloak server communication</li>
 * </ul>
 * 
 * @author Zhubin Salehi
 * @see PermissionTokenExchangeFilter
 * @see TokenForwardingGatewayFilterFactory
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    /**
     * Configures the security filter chain for the application.
     * This bean defines the security rules:
     * - Permits unauthenticated access to /actuator/** endpoints
     * - Permits unauthenticated access to Swagger UI and OpenAPI endpoints for all services
     *   (e.g., /swagger-ui.html, /swagger-ui/**, /v3/api-docs, /rifles-service/v3/api-docs, /loads-service/v3/api-docs)
     * - Requires authentication for all other endpoints
     * - Enables OAuth2 login support
     * - Configures JWT-based OAuth2 resource server
     * - Disables CSRF protection
     *
     * @param http the ServerHttpSecurity to configure
     * @return a SecurityWebFilterChain representing the security configuration
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Allow unauthenticated access to actuator, Swagger UI, and OpenAPI endpoints
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/actuator/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/rifles-service/v3/api-docs",
                                "/loads-service/v3/api-docs",
                                "/components-service/v3/api-docs")
                        .permitAll()
                        // Require authentication for all other endpoints
                        .anyExchange().authenticated())
                // Enable OAuth2 login
                .oauth2Login(withDefaults())
                // Configure OAuth2 resource server with JWT support
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
                // Disable CSRF protection
                .csrf(csrf -> csrf.disable())
                .build();
    }

    /**
     * Creates a ServerBearerExchangeFilterFunction bean.
     * This filter function automatically propagates bearer tokens from the current
     * security context to outgoing requests made with WebClient, enabling authentication
     * propagation to downstream services.
     *
     * @return a ServerBearerExchangeFilterFunction for token propagation
     */
    @Bean
    public ServerBearerExchangeFilterFunction bearerExchangeFilter() {
        return new ServerBearerExchangeFilterFunction();
    }

    /**
     * Configures OAuth2 client support for WebClient requests.
     * This filter function manages OAuth2 token acquisition and renewal for client
     * credentials flow, using the specified client registration for authentication.
     *
     * @param clientRegistrations  the repository of OAuth2 client registrations
     * @param authorizedClients    the repository of authorized clients
     * @param clientRegistrationId the ID of the default client registration to use
     * @return a configured OAuth2 filter function
     */
    @Bean
    public ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client(
            ReactiveClientRegistrationRepository clientRegistrations,
            ServerOAuth2AuthorizedClientRepository authorizedClients,
            @Value("${spring.security.oauth2.client.registration.api-gateway.client-id}") String clientRegistrationId) {
        var oauth2 = new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations, authorizedClients);
        oauth2.setDefaultClientRegistrationId(clientRegistrationId);

        return oauth2;
    }

    /**
     * Creates a dedicated WebClient for Keycloak interactions with OpenTelemetry observability.
     * This WebClient is specifically configured for communication with the Keycloak
     * authorization server, used for authentication, token introspection, and other
     * identity management operations.
     * 
     * <p>The underlying HttpClient is created using Spring Cloud Gateway's HttpClientFactory,
     * which provides:</p>
     * <ul>
     *   <li>Connection pooling with max 50 connections</li>
     *   <li>5-second response timeout</li>
     *   <li>Metrics and observability support via Micrometer</li>
     *   <li>Base URL configuration for Keycloak server</li>
     * </ul>
     * 
     * <p><b>OpenTelemetry Integration:</b></p>
     * <p>The WebClient is configured with an {@link ObservationRegistry} that bridges Micrometer
     * observations to OpenTelemetry. This enables:</p>
     * <ul>
     *   <li><b>Distributed Tracing:</b> Automatic trace context propagation using W3C Trace Context
     *       standard (traceparent and tracestate headers). Each HTTP request creates a new span
     *       linked to the parent trace, providing end-to-end visibility across the gateway and
     *       Keycloak server.</li>
     *   <li><b>HTTP Client Metrics:</b> Request duration, response status codes, connection pool
     *       metrics, and error rates are automatically collected and exported to OpenTelemetry
     *       collectors.</li>
     *   <li><b>Contextual Logging:</b> Log correlation with trace IDs for troubleshooting failed
     *       token exchange operations.</li>
     * </ul>
     * 
     * <p>The trace context is automatically propagated from incoming gateway requests through
     * this WebClient to Keycloak, maintaining the distributed trace across the entire request flow.</p>
     *
     * @param baseUrl             the base URL of the Keycloak server
     * @param observationRegistry Micrometer registry bridged to OpenTelemetry for metrics and traces
     * @param httpClientFactory   Spring Cloud Gateway's HTTP client factory with pooling and timeouts
     * @return a WebClient configured for Keycloak communication with full OpenTelemetry observability
     */
    @Bean
    public WebClient keycloakWebClient(
            @Value("${spring.webclient.keycloak.base-url}") String baseUrl,
            ObservationRegistry observationRegistry,
            HttpClientFactory httpClientFactory) {
        
        try {
            return WebClient.builder()
                    .baseUrl(baseUrl)
                    .clientConnector(new ReactorClientHttpConnector(httpClientFactory.getObject()))
                    .observationRegistry(observationRegistry)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create Keycloak WebClient", e);
        }
    }
}
