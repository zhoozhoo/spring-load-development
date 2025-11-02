package ca.zhoozhoo.loaddev.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for the MCP (Model Context Protocol) server.
 * <p>
 * This Spring Boot application provides MCP tools for managing reloading data,
 * including loads and rifles. It integrates with backend microservices through
 * service discovery and supports OAuth2 JWT authentication.
 * <p>
 * Key features:
 * <ul>
 * <li>MCP tool providers for loads and rifles operations</li>
 * <li>Reactive WebFlux support for streaming data</li>
 * <li>OAuth2 resource server with JWT validation</li>
 * <li>Service discovery integration via Eureka</li>
 * <li>Automatic authentication context propagation</li>
 * </ul>
 * 
 * @author Zhubin Salehi
 */
@EnableDiscoveryClient
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}