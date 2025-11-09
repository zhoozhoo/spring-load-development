package ca.zhoozhoo.loaddev.common.jackson;

import static systems.uom.ucum.format.UCUMFormat.Variant.CASE_SENSITIVE;

import java.io.IOException;

import javax.measure.Unit;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import systems.uom.ucum.format.UCUMFormat;

/**
 * Custom Jackson serializer for JSR-385 {@link Unit} objects.
 * <p>
 * Serializes {@link Unit} instances into case-sensitive UCUM (Unified Code for Units of Measure)
 * formatted JSON strings. A {@code null} unit serializes to a JSON {@code null} (symmetrical with the deserializer which accepts null).
 * <p>
 * Behavior:
 * <ul>
 *   <li>Uses a UCUM formatter to produce canonical strings (e.g., {@code m/s}).</li>
 *   <li>Does not attempt pretty-printing or localization; output is strictly UCUM.</li>
 *   <li>Fails only if the underlying {@link UCUMFormat} cannot format the unit (rare for standard units).</li>
 * </ul>
 * Examples:
 * <ul>
 *   <li>meter → "m"</li>
 *   <li>international inch → "[in_i]"</li>
 *   <li>meters per second → "m/s"</li>
 *   <li>null → null</li>
 * </ul>
 *
 * @author Zhubin Salehi
 * @see Unit
 * @see UnitDeserializer
 * @see UCUMFormat
 */
public class UnitSerializer extends StdScalarSerializer<Unit<?>> {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    private static final Class<Unit<?>> UNIT_CLASS = (Class<Unit<?>>) (Class<?>) Unit.class;

    public UnitSerializer() {
        super(UNIT_CLASS);
    }

    @Override
    public void serialize(Unit<?> unit, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {

        if (unit == null) {
            jsonGenerator.writeNull();
        } else {
            jsonGenerator.writeString(UCUMFormat.getInstance(CASE_SENSITIVE)
                    .format(unit, new StringBuilder())
                    .toString());
        }
    }
}
