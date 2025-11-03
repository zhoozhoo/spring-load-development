package ca.zhoozhoo.loaddev.loads.config;

import java.io.IOException;

import javax.measure.Quantity;
import javax.measure.format.MeasurementParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import jakarta.annotation.PostConstruct;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import tech.units.indriya.format.SimpleQuantityFormat;

/**
 * Jackson configuration for Spring WebFlux.
 * <p>
 * This configuration registers custom serializers and deserializers for
 * {@link javax.measure.Quantity} types from the JSR 363 (Units of Measurement) API.
 * </p>
 * <p>
 * Quantities are serialized as simple strings (e.g., "150 gr") to avoid dependency
 * conflicts with UCUM format libraries.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Configuration
public class JacksonConfig implements WebFluxConfigurer {

    private static final SimpleQuantityFormat QUANTITY_FORMAT = SimpleQuantityFormat.getInstance();

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Configures the application ObjectMapper with custom modules.
     * Registers custom Quantity serializers for JSR 363 types.
     */
    @PostConstruct
    public void configureObjectMapper() {
        // Register custom Quantity serializers
        SimpleModule quantityModule = new SimpleModule();
        quantityModule.addSerializer(Quantity.class, new QuantitySerializer());
        quantityModule.addDeserializer(Quantity.class, new QuantityDeserializer());
        objectMapper.registerModule(quantityModule);
    }

    /**
     * Configures HTTP message codecs to use the application ObjectMapper.
     *
     * @param configurer the codec configurer to customize
     */
    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
        configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
    }

    /**
     * Custom Jackson serializer for Quantity types.
     * Serializes quantities as simple strings (e.g., "150 gr").
     */
    @SuppressWarnings("rawtypes")
    private static class QuantitySerializer extends StdSerializer<Quantity> {
        
        protected QuantitySerializer() {
            super(Quantity.class);
        }
        
        @Override
        public void serialize(Quantity value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(QUANTITY_FORMAT.format(value));
        }
    }

    /**
     * Custom Jackson deserializer for Quantity types.
     * Deserializes string representations (e.g., "150 gr") to Quantity objects.
     */
    @SuppressWarnings("rawtypes")
    private static class QuantityDeserializer extends StdDeserializer<Quantity> {
        
        protected QuantityDeserializer() {
            super(Quantity.class);
        }
        
        @Override
        public Quantity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            try {
                return QUANTITY_FORMAT.parse(value);
            } catch (MeasurementParseException e) {
                throw new IOException("Failed to parse Quantity from: " + value, e);
            }
        }
    }
}
