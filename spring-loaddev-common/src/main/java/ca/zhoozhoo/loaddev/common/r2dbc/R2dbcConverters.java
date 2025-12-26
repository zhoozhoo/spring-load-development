package ca.zhoozhoo.loaddev.common.r2dbc;

import static systems.uom.ucum.format.UCUMFormat.Variant.CASE_SENSITIVE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.measure.Quantity;
import javax.money.MonetaryAmount;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModuleSupport;
import io.r2dbc.postgresql.codec.Json;
import systems.uom.ucum.format.UCUMFormat;

/**
 * R2DBC converters for JSR-385 {@link Quantity} and JSR-354 {@link MonetaryAmount} types.
 * <p>
 * Quantity format: {@code {"value": 26.0, "unit": "[in_i]", "scale": "ABSOLUTE"}}<br>
 * MonetaryAmount format: {@code {"amount": 45.99, "currency": "USD"}}
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
        converters.add(new RiflingToJsonConverter());
        converters.add(new JsonToRiflingConverter());
        converters.add(new ZeroingToJsonConverter());
        converters.add(new JsonToZeroingConverter());

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

    /**
     * Converts {@link MonetaryAmount} to PostgreSQL JSONB.
     * Example: {@code {"amount": 45.99, "currency": "USD"}}
     */
    @WritingConverter
    public static class MonetaryAmountToJsonConverter implements Converter<MonetaryAmount, Json> {

        @Override
        public Json convert(@NonNull MonetaryAmount source) {
            try {
                return Json.of(OBJECT_MAPPER.writeValueAsString(source));
            } catch (JacksonException e) {
                throw new IllegalArgumentException("Failed to write MonetaryAmount JSON", e);
            }
        }
    }

    /**
     * Converts PostgreSQL JSONB to {@link MonetaryAmount}.
     * Example: {@code {"amount": 45.99, "currency": "USD"}}
     */
    @ReadingConverter
    public static class JsonToMonetaryAmountConverter implements Converter<Json, MonetaryAmount> {

        @Override
        public MonetaryAmount convert(@NonNull Json source) {
            try {
                return OBJECT_MAPPER.readValue(source.asString(), MonetaryAmount.class);
            } catch (JacksonException e) {
                throw new IllegalArgumentException("Failed to parse MonetaryAmount JSON: " + source.asString(), e);
            }
        }
    }

    /**
     * Converts {@code Rifling} to PostgreSQL JSONB.
     * Example: {@code {"twistRate": {"value": 16.0, "unit": "[in_i]", "scale": "ABSOLUTE"}, "twistDirection": "RIGHT"}}
     * Note: Uses GenericConverter to explicitly declare supported types and avoid circular dependency.
     */
    @WritingConverter
    public static class RiflingToJsonConverter implements GenericConverter {

        private static final String RIFLING_CLASS_NAME = "ca.zhoozhoo.loaddev.rifles.model.Rifling";

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            try {
                Class<?> riflingClass = Class.forName(RIFLING_CLASS_NAME);
                Set<ConvertiblePair> pairs = new HashSet<>();
                pairs.add(new ConvertiblePair(riflingClass, Json.class));
                return pairs;
            } catch (ClassNotFoundException e) {
                // Rifling class not available in this module - return empty set
                return Set.of();
            }
        }

        @Override
        public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
            if (source == null) {
                return null;
            }
            try {
                return Json.of(OBJECT_MAPPER.writeValueAsString(source));
            } catch (JacksonException e) {
                throw new IllegalArgumentException("Failed to write Rifling JSON", e);
            }
        }
    }

    /**
     * Converts PostgreSQL JSONB to {@code Rifling}.
     * Example: {@code {"twistRate": {"value": 16.0, "unit": "[in_i]", "scale": "ABSOLUTE"}, "twistDirection": "RIGHT"}}
     */
    @ReadingConverter
    public static class JsonToRiflingConverter implements GenericConverter {

        private static final String RIFLING_CLASS_NAME = "ca.zhoozhoo.loaddev.rifles.model.Rifling";

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            try {
                Class<?> riflingClass = Class.forName(RIFLING_CLASS_NAME);
                Set<ConvertiblePair> pairs = new HashSet<>();
                pairs.add(new ConvertiblePair(Json.class, riflingClass));
                return pairs;
            } catch (ClassNotFoundException e) {
                // Rifling class not available in this module - return empty set
                return Set.of();
            }
        }

        @Override
        public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
            if (source == null) {
                return null;
            }
            if (!(source instanceof Json json)) {
                throw new IllegalArgumentException("Source must be Json type");
            }
            try {
                return OBJECT_MAPPER.readValue(json.asString(), Class.forName(RIFLING_CLASS_NAME));
            } catch (JacksonException | ClassNotFoundException e) {
                throw new IllegalArgumentException("Failed to parse Rifling JSON: " + json.asString(), e);
            }
        }
    }

    /**
     * Converts {@code Zeroing} to PostgreSQL JSONB.
     * Example: {@code {"sightHeight": {"value": 1.5, "unit": "[in_i]", "scale": "ABSOLUTE"}, "zeroDistance": {"value": 100.0, "unit": "[in_i]", "scale": "ABSOLUTE"}}}
     * Note: Uses GenericConverter to explicitly declare supported types and avoid circular dependency.
     */
    @WritingConverter
    public static class ZeroingToJsonConverter implements GenericConverter {

        private static final String ZEROING_CLASS_NAME = "ca.zhoozhoo.loaddev.rifles.model.Zeroing";

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            try {
                Class<?> zeroingClass = Class.forName(ZEROING_CLASS_NAME);
                Set<ConvertiblePair> pairs = new HashSet<>();
                pairs.add(new ConvertiblePair(zeroingClass, Json.class));
                return pairs;
            } catch (ClassNotFoundException e) {
                // Zeroing class not available in this module - return empty set
                return Set.of();
            }
        }

        @Override
        public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
            if (source == null) {
                return null;
            }
            try {
                return Json.of(OBJECT_MAPPER.writeValueAsString(source));
            } catch (JacksonException e) {
                throw new IllegalArgumentException("Failed to write Zeroing JSON", e);
            }
        }
    }

    /**
     * Converts PostgreSQL JSONB to {@code Zeroing}.
     * Example: {@code {"sightHeight": {"value": 1.5, "unit": "[in_i]", "scale": "ABSOLUTE"}, "zeroDistance": {"value": 100.0, "unit": "[in_i]", "scale": "ABSOLUTE"}}}
     */
    @ReadingConverter
    public static class JsonToZeroingConverter implements GenericConverter {

        private static final String ZEROING_CLASS_NAME = "ca.zhoozhoo.loaddev.rifles.model.Zeroing";

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            try {
                Class<?> zeroingClass = Class.forName(ZEROING_CLASS_NAME);
                Set<ConvertiblePair> pairs = new HashSet<>();
                pairs.add(new ConvertiblePair(Json.class, zeroingClass));
                return pairs;
            } catch (ClassNotFoundException e) {
                // Zeroing class not available in this module - return empty set
                return Set.of();
            }
        }

        @Override
        public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
            if (source == null) {
                return null;
            }
            if (!(source instanceof Json json)) {
                throw new IllegalArgumentException("Source must be Json type");
            }
            try {
                return OBJECT_MAPPER.readValue(json.asString(), Class.forName(ZEROING_CLASS_NAME));
            } catch (JacksonException | ClassNotFoundException e) {
                throw new IllegalArgumentException("Failed to parse Zeroing JSON: " + json.asString(), e);
            }
        }
    }
}
