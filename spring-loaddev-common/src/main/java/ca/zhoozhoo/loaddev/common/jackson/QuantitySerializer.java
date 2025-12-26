package ca.zhoozhoo.loaddev.common.jackson;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.math.BigDecimal;

import javax.measure.Quantity;

/**
 * Custom Jackson serializer for JSR-385 {@link Quantity} objects.
 * <p>
 * Writes quantities in the structured form:
 * { "value": <number>, "unit": "<UCUM>" }
 * The optional scale field is omitted; deserializers default to ABSOLUTE when absent.
 * 
 * @author Zhubin Salehi
 * @see Quantity
 * @see QuantityDeserializer
 */
public class QuantitySerializer extends StdSerializer<Quantity<?>> {

    @SuppressWarnings("unchecked")
    public QuantitySerializer() {
        super((Class<Quantity<?>>)(Class<?>) Quantity.class);
    }

    @Override
    public void serialize(Quantity<?> value, JsonGenerator generator, SerializationContext context)
            throws JacksonException {
        if (value == null) {
            generator.writeNull();
            return;
        }

        generator.writeStartObject();

        var number = value.getValue();
        if (number == null) {
            generator.writeNullProperty("value");
        } else {
            generator.writeNumberProperty("value", new BigDecimal(number.toString()));
        }
        
        context.defaultSerializeProperty("unit", value.getUnit(), generator);

        try {
            var scale = value.getScale();
            if (scale != null) {
                generator.writeStringProperty("scale", scale.toString());
            }
        } catch (Throwable ignored) {
            // Scale not available; ignore.
        }

        generator.writeEndObject();
    }
}
