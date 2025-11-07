package ca.zhoozhoo.loaddev.loads.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.test.testcontainers.KeycloakTest;

/**
 * Enhanced integration tests for {@link SecurityUtils}.
 * Tests reactive security context operations with real Keycloak JWT tokens.
 * 
 * @author Zhubin Salehi
 */
@SpringBootTest
@ActiveProfiles("test")
class SecurityUtilsIntegrationTest extends KeycloakTest {

    @Autowired
    private SecurityUtils securityUtils;

    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = getAccessToken();
    }

    @Test
    @DisplayName("SecurityUtils bean should be created")
    void shouldCreateSecurityUtilsBean() {
        assertThat(securityUtils).isNotNull();
    }

    @Test
    @DisplayName("Should validate token structure from Keycloak")
    void shouldValidateTokenStructureFromKeycloak() {
        assertThat(accessToken)
                .isNotNull()
                .contains(".")
                .matches("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$");
    }
}
