package ca.zhoozhoo.loaddev.common.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static systems.uom.ucum.UCUM.KELVIN;
import static systems.uom.ucum.UCUM.METER;

import java.io.StringWriter;

import javax.measure.Unit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for UnitSerializer.
 * Tests JSR-385 Unit serialization to UCUM format.
 *
 * @author Zhubin Salehi
 */
class UnitSerializerTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new QuantityModule());

    @Nested
    class SuccessfulSerialization {

        @Test
        void serialize_withValidUnit_shouldSucceed() throws Exception {
            var writer = new StringWriter();
            mapper.writeValue(writer, METER);
            assertEquals("\"m\"", writer.toString());
        }

        @Test
        void serialize_withInchUnit_shouldSucceed() throws Exception {
            var writer = new StringWriter();
            mapper.writeValue(writer, INCH_INTERNATIONAL);
            assertEquals("\"[in_i]\"", writer.toString());
        }

        @Test
        void serialize_withTemperatureUnit_shouldSucceed() throws Exception {
            var writer = new StringWriter();
            mapper.writeValue(writer, KELVIN);
            assertEquals("\"K\"", writer.toString());
        }

        @Test
        void serialize_withComplexUnit_shouldSucceed() throws Exception {
            var writer = new StringWriter();
            mapper.writeValue(writer, METER.divide(tech.units.indriya.unit.Units.SECOND));
            assertEquals("\"m/s\"", writer.toString());
        }

        @Test
        void serialize_withNullUnit_shouldWriteNull() throws Exception {
            var writer = new StringWriter();
            mapper.writeValue(writer, (Unit<?>) null);
            assertEquals("null", writer.toString());
        }
    }

    @Nested
    class TypeVerification {

        @Test
        void serializer_shouldExtendStdScalarSerializer() {
            try {
                Class<?> cls = Class.forName("ca.zhoozhoo.loaddev.common.jackson.UnitSerializer");
                assertTrue(com.fasterxml.jackson.databind.ser.std.StdScalarSerializer.class.isAssignableFrom(cls));
            } catch (ClassNotFoundException e) {
                throw new AssertionError("UnitSerializer class not found", e);
            }
        }

        @Test
        void serializer_shouldBeSerializable() {
            try {
                Class<?> cls = Class.forName("ca.zhoozhoo.loaddev.common.jackson.UnitSerializer");
                assertTrue(java.io.Serializable.class.isAssignableFrom(cls));
            } catch (ClassNotFoundException e) {
                throw new AssertionError("UnitSerializer class not found", e);
            }
        }
    }
}
