package ca.zhoozhoo.loaddev.rifles.config;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.Module;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModule;

/**
 * Jackson configuration for JSR-385 Quantity serialization/deserialization.
 * <p>
 * Registers custom Jackson serializers and deserializers for {@link Quantity} and {@link Unit}
 * types to properly handle JSR-385 unit-of-measurement objects in HTTP requests and responses.
 * This prevents circular reference issues with complex Quantity internal structures.
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
