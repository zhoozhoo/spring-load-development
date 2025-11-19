package ca.zhoozhoo.loaddev.rifles.config;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModule;

/**
 * Jackson configuration for JSR-385 Quantity serialization/deserialization.
 * <p>
 * Registers {@link QuantityModule} to handle {@link Quantity} and {@link Unit} types
 * in HTTP requests and responses, preventing circular reference issues.
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
