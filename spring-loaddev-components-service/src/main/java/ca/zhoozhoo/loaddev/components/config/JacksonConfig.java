package ca.zhoozhoo.loaddev.components.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModule;

/**
 * Jackson configuration for JSR-385 Quantity types.
 *
 * @author Zhubin Salehi
 */
@Configuration
public class JacksonConfig {

    @Bean
    public QuantityModule quantityModule() {
        return new QuantityModule();
    }
}
