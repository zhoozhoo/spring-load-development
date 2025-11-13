package ca.zhoozhoo.loaddev.common.jackson;

import static tech.units.indriya.quantity.Quantities.getQuantity;

import java.io.IOException;
import java.math.BigDecimal;

import javax.measure.Quantity;
import javax.measure.Quantity.Scale;
import javax.measure.Unit;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import systems.uom.ucum.internal.format.TokenException;

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
 * The deserializer stops at the first validation failure and throws {@link com.fasterxml.jackson.core.JsonParseException}.
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
public class QuantityDeserializer extends StdDeserializer<Quantity<?>> {

    private static final long serialVersionUID = 1L;

    public QuantityDeserializer() {
        super(Quantity.class);
    }

    @Override
    public Quantity<?> deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        JsonNode root = jp.getCodec().readTree(jp);

        var valueNode = requireField(jp, root, "value");
        if (valueNode.isNull()) {
            throw new JsonParseException(jp, "Invalid numeric value for 'value' field: null");
        }
        var unitNode = requireField(jp, root, "unit");
        if (unitNode.isNull()) {
            throw new JsonParseException(jp, "Invalid unit value: null");
        }
        var scaleNode = root.get("scale");

        var codec = jp.getCodec();

        // Validate and convert value (narrow catch to JsonProcessingException for SpotBugs)
        BigDecimal value;
        try {
            value = codec.treeToValue(valueNode, BigDecimal.class);
        } catch (JsonProcessingException ex) {
            throw new JsonParseException(jp, "Invalid numeric value for 'value' field: " + valueNode.toString(), ex);
        }
        if (value == null) {
            throw new JsonParseException(jp, "Invalid numeric value for 'value' field: null");
        }

        // Validate and convert unit (catch specific exceptions that can arise from parsing)
        Unit<?> unit;
        try {
            unit = codec.treeToValue(unitNode, Unit.class);
        } catch (JsonProcessingException | TokenException ex) {
            throw new JsonParseException(jp, "Invalid unit value: " + unitNode.toString(), ex);
        }
        if (unit == null) {
            throw new JsonParseException(jp, "Invalid unit value: null");
        }

        // Validate and convert scale with friendly message on failure; default ABSOLUTE if absent
        Scale scale = Scale.ABSOLUTE;
        if (scaleNode != null && !scaleNode.isMissingNode()) {
            try {
                String scaleText = codec.treeToValue(scaleNode, String.class);
                if (scaleText == null) {
                    throw new JsonParseException(jp, "Invalid scale 'null'. Expected ABSOLUTE or RELATIVE");
                }
                scale = Scale.valueOf(scaleText);
            } catch (IllegalArgumentException ex) {
                throw new JsonParseException(jp, "Invalid scale '" + scaleNode.asText() + "'. Expected ABSOLUTE or RELATIVE", ex);
            }
        }

        return getQuantity(value, unit, scale);
    }

    private JsonNode requireField(JsonParser jp, JsonNode root, String fieldName) throws JsonParseException {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isMissingNode()) {
            throw new JsonParseException(jp, "%s not found for quantity type.".formatted(fieldName));
        }
        return node;
    }
}
