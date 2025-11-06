package ca.zhoozhoo.loaddev.components.converter;

import static tech.units.indriya.quantity.Quantities.getQuantity;

import java.io.IOException;
import java.math.BigDecimal;

import javax.measure.Quantity;
import javax.measure.Quantity.Scale;
import javax.measure.Unit;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class QuantityDeserializer extends StdDeserializer<Quantity<?>> {

    private static final long serialVersionUID = 1L;

    public QuantityDeserializer() {
        super(Quantity.class);
    }

    @Override
    public Quantity<?> deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        var root = jp.readValueAsTree();

        validateRequiredField(jp, root, "value");
        validateRequiredField(jp, root, "unit");
        validateRequiredField(jp, root, "scale");

        var codec = jp.getCodec();
        var value = codec.treeToValue(root.get("value"), BigDecimal.class);
        Unit<?> unit = codec.treeToValue(root.get("unit"), Unit.class);
        var scale = Scale.valueOf(codec.treeToValue(root.get("scale"), String.class));

        return getQuantity(value, unit, scale);
    }

    private void validateRequiredField(JsonParser jp, TreeNode root, String fieldName) throws JsonParseException {
        if (root.get(fieldName) == null) {
            throw new JsonParseException(jp, "%s not found for quantity type.".formatted(fieldName));
        }
    }
}
