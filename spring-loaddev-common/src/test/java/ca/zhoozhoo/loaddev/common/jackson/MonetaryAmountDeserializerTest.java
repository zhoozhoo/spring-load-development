package ca.zhoozhoo.loaddev.common.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.money.MonetaryAmount;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link MonetaryAmountDeserializer}.
 * Verifies correct parsing and error handling of JSON monetary objects.
 */
class MonetaryAmountDeserializerTest {

    private final ObjectMapper mapper = QuantityModuleSupport.newObjectMapperWithQuantityModule();

    @Nested
    class SuccessfulDeserialization {

        @Test
        void deserialize_withValidUsd_shouldCreateMoney() throws Exception {
            var amt = mapper.readValue("{\"amount\":45.99,\"currency\":\"USD\"}", MonetaryAmount.class);
            assertEquals("USD", amt.getCurrency().getCurrencyCode());
            assertEquals(45.99, amt.getNumber().doubleValue());
        }

        @Test
        void deserialize_withIntegerAmount_shouldParseExact() throws Exception {
            var amt = mapper.readValue("{\"amount\":100,\"currency\":\"EUR\"}", MonetaryAmount.class);
            assertEquals(100, amt.getNumber().intValueExact());
        }
    }

    @Nested
    class MissingFields {

        @Test
        void deserialize_missingAmount_shouldThrowIllegalArgument() {
            var ex = assertThrows(IllegalArgumentException.class,
                    () -> mapper.readValue("{\"currency\":\"USD\"}", MonetaryAmount.class));
            assertTrue(ex.getMessage().contains("amount"));
        }

        @Test
        void deserialize_missingCurrency_shouldThrowIllegalArgument() {
            var ex = assertThrows(IllegalArgumentException.class,
                    () -> mapper.readValue("{\"amount\":10}", MonetaryAmount.class));
            assertTrue(ex.getMessage().contains("currency"));
        }

        @Test
        void deserialize_nullAmount_shouldThrowIllegalArgument() {
            var ex = assertThrows(IllegalArgumentException.class,
                    () -> mapper.readValue("{\"amount\":null,\"currency\":\"USD\"}", MonetaryAmount.class));
            assertTrue(ex.getMessage().contains("amount"));
        }

        @Test
        void deserialize_nullCurrency_shouldThrowIllegalArgument() {
            var ex = assertThrows(IllegalArgumentException.class,
                    () -> mapper.readValue("{\"amount\":10,\"currency\":null}", MonetaryAmount.class));
            assertTrue(ex.getMessage().contains("currency"));
        }
    }

    @Nested
    class InvalidInput {

        @Test
        void deserialize_withNonObject_shouldThrowIllegalArgument() {
            var ex = assertThrows(IllegalArgumentException.class, () -> mapper.readValue("123", MonetaryAmount.class));
            assertTrue(ex.getMessage().contains("amount"));
        }

        @Test
        void deserialize_withBadCurrency_shouldPropagateException() {
            assertThrows(Exception.class,
                    () -> mapper.readValue("{\"amount\":10,\"currency\":\"BAD\"}", MonetaryAmount.class));
        }
    }

    @Nested
    class TypeVerification {
        @Test
        void deserializer_shouldBeStdDeserializer() {
            assertTrue(com.fasterxml.jackson.databind.deser.std.StdDeserializer.class
                    .isAssignableFrom(MonetaryAmountDeserializer.class));
        }
    }
}
