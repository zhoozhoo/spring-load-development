package ca.zhoozhoo.loaddev.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModule;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson 3 configuration for JSR-385 Quantity/Unit types.
 * <p>
 * Registers {@link QuantityModule} for serialization/deserialization of {@code Quantity} types.
 * Uses {@code @Lazy} injection to break circular dependency with Jackson customizer beans.
 * <p>
 * <b>Design Note:</b> JsonMapper is stored directly (not copied) to avoid triggering
 * lazy proxy initialization, which would recreate the circular dependency.
 *
 * @author Zhubin Salehi
 * @see QuantityModule
 */
@Configuration
public class JacksonConfig implements WebFluxConfigurer {

    private final JsonMapper jsonMapper;

    /**
     * Constructs JacksonConfig with lazy JsonMapper injection to avoid circular dependency.
     *
     * @param jsonMapper the Spring Boot auto-configured JsonMapper (lazy-injected)
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", 
        justification = "Cannot create defensive copy - would trigger lazy proxy initialization and recreate circular dependency")
    public JacksonConfig(@Lazy JsonMapper jsonMapper) {
        // Store the lazy proxy directly - the JsonMapper will be initialized on first use
        this.jsonMapper = jsonMapper;
    }

    /**
     * Registers JSR-385 Quantity support module for Jackson serialization.
     *
     * @return the QuantityModule for JSR-385 support
     */
    @Bean
    public QuantityModule quantityModule() {
        return new QuantityModule();
    }

    /**
     * Configures WebFlux codecs to use JsonMapper with registered modules.
     *
     * @param configurer the codec configurer to customize
     */
    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().jacksonJsonEncoder(new JacksonJsonEncoder(jsonMapper));
        configurer.defaultCodecs().jacksonJsonDecoder(new JacksonJsonDecoder(jsonMapper));
    }
}
