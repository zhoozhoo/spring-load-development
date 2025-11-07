package ca.zhoozhoo.loaddev.loads.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.test.testcontainers.KeycloakTest;

/**
 * Integration tests for {@link SecurityConfiguration} with Keycloak testcontainer.
 * Tests OAuth2 resource server configuration with real Keycloak JWT tokens.
 * 
 * @author Zhubin Salehi
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class SecurityConfigurationIntegrationTest extends KeycloakTest {

    @Autowired
    private WebTestClient webTestClient;

    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = getAccessToken();
    }

    @Test
    @DisplayName("Should process authenticated requests with valid token")
    void shouldProcessAuthenticatedRequestsWithValidToken() {
        webTestClient.get()
                .uri("/actuator/health")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Should validate Keycloak container is running")
    void shouldValidateKeycloakContainerIsRunning() {
        assertThat(keycloak.isRunning()).isTrue();
        assertThat(keycloak.getAuthServerUrl())
                .isNotEmpty()
                .startsWith("http://");
    }

    @Test
    @DisplayName("Should obtain valid access token from Keycloak")
    void shouldObtainValidAccessTokenFromKeycloak() {
        assertThat(accessToken)
                .isNotNull()
                .isNotEmpty()
                .contains(".")
                .matches("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$");
    }
}
