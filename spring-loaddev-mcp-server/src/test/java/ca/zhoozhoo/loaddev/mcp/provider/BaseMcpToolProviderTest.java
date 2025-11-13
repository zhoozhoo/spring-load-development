package ca.zhoozhoo.loaddev.mcp.provider;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.zhoozhoo.loaddev.mcp.config.TestSecurityConfig;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import io.modelcontextprotocol.spec.McpClientTransport;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Abstract base class for MCP tool provider integration tests.
 * <p>
 * This base class provides common test infrastructure and utility methods for testing
 * MCP tool providers. It handles:
 * <ul>
 * <li>Spring Boot test context configuration with random port</li>
 * <li>MockWebServer setup and teardown</li>
 * <li>MCP async client initialization and cleanup</li>
 * <li>DiscoveryClient mocking for service discovery</li>
 * <li>Mock JWT authentication via {@link TestSecurityConfig}</li>
 * <li>Common utility methods for creating mock responses</li>
 * </ul>
 * <p>
 * Subclasses should:
 * <ul>
 * <li>Override {@link #setupMockServers()} to create and configure their specific mock servers</li>
 * <li>Override {@link #shutdownMockServers()} to clean up their mock servers</li>
 * <li>Override {@link #mockServiceDiscovery()} to configure service discovery for their services</li>
 * <li>Import their specific test configuration if needed</li>
 * </ul>
 * 
 * @author Zhubin Salehi
 * @see io.modelcontextprotocol.client.McpAsyncClient
 * @see okhttp3.mockwebserver.MockWebServer
 * @see TestSecurityConfig
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public abstract class BaseMcpToolProviderTest {

    @LocalServerPort
    protected int port;

    @MockitoBean
    protected DiscoveryClient discoveryClient;

    protected McpClientTransport transport;

    protected McpAsyncClient client;
    
    @Autowired
    protected McpJsonMapper mcpJsonMapper;

    /**
     * Test configuration that provides a custom WebClient bean for testing.
     * <p>
     * This WebClient will be used by service classes instead of the production one.
     * The WebClient makes HTTP requests to the MockWebServer instances configured
     * in the test setup.
     */
    @TestConfiguration
    public static class TestWebClientConfig {
        
        @Bean
        @Primary
        public WebClient webClient(ObjectMapper objectMapper) {
            // Ensure WebClient uses the application's ObjectMapper with custom modules (e.g., QuantityModule)
            return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> {
                    configurer.defaultCodecs()
                        .jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                    configurer.defaultCodecs()
                        .jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
                    })
                    .build())
                .build();
        }
    }

    /**
     * Initializes the test infrastructure before each test.
     * <p>
     * Sets up:
     * <ul>
     * <li>Mock web servers via {@link #setupMockServers()}</li>
     * <li>Service discovery mocking via {@link #mockServiceDiscovery()}</li>
     * <li>MCP async client initialization</li>
     * </ul>
     * <p>
     * Subclasses can override {@link #setupMockServers()} and {@link #mockServiceDiscovery()}
     * to provide their specific test setup.
     * 
     * @throws IOException if server setup fails
     */
    @BeforeEach
    void setUp() throws IOException {
        // Setup mock servers (implemented by subclasses)
        setupMockServers();
        
        // Mock service discovery
        mockServiceDiscovery();
        
        // Setup and initialize MCP client
        initializeMcpClient();
    }

    /**
     * Cleans up test resources after each test.
     * <p>
     * Ensures proper cleanup:
     * <ul>
     * <li>Closes MCP client connection</li>
     * <li>Shuts down mock web servers via {@link #shutdownMockServers()}</li>
     * </ul>
     * <p>
     * This prevents resource leaks and ensures each test starts with a clean state.
     * 
     * @throws IOException if cleanup fails
     */
    @AfterEach
    void tearDown() throws IOException {
        if (client != null) {
            client.close();
        }
        shutdownMockServers();
    }

    /**
     * Sets up mock web servers for the test.
     * <p>
     * Subclasses must implement this method to create and configure their
     * specific MockWebServer instances with appropriate dispatchers.
     * 
     * @throws IOException if server setup fails
     */
    protected abstract void setupMockServers() throws IOException;

    /**
     * Shuts down mock web servers after the test.
     * <p>
     * Subclasses must implement this method to properly shut down their
     * MockWebServer instances.
     * 
     * @throws IOException if server shutdown fails
     */
    protected abstract void shutdownMockServers() throws IOException;

    /**
     * Mocks the DiscoveryClient to return service instances.
     * <p>
     * Subclasses must implement this method to configure the DiscoveryClient
     * mock to return appropriate service instances for their tests.
     */
    protected abstract void mockServiceDiscovery();

    /**
     * Initializes the MCP async client with WebFlux SSE transport.
     * <p>
     * Creates a client connected to the test server on the random port,
     * then initializes it for use in tests.
     */
    protected void initializeMcpClient() {
        transport = new WebFluxSseClientTransport(
            WebClient.builder().baseUrl("http://localhost:" + port),
            mcpJsonMapper);

        client = McpClient.async(transport).build();
        client.initialize();
    }

    /**
     * Creates a service instance for a mock server.
     * <p>
     * Helper method to create a {@link ServiceInstance} that points to a
     * MockWebServer, for use with DiscoveryClient mocking.
     * 
     * @param instanceId unique identifier for this instance
     * @param serviceName the service name (e.g., "loads-service")
     * @param server the MockWebServer instance
     * @return a ServiceInstance configured to point to the mock server
     */
    protected ServiceInstance createServiceInstance(String instanceId, String serviceName, MockWebServer server) {
        return new DefaultServiceInstance(instanceId, serviceName, 
                server.getHostName(), server.getPort(), false);
    }

    /**
     * Mocks a service in the DiscoveryClient.
     * <p>
     * Helper method to configure the DiscoveryClient mock to return a specific
     * service instance for a given service name.
     * 
     * @param serviceName the service name to mock
     * @param instance the service instance to return
     */
    protected void mockService(String serviceName, ServiceInstance instance) {
        when(discoveryClient.getInstances(serviceName))
                .thenReturn(List.of(instance));
    }

    /**
     * Extracts the ID portion from a URL path.
     * <p>
     * Given a path like "/loads/123" or "/loads/123/statistics" and a prefix
     * like "/loads/", this extracts just the ID portion ("123").
     * 
     * @param path the full URL path
     * @param prefix the prefix to remove (e.g., "/loads/")
     * @return the ID portion of the path
     */
    protected String extractIdFromPath(String path, String prefix) {
        return path.replaceFirst(prefix, "").split("/")[0];
    }

    /**
     * Creates a JSON response with 200 status code.
     * 
     * @param body the JSON body to return
     * @return a MockResponse configured with the JSON body
     */
    protected MockResponse jsonResponse(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    /**
     * Creates a 404 error response with a JSON error message.
     * 
     * @param message the error message to include
     * @return a MockResponse configured with a 404 status and error message
     */
    protected MockResponse notFoundError(String message) {
        return new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\": \"" + message + "\"}");
    }

    /**
     * Creates a basic 404 response without a body.
     * 
     * @return a MockResponse configured with a 404 status
     */
    protected MockResponse notFound() {
        return new MockResponse().setResponseCode(404);
    }

    // ========================================
    // Common JSON Response Templates
    // ========================================

    /**
     * JSON template for a rifle resource.
     * Used by both test classes to simulate rifle API responses.
     */
    protected static final String RIFLE_JSON = """
            {
                "id": 1,
                "name": "Test Rifle",
                "caliber": "6.5 Creedmoor"
            }
            """;

    /**
     * JSON template for a load resource.
     * Used by LoadsToolProviderTest to simulate load API responses.
     */
    protected static final String LOAD_JSON = """
            {
                "id": 1,
                "name": "Test Load 1",
                "description": "Test description",
                "powderManufacturer": "Hodgdon",
                "powderType": "H4350",
                "bulletManufacturer": "Hornady",
                "bulletType": "ELD-M",
                "bulletWeight": { "value": 140.0, "unit": "g" },
                "primerManufacturer": "CCI",
                "primerType": "BR2",
                "distanceFromLands": { "value": 0.02, "unit": "mm" },
                "caseOverallLength": { "value": 2.8, "unit": "mm" },
                "neckTension": { "value": 0.002, "unit": "mm" },
                "rifleId": 1
            }
            """;

    /**
     * JSON template for load statistics.
     * Used by LoadsToolProviderTest to simulate statistics API responses.
     */
    protected static final String LOAD_STATISTICS_JSON = """
            [
                {
                    "date": "2025-10-10",
                    "powderCharge": { "value": 40.5, "unit": "g" },
                    "targetRange": { "value": 100, "unit": "m" },
                    "groupSize": { "value": 12.7, "unit": "mm" },
                    "averageVelocity": { "value": 820, "unit": "m/s" },
                    "standardDeviation": { "value": 7.5, "unit": "m/s" },
                    "extremeSpread": { "value": 21.3, "unit": "m/s" },
                    "shots": [
                        { "velocity": { "value": 818, "unit": "m/s" } },
                        { "velocity": { "value": 822, "unit": "m/s" } },
                        { "velocity": { "value": 819, "unit": "m/s" } },
                        { "velocity": { "value": 827, "unit": "m/s" } },
                        { "velocity": { "value": 814, "unit": "m/s" } }
                    ]
                }
            ]
            """;

    // ========================================
    // Common Mock Dispatcher Factories
    // ========================================

    /**
     * Creates a dispatcher for the rifles-service mock server.
     * <p>
     * Handles:
     * <ul>
     * <li>GET /rifles - Returns array of rifles</li>
     * <li>GET /rifles/{id} - Returns single rifle or 404 for ID 999</li>
     * </ul>
     * <p>
     * This dispatcher is reusable across test classes that need to mock
     * the rifles-service API.
     * 
     * @return a configured Dispatcher for rifles-service endpoints
     */
    protected Dispatcher createRiflesDispatcher() {
        return new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                
                if (path == null) {
                    return notFound();
                }
                
                if (path.equals("/rifles")) {
                    return jsonResponse("[" + RIFLE_JSON + "]");
                }
                
                if (path.startsWith("/rifles/")) {
                    String idPart = extractIdFromPath(path, "/rifles/");
                    
                    if ("999".equals(idPart)) {
                        return notFoundError("Rifle not found");
                    }
                    
                    return jsonResponse(RIFLE_JSON);
                }
                
                return notFound();
            }
        };
    }

    /**
     * Creates a dispatcher for the loads-service mock server.
     * <p>
     * Handles:
     * <ul>
     * <li>GET /loads - Returns array of loads</li>
     * <li>GET /loads/{id} - Returns single load or 404 for ID 999</li>
     * <li>GET /loads/{id}/statistics - Returns load statistics</li>
     * </ul>
     * <p>
     * This dispatcher is reusable across test classes that need to mock
     * the loads-service API.
     * 
     * @return a configured Dispatcher for loads-service endpoints
     */
    protected Dispatcher createLoadsDispatcher() {
        return new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                
                if (path == null) {
                    return notFound();
                }
                
                if (path.equals("/loads")) {
                    return jsonResponse("[" + LOAD_JSON + "]");
                }
                
                if (path.startsWith("/loads/")) {
                    String idPart = extractIdFromPath(path, "/loads/");
                    
                    if ("999".equals(idPart)) {
                        return notFoundError("Load not found");
                    }
                    
                    if (path.contains("/statistics")) {
                        return jsonResponse(LOAD_STATISTICS_JSON);
                    }
                    
                    return jsonResponse(LOAD_JSON);
                }
                
                return notFound();
            }
        };
    }
}
