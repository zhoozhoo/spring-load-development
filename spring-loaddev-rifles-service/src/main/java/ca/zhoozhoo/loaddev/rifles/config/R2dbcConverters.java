package ca.zhoozhoo.loaddev.rifles.config;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.format.MeasurementParseException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.r2dbc.postgresql.codec.Json;
import systems.uom.ucum.format.UCUMFormat;
import tech.units.indriya.quantity.Quantities;

import static systems.uom.ucum.format.UCUMFormat.Variant.CASE_SENSITIVE;

/**
 * R2DBC converters for JSR-385 {@link Quantity} types.
 * <p>
 * Provides bidirectional conversion between domain objects and PostgreSQL JSONB columns.
 * Quantity format: {@code {"value": 26.0, "unit": "[in_i]"}}
 * </p>
 *
 * @author Zhubin Salehi
 */
public class R2dbcConverters {

    private static final UCUMFormat UCUM_FORMAT = UCUMFormat.getInstance(CASE_SENSITIVE);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Provides all R2DBC converters for JSR-385 types.
     *
     * @return list of converters for reading and writing Quantity objects
     */
    public static List<Object> getConverters() {
        var converters = new ArrayList<>();
        converters.add(new QuantityToJsonConverter());
        converters.add(new JsonToQuantityConverter());
        return converters;
    }

    /**
     * Converts a {@link Quantity} to PostgreSQL JSONB format.
     * <p>
     * Example output: {@code {"value": 26.0, "unit": "[in_i]"}}
     * </p>
     */
    @WritingConverter
    public static class QuantityToJsonConverter implements Converter<Quantity<?>, Json> {

        @Override
        public Json convert(@NonNull Quantity<?> source) {
            var value = source.getValue();
            var unit = UCUM_FORMAT.format(source.getUnit());
            var json = String.format("{\"value\":%s,\"unit\":\"%s\"}", value, unit);
            return Json.of(json);
        }
    }

    /**
     * Converts PostgreSQL JSONB to a {@link Quantity} object.
     * <p>
     * Example input: {@code {"value": 26.0, "unit": "[in_i]"}}
     * </p>
     */
    @ReadingConverter
    public static class JsonToQuantityConverter implements Converter<Json, Quantity<?>> {

        @Override
        public Quantity<?> convert(@NonNull Json source) {
            try {
                JsonNode root = OBJECT_MAPPER.readTree(source.asString());
                
                var valueNode = root.get("value");
                var unitNode = root.get("unit");
                
                if (valueNode == null) {
                    throw new IllegalArgumentException("Missing 'value' field in Quantity JSON: " + source.asString());
                }
                if (unitNode == null) {
                    throw new IllegalArgumentException("Missing 'unit' field in Quantity JSON: " + source.asString());
                }
                
                var value = valueNode.decimalValue();
                var unitString = unitNode.asText();
                
                Unit<?> unit = UCUM_FORMAT.parse(unitString, new ParsePosition(0));
                if (unit == null) {
                    throw new IllegalArgumentException("Failed to parse unit: " + unitString);
                }
                
                return Quantities.getQuantity(value, unit);
                
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Failed to parse Quantity JSON: " + source.asString(), e);
            } catch (MeasurementParseException e) {
                throw new IllegalArgumentException("Failed to parse unit in Quantity JSON: " + source.asString(), e);
            }
        }
    }
}
