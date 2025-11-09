package ca.zhoozhoo.loaddev.common.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;

import java.math.BigDecimal;

import javax.measure.Quantity;
import javax.measure.Quantity.Scale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for QuantityDeserializer.
 * Tests JSR-385 Quantity deserialization and error paths.
 *
 * @author Zhubin Salehi
 */
class QuantityDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new QuantityModule());

    @Nested
    class SuccessfulDeserialization {

        @Test
        void deserialize_withValidQuantityJson_shouldSucceed() throws Exception {
            var quantity = mapper.readValue("""
                    {"value":26.0,"unit":"[in_i]","scale":"ABSOLUTE"}
                    """, Quantity.class);
            
            assertEquals(26.0, quantity.getValue().doubleValue());
            assertEquals(INCH_INTERNATIONAL, quantity.getUnit());
            assertEquals(Scale.ABSOLUTE, quantity.getScale());
        }

        @Test
        void deserialize_withDecimalValue_shouldSucceed() throws Exception {
            var quantity = mapper.readValue("""
                    {"value":0.157,"unit":"[in_i]","scale":"ABSOLUTE"}
                    """, Quantity.class);
            
            assertEquals(new BigDecimal("0.157"), quantity.getValue());
        }

        @Test
        void deserialize_withRelativeScale_shouldSucceed() throws Exception {
            var quantity = mapper.readValue("""
                    {"value":100.0,"unit":"K","scale":"RELATIVE"}
                    """, Quantity.class);
            
            assertEquals(Scale.RELATIVE, quantity.getScale());
        }
    }

    @Nested
    class AdditionalInvalidCases {

        @Test
        void deserialize_withInvalidScale_shouldThrowWithFriendlyMessage() {
            var ex = assertThrows(com.fasterxml.jackson.core.JsonParseException.class, () ->
                mapper.readValue("{" +
                    "\"value\":1," +
                    "\"unit\":\"m\"," +
                    "\"scale\":\"BAD\"}" , Quantity.class)
            );

            // message should indicate invalid scale
            org.junit.jupiter.api.Assertions.assertTrue(ex.getOriginalMessage().contains("Invalid scale"));
        }

        @Test
        void deserialize_withNonNumericValue_shouldThrow() {
            assertThrows(com.fasterxml.jackson.core.JsonParseException.class, () ->
                mapper.readValue("{\"value\":\"abc\",\"unit\":\"m\",\"scale\":\"ABSOLUTE\"}", Quantity.class)
            );
        }
    }

    @Nested
    class MissingRequiredFields {

        @Test
        void deserialize_withMissingValueField_shouldThrow() {
            assertEquals("value not found for quantity type.", 
                    assertThrows(JsonParseException.class, () -> 
                        mapper.readValue("""
                                {"unit":"[in_i]","scale":"ABSOLUTE"}
                                """, Quantity.class)
                    ).getOriginalMessage());
        }

        @Test
        void deserialize_withMissingUnitField_shouldThrow() {
            assertEquals("unit not found for quantity type.", 
                    assertThrows(JsonParseException.class, () -> 
                        mapper.readValue("""
                                {"value":26.0,"scale":"ABSOLUTE"}
                                """, Quantity.class)
                    ).getOriginalMessage());
        }

        @Test
        void deserialize_withMissingScaleField_shouldThrow() {
            assertEquals("scale not found for quantity type.", 
                    assertThrows(JsonParseException.class, () -> 
                        mapper.readValue("""
                                {"value":26.0,"unit":"[in_i]"}
                                """, Quantity.class)
                    ).getOriginalMessage());
        }
    }

    @Nested
    class InvalidInput {

        @Test
        void deserialize_withInvalidJson_shouldThrow() {
            assertThrows(Exception.class, () -> mapper.readValue("{invalid json", Quantity.class));
        }

        @Test
        void deserialize_withEmptyJson_shouldThrow() {
            assertThrows(JsonParseException.class, () -> mapper.readValue("{}", Quantity.class));
        }
    }

    @Nested
    class TypeVerification {

        @Test
        void deserializer_shouldExtendStdDeserializer() {
            assertInstanceOf(com.fasterxml.jackson.databind.deser.std.StdDeserializer.class, 
                    new QuantityDeserializer());
        }

        @Test
        void deserializer_shouldBeSerializable() {
            assertInstanceOf(java.io.Serializable.class, new QuantityDeserializer());
        }
    }
}
