package ca.zhoozhoo.loaddev.common.jackson;

import static systems.uom.ucum.format.UCUMFormat.Variant.CASE_SENSITIVE;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

import javax.measure.Unit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import systems.uom.ucum.format.UCUMFormat;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.exc.StreamWriteException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdScalarSerializer;

/// Custom Jackson serializer for JSR-385 [Unit] objects.
///
/// Serializes [Unit] instances into case-sensitive UCUM (Unified Code for Units of Measure)
/// formatted JSON strings. A `null` unit serializes to JSON `null`.
///
/// Behavior:
///
/// - Uses a cached case-sensitive [UCUMFormat] to produce canonical strings (e.g., `m/s`).
/// - No localization or pretty-printing; output is strictly UCUM.
/// - Rarely fails; only if the underlying formatter cannot format the unit.
///
/// Examples:
///
/// - meter → `"m"`
/// - international inch → `"[in_i]"`
/// - meters per second → `"m/s"`
/// - `null` → `null`
///
/// @author Zhubin Salehi
/// @see Unit
/// @see UnitDeserializer
/// @see UCUMFormat
@SuppressFBWarnings(value = "SE_NO_SUITABLE_CONSTRUCTOR", justification = "Jackson runtime never Java-serializes serializer instances; class implements Serializable only for framework compatibility and test expectations.")
public final class UnitSerializer extends StdScalarSerializer<Unit<?>> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    private static final Class<Unit<?>> UNIT_CLASS = (Class<Unit<?>>) (Class<?>) Unit.class;

    /// Cached UCUM formatter instance (case-sensitive).
    private static final UCUMFormat UCUM = UCUMFormat.getInstance(CASE_SENSITIVE);

    public UnitSerializer() {
        super(UNIT_CLASS);
    }

    @Override
    public void serialize(Unit<?> unit, JsonGenerator jsonGenerator, SerializationContext serializerProvider)
            throws JacksonException {

        try {
            if (unit == null) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeString(UCUM.format(unit, new StringBuilder()).toString());
            }
        } catch (IOException ioe) {
            throw new StreamWriteException(jsonGenerator, ioe.getMessage(), ioe);
        }
    }
}
