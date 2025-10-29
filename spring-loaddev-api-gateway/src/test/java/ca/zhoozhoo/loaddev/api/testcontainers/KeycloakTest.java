package ca.zhoozhoo.loaddev.api.testcontainers;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import dasniko.testcontainers.keycloak.KeycloakContainer;

/**
 * Abstract base class for API Gateway integration tests.
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
}
