package ca.zhoozhoo.loaddev.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModule;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Jackson configuration to support JSR-385 Quantity/Unit types in MCP server.
 * <p>
 * This configuration registers the {@link QuantityModule} to enable proper serialization
 * and deserialization of {@code javax.measure.Quantity} types used throughout the application
 * for type-safe handling of physical measurements (mass, length, velocity).
 * <p>
 * <b>Important Design Pattern:</b> Uses constructor injection with {@code @Lazy} annotation
 * to break circular dependency. The circular dependency chain is:
 * <ol>
 *   <li>JacksonConfig requires ObjectMapper to configure WebFlux codecs</li>
 *   <li>ObjectMapper creation triggers Jackson2ObjectMapperBuilderCustomizer beans</li>
 *   <li>Customizers need the {@code quantityModule()} bean from this configuration</li>
 *   <li>Spring detects circular reference during bean initialization</li>
 * </ol>
 * The {@code @Lazy} annotation defers actual ObjectMapper injection until first use,
 * breaking the cycle and allowing {@code quantityModule()} to register first.
 * <p>
 * <b>Note on SpotBugs EI_EXPOSE_REP2:</b> The ObjectMapper is stored directly without
 * defensive copying because:
 * <ul>
 *   <li>Creating a copy in the constructor would trigger initialization and recreate
 *       the circular dependency that {@code @Lazy} is meant to prevent</li>
 *   <li>ObjectMapper is effectively immutable after Spring Boot auto-configuration completes</li>
 *   <li>Registered Jackson modules cannot be modified after registration</li>
 *   <li>This is a configuration class, not a security-sensitive data holder</li>
 * </ul>
 *
 * @author Zhubin Salehi
 * @see QuantityModule
 * @see org.springframework.context.annotation.Lazy
 */
@Configuration
public class JacksonConfig implements WebFluxConfigurer {

    private final ObjectMapper objectMapper;

    /**
     * Constructs JacksonConfig with lazy ObjectMapper injection.
     * <p>
     * The {@code @Lazy} annotation is critical - it creates a proxy that defers
     * the actual ObjectMapper injection until first method call, preventing
     * circular dependency during Spring context initialization.
     * <p>
     * Note: We store the lazy proxy directly rather than calling copy() in the constructor,
     * as invoking methods on the proxy during construction would defeat the lazy initialization
     * and recreate the circular dependency.
     *
     * @param objectMapper the Spring Boot auto-configured ObjectMapper (lazy-injected)
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", 
        justification = "Cannot create defensive copy - would trigger lazy proxy initialization and recreate circular dependency")
    public JacksonConfig(@Lazy ObjectMapper objectMapper) {
        // Store the lazy proxy directly - the ObjectMapper will be initialized on first use
        this.objectMapper = objectMapper;
    }

    /**
     * Registers the JSR-385 Quantity support module with Jackson.
     * <p>
     * This module enables serialization/deserialization of {@code Quantity<?>}
     * types (e.g., {@code Quantity<Mass>}, {@code Quantity<Length>}) to/from JSON.
     * Without this module, Jackson would fail to serialize Quantity fields.
     *
     * @return the QuantityModule for JSR-385 support
     */
    @Bean
    public Module quantityModule() {
        return new QuantityModule();
    }

    /**
     * Configures WebFlux HTTP message codecs to use the application's ObjectMapper.
     * <p>
     * This ensures that all Jackson modules registered with the ObjectMapper
     * (including {@link #quantityModule()}) are applied to HTTP request/response
     * processing in reactive endpoints.
     *
     * @param configurer the codec configurer to customize
     */
    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
        configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
    }
}
