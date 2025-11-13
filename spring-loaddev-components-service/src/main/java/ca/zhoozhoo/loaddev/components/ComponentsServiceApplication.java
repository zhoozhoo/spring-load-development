package ca.zhoozhoo.loaddev.components;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for the Components Service.
 * <p>
 * This service manages ammunition component data including projectiles, cases, primers, and propellants.
 * It provides RESTful APIs for creating, retrieving, updating, and deleting component information
 * for authenticated users in a multi-tenant environment.
 * </p>
 *
 * @author Zhubin Salehi
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableDiscoveryClient
public class ComponentsServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ComponentsServiceApplication.class, args);
    }
}
