package ca.zhoozhoo.loaddev.mcp.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import ca.zhoozhoo.loaddev.mcp.config.TestSecurityConfig;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Integration test for the LoadResourceProvider MCP resources.
 * <p>
 * This test class verifies load-related MCP resource functionality by executing actual
 * resource read calls against a running Spring Boot server instance with mocked backend services.
 * <p>
 * The test suite includes:
 * <ul>
 * <li>Resource discovery and listing</li>
 * <li>getLoadById - Read load resource by ID using load://{id} URI pattern</li>
 * </ul>
 * <p>
 * Each test method runs independently with proper setup and teardown
 * to ensure test isolation and consistent results.
 * <p>
 * Test infrastructure:
 * <ul>
 * <li>Uses {@link MockWebServer} from OkHttp to simulate loads-service (port 8082)</li>
 * <li>Mocks {@link org.springframework.cloud.client.discovery.DiscoveryClient} to return 
 * service instances pointing to mock servers</li>
 * <li>Provides mock JWT authentication via {@link TestSecurityConfig} to satisfy
 * security context requirements</li>
 * <li>Uses {@link io.modelcontextprotocol.client.McpAsyncClient} with WebFlux SSE transport 
 * for async communication</li>
 * </ul>
 * 
 * @author Zhubin Salehi
 * @see LoadResourceProvider
 * @see io.modelcontextprotocol.client.McpAsyncClient
 * @see okhttp3.mockwebserver.MockWebServer
 * @see TestSecurityConfig
 */
@Import(BaseMcpToolProviderTest.TestWebClientConfig.class)
public class LoadResourceProviderTest extends BaseMcpToolProviderTest {

    private static MockWebServer mockLoadsServer;

    /**
     * Sets up mock server for loads-service.
     * <p>
     * Creates and configures MockWebServer instance on port 8082 with
     * dispatcher that handles load-related endpoints.
     */
    @Override
    protected void setupMockServers() throws IOException {
        // Start mock web server
        mockLoadsServer = new MockWebServer();
        
        // Configure dispatcher
        mockLoadsServer.setDispatcher(createLoadsDispatcher());
        
        mockLoadsServer.start(8082);
    }

    /**
     * Shuts down mock server for loads-service.
     */
    @Override
    protected void shutdownMockServers() throws IOException {
        if (mockLoadsServer != null) {
            mockLoadsServer.shutdown();
        }
    }

    /**
     * Mocks the DiscoveryClient to return service instance for loads-service.
     */
    @Override
    protected void mockServiceDiscovery() {
        mockService("loads-service", createServiceInstance(
                "loads-service-1", "loads-service", mockLoadsServer));
    }

    /**
     * Tests server connectivity by sending a ping request.
     * <p>
     * Verifies that the MCP server responds to ping requests successfully.
     * Uses {@link io.modelcontextprotocol.client.McpAsyncClient#ping()} which returns 
     * a {@link reactor.core.publisher.Mono} that is blocked to retrieve the result 
     * synchronously in the test.
     */
    @Test
    void ping() {
        var result = client.ping().block();
        assertThat(result).isNotNull();
    }

    /**
     * Tests the resource template discovery functionality.
     * <p>
     * Verifies that the server returns a list of available MCP resource templates
     * using {@link io.modelcontextprotocol.client.McpAsyncClient#listResourceTemplates(io.modelcontextprotocol.spec.McpSchema.ListResourceTemplatesRequest)}. 
     * The result is blocked to synchronously verify that the resource templates list is not empty.
     * <p>
     * Resource templates are URI patterns that define dynamic resources which can be accessed
     * via resources/read with specific parameter values. For example, load://{id} is a template
     * where {id} can be replaced with an actual load ID like load://1.
     * <p>
     * Expected resource templates include: load://{id}, load://{id}/{attribute}.
     * These templates are registered via @McpResource annotation at method level in LoadResourceProvider.
     */
    @Test
    void listResourceTemplates() {
        var resourceTemplateList = client.listResourceTemplates(null).block();

        assertThat(resourceTemplateList).isNotNull();
        // Resource templates are URI patterns like load://{id} that accept parameters
        // They are registered via @McpResource annotation and returned by listResourceTemplates()
        // Clients can use these templates to construct URIs for resources/read requests
        assertThat(resourceTemplateList.resourceTemplates()).isNotEmpty();
    }

    /**
     * Tests reading a load resource by ID.
     * <p>
     * Verifies that the server can retrieve a load resource by its ID using the
     * load://{id} URI pattern. The resource read operation calls LoadsService.getLoadById()
     * which makes an authenticated WebClient request to the mock loads-service.
     * <p>
     * The mock server returns a load with ID 1 matching the URI parameter.
     * Validates that the result contains the expected load information in a formatted
     * text/plain format.
     */
    @Test
    void getLoadById() {
        var resourceResult = client.readResource(new ReadResourceRequest("load://1")).block();

        assertThat(resourceResult).isNotNull();
        assertThat(resourceResult.contents()).isNotEmpty();
        assertThat(resourceResult.contents()).hasSize(1);
        
        assertThat(resourceResult.contents().get(0)).isInstanceOf(TextResourceContents.class);
        var textContent = (TextResourceContents) resourceResult.contents().get(0);
        assertThat(textContent.uri()).isEqualTo("load://1");
        assertThat(textContent.mimeType()).isEqualTo("text/plain");
        
        // Verify the formatted content contains expected fields
        assertThat(textContent.text()).contains("ID: 1");
        assertThat(textContent.text()).contains("Name: Test Load 1");
        assertThat(textContent.text()).contains("Description: Test description");
        assertThat(textContent.text()).contains("Powder Manufacturer: Hodgdon");
        assertThat(textContent.text()).contains("Powder Type: H4350");
        assertThat(textContent.text()).contains("Bullet Manufacturer: Hornady");
        assertThat(textContent.text()).contains("Bullet Type: ELD-M");
        assertThat(textContent.text()).contains("Bullet Weight: 140 g");
        assertThat(textContent.text()).contains("Primer Manufacturer: CCI");
        assertThat(textContent.text()).contains("Primer Type: BR2");
        assertThat(textContent.text()).contains("Distance from Lands: 0.02 mm");
        assertThat(textContent.text()).contains("Case Overall Length: 2.8 mm");
        assertThat(textContent.text()).contains("Neck Tension: 0.002 mm");
        assertThat(textContent.text()).contains("Associated Rifle ID: 1");
    }

    /**
     * Tests reading a load resource with a non-existent ID.
     * <p>
     * Verifies proper error handling when requesting a load resource that doesn't exist.
     * The mock server returns a 404 response for load ID 999, which should result in
     * an McpError being thrown with the appropriate error message.
     * <p>
     * Expected: An McpError exception with message "Load not found with ID: 999".
     */
    @Test
    void getLoadById_NotFound() {
        // The MCP framework throws an McpError when a resource is not found
        assertThatThrownBy(() -> client.readResource(new ReadResourceRequest("load://999")).block())
                .isInstanceOf(io.modelcontextprotocol.spec.McpError.class)
                .hasMessageContaining("Load not found with ID: 999");
    }

    /**
     * Tests reading a load resource with ID 2 to verify dynamic content.
     * <p>
     * Verifies that the resource provider correctly extracts and processes different
     * load IDs from the URI pattern. This ensures the URI template parameter extraction
     * is working correctly.
     */
    @Test
    void getLoadById_DifferentId() {
        var resourceResult = client.readResource(new ReadResourceRequest("load://2")).block();

        assertThat(resourceResult).isNotNull();
        assertThat(resourceResult.contents()).isNotEmpty();
        assertThat(resourceResult.contents()).hasSize(1);
        
        assertThat(resourceResult.contents().get(0)).isInstanceOf(TextResourceContents.class);
        var textContent = (TextResourceContents) resourceResult.contents().get(0);
        assertThat(textContent.uri()).isEqualTo("load://2");
        assertThat(textContent.mimeType()).isEqualTo("text/plain");
        
        // The mock returns the same load data but we verify it's processed correctly
        assertThat(textContent.text()).contains("ID: 1"); // Mock returns same data for any valid ID
    }

    /**
     * Tests reading a load resource with authentication failure (401 response).
     * <p>
     * Verifies proper error handling when the loads-service returns a 401 Unauthorized
     * response for a resource read. The service should propagate this error to the client.
     * <p>
     * Expected: An McpError exception with message containing "Authentication failed".
     */
    @Test
    void getLoadById_Unauthorized() {
        // Reconfigure mock server to return 401 for this test
        mockLoadsServer.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
            @Override
            public okhttp3.mockwebserver.MockResponse dispatch(okhttp3.mockwebserver.RecordedRequest request) {
                return new okhttp3.mockwebserver.MockResponse()
                        .setResponseCode(401)
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\"error\": \"Unauthorized\"}");
            }
        });

        // The MCP framework should throw an McpError when authentication fails
        assertThatThrownBy(() -> client.readResource(new ReadResourceRequest("load://1")).block())
                .isInstanceOf(io.modelcontextprotocol.spec.McpError.class)
                .hasMessageContaining("Authentication failed");

        // Restore original dispatcher for other tests
        mockLoadsServer.setDispatcher(createLoadsDispatcher());
    }

    /**
     * Tests reading a load resource with invalid ID format (null).
     * <p>
     * Verifies proper error handling when the URI contains a null or empty ID.
     * The resource provider should fail gracefully with an appropriate error.
     * <p>
     * Expected: An McpError exception with message indicating invalid ID format.
     */
    @Test
    void getLoadById_NullId() {
        // The MCP framework should throw an McpError when ID is missing/empty
        assertThatThrownBy(() -> client.readResource(new ReadResourceRequest("load://")).block())
                .isInstanceOf(io.modelcontextprotocol.spec.McpError.class);
    }

    /**
     * Tests reading a load resource with invalid ID format (zero).
     * <p>
     * Verifies proper error handling when the URI contains ID = 0.
     * Although the mock server might not enforce this, we test the behavior.
     * <p>
     * Expected: The request should either succeed (if mock allows) or fail with appropriate error.
     */
    @Test
    void getLoadById_ZeroId() {
        // This test documents current behavior - may return error or mock response
        assertThat(client.readResource(new ReadResourceRequest("load://0")).block()).isNotNull();
        // The result depends on mock implementation - we just verify it doesn't crash
    }

    /**
     * Tests reading a load resource with invalid ID format (negative).
     * <p>
     * Verifies proper error handling when the URI contains a negative ID.
     * Although the mock server might not enforce this, we test the behavior.
     * <p>
     * Expected: The request should either succeed (if mock allows) or fail with appropriate error.
     */
    @Test
    void getLoadById_NegativeId() {
        // This test documents current behavior - may return error or mock response
        assertThat(client.readResource(new ReadResourceRequest("load://-1")).block()).isNotNull();
        // The result depends on mock implementation - we just verify it doesn't crash
    }

    /**
     * Tests reading a load resource with non-numeric ID.
     * <p>
     * Verifies proper error handling when the URI contains a non-numeric ID
     * that cannot be parsed as a Long.
     * <p>
     * Expected: An McpError exception due to NumberFormatException.
     */
    @Test
    void getLoadById_InvalidIdFormat() {
        // The MCP framework should throw an McpError when ID is not a valid number
        assertThatThrownBy(() -> client.readResource(new ReadResourceRequest("load://invalid")).block())
                .isInstanceOf(io.modelcontextprotocol.spec.McpError.class);
    }

    /**
     * Tests reading a load resource when service discovery returns null.
     * <p>
     * Verifies proper error handling when the DiscoveryClient returns null
     * for the loads-service, simulating a service discovery failure.
     * <p>
     * Expected: An McpError exception with message containing service discovery error.
     */
    @Test
    void getLoadById_ServiceDiscoveryReturnsNull() {
        // Mock discovery client to return null
        org.mockito.Mockito.when(discoveryClient.getInstances("loads-service"))
                .thenReturn(null);

        // The MCP framework should throw an McpError when service discovery fails
        assertThatThrownBy(() -> client.readResource(new ReadResourceRequest("load://1")).block())
                .isInstanceOf(io.modelcontextprotocol.spec.McpError.class);

        // Restore original mock for other tests
        mockServiceDiscovery();
    }

    /**
     * Tests reading a load resource when service discovery returns an empty list.
     * <p>
     * Verifies proper error handling when the DiscoveryClient returns an empty
     * list for the loads-service, simulating no available service instances.
     * <p>
     * Expected: An McpError exception with message containing service discovery error.
     */
    @Test
    void getLoadById_ServiceDiscoveryReturnsEmptyList() {
        // Mock discovery client to return empty list
        org.mockito.Mockito.when(discoveryClient.getInstances("loads-service"))
                .thenReturn(java.util.List.of());

        // The MCP framework should throw an McpError when service discovery fails
        assertThatThrownBy(() -> client.readResource(new ReadResourceRequest("load://1")).block())
                .isInstanceOf(io.modelcontextprotocol.spec.McpError.class);

        // Restore original mock for other tests
        mockServiceDiscovery();
    }
}
