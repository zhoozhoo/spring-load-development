package ca.zhoozhoo.loaddev.load_development;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableDiscoveryClient
public class LoadDevelopmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoadDevelopmentApplication.class, args);
    }
}
