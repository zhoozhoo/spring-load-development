package ca.zhoozhoo.loaddev.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

import ca.zhoozhoo.loaddev.common.opentelemetry.ContextPropagationConfiguration;
import ca.zhoozhoo.loaddev.common.opentelemetry.OpenTelemetryConfiguration;

/// Main application class for the MCP (Model Context Protocol) server.
///
/// This Spring Boot application provides MCP tools for managing reloading data,
/// including loads and rifles. It integrates with backend microservices through
/// service discovery and supports OAuth2 JWT authentication.
///
/// Key features:
///
/// - MCP tool providers for loads and rifles operations
/// - Reactive WebFlux support for streaming data
/// - OAuth2 resource server with JWT validation
/// - Service discovery integration via Eureka
/// - Automatic authentication context propagation
///
/// @author Zhubin Salehi
@EnableDiscoveryClient
@SpringBootApplication
@Import({OpenTelemetryConfiguration.class, ContextPropagationConfiguration.class})
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}