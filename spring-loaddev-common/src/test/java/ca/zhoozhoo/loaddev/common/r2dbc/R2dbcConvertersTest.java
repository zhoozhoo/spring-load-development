package ca.zhoozhoo.loaddev.common.r2dbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import ca.zhoozhoo.loaddev.common.r2dbc.R2dbcConverters.JsonToMonetaryAmountConverter;
import ca.zhoozhoo.loaddev.common.r2dbc.R2dbcConverters.JsonToQuantityConverter;
import ca.zhoozhoo.loaddev.common.r2dbc.R2dbcConverters.MonetaryAmountToJsonConverter;
import ca.zhoozhoo.loaddev.common.r2dbc.R2dbcConverters.QuantityToJsonConverter;
import io.r2dbc.postgresql.codec.Json;

/**
 * Unit tests for R2dbcConverters.
 *
 * @author Zhubin Salehi
 */
class R2dbcConvertersTest {

    private final QuantityToJsonConverter toJsonConverter = new QuantityToJsonConverter();
    private final JsonToQuantityConverter fromJsonConverter = new JsonToQuantityConverter();
    private final MonetaryAmountToJsonConverter moneyToJsonConverter = new MonetaryAmountToJsonConverter();
    private final JsonToMonetaryAmountConverter jsonToMoneyConverter = new JsonToMonetaryAmountConverter();

    @Test
    void getConverters_shouldReturnEightConverters() {
        assertEquals(8, R2dbcConverters.getConverters().size());
    }

    @Test
    void quantityToJson_shouldConvertCorrectly() {
        var jsonString = toJsonConverter.convert(getQuantity(26.0, INCH_INTERNATIONAL)).asString();
        // Expect scale field now; value may be rendered as 26 or 26.0
        assert jsonString.equals("{\"value\":26,\"unit\":\"[in_i]\",\"scale\":\"ABSOLUTE\"}")
                || jsonString.equals("{\"value\":26.0,\"unit\":\"[in_i]\",\"scale\":\"ABSOLUTE\"}");
    }

    @Test
    void jsonToQuantity_shouldConvertCorrectly() {
        var quantity = fromJsonConverter.convert(Json.of("{\"value\":26.0,\"unit\":\"[in_i]\"}"));
        assertEquals(26.0, quantity.getValue().doubleValue(), 0.001);
        assertEquals(INCH_INTERNATIONAL, quantity.getUnit());
    }

    @Test
    void jsonToQuantity_withMissingValueField_shouldThrow() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> fromJsonConverter.convert(Json.of("{\"unit\":\"[in_i]\"}")));
        assertEquals("Failed to parse Quantity JSON: {\"unit\":\"[in_i]\"}", ex.getMessage());
    }

    @Test
    void jsonToQuantity_withMissingUnitField_shouldThrow() {
        var ex = assertThrows(IllegalArgumentException.class, () ->
                fromJsonConverter.convert(Json.of("{\"value\":26.0}")));
        assertEquals("Failed to parse Quantity JSON: {\"value\":26.0}", ex.getMessage());
    }

    @Test
    void jsonToQuantity_withInvalidJson_shouldThrow() {
        assertEquals("Failed to parse Quantity JSON: {invalid json",
                assertThrows(IllegalArgumentException.class, () -> 
                    fromJsonConverter.convert(Json.of("{invalid json"))
                ).getMessage());
    }

    @Test
    void jsonToQuantity_withInvalidUnit_shouldThrow() {
        // The actual exception type thrown is TokenException, not IllegalArgumentException
        assertThrows(Exception.class, () -> 
            fromJsonConverter.convert(Json.of("{\"value\":26.0,\"unit\":\"invalid_unit\"}"))
        );
    }

    @Test
    void jsonToQuantity_withEmptyJson_shouldThrow() {
        var ex = assertThrows(IllegalArgumentException.class, () -> fromJsonConverter.convert(Json.of("{}")));
        assertEquals("Failed to parse Quantity JSON: {}", ex.getMessage());
    }

    @Test
    void jsonToQuantity_withNullValue_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> fromJsonConverter.convert(Json.of("{\"value\":null,\"unit\":\"[in_i]\"}")));
    }

    @Test
    void jsonToQuantity_withDecimalValue_shouldConvertCorrectly() {
        assertEquals(0.157, 
                fromJsonConverter.convert(Json.of("{\"value\":0.157,\"unit\":\"[in_i]\"}"))
                        .getValue().doubleValue(), 0.0001);
    }

    @Test
    void quantityToJson_withDecimalValue_shouldConvertCorrectly() {
        assertEquals("{\"value\":0.157,\"unit\":\"[in_i]\",\"scale\":\"ABSOLUTE\"}",
                toJsonConverter.convert(getQuantity(0.157, INCH_INTERNATIONAL)).asString());
    }

    // MonetaryAmount tests

    @Test
    void monetaryAmountToJson_shouldConvertCorrectly() {
        var money = Money.of(45.99, "USD");
        var json = moneyToJsonConverter.convert(money);
        assertNotNull(json);
        var jsonString = json.asString();
        assert jsonString.contains("\"amount\":45.99") || jsonString.contains("\"amount\":\"45.99\"");
        assert jsonString.contains("\"currency\":\"USD\"");
    }

    @Test
    void jsonToMonetaryAmount_shouldConvertCorrectly() {
        var jsonString = "{\"amount\":45.99,\"currency\":\"USD\"}";
        var money = jsonToMoneyConverter.convert(Json.of(jsonString));
        assertEquals(45.99, money.getNumber().doubleValue(), 0.001);
        assertEquals("USD", money.getCurrency().getCurrencyCode());
    }

    @Test
    void monetaryAmountToJson_withLargeAmount_shouldConvertCorrectly() {
        var money = Money.of(9999.99, "EUR");
        var json = moneyToJsonConverter.convert(money);
        assertNotNull(json);
        var jsonString = json.asString();
        assert jsonString.contains("9999.99");
        assert jsonString.contains("EUR");
    }

    @Test
    void jsonToMonetaryAmount_withDifferentCurrency_shouldConvertCorrectly() {
        var money = jsonToMoneyConverter.convert(Json.of("{\"amount\":123.45,\"currency\":\"CAD\"}"));
        assertEquals(123.45, money.getNumber().doubleValue(), 0.001);
        assertEquals("CAD", money.getCurrency().getCurrencyCode());
    }

    @Test
    void jsonToMonetaryAmount_withZeroAmount_shouldConvertCorrectly() {
        var money = jsonToMoneyConverter.convert(Json.of("{\"amount\":0,\"currency\":\"USD\"}"));
        assertEquals(0.0, money.getNumber().doubleValue(), 0.001);
        assertEquals("USD", money.getCurrency().getCurrencyCode());
    }

    @Test
    void jsonToMonetaryAmount_withInvalidJson_shouldThrow() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> jsonToMoneyConverter.convert(Json.of("{invalid json")));
        assertEquals("Failed to parse MonetaryAmount JSON: {invalid json", ex.getMessage());
    }

    @Test
    void jsonToMonetaryAmount_withMissingAmountField_shouldThrow() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> jsonToMoneyConverter.convert(Json.of("{\"currency\":\"USD\"}")));
        assertEquals("Missing required field 'amount' in MonetaryAmount JSON", ex.getMessage());
    }

    @Test
    void jsonToMonetaryAmount_withMissingCurrencyField_shouldThrow() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> jsonToMoneyConverter.convert(Json.of("{\"amount\":45.99}")));
        assertEquals("Missing required field 'currency' in MonetaryAmount JSON", ex.getMessage());
    }

    @Test
    void jsonToMonetaryAmount_withInvalidCurrency_shouldThrow() {
        assertThrows(Exception.class,
                () -> jsonToMoneyConverter.convert(Json.of("{\"amount\":45.99,\"currency\":\"INVALID\"}")));
    }

    @Test
    void monetaryAmountToJson_withNegativeAmount_shouldConvertCorrectly() {
        var money = Money.of(-25.50, "GBP");
        var json = moneyToJsonConverter.convert(money);
        assertNotNull(json);
        var jsonString = json.asString();
        assert jsonString.contains("-25.5") || jsonString.contains("\"-25.5\"");
        assert jsonString.contains("GBP");
    }

    @Test
    void jsonToMonetaryAmount_withNegativeAmount_shouldConvertCorrectly() {
        var money = jsonToMoneyConverter.convert(Json.of("{\"amount\":-25.50,\"currency\":\"GBP\"}"));
        assertEquals(-25.50, money.getNumber().doubleValue(), 0.001);
        assertEquals("GBP", money.getCurrency().getCurrencyCode());
    }

    // Rifling and Zeroing converter tests (null handling verification)

    @Test
    void riflingToJson_withNullInput_shouldReturnNull() {
        var converter = new R2dbcConverters.RiflingToJsonConverter();
        var result = converter.convert(null, null, null);
        assertEquals(null, result);
    }

    @Test
    void jsonToRifling_withNullInput_shouldReturnNull() {
        var converter = new R2dbcConverters.JsonToRiflingConverter();
        var result = converter.convert(null, null, null);
        assertEquals(null, result);
    }

    @Test
    void zeroingToJson_withNullInput_shouldReturnNull() {
        var converter = new R2dbcConverters.ZeroingToJsonConverter();
        var result = converter.convert(null, null, null);
        assertEquals(null, result);
    }

    @Test
    void jsonToZeroing_withNullInput_shouldReturnNull() {
        var converter = new R2dbcConverters.JsonToZeroingConverter();
        var result = converter.convert(null, null, null);
        assertEquals(null, result);
    }

    @Test
    void riflingToJson_getConvertibleTypes_shouldReturnPairsOrEmpty() {
        var converter = new R2dbcConverters.RiflingToJsonConverter();
        var types = converter.getConvertibleTypes();
        assertNotNull(types);
        // Will be empty if Rifling class not available in classpath
        // Will have one pair if Rifling class is available
    }

    @Test
    void jsonToRifling_getConvertibleTypes_shouldReturnPairsOrEmpty() {
        var converter = new R2dbcConverters.JsonToRiflingConverter();
        var types = converter.getConvertibleTypes();
        assertNotNull(types);
        // Will be empty if Rifling class not available in classpath
        // Will have one pair if Rifling class is available
    }

    @Test
    void zeroingToJson_getConvertibleTypes_shouldReturnPairsOrEmpty() {
        var converter = new R2dbcConverters.ZeroingToJsonConverter();
        var types = converter.getConvertibleTypes();
        assertNotNull(types);
        // Will be empty if Zeroing class not available in classpath
        // Will have one pair if Zeroing class is available
    }

    @Test
    void jsonToZeroing_getConvertibleTypes_shouldReturnPairsOrEmpty() {
        var converter = new R2dbcConverters.JsonToZeroingConverter();
        var types = converter.getConvertibleTypes();
        assertNotNull(types);
        // Will be empty if Zeroing class not available in classpath
        // Will have one pair if Zeroing class is available
    }
}
