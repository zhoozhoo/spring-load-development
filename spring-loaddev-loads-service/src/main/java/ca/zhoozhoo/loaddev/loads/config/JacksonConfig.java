package ca.zhoozhoo.loaddev.loads.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.Module;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModule;

/**
 * Jackson configuration for JSON serialization and deserialization.
 * <p>
 * Registers the {@link QuantityModule} to enable proper JSON handling of
 * JSR-385 {@link javax.measure.Quantity} types in REST API responses and requests.
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
