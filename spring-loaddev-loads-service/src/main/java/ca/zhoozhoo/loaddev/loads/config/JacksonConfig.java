package ca.zhoozhoo.loaddev.loads.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.Module;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModule;

@Configuration
public class JacksonConfig {

    @Bean
    public Module unitJacksonModule() {
        return new QuantityModule();
    }
}
