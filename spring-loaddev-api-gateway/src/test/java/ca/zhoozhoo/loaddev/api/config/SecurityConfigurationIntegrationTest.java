package ca.zhoozhoo.loaddev.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import ca.zhoozhoo.loaddev.test.testcontainers.KeycloakTest;
import io.micrometer.observation.ObservationRegistry;

/**
 * Integration tests for {@link SecurityConfiguration}.
 * Tests the security configuration beans and OAuth2 setup with Keycloak.
 * 
 * @author Zhubin Salehi
 */
@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigurationIntegrationTest extends KeycloakTest {

    @Autowired
    private ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private ServerBearerExchangeFilterFunction bearerExchangeFilter;

    @Autowired
    private WebClient webClient;

    @Autowired
    private WebClient keycloakWebClient;

    @Autowired
    private ObservationRegistry observationRegistry;

    @Test
    @DisplayName("Should create Bearer token exchange filter bean")
    void shouldCreateBearerExchangeFilterBean() {
        assertThat(bearerExchangeFilter)
                .isNotNull()
                .isInstanceOf(ServerBearerExchangeFilterFunction.class);
    }

    @Test
    @DisplayName("Should create WebClient bean")
    void shouldCreateWebClientBean() {
        assertThat(webClient)
                .isNotNull()
                .isInstanceOf(WebClient.class);
    }

    @Test
    @DisplayName("Should create Keycloak WebClient with base URL")
    void shouldCreateKeycloakWebClient() {
        assertThat(keycloakWebClient)
                .isNotNull()
                .isInstanceOf(WebClient.class);
    }

    @Test
    @DisplayName("Should have ObservationRegistry configured")
    void shouldHaveObservationRegistry() {
        assertThat(observationRegistry)
                .isNotNull()
                .isInstanceOf(ObservationRegistry.class);
    }

    @Test
    @DisplayName("Should have client registration repository configured")
    void shouldHaveClientRegistrationRepository() {
        assertThat(clientRegistrationRepository).isNotNull();
        var registration = clientRegistrationRepository.findByRegistrationId("api-gateway").block();
        assertThat(registration).isNotNull();
        assertThat(registration.getClientId()).isEqualTo("reloading-client");
        assertThat(registration.getClientSecret()).isEqualTo("2EvQuluZfxaaRms8V4NhzBDWzVCSXtty");
    }

    @Test
    @DisplayName("Should configure Keycloak provider correctly")
    void shouldConfigureKeycloakProvider() {
        var registration = clientRegistrationRepository.findByRegistrationId("api-gateway").block();
        assertThat(registration).isNotNull();
        assertThat(registration.getProviderDetails().getIssuerUri()).contains("reloading");
    }

    @Test
    @DisplayName("Keycloak container should be running")
    void keycloakContainerShouldBeRunning() {
        assertThat(keycloak.isRunning()).isTrue();
        assertThat(keycloak.getAuthServerUrl())
                .isNotEmpty()
                .startsWith("http://");
    }

    @Test
    @DisplayName("Should verify Keycloak realm is accessible")
    void shouldVerifyKeycloakRealmIsAccessible() {
        var realmUrl = keycloak.getAuthServerUrl() + "/realms/reloading";
        var response = keycloakWebClient.get()
                .uri(realmUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        assertThat(response)
                .isNotNull()
                .contains("reloading")
                .contains("realm")
                .containsAnyOf("public_key", "token-service", "account-service");
    }
}
