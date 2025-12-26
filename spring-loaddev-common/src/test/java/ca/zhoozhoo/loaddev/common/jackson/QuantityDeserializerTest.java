package ca.zhoozhoo.loaddev.common.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.measure.Quantity;
import javax.measure.Quantity.Scale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Unit tests for QuantityDeserializer.
 * Tests JSR-385 Quantity deserialization and error paths.
 *
 * @author Zhubin Salehi
 */
class QuantityDeserializerTest {

        private final JsonMapper mapper = new JsonMapper()
            .rebuild()
            .addModule(new QuantityModule())
            .build();

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
            var ex = assertThrows(MismatchedInputException.class, () ->
                mapper.readValue("{" +
                    "\"value\":1," +
                    "\"unit\":\"m\"," +
                    "\"scale\":\"BAD\"}" , Quantity.class)
            );

            // message should indicate invalid scale
            assertTrue(ex.getMessage().contains("Invalid scale"));
        }

        @Test
        void deserialize_withNonNumericValue_shouldThrow() {
            assertThrows(MismatchedInputException.class, () ->
                mapper.readValue("{\"value\":\"abc\",\"unit\":\"m\",\"scale\":\"ABSOLUTE\"}", Quantity.class)
            );
        }

        @Test
        void deserialize_withNullValue_shouldThrowFriendlyMessage() {
            var ex = assertThrows(MismatchedInputException.class, () ->
                mapper.readValue("{\"value\":null,\"unit\":\"m\",\"scale\":\"ABSOLUTE\"}", Quantity.class)
            );
            assertTrue(ex.getMessage().contains("Invalid numeric value"));
        }

        @Test
        void deserialize_withNullUnit_shouldThrowFriendlyMessage() {
            var ex = assertThrows(MismatchedInputException.class, () ->
                mapper.readValue("{\"value\":1,\"unit\":null,\"scale\":\"ABSOLUTE\"}", Quantity.class)
            );
            assertTrue(ex.getMessage().contains("Invalid unit value"));
        }

        @Test
        void deserialize_withInvalidUnitString_shouldThrowFromQuantityDeserializer() {
            var ex = assertThrows(MismatchedInputException.class, () ->
                mapper.readValue("{\"value\":1,\"unit\":\"BAD\",\"scale\":\"ABSOLUTE\"}", Quantity.class)
            );
            assertTrue(ex.getMessage().contains("Invalid unit value"));
        }

        @Test
        void deserialize_withNullScale_shouldThrowFriendlyMessage() {
            var ex = assertThrows(MismatchedInputException.class, () ->
                mapper.readValue("{\"value\":1,\"unit\":\"m\",\"scale\":null}", Quantity.class)
            );
            assertTrue(ex.getMessage().contains("Invalid scale"));
        }
    }

    @Nested
    class MissingRequiredFields {

        @Test
        void deserialize_withMissingValueField_shouldThrow() {
                assertTrue(
                    assertThrows(MismatchedInputException.class, () -> 
                        mapper.readValue("""
                                {"unit":"[in_i]","scale":"ABSOLUTE"}
                                """, Quantity.class)
                    ).getMessage().contains("value not found for quantity type."));
        }

        @Test
        void deserialize_withMissingUnitField_shouldThrow() {
                assertTrue(
                    assertThrows(MismatchedInputException.class, () -> 
                        mapper.readValue("""
                                {"value":26.0,"scale":"ABSOLUTE"}
                                """, Quantity.class)
                    ).getMessage().contains("unit not found for quantity type."));
        }

    @Test
    void deserialize_withMissingScaleField_shouldDefaultToAbsolute() throws Exception {
        var quantity = mapper.readValue("""
            {"value":26.0,"unit":"[in_i]"}
            """, Quantity.class);
        assertEquals(Scale.ABSOLUTE, quantity.getScale());
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
            assertThrows(MismatchedInputException.class, () -> mapper.readValue("{}", Quantity.class));
        }
    }

    @Nested
    class TypeVerification {

        @Test
        void deserializer_shouldExtendStdDeserializer() {
            assertInstanceOf(StdDeserializer.class, 
                    new QuantityDeserializer());
        }

        @Test
        void deserializer_shouldBeSerializable() {
            assertInstanceOf(Serializable.class, new QuantityDeserializer());
        }
    }
}
