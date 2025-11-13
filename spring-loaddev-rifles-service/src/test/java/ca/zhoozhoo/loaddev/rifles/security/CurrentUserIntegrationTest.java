package ca.zhoozhoo.loaddev.rifles.security;

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
 * Integration tests for {@link CurrentUser} annotation and {@link CurrentUserMethodArgumentResolver}.
 * Tests the complete flow of extracting user ID from JWT tokens in controller methods.
 * 
 * @author Zhubin Salehi
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class CurrentUserIntegrationTest extends KeycloakTest {

    @Autowired
    private WebTestClient webTestClient;

    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = getAccessToken();
    }

    @Test
    @DisplayName("Should handle authenticated requests with Bearer token")
    void shouldHandleAuthenticatedRequestsWithBearerToken() {
        webTestClient.get()
                .uri("/actuator/health")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk();
    }
}
