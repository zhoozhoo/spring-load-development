package ca.zhoozhoo.loaddev.rifles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for the Rifles Service.
 * <p>
 * This service manages rifle firearm data including specifications such as caliber, barrel length,
 * and twist rate. It provides RESTful APIs for creating, retrieving, updating, and deleting rifle
 * information for authenticated users in a multi-tenant environment.
 * </p>
 *
 * @author Zhubin Salehi
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableDiscoveryClient
public class RiflesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiflesServiceApplication.class, args);
    }
}
