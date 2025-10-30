package ca.zhoozhoo.loaddev.loads.testcontainers;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

import dasniko.testcontainers.keycloak.KeycloakContainer;

/**
 * Abstract base class for loads service integration tests.
 * Provides Keycloak testcontainer with dynamic property registration.
 * Test classes should extend this class to inherit the Keycloak container setup.
 * 
 * @author Zhubin Salehi
 */
public abstract class KeycloakTest {

    // Keycloak container is managed as a singleton and closed by JVM shutdown hook
    @SuppressWarnings("resource")
    protected static final KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.4")
            .withRealmImportFile("realm.json");

    static {
        keycloak.start();
    }
    
    /**
     * Dynamically registers Keycloak properties after container starts.
     * These properties override the static values in application-test.yml
     * 
     * @param registry the dynamic property registry
     */
    @DynamicPropertySource
    static void keycloakProperties(DynamicPropertyRegistry registry) {
        var authServerUrl = keycloak.getAuthServerUrl();
        var issuerUri = authServerUrl + "/realms/reloading";
        var tokenUri = issuerUri + "/protocol/openid-connect/token";
        var authorizationUri = issuerUri + "/protocol/openid-connect/auth";
        var userInfoUri = issuerUri + "/protocol/openid-connect/userinfo";
        var jwkSetUri = issuerUri + "/protocol/openid-connect/certs";
        
        // Override the OAuth2 provider properties
        registry.add("spring.security.oauth2.client.provider.keycloak.token-uri", () -> tokenUri);
        registry.add("spring.security.oauth2.client.provider.keycloak.authorization-uri", () -> authorizationUri);
        registry.add("spring.security.oauth2.client.provider.keycloak.user-info-uri", () -> userInfoUri);
        registry.add("spring.security.oauth2.client.provider.keycloak.jwk-set-uri", () -> jwkSetUri);
        
        // Override the OAuth2 resource server properties
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> jwkSetUri);
        
        // Override the Keycloak WebClient base URL
        registry.add("spring.webclient.keycloak.base-url", () -> authServerUrl);
    }

    /**
     * Gets the Keycloak container instance.
     * 
     * @return the KeycloakContainer instance
     */
    protected static KeycloakContainer getKeycloakContainer() {
        return keycloak;
    }

    /**
     * Helper method to obtain an access token from Keycloak using client credentials flow.
     * Uses a separate WebClient instance that bypasses the loads service security to directly
     * communicate with the Keycloak testcontainer.
     * 
     * @return the access token
     */
    protected String getAccessToken() {
        var tokenUrl = keycloak.getAuthServerUrl() + "/realms/reloading/protocol/openid-connect/token";

        try {
            return WebClient.builder()
                    .build()
                    .post()
                    .uri(tokenUrl)
                    .header(CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .bodyValue("""
                            grant_type=client_credentials\
                            &client_id=reloading-client\
                            &client_secret=2EvQuluZfxaaRms8V4NhzBDWzVCSXtty\
                            """)
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .map(TokenResponse::accessToken)
                    .block();
        } catch (Exception e) {
            return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0In0.test";
        }
    }

    /**
     * Record for deserializing token response from Keycloak.
     * Uses Jackson annotations to map JSON field names to Java names.
     */
    protected record TokenResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("access_token") String accessToken,
            @com.fasterxml.jackson.annotation.JsonProperty("token_type") String tokenType,
            @com.fasterxml.jackson.annotation.JsonProperty("expires_in") int expiresIn) {
    }
}
