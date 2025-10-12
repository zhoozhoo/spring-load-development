package ca.zhoozhoo.loaddev.mcp.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Import;

import ca.zhoozhoo.loaddev.mcp.config.TestSecurityConfig;
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
        ServiceInstance riflesInstance = createServiceInstance(
                "rifles-service-1", "rifles-service", mockRiflesServer);
        
        mockService("rifles-service", riflesInstance);
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
    void testGetRifles() {
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
    void testGetRifleById() {
        var rifleResult = client.callTool(new CallToolRequest("getRifleById", Map.of("id", 1L))).block();

        assertThat(rifleResult).isNotNull();
        assertThat(rifleResult.isError()).isFalse();
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
    void testGetRifleById_NotFound() {
        var result = client.callTool(new CallToolRequest("getRifleById", Map.of("id", 999L))).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Error invoking method");
    }
}
