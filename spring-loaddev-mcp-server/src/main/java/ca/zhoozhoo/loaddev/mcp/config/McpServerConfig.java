package ca.zhoozhoo.loaddev.mcp.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import ca.zhoozhoo.loaddev.mcp.tools.LoadTools;

@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider loadToolsCallbackProvider(LoadTools loadTools) {
        return MethodToolCallbackProvider.builder().toolObjects(loadTools).build();
    }

    @Bean
    public WebClient keycloakWebClient() {
        return WebClient.builder().build();
    }
}
