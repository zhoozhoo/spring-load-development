package ca.zhoozhoo.loaddev.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.mcp.config.TestSecurityConfig;

/**
 * Integration tests for MCP Server Application.
 * <p>
 * Tests the complete application startup and configuration,
 * ensuring all beans are properly wired and the server starts successfully.
 * 
 * @author Zhubin Salehi
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class McpServerApplicationTests {

    @LocalServerPort
    private int port;

    @Test
    void contextLoads() {
        // Verify the application context loads successfully
        assertThat(port).isGreaterThan(0);
    }

    @Test
    void applicationStartsSuccessfully() {
        // This test verifies that all beans are properly configured
        // and the application starts without errors
        assertThat(port).isBetween(1024, 65535);
    }
}
