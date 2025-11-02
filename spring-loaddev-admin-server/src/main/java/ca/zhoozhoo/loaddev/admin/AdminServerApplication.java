package ca.zhoozhoo.loaddev.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

/**
 * Main application class for the Spring Boot Admin Server.
 * <p>
 * This application provides a web-based user interface for managing and monitoring
 * all microservices in the Spring Load Development system. It integrates with Eureka
 * service discovery to automatically detect and display registered services, their
 * health status, metrics, and logs.
 * </p>
 *
 * @author Zhubin Salehi
 */
@SpringBootApplication
@EnableAdminServer
@EnableDiscoveryClient
public class AdminServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServerApplication.class, args);
    }
}