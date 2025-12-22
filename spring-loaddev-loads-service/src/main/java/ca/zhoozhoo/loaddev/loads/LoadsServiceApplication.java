package ca.zhoozhoo.loaddev.loads;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import ca.zhoozhoo.loaddev.common.opentelemetry.ContextPropagationConfiguration;
import ca.zhoozhoo.loaddev.common.opentelemetry.OpenTelemetryConfiguration;

/**
 * Main application class for the Loads Service.
 * <p>
 * This service manages ammunition load data, including load configurations, shooting groups,
 * and individual shot velocity data. It provides RESTful APIs for creating, retrieving,
 * updating, and deleting load-related information for authenticated users.
 * </p>
 *
 * @author Zhubin Salehi
 * @version 0.0.8
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableDiscoveryClient
@Import({OpenTelemetryConfiguration.class, ContextPropagationConfiguration.class})
public class LoadsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoadsServiceApplication.class, args);
    }
}
