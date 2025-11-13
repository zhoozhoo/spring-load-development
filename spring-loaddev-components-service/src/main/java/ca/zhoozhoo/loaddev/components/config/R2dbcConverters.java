package ca.zhoozhoo.loaddev.components.config;

import static systems.uom.ucum.format.UCUMFormat.Variant.CASE_SENSITIVE;

import java.util.ArrayList;
import java.util.List;

import javax.measure.Quantity;
import javax.money.MonetaryAmount;

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
 * R2DBC converters for JSR-385 {@link Quantity} and JSR-354 {@link MonetaryAmount} types.
 * <p>
 * Provides bidirectional conversion between domain objects and PostgreSQL JSONB columns.
 * Quantity format (writer): {@code {"value": 150, "unit": "[gr]", "scale": "ABSOLUTE"}}
 * MonetaryAmount format: {@code {"amount": 45.99, "currency": "USD"}}
 * </p>
 *
 * @author Zhubin Salehi
 */
public class R2dbcConverters {

    private static final UCUMFormat UCUM_FORMAT = UCUMFormat.getInstance(CASE_SENSITIVE);
    private static final ObjectMapper OBJECT_MAPPER = QuantityModuleSupport.newObjectMapperWithQuantityModule();

    /**
     * Provides all R2DBC converters for JSR-385 and JSR-354 types.
     *
     * @return list of converters for reading and writing Quantity and MonetaryAmount objects
     */
    public static List<Object> getConverters() {
        var converters = new ArrayList<>();
        converters.add(new QuantityToJsonConverter());
        converters.add(new JsonToQuantityConverter());
        converters.add(new MonetaryAmountToJsonConverter());
        converters.add(new JsonToMonetaryAmountConverter());

        return converters;
    }

    /**
     * Converts a {@link Quantity} to PostgreSQL JSONB format.
     * <p>
     * Example output: {@code {"value": 150, "unit": "[gr]"}}
     * </p>
     */
    @WritingConverter
    public static class QuantityToJsonConverter implements Converter<Quantity<?>, Json> {

        @Override
        public Json convert(@NonNull Quantity<?> source) {
            return Json.of("{\"value\":%s,\"unit\":\"%s\",\"scale\":\"%s\"}".formatted(source.getValue(),
                    UCUM_FORMAT.format(source.getUnit()), source.getScale()));
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

    /**
     * Converts a {@link MonetaryAmount} to PostgreSQL JSONB format.
     * <p>
     * Example output: {@code {"amount": 45.99, "currency": "USD"}}
     * </p>
     */
    @WritingConverter
    public static class MonetaryAmountToJsonConverter implements Converter<MonetaryAmount, Json> {

        @Override
        public Json convert(@NonNull MonetaryAmount source) {
            try {
                return Json.of(OBJECT_MAPPER.writeValueAsString(source));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Failed to write MonetaryAmount JSON", e);
            }
        }
    }

    /**
     * Converts PostgreSQL JSONB to a {@link MonetaryAmount} object.
     * <p>
     * Example input: {@code {"amount": 45.99, "currency": "USD"}}
     * </p>
     */
    @ReadingConverter
    public static class JsonToMonetaryAmountConverter implements Converter<Json, MonetaryAmount> {

        @Override
        public MonetaryAmount convert(@NonNull Json source) {
            try {
                return OBJECT_MAPPER.readValue(source.asString(), MonetaryAmount.class);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Failed to parse MonetaryAmount JSON: %s".formatted(source.asString()) , e);
            }
        }
    }
}
