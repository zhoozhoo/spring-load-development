package ca.zhoozhoo.loaddev.rifles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import ca.zhoozhoo.loaddev.common.opentelemetry.ContextPropagationConfiguration;
import ca.zhoozhoo.loaddev.common.opentelemetry.OpenTelemetryConfiguration;

/**
 * Main application class for the Rifles Service.
 * <p>
 * Manages rifle firearm specifications with RESTful APIs for authenticated users
 * in a multi-tenant environment.
 *
 * @author Zhubin Salehi
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableDiscoveryClient
@Import({OpenTelemetryConfiguration.class, ContextPropagationConfiguration.class})
public class RiflesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiflesServiceApplication.class, args);
    }
}
