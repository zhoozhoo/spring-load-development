package ca.zhoozhoo.loaddev.rifles.config;

import static systems.uom.ucum.format.UCUMFormat.Variant.CASE_SENSITIVE;

import java.util.ArrayList;
import java.util.List;

import javax.measure.Quantity;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModuleSupport;
import io.r2dbc.postgresql.codec.Json;
import systems.uom.ucum.format.UCUMFormat;

/**
 * R2DBC converters for JSR-385 {@link Quantity} types.
 * <p>
 * Converts between Quantity objects and PostgreSQL JSONB format:
 * {@code {"value": 26.0, "unit": "[in_i]", "scale": "ABSOLUTE"}}
 *
 * @author Zhubin Salehi
 */
public class R2dbcConverters {

    private static final UCUMFormat UCUM_FORMAT = UCUMFormat.getInstance(CASE_SENSITIVE);
    private static final ObjectMapper OBJECT_MAPPER = QuantityModuleSupport.newObjectMapperWithQuantityModule();

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
     * Converts {@link Quantity} to PostgreSQL JSONB.
     * Example: {@code {"value": 26.0, "unit": "[in_i]", "scale": "ABSOLUTE"}}
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
     * Converts PostgreSQL JSONB to {@link Quantity}.
     * Example: {@code {"value": 26.0, "unit": "[in_i]"}}
     */
    @ReadingConverter
    public static class JsonToQuantityConverter implements Converter<Json, Quantity<?>> {

        @Override
        public Quantity<?> convert(@NonNull Json source) {
            try {
                return OBJECT_MAPPER.readValue(source.asString(), Quantity.class);
            } catch (JacksonException e) {
                throw new IllegalArgumentException("Failed to parse Quantity JSON: " + source.asString(), e);
            }
        }
    }
}
