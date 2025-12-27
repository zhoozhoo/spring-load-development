package ca.zhoozhoo.loaddev.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link UmaTokenExchangeService}.
 * Tests token exchange logic with mocked WebClient.
 * 
 * <p>Note: Cache testing is handled by integration tests since Spring Cache
 * requires Spring context and AOP proxies to function properly.</p>
 *
 * @author Zhubin Salehi
 */
@ExtendWith(MockitoExtension.class)
class UmaTokenExchangeServiceTest {

    @Mock
    private WebClient keycloakWebClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private UmaTokenExchangeService service;

    private static final String ACCESS_TOKEN = "access-token-12345";
    private static final String PERMISSION_TOKEN = "permission-token-67890";
    private static final String TOKEN_URI = "http://localhost:8080/auth/token";
    private static final String CLIENT_ID = "api-gateway";
    private static final String CLIENT_SECRET = "secret";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "tokenUri", TOKEN_URI);
        ReflectionTestUtils.setField(service, "clientId", CLIENT_ID);
        ReflectionTestUtils.setField(service, "clientSecret", CLIENT_SECRET);
    }

    @Test
    @DisplayName("Should successfully exchange token")
    void shouldSuccessfullyExchangeToken() {
        // Given
        setupSuccessfulWebClientMock();

        // Then
        StepVerifier.create(service.exchangeForPermissionToken(ACCESS_TOKEN))
                .assertNext(token -> {
                    assertThat(token.accessToken()).isEqualTo(PERMISSION_TOKEN);
                    assertThat(token.tokenType()).isEqualTo("Bearer");
                    assertThat(token.expiresIn()).isEqualTo(300);
                    assertThat(token.scope()).isEqualTo("openid");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should format authorization header correctly")
    void shouldFormatAuthorizationHeader() {
        // Given
        var token = new UmaPermissionToken(PERMISSION_TOKEN, "Bearer", 300, "openid");

        // Then
        assertThat(token.toAuthorizationHeaderValue()).isEqualTo("Bearer " + PERMISSION_TOKEN);
    }

    @Test
    @DisplayName("Should handle 401 Unauthorized error")
    void shouldHandle401Error() {
        // Given
        var exception = WebClientResponseException.create(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                HttpHeaders.EMPTY,
                "{\"error\":\"invalid_token\"}".getBytes(),
                null);
        setupFailedWebClientMock(exception);

        // Then
        StepVerifier.create(service.exchangeForPermissionToken(ACCESS_TOKEN))
                .expectErrorMatches(error -> error instanceof TokenExchangeException &&
                        error.getMessage().contains("Authentication failed"))
                .verify();
    }

    @Test
    @DisplayName("Should handle 403 Forbidden error")
    void shouldHandle403Error() {
        // Given
        setupFailedWebClientMock(WebClientResponseException.create(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                HttpHeaders.EMPTY,
                "{}".getBytes(),
                null));

        // Then
        StepVerifier.create(service.exchangeForPermissionToken(ACCESS_TOKEN))
                .expectErrorMatches(error -> error instanceof TokenExchangeException &&
                        error.getMessage().contains("not authorized"))
                .verify();
    }

    @Test
    @DisplayName("Should handle 400 Bad Request error")
    void shouldHandle400Error() {
        // Given
        setupFailedWebClientMock(WebClientResponseException.create(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                HttpHeaders.EMPTY,
                "{\"error\":\"invalid_request\"}".getBytes(),
                null));

        // Then
        StepVerifier.create(service.exchangeForPermissionToken(ACCESS_TOKEN))
                .expectErrorMatches(error -> error instanceof TokenExchangeException &&
                        error.getMessage().contains("Invalid token exchange request"))
                .verify();
    }

    @Test
    @DisplayName("Should handle 429 Rate Limit error")
    void shouldHandle429Error() {
        // Given
        setupFailedWebClientMock(WebClientResponseException.create(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too Many Requests",
                HttpHeaders.EMPTY,
                "{}".getBytes(),
                null));

        // Then - After retries with exponential backoff, expect retry exhausted
        // Note: Retry logic wraps the original exception, and error mapping happens
        // after retry
        StepVerifier.create(service.exchangeForPermissionToken(ACCESS_TOKEN))
                .expectErrorSatisfies(error -> {
                    assertThat(error.getMessage()).contains("Retries exhausted: 2/2");
                    // The original WebClientResponseException is the cause
                    assertThat(error.getCause())
                            .isInstanceOf(WebClientResponseException.TooManyRequests.class);
                })
                .verify();
    }

    @Test
    @DisplayName("Should handle 500 Server Error")
    void shouldHandle500Error() {
        // Given
        setupFailedWebClientMock(WebClientResponseException.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                HttpHeaders.EMPTY,
                "{}".getBytes(),
                null));

        // Then - After retries with exponential backoff, expect retry exhausted
        // Note: Retry logic wraps the original exception, and error mapping happens
        // after retry
        StepVerifier.create(service.exchangeForPermissionToken(ACCESS_TOKEN))
                .expectErrorSatisfies(error -> {
                    assertThat(error.getMessage()).contains("Retries exhausted: 2/2");
                    // The original WebClientResponseException is the cause
                    assertThat(error.getCause())
                            .isInstanceOf(WebClientResponseException.InternalServerError.class);
                })
                .verify();
    }

    @SuppressWarnings("unchecked")
    private void setupSuccessfulWebClientMock() {
        // Mock WebClient chain to return JSON string
        String jsonResponse = """
                {
                    "access_token": "%s",
                    "token_type": "Bearer",
                    "expires_in": 300,
                    "scope": "openid"
                }
                """.formatted(PERMISSION_TOKEN);

        when(keycloakWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Return response as String that will be deserialized
        when(responseSpec.bodyToMono(any(Class.class))).thenAnswer(invocation -> {
            // Simulate JSON deserialization by using Jackson
            var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var clazz = invocation.getArgument(0, Class.class);
            var obj = objectMapper.readValue(jsonResponse, clazz);
            return Mono.just(obj);
        });
    }

    @SuppressWarnings("unchecked")
    private void setupFailedWebClientMock(Throwable exception) {
        when(keycloakWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.error(exception));
    }
}
