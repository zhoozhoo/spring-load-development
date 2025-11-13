package ca.zhoozhoo.loaddev.common.jackson;

import static systems.uom.ucum.format.UCUMFormat.Variant.CASE_SENSITIVE;

import java.io.IOException;
import java.text.ParsePosition;

import javax.measure.Unit;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import systems.uom.ucum.format.UCUMFormat;

/**
 * Custom Jackson deserializer for JSR-385 {@link Unit} objects.
 * <p>
 * Deserializes UCUM (Unified Code for Units of Measure) formatted strings into
 * {@link Unit} instances. Expects a JSON String in case-sensitive UCUM format; JSON
 * {@code null} is accepted and deserializes to {@code null}.
 * <p>
 * Validation behavior:
 * <ul>
 *   <li>Performs full-input validation using UCUM parser; partial parses (e.g., {@code "m/sX"}) are rejected.</li>
 *   <li>On invalid syntax, throws a Jackson mapping exception with an informative message including the failure index.</li>
 *   <li>Non-string tokens (numbers, objects, arrays) result in a wrong-token exception.</li>
 * </ul>
 * <p>
 * Examples:
 * <ul>
 *   <li>"m" → meter</li>
 *   <li>"[in_i]" → international inch</li>
 *   <li>"m/s" → meters per second</li>
 *   <li>"" → dimensionless (one)</li>
 *   <li>null → null</li>
 * </ul>
 *
 * @author Zhubin Salehi
 * @see Unit
 * @see UnitSerializer
 * @see UCUMFormat
 */
public class UnitDeserializer extends StdScalarDeserializer<Unit<?>> {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    private static final Class<Unit<?>> UNIT_CLASS = (Class<Unit<?>>) (Class<?>) Unit.class;

    /** Cached UCUM parser instance (case-sensitive). Consult UCUMFormat docs for thread-safety guarantees. */
    private static final UCUMFormat UCUM = UCUMFormat.getInstance(CASE_SENSITIVE);

    public UnitDeserializer() {
        super(UNIT_CLASS);
    }

    @Override
    public Unit<?> deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
        JsonToken token = jsonParser.getCurrentToken();

        if (token == JsonToken.VALUE_NULL) {
            // Accept explicit JSON null as a null Unit reference
            return null;
        }

        if (token != JsonToken.VALUE_STRING) {
            throw ctx.wrongTokenException(jsonParser, Unit.class,
                    JsonToken.VALUE_STRING,
                    "Expected unit value in String format");
        }

        String text = jsonParser.getText();
        ParsePosition pos = new ParsePosition(0);
        Unit<?> unit = UCUM.parse(text, pos);

        // UCUMFormat.parse returns null on failure or may consume only part of the input.
        if (unit == null || pos.getIndex() != text.length()) {
            throw ctx.weirdStringException(text, Unit.class,
                    "Invalid UCUM unit syntax at index " + pos.getIndex());
        }

        return unit;
    }
}
