package ca.zhoozhoo.loaddev.common.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import java.math.BigDecimal;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Unit tests for {@link MonetaryAmountSerializer}.
 * Verifies JSON shape and number precision for JSR-354 monetary amounts.
 */
class MonetaryAmountSerializerTest {

    private final ObjectMapper mapper = QuantityModuleSupport.newObjectMapperWithQuantityModule();

    @Nested
    class SuccessfulSerialization {

        @Test
        void serialize_withUsdAmount_shouldWriteCorrectJson() throws Exception {
            var amount = Money.of(45.99, "USD");
            var writer = new StringWriter();
            mapper.writeValue(writer, amount);
            assertEquals("{\"amount\":45.99,\"currency\":\"USD\"}", writer.toString());
        }

        @Test
        void serialize_withIntegerAmount_shouldWriteExactInteger() throws Exception {
            var amount = Money.of(100, "EUR");
            var json = mapper.writeValueAsString(amount);
            assertEquals("{\"amount\":100,\"currency\":\"EUR\"}", json);
        }

        @Test
        void serialize_withHighPrecisionAmount_shouldPreserveScale() throws Exception {
            var amount = Money.of(new BigDecimal("0.15700"), "CAD");
            var json = mapper.writeValueAsString(amount);
            // Jackson will strip trailing zeros when using numberValueExact(BigDecimal.class)
            assertEquals("{\"amount\":0.157,\"currency\":\"CAD\"}", json);
        }
    }

    @Nested
    class TypeVerification {

        @Test
        void serializer_shouldBeStdSerializer() {
            assertTrue(StdSerializer.class.isAssignableFrom(MonetaryAmountSerializer.class));
        }
    }
}
