package ca.zhoozhoo.loaddev.common.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static systems.uom.ucum.UCUM.METER;

import javax.measure.Unit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

/**
 * Unit tests for UnitDeserializer.
 * Tests JSR-385 Unit deserialization and error paths.
 *
 * @author Zhubin Salehi
 */
class UnitDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new QuantityModule());

    @Nested
    class SuccessfulDeserialization {

        @Test
        void deserialize_withValidUnitString_shouldSucceed() throws Exception {
            assertEquals(INCH_INTERNATIONAL, mapper.readValue("\"[in_i]\"", Unit.class));
        }

        @Test
        void deserialize_withMeterUnit_shouldSucceed() throws Exception {
            assertEquals(METER, mapper.readValue("\"m\"", Unit.class));
        }

        @Test
        void deserialize_withComplexUnit_shouldSucceed() throws Exception {
            assertEquals("m/s", mapper.readValue("\"m/s\"", Unit.class).toString());
        }

        @Test
        void deserialize_withEmptyString_shouldReturnOne() throws Exception {
            assertEquals("one", mapper.readValue("\"\"", Unit.class).toString());
        }

        @Test
        void deserialize_withNull_shouldReturnNull() throws Exception {
            assertNull(mapper.readValue("null", Unit.class));
        }
    }

    @Nested
    class InvalidInput {

        @Test
        void deserialize_withInvalidUnitFormat_shouldThrow() {
            assertThrows(Exception.class, () -> mapper.readValue("\"invalid_unit\"", Unit.class));
        }

        @Test
        void deserialize_withPartialUcum_shouldThrow() {
            assertThrows(Exception.class, () -> mapper.readValue("\"m/sX\"", Unit.class));
        }

        @Test
        void deserialize_withNonStringValue_shouldThrow() {
            assertThrows(MismatchedInputException.class, () -> 
                mapper.readValue("123", Unit.class)
            );
        }

        @Test
        void deserialize_withObjectValue_shouldThrow() {
            assertThrows(MismatchedInputException.class, () -> 
                mapper.readValue("{\"unit\":\"m\"}", Unit.class)
            );
        }
    }

    @Nested
    class TypeVerification {

        @Test
        void deserializer_shouldExtendStdScalarDeserializer() {
            try {
                Class<?> cls = Class.forName("ca.zhoozhoo.loaddev.common.jackson.UnitDeserializer");
                assertTrue(com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer.class.isAssignableFrom(cls));
            } catch (ClassNotFoundException e) {
                throw new AssertionError("UnitDeserializer class not found", e);
            }
        }

        @Test
        void deserializer_shouldBeSerializable() {
            try {
                Class<?> cls = Class.forName("ca.zhoozhoo.loaddev.common.jackson.UnitDeserializer");
                assertTrue(java.io.Serializable.class.isAssignableFrom(cls));
            } catch (ClassNotFoundException e) {
                throw new AssertionError("UnitDeserializer class not found", e);
            }
        }
    }
}
