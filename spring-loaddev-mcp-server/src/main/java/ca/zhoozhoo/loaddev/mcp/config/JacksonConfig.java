package ca.zhoozhoo.loaddev.mcp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModule;

/**
 * Jackson configuration to support JSR-385 Quantity/Unit types in MCP server.
 */
@Configuration
public class JacksonConfig implements WebFluxConfigurer {

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public Module quantityModule() {
        return new QuantityModule();
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        // Use the application ObjectMapper for WebFlux JSON codecs.
        configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
        configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
    }
}
