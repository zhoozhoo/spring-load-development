package ca.zhoozhoo.loaddev.components.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModuleSupport;

/**
 * Unit tests for MonetaryAmount JSON with the shared QuantityModule, without starting Spring context.
 */
class MonetaryAmountJacksonTest {

    private final ObjectMapper mapper = QuantityModuleSupport.newObjectMapperWithQuantityModule();

    @Test
    void serializeAndDeserializeMonetaryAmount_roundTrip() throws Exception {
        MonetaryAmount original = Money.of(12.34, "USD");
        String json = mapper.writeValueAsString(original);
        assertTrue(json.contains("\"amount\":12.34"));
        assertTrue(json.contains("\"currency\":\"USD\""));
        MonetaryAmount back = mapper.readValue(json, MonetaryAmount.class);
        assertEquals(original.getNumber().numberValueExact(java.math.BigDecimal.class), back.getNumber().numberValueExact(java.math.BigDecimal.class));
        assertEquals(original.getCurrency(), back.getCurrency());
    }

    @Test
    void deserialize_invalidMissingAmount_shouldThrow() {
        var ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
            mapper.readValue("{\"currency\":\"USD\"}", MonetaryAmount.class)
        );
        assertTrue(ex.getMessage().contains("amount"));
    }
}
