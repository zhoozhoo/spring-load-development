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
import reactor.core.publisher.Mono;

/**
 * Integration test for the LoadsToolProvider MCP tools.
 * <p>
 * This test class verifies load-related MCP tool functionality by executing actual
 * tool calls against a running Spring Boot server instance with mocked backend services.
 * <p>
 * The test suite includes:
 * <ul>
 * <li>Server connectivity verification (ping)</li>
 * <li>Tool discovery and listing</li>
 * <li>getLoads - Retrieve all loads</li>
 * <li>getLoad - Retrieve a specific load by ID</li>
 * <li>getLoadDetails - Retrieve detailed information including rifle and statistics</li>
 * </ul>
 * <p>
 * Each test method runs independently with proper setup and teardown
 * to ensure test isolation and consistent results.
 * <p>
 * Test infrastructure:
 * <ul>
 * <li>Uses {@link MockWebServer} from OkHttp to simulate loads-service (port 8082) 
 * and rifles-service (port 8083)</li>
 * <li>Mocks {@link DiscoveryClient} to return service instances pointing to mock servers</li>
 * <li>Provides mock JWT authentication via {@link TestSecurityConfig} to satisfy
 * security context requirements</li>
 * <li>Uses {@link McpAsyncClient} with WebFlux SSE transport for async communication</li>
 * </ul>
 * 
 * @author Zhubin Salehi
 * @see LoadsToolProvider
 * @see io.modelcontextprotocol.client.McpAsyncClient
 * @see okhttp3.mockwebserver.MockWebServer
 * @see TestSecurityConfig
 */
@Import(BaseMcpToolProviderTest.TestWebClientConfig.class)
public class LoadsToolProviderTest extends BaseMcpToolProviderTest {

    private static MockWebServer mockLoadsServer;
    private static MockWebServer mockRiflesServer;

    /**
     * Sets up mock servers for loads-service and rifles-service.
     * <p>
     * Creates and configures MockWebServer instances on ports 8082 and 8083 with
     * dispatchers that handle load and rifle-related endpoints.
     */
    @Override
    protected void setupMockServers() throws IOException {
        // Start mock web servers
        mockLoadsServer = new MockWebServer();
        mockRiflesServer = new MockWebServer();
        
        // Configure dispatchers
        mockLoadsServer.setDispatcher(createLoadsDispatcher());
        mockRiflesServer.setDispatcher(createRiflesDispatcher());
        
        mockLoadsServer.start(8082);
        mockRiflesServer.start(8083);
    }

    /**
     * Shuts down mock servers for loads-service and rifles-service.
     */
    @Override
    protected void shutdownMockServers() throws IOException {
        if (mockLoadsServer != null) {
            mockLoadsServer.shutdown();
        }
        if (mockRiflesServer != null) {
            mockRiflesServer.shutdown();
        }
    }

    /**
     * Mocks the DiscoveryClient to return service instances for loads-service and rifles-service.
     */
    @Override
    protected void mockServiceDiscovery() {
        ServiceInstance loadsInstance = createServiceInstance(
                "loads-service-1", "loads-service", mockLoadsServer);
        ServiceInstance riflesInstance = createServiceInstance(
                "rifles-service-1", "rifles-service", mockRiflesServer);
        
        mockService("loads-service", loadsInstance);
        mockService("rifles-service", riflesInstance);
    }

    // ========================================
    // Test Methods
    // ========================================

    /**
     * Tests server connectivity by sending a ping request.
     * <p>
     * Verifies that the MCP server responds to ping requests successfully.
     * Uses {@link McpAsyncClient#ping()} which returns a {@link Mono} that is
     * blocked to retrieve the result synchronously in the test.
     */
    @Test
    void ping() {
        var result = client.ping().block();
        assertThat(result).isNotNull();
    }

    /**
     * Tests the tool discovery functionality.
     * <p>
     * Verifies that the server returns a list of available MCP tools
     * using {@link McpAsyncClient#listTools()}. The result is blocked to
     * synchronously verify that the tools list is not empty.
     * <p>
     * Expected tools include: getLoads, getLoad, getLoadDetails, getRifles, getRifleById.
     */
    @Test
    void listTools() {
        var toolsList = client.listTools().block();

        assertThat(toolsList).isNotNull();
        assertThat(toolsList.tools()).isNotEmpty();
    }

    /**
     * Tests the getLoads tool.
     * <p>
     * Verifies that the server can retrieve a list of all loads by invoking
     * the MCP tool with no arguments. The tool calls LoadsService which makes
     * an authenticated WebClient request to the mock loads-service, extracting
     * the JWT token from the security context.
     * <p>
     * Validates that the result is not null and indicates no errors.
     */
    @Test
    void getLoads() {
        var loadsResult = client.callTool(new CallToolRequest("getLoads", Map.of())).block();

        assertThat(loadsResult).isNotNull();
        assertThat(loadsResult.isError()).isFalse();
    }

    /**
     * Tests the getLoad tool with a specific load ID.
     * <p>
     * Verifies that the server can retrieve a single load by its ID by invoking
     * the MCP tool with an ID parameter. The tool calls LoadsService.getLoadById()
     * which makes an authenticated WebClient request to the mock loads-service.
     * <p>
     * The mock server returns a load with ID 1 matching the request parameter.
     * Validates that the result is not null and indicates no errors.
     */
    @Test
    void getLoadById() {
        var loadResult = client.callTool(new CallToolRequest("getLoad", Map.of("id", 1L))).block();

        System.out.println(loadResult);
        assertThat(loadResult).isNotNull();
        assertThat(loadResult.isError()).isFalse();
    }

    /**
     * Tests the getLoadDetails tool with a specific load ID.
     * <p>
     * Verifies that the server can retrieve detailed information for a specific load
     * by invoking the MCP tool with an ID parameter. This tool makes multiple backend
     * service calls:
     * <ul>
     * <li>Calls LoadsService.getLoadById() to get basic load information</li>
     * <li>Calls RiflesService.getRifleById() to get the associated rifle</li>
     * <li>Calls LoadsService.getGroupsByLoadId() to get statistics/groups</li>
     * </ul>
     * <p>
     * All service calls use authenticated WebClient requests with JWT tokens from
     * the security context, hitting the configured MockWebServer instances.
     * <p>
     * Validates that the result is not null.
     */
    @Test
    void getLoadDetails() {
        var loadDetailsResult = client.callTool(new CallToolRequest("getLoadDetails", Map.of("id", 1L))).block();

        assertThat(loadDetailsResult).isNotNull();
    }

    /**
     * Tests getLoad with a null ID parameter.
     * <p>
     * Verifies that the tool returns an error with INVALID_PARAMS error code
     * when called with a null ID. The LoadsToolProvider validates the ID
     * parameter before making service calls.
     * <p>
     * Expected: isError = true, error message indicates ID must be positive
     */
    @Test
    void getLoadById_NullId() {
        var result = client.callTool(new CallToolRequest("getLoad", Map.of())).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Load ID must be a positive number");
    }

    /**
     * Tests getLoad with a zero ID parameter.
     * <p>
     * Verifies that the tool returns an error with INVALID_PARAMS error code
     * when called with ID = 0. The LoadsToolProvider validates that IDs must
     * be positive numbers.
     * <p>
     * Expected: isError = true, error message indicates ID must be positive
     */
    @Test
    void getLoadById_ZeroId() {
        var result = client.callTool(new CallToolRequest("getLoad", Map.of("id", 0L))).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Load ID must be a positive number");
    }

    /**
     * Tests getLoad with a negative ID parameter.
     * <p>
     * Verifies that the tool returns an error with INVALID_PARAMS error code
     * when called with a negative ID. The LoadsToolProvider validates that IDs
     * must be positive numbers.
     * <p>
     * Expected: isError = true, error message indicates ID must be positive
     */
    @Test
    void getLoadById_NegativeId() {
        var result = client.callTool(new CallToolRequest("getLoad", Map.of("id", -1L))).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Load ID must be a positive number");
    }

    /**
     * Tests getLoad with a non-existent load ID.
     * <p>
     * Verifies proper error handling when requesting a load that doesn't exist.
     * The mock server returns a 404 response for load ID 999, which should
     * result in an error being propagated to the client.
     * <p>
     * Expected: isError = true, error message contains error details
     */
    @Test
    void getLoadById_NotFound() {
        var result = client.callTool(new CallToolRequest("getLoad", Map.of("id", 999L))).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Error invoking method");
    }

    /**
     * Tests getLoadDetails with a null ID parameter.
     * <p>
     * Verifies that the tool returns an error with INVALID_PARAMS error code
     * when called with a null ID. The LoadsToolProvider validates the ID
     * parameter before making service calls.
     * <p>
     * Expected: isError = true, error message indicates ID must be positive
     */
    @Test
    void getLoadDetails_NullId() {
        var result = client.callTool(new CallToolRequest("getLoadDetails", Map.of())).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Load ID must be a positive number");
    }

    /**
     * Tests getLoadDetails with a zero ID parameter.
     * <p>
     * Verifies that the tool returns an error with INVALID_PARAMS error code
     * when called with ID = 0. The LoadsToolProvider validates that IDs must
     * be positive numbers.
     * <p>
     * Expected: isError = true, error message indicates ID must be positive
     */
    @Test
    void getLoadDetails_ZeroId() {
        var result = client.callTool(new CallToolRequest("getLoadDetails", Map.of("id", 0L))).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Load ID must be a positive number");
    }

    /**
     * Tests getLoadDetails with a negative ID parameter.
     * <p>
     * Verifies that the tool returns an error with INVALID_PARAMS error code
     * when called with a negative ID. The LoadsToolProvider validates that IDs
     * must be positive numbers.
     * <p>
     * Expected: isError = true, error message indicates ID must be positive
     */
    @Test
    void getLoadDetails_NegativeId() {
        var result = client.callTool(new CallToolRequest("getLoadDetails", Map.of("id", -1L))).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Load ID must be a positive number");
    }

    /**
     * Tests getLoadDetails with a non-existent load ID.
     * <p>
     * Verifies proper error handling when requesting details for a load that doesn't exist.
     * The mock server returns a 404 response for load ID 999, which should
     * result in an error being propagated to the client.
     * <p>
     * Expected: isError = true, error message contains error details
     */
    @Test
    void getLoadDetails_NotFound() {
        var result = client.callTool(new CallToolRequest("getLoadDetails", Map.of("id", 999L))).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Error invoking method");
    }

    /**
     * Tests getLoads with authentication failure (401 response).
     * <p>
     * Verifies proper error handling when the loads-service returns a 401 Unauthorized
     * response. The service should map this to INVALID_REQUEST with "Authentication failed".
     * <p>
     * Expected: isError = true, error message indicates authentication failure
     */
    @Test
    void getLoads_Unauthorized() {
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

        var result = client.callTool(new CallToolRequest("getLoads", Map.of())).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Authentication failed");

        // Restore original dispatcher for other tests
        mockLoadsServer.setDispatcher(createLoadsDispatcher());
    }

    /**
     * Tests getLoad with authentication failure (401 response).
     * <p>
     * Verifies proper error handling when the loads-service returns a 401 Unauthorized
     * response for a specific load. The service should map this to INVALID_REQUEST.
     * <p>
     * Expected: isError = true, error message indicates authentication failure
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

        var result = client.callTool(new CallToolRequest("getLoad", Map.of("id", 1L))).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Authentication failed");

        // Restore original dispatcher for other tests
        mockLoadsServer.setDispatcher(createLoadsDispatcher());
    }

    /**
     * Tests getLoadDetails with authentication failure (401 response).
     * <p>
     * Verifies proper error handling when the loads-service returns a 401 Unauthorized
     * response when fetching load details or statistics.
     * <p>
     * Expected: isError = true, error message indicates authentication failure
     */
    @Test
    void getLoadDetails_Unauthorized() {
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

        var result = client.callTool(new CallToolRequest("getLoadDetails", Map.of("id", 1L))).block();

        assertThat(result).isNotNull();
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).isNotEmpty();
        var content = result.content().get(0);
        assertThat(content).isInstanceOf(TextContent.class);
        var textContent = (TextContent) content;
        assertThat(textContent.text()).contains("Authentication failed");

        // Restore original dispatcher for other tests
        mockLoadsServer.setDispatcher(createLoadsDispatcher());
    }

    /**
     * Tests getLoads when service discovery returns null.
     * <p>
     * Verifies proper error handling when the DiscoveryClient returns null
     * for the loads-service, simulating a service discovery failure.
     * <p>
     * Expected: isError = true, error message contains service discovery error
     */
    @Test
    void getLoads_ServiceDiscoveryReturnsNull() {
        // Mock discovery client to return null
        org.mockito.Mockito.when(discoveryClient.getInstances("loads-service"))
                .thenReturn(null);

        var result = client.callTool(new CallToolRequest("getLoads", Map.of())).block();

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
     * Tests getLoads when service discovery returns an empty list.
     * <p>
     * Verifies proper error handling when the DiscoveryClient returns an empty
     * list for the loads-service, simulating no available service instances.
     * <p>
     * Expected: isError = true, error message contains service discovery error
     */
    @Test
    void getLoads_ServiceDiscoveryReturnsEmptyList() {
        // Mock discovery client to return empty list
        org.mockito.Mockito.when(discoveryClient.getInstances("loads-service"))
                .thenReturn(java.util.List.of());

        var result = client.callTool(new CallToolRequest("getLoads", Map.of())).block();

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
     * Tests getLoadById when service discovery returns null.
     * <p>
     * Verifies proper error handling when the DiscoveryClient returns null
     * for the loads-service during a getLoadById call.
     * <p>
     * Expected: isError = true, error message contains service discovery error
     */
    @Test
    void getLoadById_ServiceDiscoveryReturnsNull() {
        // Mock discovery client to return null
        org.mockito.Mockito.when(discoveryClient.getInstances("loads-service"))
                .thenReturn(null);

        var result = client.callTool(new CallToolRequest("getLoad", Map.of("id", 1L))).block();

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
     * Tests getLoadById when service discovery returns an empty list.
     * <p>
     * Verifies proper error handling when the DiscoveryClient returns an empty
     * list for the loads-service during a getLoadById call.
     * <p>
     * Expected: isError = true, error message contains service discovery error
     */
    @Test
    void getLoadById_ServiceDiscoveryReturnsEmptyList() {
        // Mock discovery client to return empty list
        org.mockito.Mockito.when(discoveryClient.getInstances("loads-service"))
                .thenReturn(java.util.List.of());

        var result = client.callTool(new CallToolRequest("getLoad", Map.of("id", 1L))).block();

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
     * Tests getLoadDetails when service discovery returns null.
     * <p>
     * Verifies proper error handling when the DiscoveryClient returns null
     * for the loads-service during a getLoadDetails call.
     * <p>
     * Expected: isError = true, error message contains service discovery error
     */
    @Test
    void getLoadDetails_ServiceDiscoveryReturnsNull() {
        // Mock discovery client to return null
        org.mockito.Mockito.when(discoveryClient.getInstances("loads-service"))
                .thenReturn(null);

        var result = client.callTool(new CallToolRequest("getLoadDetails", Map.of("id", 1L))).block();

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
     * Tests getLoadDetails when service discovery returns an empty list.
     * <p>
     * Verifies proper error handling when the DiscoveryClient returns an empty
     * list for the loads-service during a getLoadDetails call.
     * <p>
     * Expected: isError = true, error message contains service discovery error
     */
    @Test
    void getLoadDetails_ServiceDiscoveryReturnsEmptyList() {
        // Mock discovery client to return empty list
        org.mockito.Mockito.when(discoveryClient.getInstances("loads-service"))
                .thenReturn(java.util.List.of());

        var result = client.callTool(new CallToolRequest("getLoadDetails", Map.of("id", 1L))).block();

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

