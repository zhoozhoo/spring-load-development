package ca.zhoozhoo.loaddev.genai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GenAIServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenAIServiceApplication.class, args);
    }
}