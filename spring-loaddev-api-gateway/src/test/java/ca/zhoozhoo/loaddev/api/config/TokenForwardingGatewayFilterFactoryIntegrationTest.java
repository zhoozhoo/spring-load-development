package ca.zhoozhoo.loaddev.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.api.testcontainers.KeycloakTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Integration tests for {@link TokenForwardingGatewayFilterFactory}.
 * Tests the complete flow of token forwarding through Spring Cloud Gateway to a mock downstream service.
 * Uses MockWebServer to simulate a downstream microservice and verify token forwarding behavior.
 * 
 * @author Zhubin Salehi
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Import(TokenForwardingGatewayFilterFactoryIntegrationTest.TestRouteConfiguration.class)
class TokenForwardingGatewayFilterFactoryIntegrationTest extends KeycloakTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TokenForwardingGatewayFilterFactory filterFactory;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @DynamicPropertySource
    static void registerMockWebServerUrl(DynamicPropertyRegistry registry) {
        registry.add("test.downstream.service.url", 
                () -> "http://localhost:" + mockWebServer.getPort());
    }

    @Test
    @DisplayName("Should create TokenForwardingGatewayFilterFactory bean")
    void shouldCreateFilterFactoryBean() {
        assertThat(filterFactory)
                .isNotNull()
                .isInstanceOf(TokenForwardingGatewayFilterFactory.class);
    }

    @Test
    @DisplayName("Should forward request to downstream service via gateway")
    void shouldForwardRequestToDownstreamService() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"message\": \"success\"}"));

        webTestClient.get()
                .uri("/test-service/api/endpoint")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("success");

        var request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/api/endpoint");
        assertThat(request.getMethod()).isEqualTo("GET");
    }

    @Test
    @DisplayName("Should forward Authorization header when present")
    void shouldForwardAuthorizationHeader() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"authenticated\": true}"));

        webTestClient.get()
                .uri("/test-service/api/secure")
                .header(AUTHORIZATION, "Bearer test-token-12345")
                .exchange()
                .expectStatus().isOk();

        var request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization"))
                .isNotNull()
                .startsWith("Bearer ");
    }

    @Test
    @DisplayName("Should handle downstream service errors")
    void shouldHandleDownstreamServiceErrors() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\": \"Internal Server Error\"}"));

        webTestClient.get()
                .uri("/test-service/api/failing")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Internal Server Error");
    }

    @Test
    @DisplayName("Should handle downstream service not found")
    void shouldHandleDownstreamNotFound() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\": \"Not Found\"}"));

        webTestClient.get()
                .uri("/test-service/api/nonexistent")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    @DisplayName("Should strip prefix before forwarding to downstream")
    void shouldStripPrefixBeforeForwarding() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"path\": \"/resource\"}"));

        webTestClient.get()
                .uri("/test-service/resource")
                .exchange()
                .expectStatus().isOk();

        var request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/resource");
    }

    @Test
    @DisplayName("Should forward POST requests with body")
    void shouldForwardPostRequestsWithBody() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"id\": \"123\", \"status\": \"created\"}"));

        webTestClient.post()
                .uri("/test-service/api/create")
                .header("Content-Type", "application/json")
                .bodyValue("{\"name\": \"test\", \"value\": \"data\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("123")
                .jsonPath("$.status").isEqualTo("created");

        var request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/api/create");
        assertThat(request.getBody().readUtf8())
                .contains("\"name\"")
                .contains("\"test\"");
    }

    @Test
    @DisplayName("Should handle multiple sequential requests")
    void shouldHandleMultipleSequentialRequests() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"request\": 1}"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"request\": 2}"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"request\": 3}"));

        webTestClient.get().uri("/test-service/api/1").exchange().expectStatus().isOk();
        webTestClient.get().uri("/test-service/api/2").exchange().expectStatus().isOk();
        webTestClient.get().uri("/test-service/api/3").exchange().expectStatus().isOk();

        assertThat(mockWebServer.getRequestCount()).isEqualTo(3);
        
        var request1 = mockWebServer.takeRequest();
        var request2 = mockWebServer.takeRequest();
        var request3 = mockWebServer.takeRequest();
        
        assertThat(request1.getPath()).isEqualTo("/api/1");
        assertThat(request2.getPath()).isEqualTo("/api/2");
        assertThat(request3.getPath()).isEqualTo("/api/3");
    }

    @Test
    @DisplayName("Should preserve query parameters when forwarding")
    void shouldPreserveQueryParameters() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"filtered\": true}"));

        webTestClient.get()
                .uri("/test-service/api/search?query=test&limit=10&sort=asc")
                .exchange()
                .expectStatus().isOk();

        var request = mockWebServer.takeRequest();
        assertThat(request.getPath())
                .contains("query=test")
                .contains("limit=10")
                .contains("sort=asc");
    }

    @Test
    @DisplayName("Should forward custom headers")
    void shouldForwardCustomHeaders() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"success\": true}"));

        webTestClient.get()
                .uri("/test-service/api/headers")
                .header("X-Request-ID", "req-123")
                .header("X-Correlation-ID", "corr-456")
                .header("X-Custom-Header", "custom-value")
                .exchange()
                .expectStatus().isOk();

        var request = mockWebServer.takeRequest();
        assertThat(request.getHeader("X-Request-ID")).isEqualTo("req-123");
        assertThat(request.getHeader("X-Correlation-ID")).isEqualTo("corr-456");
        assertThat(request.getHeader("X-Custom-Header")).isEqualTo("custom-value");
    }

    @Test
    @DisplayName("Should create Config record")
    void shouldCreateConfigRecord() {
        var config = new TokenForwardingGatewayFilterFactory.Config();
        
        assertThat(config)
                .isNotNull()
                .isInstanceOf(TokenForwardingGatewayFilterFactory.Config.class);
    }

    @Test
    @DisplayName("Config records should be equal")
    void configRecordsShouldBeEqual() {
        var config1 = new TokenForwardingGatewayFilterFactory.Config();
        var config2 = new TokenForwardingGatewayFilterFactory.Config();
        
        assertThat(config1)
                .isEqualTo(config2)
                .hasSameHashCodeAs(config2);
        assertThat(config1.toString()).isEqualTo(config2.toString());
    }

    @TestConfiguration
    static class TestRouteConfiguration {
        
        @Bean
        RouteLocator testRoutes(RouteLocatorBuilder builder, 
                               TokenForwardingGatewayFilterFactory tokenForwardingFactory) {
            return builder.routes()
                    .route("test-service-route", r -> r
                            .path("/test-service/**")
                            .filters(f -> f
                                    .stripPrefix(1)
                                    .filter(tokenForwardingFactory.apply(
                                            new TokenForwardingGatewayFilterFactory.Config())))
                            .uri("${test.downstream.service.url}"))
                    .build();
        }
    }
}
