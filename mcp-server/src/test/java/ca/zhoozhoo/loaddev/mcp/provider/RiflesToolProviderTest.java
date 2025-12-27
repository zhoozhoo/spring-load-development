package ca.zhoozhoo.loaddev.mcp.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Import;

import ca.zhoozhoo.loaddev.mcp.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.mcp.dto.RifleDto;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Integration test for the RiflesToolProvider MCP tools.
 * <p>
 * This test class verifies rifle-related MCP tool functionality by executing actual
 * tool calls against a running Spring Boot server instance with mocked backend services.
 * <p>
 * The test suite includes:
 * <ul>
 * <li>getRifles - Retrieve all rifles</li>
 * <li>getRifleById - Retrieve a specific rifle by ID</li>
 * </ul>
 * <p>
 * Each test method runs independently with proper setup and teardown
 * to ensure test isolation and consistent results.
 * <p>
 * Test infrastructure:
 * <ul>
 * <li>Uses {@link MockWebServer} from OkHttp to simulate rifles-service (port 8083)</li>
 * <li>Mocks {@link DiscoveryClient} to return service instances pointing to mock servers</li>
 * <li>Provides mock JWT authentication via {@link TestSecurityConfig} to satisfy
 * security context requirements</li>
 * <li>Uses {@link McpAsyncClient} with WebFlux SSE transport for async communication</li>
 * </ul>
 * 
 * @author Zhubin Salehi
 * @see RiflesToolProvider
 * @see io.modelcontextprotocol.client.McpAsyncClient
 * @see okhttp3.mockwebserver.MockWebServer
 * @see TestSecurityConfig
 */
@Import(BaseMcpToolProviderTest.TestWebClientConfig.class)
public class RiflesToolProviderTest extends BaseMcpToolProviderTest {

    private static MockWebServer mockRiflesServer;
    private static final String RIFLE_WITH_QUANTITIES_JSON = """
            {
                \"id\": 1,
                \"name\": \"Test Rifle\",
                \"caliber\": \"6.5 Creedmoor\",
                \"barrelLength\": { \"value\": 61.0, \"unit\": \"cm\" },
                \"rifling\": {
                    \"twistRate\": { \"value\": 8.0, \"unit\": \"[in_i]\" },
                    \"twistDirection\": \"RIGHT\",
                    \"numberOfGrooves\": 4
                },
                \"zeroing\": {
                    \"sightHeight\": { \"value\": 1.5, \"unit\": \"[in_i]\" },
                    \"zeroDistance\": { \"value\": 100.0, \"unit\": \"[yd_i]\" }
                }
            }
            """;

    /**
     * Sets up the rifles-service mock server.
     * <p>
     * Creates and configures the MockWebServer instance on port 8083 with
     * a dispatcher that handles rifle-related endpoints.
     */
    @Override
    protected void setupMockServers() throws IOException {
        mockRiflesServer = new MockWebServer();
        mockRiflesServer.setDispatcher(createRiflesDispatcher());
        mockRiflesServer.start(8083);
    }

    /**
     * Shuts down the rifles-service mock server.
     */
    @Override
    protected void shutdownMockServers() throws IOException {
        if (mockRiflesServer != null) {
            mockRiflesServer.shutdown();
        }
    }

    /**
     * Mocks the DiscoveryClient to return the rifles-service instance.
     */
    @Override
    protected void mockServiceDiscovery() {
        mockService("rifles-service", createServiceInstance(
                "rifles-service-1", "rifles-service", mockRiflesServer));
    }

    /**
     * Tests the getRifles tool.
     * <p>
     * Verifies that the server can retrieve a list of all rifles by invoking
     * the MCP tool with no arguments. The tool calls RiflesService which makes
     * an authenticated WebClient request to the mock rifles-service, extracting
     * the JWT token from the security context.
     * <p>
     * Validates that the result is not null and indicates no errors.
     */
    @Test
    void getRifles() {
        var riflesResult = client.callTool(new CallToolRequest("getRifles", Map.of())).block();

        assertThat(riflesResult).isNotNull();
        assertThat(riflesResult.isError()).isFalse();
    }

    /**
     * Tests the getRifleById tool with a specific rifle ID.
     * <p>
     * Verifies that the server can retrieve a single rifle by its ID by invoking
     * the MCP tool with an ID parameter. The tool calls RiflesService.getRifleById()
     * which makes an authenticated WebClient request to the mock rifles-service.
     * <p>
     * The mock server returns a rifle with ID 1 matching the request parameter.
     * Validates that the result is not null and indicates no errors.
     */
    @Test
    void getRifleById() {
        var rifleResult = client.callTool(new CallToolRequest("getRifleById", Map.of("id", 1L))).block();

        assertThat(rifleResult).isNotNull();
        assertThat(rifleResult.isError()).isFalse();
    }

    /**
     * Positive-path test with non-null Quantity fields (barrelLength, freeBore).
     * <p>
     * Reconfigures the mock rifles-service dispatcher to return a rifle JSON payload
     * containing embedded quantity objects in the shape {"value": X, "unit": "cm"}.
     * This validates that:
     * <ul>
     *   <li>Inbound deserialization into {@link RifleDto} succeeds via QuantityModule.</li>
     *   <li>MCP envelope serialization of the resulting {@code Mono<RifleDto>} does not fail when Quantity fields are non-null.</li>
     *   <li>The JSON response surface includes the expected nested quantity structure.</li>
     * </ul>
     * If this test were to fail with a generic conversion error, we would adopt the same
     * pre-serialization workaround used for loads. Success here allows rifles to remain
     * strongly typed.
     */
    @Test
    void getRifleById_WithQuantities() {
        // Override dispatcher to return rifle JSON including Quantity fields
        mockRiflesServer.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
            @Override
            public okhttp3.mockwebserver.MockResponse dispatch(okhttp3.mockwebserver.RecordedRequest request) {
                String path = request.getPath();
                if (path == null) {
                    return new okhttp3.mockwebserver.MockResponse().setResponseCode(404);
                }
                if (path.equals("/rifles")) {
                    return new okhttp3.mockwebserver.MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody("[" + RIFLE_WITH_QUANTITIES_JSON + "]");
                }
                if (path.startsWith("/rifles/")) {
                    return new okhttp3.mockwebserver.MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody(RIFLE_WITH_QUANTITIES_JSON);
                }
                return new okhttp3.mockwebserver.MockResponse().setResponseCode(404);
            }
        });

        var rifleResult = client.callTool(new CallToolRequest("getRifleById", Map.of("id", 1L))).block();

        assertThat(rifleResult).isNotNull();
        assertThat(rifleResult.isError()).isFalse();
        assertThat(rifleResult.content()).isNotEmpty();
        var content = rifleResult.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        // Accept either 61 or 61.0 depending on Jackson numeric formatting
        assertThat(textContent.text())
            .satisfies(s -> assertThat(s).containsAnyOf(
                "\"barrelLength\":{\"value\":61.0,\"unit\":\"cm\",\"scale\":\"ABSOLUTE\"}",
                "\"barrelLength\":{\"value\":61,\"unit\":\"cm\",\"scale\":\"ABSOLUTE\"}"));
        // Verify rifling object structure
        assertThat(textContent.text())
            .satisfies(s -> assertThat(s).containsAnyOf(
                "\"twistRate\":{\"value\":8.0,\"unit\":\"[in_i]\",\"scale\":\"ABSOLUTE\"}",
                "\"twistRate\":{\"value\":8,\"unit\":\"[in_i]\",\"scale\":\"ABSOLUTE\"}"));
        assertThat(textContent.text()).contains("\"twistDirection\":\"RIGHT\"");
        assertThat(textContent.text()).contains("\"numberOfGrooves\":4");
        // Verify zeroing object structure
        assertThat(textContent.text())
            .satisfies(s -> assertThat(s).containsAnyOf(
                "\"sightHeight\":{\"value\":1.5,\"unit\":\"[in_i]\",\"scale\":\"ABSOLUTE\"}",
                "\"sightHeight\":{\"value\":1.5,\"unit\":\"[in_i]\",\"scale\":\"ABSOLUTE\"}"));
        assertThat(textContent.text())
            .satisfies(s -> assertThat(s).containsAnyOf(
                "\"zeroDistance\":{\"value\":100.0,\"unit\":\"[yd_i]\",\"scale\":\"ABSOLUTE\"}",
                "\"zeroDistance\":{\"value\":100,\"unit\":\"[yd_i]\",\"scale\":\"ABSOLUTE\"}"));

        // Restore original dispatcher for other tests
        mockRiflesServer.setDispatcher(createRiflesDispatcher());
    }

    // ========================================
    // Negative Test Cases
    // ========================================

        /**
     * Tests the getRifleById tool with an ID that doesn't exist.
     * <p>
     * Verifies that the server properly handles requests for non-existent rifles
     * by returning an appropriate error. The mock rifles-service returns a 404
     * Not Found status, which should result in an error being propagated to the
     * client.
     * <p>
     * Expected: isError = true, error message contains authentication or error details
     */
    @Test
    void getRifleById_NotFound() {
        var result = client.callTool(new CallToolRequest("getRifleById", Map.of("id", 999L))).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Error invoking method");
    }

    /**
     * Tests getRifles with authentication failure (401 response).
     * <p>
     * Verifies proper error handling when the rifles-service returns a 401 Unauthorized
     * response. The service should propagate this error to the client.
     * <p>
     * Expected: isError = true, error message contains error details
     */
    @Test
    void getRifles_Unauthorized() {
        // Reconfigure mock server to return 401 for this test
        mockRiflesServer.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
            @Override
            public okhttp3.mockwebserver.MockResponse dispatch(okhttp3.mockwebserver.RecordedRequest request) {
                return new okhttp3.mockwebserver.MockResponse()
                        .setResponseCode(401)
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\"error\": \"Unauthorized\"}");
            }
        });

        var result = client.callTool(new CallToolRequest("getRifles", Map.of())).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Error invoking method");

        // Restore original dispatcher for other tests
        mockRiflesServer.setDispatcher(createRiflesDispatcher());
    }

    /**
     * Tests getRifleById with authentication failure (401 response).
     * <p>
     * Verifies proper error handling when the rifles-service returns a 401 Unauthorized
     * response for a specific rifle. The service should propagate this error to the client.
     * <p>
     * Expected: isError = true, error message contains error details
     */
    @Test
    void getRifleById_Unauthorized() {
        // Reconfigure mock server to return 401 for this test
        mockRiflesServer.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
            @Override
            public okhttp3.mockwebserver.MockResponse dispatch(okhttp3.mockwebserver.RecordedRequest request) {
                return new okhttp3.mockwebserver.MockResponse()
                        .setResponseCode(401)
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\"error\": \"Unauthorized\"}");
            }
        });

        var result = client.callTool(new CallToolRequest("getRifleById", Map.of("id", 1L))).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Error invoking method");

        // Restore original dispatcher for other tests
        mockRiflesServer.setDispatcher(createRiflesDispatcher());
    }

    /**
     * Tests getRifles when service discovery returns null.
     * <p>
     * Verifies proper error handling when the DiscoveryClient returns null
     * for the rifles-service, simulating a service discovery failure.
     * <p>
     * Expected: isError = true, error message contains service discovery error
     */
    @Test
    void getRifles_ServiceDiscoveryReturnsNull() {
        // Mock discovery client to return null
        org.mockito.Mockito.when(discoveryClient.getInstances("rifles-service"))
                .thenReturn(null);

        var result = client.callTool(new CallToolRequest("getRifles", Map.of())).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Error invoking method");

        // Restore original mock for other tests
        mockServiceDiscovery();
    }

    /**
     * Tests getRifles when service discovery returns an empty list.
     * <p>
     * Verifies proper error handling when the DiscoveryClient returns an empty
     * list for the rifles-service, simulating no available service instances.
     * <p>
     * Expected: isError = true, error message contains service discovery error
     */
    @Test
    void getRifles_ServiceDiscoveryReturnsEmptyList() {
        // Mock discovery client to return empty list
        org.mockito.Mockito.when(discoveryClient.getInstances("rifles-service"))
                .thenReturn(java.util.List.of());

        var result = client.callTool(new CallToolRequest("getRifles", Map.of())).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Error invoking method");

        // Restore original mock for other tests
        mockServiceDiscovery();
    }

    /**
     * Tests getRifleById when service discovery returns null.
     * <p>
     * Verifies proper error handling when the DiscoveryClient returns null
     * for the rifles-service during a getRifleById call.
     * <p>
     * Expected: isError = true, error message contains service discovery error
     */
    @Test
    void getRifleById_ServiceDiscoveryReturnsNull() {
        // Mock discovery client to return null
        org.mockito.Mockito.when(discoveryClient.getInstances("rifles-service"))
                .thenReturn(null);

        var result = client.callTool(new CallToolRequest("getRifleById", Map.of("id", 1L))).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Error invoking method");

        // Restore original mock for other tests
        mockServiceDiscovery();
    }

    /**
     * Tests getRifleById when service discovery returns an empty list.
     * <p>
     * Verifies proper error handling when the DiscoveryClient returns an empty
     * list for the rifles-service during a getRifleById call.
     * <p>
     * Expected: isError = true, error message contains service discovery error
     */
    @Test
    void getRifleById_ServiceDiscoveryReturnsEmptyList() {
        // Mock discovery client to return empty list
        org.mockito.Mockito.when(discoveryClient.getInstances("rifles-service"))
                .thenReturn(java.util.List.of());

        var result = client.callTool(new CallToolRequest("getRifleById", Map.of("id", 1L))).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Error invoking method");

        // Restore original mock for other tests
        mockServiceDiscovery();
    }
}
