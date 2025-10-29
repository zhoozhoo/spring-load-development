package ca.zhoozhoo.loaddev.rifles.testcontainers;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import dasniko.testcontainers.keycloak.KeycloakContainer;

@SpringBootTest
public abstract class KeycloakTest {

    @SuppressWarnings("resource")
    private static final KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.4")
            .withRealmImportFile("../docker/keycloak/realm.json");

    static {
        keycloak.start();
    }

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/baeldung");
    }

    @DynamicPropertySource
    static void keycloakProperties(DynamicPropertyRegistry registry) {
        var issuerUri = keycloak.getAuthServerUrl() + "/realms/reloading";
        var tokenUri = issuerUri + "/protocol/openid-connect/token";
        
        registry.add("keycloak.issuer-uri", () -> issuerUri);
        registry.add("keycloak.token-uri", () -> tokenUri);
        registry.add("spring.security.oauth2.client.provider.keycloak.issuer-uri", () -> issuerUri);
        registry.add("spring.security.oauth2.client.provider.keycloak.token-uri", () -> tokenUri);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> issuerUri);
    }

}
