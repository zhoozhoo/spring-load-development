package ca.zhoozhoo.loaddevelopment.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Main application class for the Spring Cloud Config Server.
 * <p>
 * This application provides centralized external configuration management for all
 * microservices in the Spring Load Development system. It serves configuration files
 * from a Git repository, allowing for version-controlled, environment-specific
 * configuration that can be updated without redeploying services.
 * </p>
 *
 * @author Zhubin Salehi
 */
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}