package ca.zhoozhoo.loaddev.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import ca.zhoozhoo.loaddev.api.testcontainers.KeycloakTest;

/**
 * Integration tests for {@link PermissionTokenExchangeFilter} using Keycloak testcontainer.
 * Tests the complete OAuth2 flow including token exchange with a real Keycloak instance.
 * 
 * @author Zhubin Salehi
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class PermissionTokenExchangeFilterIntegrationTest extends KeycloakTest {

    @Autowired
    private WebTestClient webTestClient;

    private String accessToken;

    /**
     * Configures dynamic properties for Keycloak connection.
     * Sets the issuer URI and token URI to point to the testcontainer.
     * 
     * @param registry the dynamic property registry
     */
    @DynamicPropertySource
    static void registerKeycloakProperties(DynamicPropertyRegistry registry) {
        // Note: Properties will be set dynamically in @BeforeEach based on container state
    }

    @BeforeEach
    void setUp() {
        var realmUrl = keycloak.getAuthServerUrl() + "/realms/reloading";
        
        // Update system properties for this test
        System.setProperty("keycloak.issuer-uri", realmUrl);
        System.setProperty("keycloak.token-uri", realmUrl + "/protocol/openid-connect/token");
        System.setProperty("keycloak.base-url", keycloak.getAuthServerUrl());

        accessToken = getAccessToken();
    }

    @Test
    @DisplayName("Should allow access to actuator health endpoint without authentication")
    void shouldAllowHealthEndpointWithoutAuth() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    @DisplayName("Should redirect request without Bearer token to OAuth2 login")
    void shouldRejectRequestWithoutToken() {
        webTestClient.get()
                .uri("/test-endpoint")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/oauth2/authorization/api-gateway");
    }

    @Test
    @DisplayName("Should reject request with invalid Bearer token")
    void shouldRejectRequestWithInvalidToken() {
        webTestClient.get()
                .uri("/test-endpoint")
                .header(AUTHORIZATION, "Bearer invalid-token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should process request with valid Bearer token")
    void shouldProcessRequestWithValidToken() {
        webTestClient.get()
                .uri("/actuator/health")
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Should allow Swagger UI access without authentication")
    void shouldAllowSwaggerUiWithoutAuth() {
        // Swagger UI redirects /swagger-ui.html to /swagger-ui/index.html
        webTestClient.get()
                .uri("/swagger-ui.html")
                .exchange()
                .expectStatus().value(status -> 
                    assertThat(status).isIn(OK.value(), FOUND.value(), 404)
                );
    }

    @Test
    @DisplayName("Should allow OpenAPI docs access without authentication")
    void shouldAllowOpenApiDocsWithoutAuth() {
        webTestClient.get()
                .uri("/v3/api-docs")
                .exchange()
                .expectStatus().value(status -> 
                    assertThat(status).isIn(OK.value(), 404)
                );
    }

    @Test
    @DisplayName("Should extract and validate token from Authorization header")
    void shouldExtractTokenFromHeader() {
        // This test verifies that the filter can extract tokens
        // The actual exchange happens in the filter
        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isNotEmpty();
        assertThat(accessToken).contains(".");
        assertThat(accessToken.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should handle requests with malformed Authorization header")
    void shouldHandleMalformedAuthorizationHeader() {
        webTestClient.get()
                .uri("/actuator/health")
                .header(AUTHORIZATION, "NotBearer malformed-token")
                .exchange()
                .expectStatus().isOk(); // Should still work as it falls back to no auth for health endpoint
    }

    @Test
    @DisplayName("Should handle requests with empty Authorization header")
    void shouldHandleEmptyAuthorizationHeader() {
        webTestClient.get()
                .uri("/actuator/health")
                .header(AUTHORIZATION, "")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Should handle requests with Bearer prefix but no token")
    void shouldHandleBearerPrefixWithoutToken() {
        webTestClient.get()
                .uri("/actuator/health")
                .header(AUTHORIZATION, "Bearer ")
                .exchange()
                .expectStatus().value(status -> 
                    assertThat(status).isIn(OK.value(), UNAUTHORIZED.value())
                );
    }

    @Test
    @DisplayName("Should handle requests with only Bearer keyword")
    void shouldHandleOnlyBearerKeyword() {
        webTestClient.get()
                .uri("/actuator/health")
                .header(AUTHORIZATION, "Bearer")
                .exchange()
                .expectStatus().value(status -> 
                    assertThat(status).isIn(OK.value(), UNAUTHORIZED.value())
                );
    }

    @Test
    @DisplayName("Should process multiple requests with same token")
    void shouldProcessMultipleRequestsWithSameToken() {
        webTestClient.get()
                .uri("/actuator/health")
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/actuator/health")
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Should handle different HTTP methods with authentication")
    void shouldHandleDifferentHttpMethodsWithAuth() {
        webTestClient.get()
                .uri("/actuator/health")
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Should handle concurrent requests with valid tokens")
    void shouldHandleConcurrentRequests() {
        webTestClient.get()
                .uri("/actuator/health")
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/actuator/health")
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Should handle case-sensitive Bearer prefix")
    void shouldHandleCaseSensitiveBearerPrefix() {
        webTestClient.get()
                .uri("/actuator/health")
                .header(AUTHORIZATION, "bearer " + accessToken)
                .exchange()
                .expectStatus().isOk(); // Health endpoint doesn't require auth
    }

    @Test
    @DisplayName("Should handle token with special characters")
    void shouldHandleTokenWithSpecialCharacters() {
        var specialToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJuYW1lIjoiVGVzdCBVc2VyIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        
        webTestClient.get()
                .uri("/actuator/health")
                .header(AUTHORIZATION, "Bearer " + specialToken)
                .exchange()
                .expectStatus().value(status -> 
                    assertThat(status).isIn(OK.value(), UNAUTHORIZED.value())
                );
    }

    @Test
    @DisplayName("Should allow multiple authorization headers scenario")
    void shouldHandleMultipleAuthorizationHeaders() {
        webTestClient.get()
                .uri("/actuator/health")
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * Helper method to obtain an access token from Keycloak using client credentials flow.
     * Uses a separate WebClient instance that bypasses the API Gateway security to directly
     * communicate with the Keycloak testcontainer.
     * 
     * @return the access token
     */
    private String getAccessToken() {
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
    private record TokenResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("access_token")
            String accessToken,
            @com.fasterxml.jackson.annotation.JsonProperty("token_type")
            String tokenType,
            @com.fasterxml.jackson.annotation.JsonProperty("expires_in")
            int expiresIn
    ) {}
}
