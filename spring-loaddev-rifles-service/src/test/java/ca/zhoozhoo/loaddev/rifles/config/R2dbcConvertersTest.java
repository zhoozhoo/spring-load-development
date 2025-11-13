package ca.zhoozhoo.loaddev.rifles.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;

import org.junit.jupiter.api.Test;

import ca.zhoozhoo.loaddev.rifles.config.R2dbcConverters.JsonToQuantityConverter;
import ca.zhoozhoo.loaddev.rifles.config.R2dbcConverters.QuantityToJsonConverter;
import io.r2dbc.postgresql.codec.Json;

/**
 * Unit tests for R2dbcConverters.
 *
 * @author Zhubin Salehi
 */
class R2dbcConvertersTest {

    private final QuantityToJsonConverter toJsonConverter = new QuantityToJsonConverter();
    private final JsonToQuantityConverter fromJsonConverter = new JsonToQuantityConverter();

    @Test
    void getConverters_shouldReturnTwoConverters() {
        assertEquals(2, R2dbcConverters.getConverters().size());
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
}
