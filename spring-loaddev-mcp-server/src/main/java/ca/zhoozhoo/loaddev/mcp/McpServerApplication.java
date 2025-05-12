package ca.zhoozhoo.loaddev.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import ca.zhoozhoo.loaddev.mcp.service.LoadsService;

@EnableDiscoveryClient
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider weatherTools(LoadsService loadsService) {
        return MethodToolCallbackProvider.builder().toolObjects(loadsService).build();
    }
}