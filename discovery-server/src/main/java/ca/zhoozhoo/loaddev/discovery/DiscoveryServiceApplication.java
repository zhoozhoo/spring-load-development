package ca.zhoozhoo.loaddev.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Main application class for the Eureka Discovery Server.
 * <p>
 * This application provides service registration and discovery for all microservices
 * in the Spring Load Development system. It acts as a central registry where services
 * register themselves and discover other services, enabling dynamic service-to-service
 * communication and load balancing.
 * </p>
 *
 * @author Zhubin Salehi
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServiceApplication.class, args);
    }
}
