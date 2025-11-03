package ca.zhoozhoo.loaddev.loads.config;

import java.util.ArrayList;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.format.MeasurementParseException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

import io.r2dbc.postgresql.codec.Json;
import tech.units.indriya.format.SimpleQuantityFormat;

/**
 * R2DBC converters for {@link Quantity} types.
 * <p>
 * Provides bidirectional conversion between {@link Quantity} objects and PostgreSQL JSONB columns.
 * The JSON format stores the quantity as a string with value and unit (e.g., "150 gr").
 * </p>
 * <p>
 * Example JSON format: {@code {"quantity": "150 gr"}}
 * </p>
 *
 * @author Zhubin Salehi
 */
public class QuantityConverters {

    private static final SimpleQuantityFormat QUANTITY_FORMAT = SimpleQuantityFormat.getInstance();

    /**
     * Provides all quantity converters for R2DBC configuration.
     *
     * @return list of converters for reading and writing Quantity objects
     */
    public static List<Object> getConverters() {
        List<Object> converters = new ArrayList<>();
        converters.add(new QuantityToJsonConverter());
        converters.add(new JsonToQuantityConverter());
        return converters;
    }

    /**
     * Converts a {@link Quantity} to PostgreSQL JSONB format for database storage.
     * <p>
     * The Quantity is serialized to a simple JSON object containing the formatted
     * quantity string (e.g., {"quantity": "150 gr"}).
     * </p>
     */
    @WritingConverter
    public static class QuantityToJsonConverter implements Converter<Quantity<?>, Json> {

        @Override
        public Json convert(@NonNull Quantity<?> source) {
            String quantityStr = QUANTITY_FORMAT.format(source);
            String json = String.format("{\"quantity\":\"%s\"}", quantityStr);
            return Json.of(json);
        }
    }

    /**
     * Converts PostgreSQL JSONB to a {@link Quantity} object.
     * <p>
     * Deserializes the JSON containing the formatted quantity string (e.g., {"quantity": "150 gr"})
     * and parses it back into a Quantity object using {@link SimpleQuantityFormat}.
     * </p>
     */
    @ReadingConverter
    public static class JsonToQuantityConverter implements Converter<Json, Quantity<?>> {

        @Override
        public Quantity<?> convert(@NonNull Json source) {
            try {
                String json = source.asString();
                // Extract the quantity string from JSON (e.g., {"quantity": "150 gr"})
                String quantityString = extractQuantityString(json);
                return QUANTITY_FORMAT.parse(quantityString);
            } catch (MeasurementParseException e) {
                throw new IllegalArgumentException(
                    "Failed to deserialize JSON to Quantity: " + source.asString(), e);
            }
        }

        /**
         * Extracts a quantity string from JSON for parsing.
         */
        private String extractQuantityString(String json) {
            // Simple JSON parsing: extract value between "quantity":" or "quantity": " and closing quote
            int start = json.indexOf("\"quantity\":");
            if (start == -1) {
                throw new IllegalArgumentException("Invalid Quantity JSON format (missing 'quantity' field): " + json);
            }
            // Skip past "quantity": and any whitespace before the opening quote
            start += 11; // length of "quantity":
            // Skip whitespace
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }
            // Skip opening quote
            if (start < json.length() && json.charAt(start) == '"') {
                start++;
            }
            int end = json.indexOf("\"", start);
            if (end == -1) {
                throw new IllegalArgumentException("Invalid Quantity JSON format (malformed): " + json);
            }
            return json.substring(start, end);
        }
    }
}
