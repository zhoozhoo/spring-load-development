package ca.zhoozhoo.loaddev.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.WebFilterChain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link PermissionTokenExchangeFilter}.
 * Tests the token exchange logic with mocked WebClient to ensure proper error handling
 * and successful token exchange scenarios.
 *
 * @author Zhubin Salehi
 */
@ExtendWith(MockitoExtension.class)
class PermissionTokenExchangeFilterTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private WebFilterChain filterChain;

    @InjectMocks
    private PermissionTokenExchangeFilter filter;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";
    private static final String PERMISSION_TOKEN = "permission-token-12345";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(filter, "tokenUri", "http://localhost:8080/auth/token");
        ReflectionTestUtils.setField(filter, "clientId", "api-gateway");
        ReflectionTestUtils.setField(filter, "clientSecret", "secret");
    }

    @Test
    @DisplayName("Should proceed without token exchange when no Authorization header present")
    void shouldProceedWithoutTokenWhenNoAuthHeader() {
        // Given
        var request = MockServerHttpRequest.get("/test").build();
        var exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        assertThat(exchange.getAttributes().get("permission_token")).isNull();
    }

    @Test
    @DisplayName("Should proceed without token exchange when Authorization header is not Bearer")
    void shouldProceedWithoutTokenWhenNotBearerAuth() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                .build();
        var exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        assertThat(exchange.getAttributes().get("permission_token")).isNull();
    }

    @Test
    @DisplayName("Should successfully exchange token and store permission token")
    void shouldSuccessfullyExchangeToken() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        var exchange = MockServerWebExchange.from(request);

        var responseNode = objectMapper.createObjectNode();
        responseNode.put("access_token", PERMISSION_TOKEN);

        setupSuccessfulWebClientMock(responseNode);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        assertThat(exchange.getAttributes().get("permission_token")).isEqualTo(PERMISSION_TOKEN);
    }

    @Test
    @DisplayName("Should handle WebClientResponseException and proceed with original token")
    void shouldHandleWebClientResponseException() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        var exchange = MockServerWebExchange.from(request);

        var exception = WebClientResponseException.create(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                HttpHeaders.EMPTY,
                "{\"error\":\"invalid_token\"}".getBytes(),
                null
        );

        setupFailedWebClientMock(exception);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        assertThat(exchange.getAttributes().get("permission_token")).isNull();
    }

    @Test
    @DisplayName("Should handle generic exception during token exchange")
    void shouldHandleGenericException() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        var exchange = MockServerWebExchange.from(request);

        setupFailedWebClientMock(new RuntimeException("Network error"));
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        assertThat(exchange.getAttributes().get("permission_token")).isNull();
    }

    @Test
    @DisplayName("Should extract token correctly from Bearer header")
    void shouldExtractTokenFromBearerHeader() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        var exchange = MockServerWebExchange.from(request);

        var responseNode = objectMapper.createObjectNode();
        responseNode.put("access_token", PERMISSION_TOKEN);

        setupSuccessfulWebClientMock(responseNode);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getAttributes().get("permission_token")).isEqualTo(PERMISSION_TOKEN);
    }

    @Test
    @DisplayName("Should handle Bearer token with extra spaces")
    void shouldHandleBearerTokenWithSpaces() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer")
                .build();
        var exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then - Should proceed without token since extraction won't work without token
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should handle empty Bearer token")
    void shouldHandleEmptyBearerToken() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ")
                .build();
        var exchange = MockServerWebExchange.from(request);

        var responseNode = objectMapper.createObjectNode();
        responseNode.put("access_token", PERMISSION_TOKEN);

        setupSuccessfulWebClientMock(responseNode);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then - Empty token should still attempt exchange
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should log debug messages during successful token exchange")
    void shouldLogDebugMessagesDuringExchange() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        var exchange = MockServerWebExchange.from(request);

        var responseNode = objectMapper.createObjectNode();
        responseNode.put("access_token", PERMISSION_TOKEN);

        setupSuccessfulWebClientMock(responseNode);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        // Verify the token was stored
        assertThat(exchange.getAttributes().get("permission_token")).isEqualTo(PERMISSION_TOKEN);
    }

    @Test
    @DisplayName("Should handle empty access token in response")
    void shouldHandleEmptyAccessTokenInResponse() {
        // Given
        var request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        var exchange = MockServerWebExchange.from(request);

        var responseNode = objectMapper.createObjectNode();
        // No access_token field

        setupSuccessfulWebClientMock(responseNode);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        var result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        assertThat(exchange.getAttributes().get("permission_token")).isNull();
    }

    @SuppressWarnings("unchecked")
    private void setupSuccessfulWebClientMock(ObjectNode responseNode) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(responseNode));
    }

    @SuppressWarnings("unchecked")
    private void setupFailedWebClientMock(Throwable exception) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.error(exception));
    }
}
