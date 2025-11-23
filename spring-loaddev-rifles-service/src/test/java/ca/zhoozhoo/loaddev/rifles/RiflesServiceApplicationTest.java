package ca.zhoozhoo.loaddev.rifles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.rifles.config.TestSecurityConfig;

/**
 * Integration test for RiflesServiceApplication.
 * Verifies that the Spring application context loads successfully.
 *
 * @author Zhubin Salehi
 */
@SpringBootTest(properties = "spring.autoconfigure.exclude=ca.zhoozhoo.loaddev.security.SecurityAutoConfiguration")
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class RiflesServiceApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
        // and that all beans are created without errors
        assertNotNull(applicationContext, "Application context should be loaded");
    }

    @Test
    void mainMethod_shouldExist() {
        // Verify that the main method exists and can be referenced
        assertDoesNotThrow(() -> assertNotNull(
                RiflesServiceApplication.class.getMethod("main", String[].class),
                "Main method should exist"));
    }
}
