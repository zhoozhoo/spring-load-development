package ca.zhoozhoo.loaddev.components.config;

import java.math.BigDecimal;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.format.MeasurementParseException;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;
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
 * R2DBC converters for JSR-385 {@link Quantity} and JSR-354 {@link MonetaryAmount} types.
 * <p>
 * Provides bidirectional conversion between domain objects and PostgreSQL JSONB columns.
 * Quantity format: {@code {"value": 150, "unit": "[gr]"}}
 * MonetaryAmount format: {@code {"amount": 45.99, "currency": "USD"}}
 * </p>
 *
 * @author Zhubin Salehi
 */
public class R2dbcConverters {

    private static final UCUMFormat UCUM_FORMAT = UCUMFormat.getInstance(CASE_SENSITIVE);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
            var value = source.getValue();
            var unit = UCUM_FORMAT.format(source.getUnit());
            var json = String.format("{\"value\":%s,\"unit\":\"%s\"}", value, unit);
            return Json.of(json);
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
            var amount = source.getNumber().numberValue(BigDecimal.class);
            var currency = source.getCurrency().getCurrencyCode();
            var json = String.format("{\"amount\":%s,\"currency\":\"%s\"}", amount, currency);
            return Json.of(json);
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
                JsonNode root = OBJECT_MAPPER.readTree(source.asString());
                
                var amountNode = root.get("amount");
                var currencyNode = root.get("currency");
                
                if (amountNode == null) {
                    throw new IllegalArgumentException("Missing 'amount' field in MonetaryAmount JSON: " + source.asString());
                }
                if (currencyNode == null) {
                    throw new IllegalArgumentException("Missing 'currency' field in MonetaryAmount JSON: " + source.asString());
                }
                
                var amount = amountNode.decimalValue();
                var currencyCode = currencyNode.asText();
                
                CurrencyUnit currency = Monetary.getCurrency(currencyCode);
                return Money.of(amount, currency);
                
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Failed to parse MonetaryAmount JSON: " + source.asString(), e);
            }
        }
    }
}
