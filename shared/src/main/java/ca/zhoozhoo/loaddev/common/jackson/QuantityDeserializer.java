package ca.zhoozhoo.loaddev.common.jackson;

import static systems.uom.ucum.format.UCUMFormat.Variant.CASE_SENSITIVE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tools.jackson.databind.exc.MismatchedInputException.from;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParsePosition;

import javax.measure.Quantity;
import javax.measure.Quantity.Scale;
import javax.measure.Unit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import systems.uom.ucum.format.UCUMFormat;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom Jackson deserializer for JSR-385 {@link Quantity} objects.
 * <p>
 * Deserializes JSON representations of quantities with the following structure:
 * <pre>
 * {
 *   "value": 26.0,
 *   "unit": "[in_i]",
 *   "scale": "ABSOLUTE"   // optional; defaults to ABSOLUTE when omitted
 * }
 * </pre>
 * Required fields:
 * <ul>
 *   <li><b>value</b> – numeric (mapped to {@link BigDecimal})</li>
 *   <li><b>unit</b> – UCUM string (see {@link UnitDeserializer})</li>
 *   <li><b>scale</b> – (optional) one of {@code ABSOLUTE} or {@code RELATIVE}; defaults to {@code ABSOLUTE} if missing</li>
 * </ul>
 * Validation & error behavior:
 * <ul>
 *   <li>Missing required field (<b>value</b>, <b>unit</b>) → {@code "<field> not found for quantity type."}</li>
 *   <li>Missing optional field (<b>scale</b>) → defaults to {@code ABSOLUTE}</li>
 *   <li>Non-numeric value → {@code Invalid numeric value for 'value' field: ...}</li>
 *   <li>Invalid unit string → {@code Invalid unit value: ...}</li>
 *   <li>Invalid scale enum → {@code Invalid scale '<value>'. Expected ABSOLUTE or RELATIVE}</li>
 * </ul>
 * The deserializer stops at the first validation failure and throws a Jackson mapping exception
 * (e.g., {@link tools.jackson.databind.exc.MismatchedInputException}).
 * <p>
 * Example usage:
 * <pre>
 * ObjectMapper mapper = QuantityModuleSupport.newObjectMapperWithQuantityModule();
 * Quantity<?> q = mapper.readValue("{\n  \"value\": 1.5, \n  \"unit\": \"m\", \n  \"scale\": \"ABSOLUTE\" }", Quantity.class);
 * </pre>
 *
 * @author Zhubin Salehi
 * @see Quantity
 * @see UnitDeserializer
 */
@SuppressFBWarnings(value = "SE_NO_SUITABLE_CONSTRUCTOR", justification = "Jackson runtime never Java-serializes deserializer instances; class implements Serializable only for framework compatibility and test expectations.")
public final class QuantityDeserializer extends StdDeserializer<Quantity<?>> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    private static final UCUMFormat UCUM = UCUMFormat.getInstance(CASE_SENSITIVE);

    public QuantityDeserializer() {
        super(Quantity.class);
    }

    @Override
    public Quantity<?> deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        JsonNode root = parser.objectReadContext().readTree(parser);

        var valueNode = requireField(parser, root, "value");
        if (valueNode.isNull() || !valueNode.isNumber()) {
            throw from(parser, Quantity.class,
                    "Invalid numeric value for 'value' field: %s".formatted(valueNode.toString()));
        }
        var unitNode = requireField(parser, root, "unit");
        if (unitNode.isNull() || !unitNode.isString()) {
            throw from(parser, Quantity.class, "Invalid unit value: %s".formatted(unitNode.toString()));
        }
        var scaleNode = root.get("scale");

        // Extract numeric value
        var value = valueNode.decimalValue();

        // Parse UCUM unit string with full-input validation
        var unitText = unitNode.asString();
        var pos = new ParsePosition(0);
        Unit<?> unit;
        try {
            unit = UCUM.parse(unitText, pos);
            if (unit == null || pos.getIndex() != unitText.length()) {
                throw from(parser, Quantity.class,
                        "Invalid unit value: %s".formatted(unitNode.toString()));
            }
        } catch (RuntimeException ex) {
            throw from(parser, Quantity.class,
                    "Invalid unit value: \"%s\"".formatted(unitText));
        }

        // Validate and convert scale with Java 21 pattern-matching switch; default ABSOLUTE if absent
        var scale = switch (scaleNode) {
            case null -> Scale.ABSOLUTE;
            case JsonNode n when n.isMissingNode() -> Scale.ABSOLUTE;
            case JsonNode n when n.isString() -> {
                try {
                    yield Scale.valueOf(n.asString());
                } catch (IllegalArgumentException ex) {
                    throw from(parser, Quantity.class,
                            ("Invalid scale '%s'. Expected ABSOLUTE or RELATIVE").formatted(n.asString()));
                }
            }
            default -> throw from(parser, Quantity.class,
                    "Invalid scale 'null'. Expected ABSOLUTE or RELATIVE");
        };

        return getQuantity(value, unit, scale);
    }

    private JsonNode requireField(JsonParser parser, JsonNode root, String fieldName) throws JacksonException {
        var node = root.get(fieldName);
        if (node == null || node.isMissingNode()) {
            throw from(parser, Quantity.class,
                    ("%s not found for quantity type.").formatted(fieldName));
        }
        return node;
    }
}
