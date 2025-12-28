package ca.zhoozhoo.loaddev.common.jackson;

import static systems.uom.ucum.format.UCUMFormat.Variant.CASE_SENSITIVE;
import static tools.jackson.core.JsonToken.VALUE_NULL;
import static tools.jackson.core.JsonToken.VALUE_STRING;

import java.io.Serial;
import java.io.Serializable;
import java.text.ParsePosition;

import javax.measure.Unit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import systems.uom.ucum.format.UCUMFormat;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;

/**
 * Custom Jackson deserializer for JSR-385 {@link Unit} objects.
 * <p>
 * Deserializes UCUM (Unified Code for Units of Measure) formatted strings into
 * {@link Unit} instances. Expects a JSON string in case-sensitive UCUM format; JSON
 * {@code null} is accepted and deserializes to {@code null}.
 * <p>
 * Validation behavior:
 * <ul>
 *   <li>Performs full-input validation using UCUM parser; partial parses (e.g., {@code m/sX}) are rejected.</li>
 *   <li>On invalid syntax, throws a Jackson mapping exception with an informative message including the failure index.</li>
 *   <li>Non-string tokens (numbers, objects, arrays) result in a wrong-token exception.</li>
 * </ul>
 * <p>
 * Examples:
 * <ul>
 *   <li>{@code "m"} → meter</li>
 *   <li>{@code "[in_i]"} → international inch</li>
 *   <li>{@code "m/s"} → meters per second</li>
 *   <li>{@code ""} → dimensionless (one)</li>
 *   <li>{@code null} → {@code null}</li>
 * </ul>
 *
 * @author Zhubin Salehi
 * @see Unit
 * @see UnitSerializer
 * @see UCUMFormat
 */
@SuppressFBWarnings(value = "SE_NO_SUITABLE_CONSTRUCTOR", justification = "Jackson runtime never Java-serializes deserializer instances; class implements Serializable only for framework compatibility and test expectations.")
public final class UnitDeserializer extends StdScalarDeserializer<Unit<?>> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    private static final Class<Unit<?>> UNIT_CLASS = (Class<Unit<?>>) (Class<?>) Unit.class;

    /** Cached UCUM parser instance (case-sensitive). Consult UCUMFormat docs for thread-safety guarantees. */
    private static final UCUMFormat UCUM = UCUMFormat.getInstance(CASE_SENSITIVE);

    public UnitDeserializer() {
        super(UNIT_CLASS);
    }

    @Override
    public Unit<?> deserialize(JsonParser jsonParser, DeserializationContext ctx) throws JacksonException {
        var token = jsonParser.currentToken();

        if (token == VALUE_NULL) {
            // Accept explicit JSON null as a null Unit reference
            return null;
        }

        if (token != VALUE_STRING) {
            throw ctx.wrongTokenException(jsonParser, Unit.class,
                VALUE_STRING,
                    "Expected unit value in String format");
        }

        var text = jsonParser.getString();
        var pos = new ParsePosition(0);
        var unit = UCUM.parse(text, pos);

        // UCUMFormat.parse returns null on failure or may consume only part of the input.
        if (unit == null || pos.getIndex() != text.length()) {
            throw ctx.weirdStringException(text, Unit.class,
                    "Invalid UCUM unit syntax at index " + pos.getIndex());
        }

        return unit;
    }
}
