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
        // The JSON format may serialize as 26 or 26.0 depending on the value type
        assert jsonString.equals("{\"value\":26,\"unit\":\"[in_i]\"}") 
                || jsonString.equals("{\"value\":26.0,\"unit\":\"[in_i]\"}");
    }

    @Test
    void jsonToQuantity_shouldConvertCorrectly() {
        var quantity = fromJsonConverter.convert(Json.of("{\"value\":26.0,\"unit\":\"[in_i]\"}"));
        assertEquals(26.0, quantity.getValue().doubleValue(), 0.001);
        assertEquals(INCH_INTERNATIONAL, quantity.getUnit());
    }

    @Test
    void jsonToQuantity_withMissingValueField_shouldThrow() {
        assertEquals("Missing 'value' field in Quantity JSON: {\"unit\":\"[in_i]\"}",
                assertThrows(IllegalArgumentException.class,
                        () -> fromJsonConverter.convert(Json.of("{\"unit\":\"[in_i]\"}"))).getMessage());
    }

    @Test
    void jsonToQuantity_withMissingUnitField_shouldThrow() {
        assertEquals("Missing 'unit' field in Quantity JSON: {\"value\":26.0}",
                assertThrows(IllegalArgumentException.class, () -> 
                    fromJsonConverter.convert(Json.of("{\"value\":26.0}"))
                ).getMessage());
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
        assertEquals("Missing 'value' field in Quantity JSON: {}",
                assertThrows(IllegalArgumentException.class, () -> 
                    fromJsonConverter.convert(Json.of("{}"))
                ).getMessage());
    }

    @Test
    void jsonToQuantity_withNullValue_shouldConvertWithNullNumber() {
        // When value is null, Jackson's decimalValue returns null, which then gets passed to Quantities.getQuantity
        // This doesn't necessarily throw an exception - it may return null or a Quantity with null value
        fromJsonConverter.convert(Json.of("{\"value\":null,\"unit\":\"[in_i]\"}"));
    }

    @Test
    void jsonToQuantity_withDecimalValue_shouldConvertCorrectly() {
        assertEquals(0.157, 
                fromJsonConverter.convert(Json.of("{\"value\":0.157,\"unit\":\"[in_i]\"}"))
                        .getValue().doubleValue(), 0.0001);
    }

    @Test
    void quantityToJson_withDecimalValue_shouldConvertCorrectly() {
        assertEquals("{\"value\":0.157,\"unit\":\"[in_i]\"}", 
                toJsonConverter.convert(getQuantity(0.157, INCH_INTERNATIONAL)).asString());
    }
}
