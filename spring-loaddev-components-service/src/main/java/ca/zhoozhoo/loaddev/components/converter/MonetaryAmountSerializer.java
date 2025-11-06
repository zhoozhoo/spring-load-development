package ca.zhoozhoo.loaddev.components.converter;

import java.io.IOException;

import javax.money.MonetaryAmount;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Jackson serializer for JSR-354 {@link MonetaryAmount} types.
 * <p>
 * Serializes {@link MonetaryAmount} objects to JSON format:
 * {@code {"amount": 45.99, "currency": "USD"}}
 * </p>
 *
 * @author Zhubin Salehi
 */
class MonetaryAmountSerializer extends StdSerializer<MonetaryAmount> {

    private static final long serialVersionUID = 1L;

    public MonetaryAmountSerializer() {
        super(MonetaryAmount.class);
    }

    @Override
    public void serialize(MonetaryAmount value, JsonGenerator gen, SerializerProvider provider) 
            throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("amount", value.getNumber().numberValueExact(java.math.BigDecimal.class));
        gen.writeStringField("currency", value.getCurrency().getCurrencyCode());
        gen.writeEndObject();
    }
}
