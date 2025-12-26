package ca.zhoozhoo.loaddev.common.jackson;

import java.math.BigDecimal;

import javax.money.MonetaryAmount;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;


/**
 * Jackson serializer for JSR-354 {@link MonetaryAmount} types.
 * <p>
 * Serializes {@link MonetaryAmount} to JSON objects of the form
 * {@code {"amount": 45.99, "currency": "USD"}}.
 * </p>
 *
 * @author Zhubin Salehi
 * @see MonetaryAmountDeserializer
 */
public class MonetaryAmountSerializer extends StdSerializer<MonetaryAmount> {

    public MonetaryAmountSerializer() {
        super(MonetaryAmount.class);
    }

    @Override
    public void serialize(MonetaryAmount amount, JsonGenerator generator, SerializationContext context) 
            throws JacksonException {
        generator.writeStartObject();
        generator.writeNumberProperty("amount", amount.getNumber().numberValueExact(BigDecimal.class));
        generator.writeStringProperty("currency", amount.getCurrency().getCurrencyCode());
        generator.writeEndObject();
    }
}
