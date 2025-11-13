package ca.zhoozhoo.loaddev.components.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.Module;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModule;

/**
 * Jackson configuration for custom serialization/deserialization.
 * <p>
 * Registers the JSR-385 (Units of Measurement) and JSR-354 (Money and Currency)
 * Jackson module for proper JSON handling of Quantity and MonetaryAmount types.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Module quantityModule() {
        return new QuantityModule();
    }
}
