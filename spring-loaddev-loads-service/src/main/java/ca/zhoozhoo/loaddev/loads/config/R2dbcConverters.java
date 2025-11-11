package ca.zhoozhoo.loaddev.loads.config;

import static systems.uom.ucum.format.UCUMFormat.Variant.CASE_SENSITIVE;

import java.util.ArrayList;
import java.util.List;

import javax.measure.Quantity;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModuleSupport;
import io.r2dbc.postgresql.codec.Json;
import systems.uom.ucum.format.UCUMFormat;

/**
 * R2DBC converters for {@link Quantity} types.
 * <p>
 * Provides bidirectional conversion between {@link Quantity} objects and PostgreSQL JSONB columns.
 * Standard JSON format (writer) stores the quantity fields explicitly, e.g., {"value":150,"unit":"[gr]","scale":"ABSOLUTE"}.
 * Backward compatibility: legacy records shaped as {"quantity":"150 gr"} are still readable via a fallback parser.
 * </p>
 * <p>
 * Example JSON format (new): {@code {"value": 150, "unit": "[gr]", "scale": "ABSOLUTE"}}
 * Example JSON format (legacy): {@code {"quantity": "150 gr"}}
 * </p>
 *
 * @author Zhubin Salehi
 */
public class R2dbcConverters {

    private static final UCUMFormat UCUM_FORMAT = UCUMFormat.getInstance(CASE_SENSITIVE);
    private static final ObjectMapper OBJECT_MAPPER = QuantityModuleSupport.newObjectMapperWithQuantityModule();

    /**
     * Provides all quantity converters for R2DBC configuration.
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
            return Json.of("{\"value\":%s,\"unit\":\"%s\",\"scale\":\"%s\"}".formatted(
                    source.getValue(), UCUM_FORMAT.format(source.getUnit()), source.getScale()));
        }
    }

    /**
     * Converts PostgreSQL JSONB to a {@link Quantity} object.
     * <p>
     * Example input: {@code {"value": 150, "unit": "[gr]"}}
     * </p>
     */
    @ReadingConverter
    public static class JsonToQuantityConverter implements Converter<Json, Quantity<?>> {

        @Override
        public Quantity<?> convert(@NonNull Json source) {
            try {
                return OBJECT_MAPPER.readValue(source.asString(), Quantity.class);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Failed to parse Quantity JSON: " + source.asString(), e);
            }
        }
    }
}
