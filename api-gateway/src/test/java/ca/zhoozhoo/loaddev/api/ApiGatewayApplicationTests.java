package ca.zhoozhoo.loaddev.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for the API Gateway application.
 * Verifies that the Spring Boot application context loads successfully
 * with all required beans and configurations.
 * 
 * @author Zhubin Salehi
 */
@SpringBootTest
@ActiveProfiles("test")
class ApiGatewayApplicationTests {

    /**
     * Verifies that the Spring application context loads successfully.
     * This test ensures all auto-configurations, bean definitions, and
     * component scanning work correctly together.
     */
    @Test
    void contextLoads() {
        // Context loading is verified by the test framework
        // If this test passes, the application context is valid
    }
}
