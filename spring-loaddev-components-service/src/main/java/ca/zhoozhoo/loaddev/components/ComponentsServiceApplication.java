package ca.zhoozhoo.loaddev.components;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import ca.zhoozhoo.loaddev.common.opentelemetry.ContextPropagationConfiguration;
import ca.zhoozhoo.loaddev.common.opentelemetry.FilterConfiguration;
import ca.zhoozhoo.loaddev.common.opentelemetry.OpenTelemetryConfiguration;

/**
 * Components Service managing ammunition component data with multi-tenant RESTful APIs.
 *
 * @author Zhubin Salehi
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableDiscoveryClient
@Import({OpenTelemetryConfiguration.class, ContextPropagationConfiguration.class, FilterConfiguration.class})
public class ComponentsServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ComponentsServiceApplication.class, args);
    }
}
