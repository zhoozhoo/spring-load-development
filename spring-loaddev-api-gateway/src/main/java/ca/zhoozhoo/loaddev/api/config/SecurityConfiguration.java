package ca.zhoozhoo.loaddev.api.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.log4j.Log4j2;

/**
 * Configuration class for Spring Security settings in a Reactive Web application.
 * This class defines the security policies and configurations for the application,
 * including OAuth2 login, resource server capabilities, bearer token propagation,
 * and webclient setup for service communication.
 */
@Configuration
@EnableWebFluxSecurity
@Log4j2
public class SecurityConfiguration {

    /**
     * Configures the security filter chain for the application.
     * This bean defines the security rules:
     * - Permits unauthenticated access to /actuator/** endpoints
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
                // Allow unauthenticated access to /actuator/** endpoints
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
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
     * This filter function automatically propagates bearer tokens from the current security context
     * to outgoing requests made with WebClient, enabling authentication propagation to downstream services.
     *
     * @return a ServerBearerExchangeFilterFunction for token propagation
     */
    @Bean
    public ServerBearerExchangeFilterFunction bearerExchangeFilter() {
        return new ServerBearerExchangeFilterFunction();
    }

    /**
     * Configures a WebClient with load balancing capabilities.
     * This WebClient is used for making requests to other services through service discovery,
     * with automatic load balancing across available service instances.
     *
     * @param lbFunction the ReactorLoadBalancerExchangeFilterFunction for load balancing
     * @return a WebClient instance with load balancing support
     */
    @Bean
    public WebClient webClient(ReactorLoadBalancerExchangeFilterFunction lbFunction) {
        return WebClient.builder()
                .filter(lbFunction)
                .build();
    }

    /**
     * Configures OAuth2 client support for WebClient requests.
     * This filter function manages OAuth2 token acquisition and renewal for client credentials flow,
     * using the specified client registration for authentication.
     *
     * @param clientRegistrations the repository of OAuth2 client registrations
     * @param authorizedClients the repository of authorized clients
     * @param clientRegistrationId the ID of the default client registration to use
     * @return a configured OAuth2 filter function
     */
    @Bean
    public ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client(
            ReactiveClientRegistrationRepository clientRegistrations,
            ServerOAuth2AuthorizedClientRepository authorizedClients,
            @Value("${spring.security.oauth2.client.registration.api-gateway.client-id}") String clientRegistrationId) {

        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                clientRegistrations, authorizedClients);
        oauth2.setDefaultClientRegistrationId(clientRegistrationId);
        return oauth2;
    }

    /**
     * Creates a dedicated WebClient for Keycloak interactions.
     * This WebClient is specifically configured for communication with the Keycloak authorization server,
     * used for authentication, token introspection, and other identity management operations.
     *
     * @param baseUrl the base URL of the Keycloak server
     * @return a WebClient configured for Keycloak communication
     */
    @Bean
    public WebClient keycloakWebClient(@Value("${spring.webclient.keycloak.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
