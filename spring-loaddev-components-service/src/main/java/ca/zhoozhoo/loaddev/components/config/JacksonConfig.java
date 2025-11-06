package ca.zhoozhoo.loaddev.components.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.Module;

import ca.zhoozhoo.loaddev.components.converter.Jsr385Jsr354Module;
import lombok.extern.log4j.Log4j2;

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
@Log4j2
public class JacksonConfig {

    @Bean
    public Module jsr385Jsr354Module() {
        return new Jsr385Jsr354Module();
    }
}
