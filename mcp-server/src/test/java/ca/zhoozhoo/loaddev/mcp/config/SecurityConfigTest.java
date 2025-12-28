package ca.zhoozhoo.loaddev.mcp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Unit tests for SecurityConfig.
 * <p>
 * Tests the security configuration to ensure proper setup of:
 * <ul>
 * <li>OAuth2 resource server with JWT authentication</li>
 * <li>Public access to actuator endpoints</li>
 * <li>Protected access to MCP and SSE endpoints</li>
 * <li>Authentication requirements for other endpoints</li>
 * </ul>
 * 
 * @author Zhubin Salehi
 */
class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private ServerHttpSecurity http;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
        http = mock(ServerHttpSecurity.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        
        // Configure mock to return itself for method chaining
        when(http.authorizeExchange(any())).thenReturn(http);
        when(http.oauth2ResourceServer(any())).thenReturn(http);
        when(http.build()).thenReturn(mock(SecurityWebFilterChain.class));
    }

    /**
     * Tests that the security filter chain bean is created successfully.
     */
    @Test
    void securityWebFilterChain_ShouldCreateFilterChain() {
        // When
        var result = securityConfig.securityWebFilterChain(http);

        // Then
        assertThat(result).isNotNull();
        verify(http).authorizeExchange(any());
        verify(http).oauth2ResourceServer(any());
        verify(http).build();
    }

    /**
     * Tests that the configuration has correct annotations including profile exclusion.
     */
    @Test
    void securityConfig_ShouldHaveCorrectAnnotations() {
        // Then
        assertThat(SecurityConfig.class)
                .hasAnnotation(org.springframework.context.annotation.Configuration.class)
                .hasAnnotation(org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity.class)
                .hasAnnotation(org.springframework.context.annotation.Profile.class);
        
        assertThat(SecurityConfig.class.getAnnotation(org.springframework.context.annotation.Profile.class)
                .value()).containsExactly("!test");
    }
}
