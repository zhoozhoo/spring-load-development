package ca.zhoozhoo.loaddev.common.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import javax.measure.Quantity;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Custom Jackson serializer for JSR-385 {@link Quantity} objects.
 * <p>
 * Writes quantities in the structured form:
 * { "value": <number>, "unit": "<UCUM>" }
 * The optional scale field is omitted; deserializers default to ABSOLUTE when absent.
 */
public class QuantitySerializer extends StdSerializer<Quantity<?>> {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public QuantitySerializer() {
        super((Class<Quantity<?>>)(Class<?>) Quantity.class);
    }

    @Override
    public void serialize(Quantity<?> value, JsonGenerator jsonGenerator, SerializerProvider provider)
            throws IOException {
        if (value == null) {
            jsonGenerator.writeNull();
            return;
        }

        jsonGenerator.writeStartObject();
        // Write numeric value with stable precision handling
        var number = value.getValue();
        if (number == null) {
            jsonGenerator.writeNullField("value");
        } else {
            // Use BigDecimal for consistency across numeric types
            jsonGenerator.writeNumberField("value", new BigDecimal(number.toString()));
        }
        // Delegate unit serialization to UnitSerializer
        jsonGenerator.writeFieldName("unit");
        provider.defaultSerializeValue(value.getUnit(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
