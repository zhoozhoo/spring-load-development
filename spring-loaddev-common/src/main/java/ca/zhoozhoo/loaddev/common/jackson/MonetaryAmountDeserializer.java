package ca.zhoozhoo.loaddev.common.jackson;

import static javax.money.Monetary.getCurrency;

import java.io.IOException;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Jackson deserializer for JSR-354 {@link MonetaryAmount} types.
 * <p>
 * Deserializes JSON format: {@code {"amount": 45.99, "currency": "USD"}}
 * to {@link MonetaryAmount} objects.
 * </p>
 *
 * @author Zhubin Salehi
 */
class MonetaryAmountDeserializer extends StdDeserializer<MonetaryAmount> {

    private static final long serialVersionUID = 1L;

    public MonetaryAmountDeserializer() {
        super(MonetaryAmount.class);
    }

    @Override
    public MonetaryAmount deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        var codec = parser.getCodec();
        JsonNode root = codec.readTree(parser);
        
        var amountNode = root.get("amount");
        var currencyNode = root.get("currency");
        
        validateRequiredField(amountNode, "amount");
        validateRequiredField(currencyNode, "currency");
        
        return Money.of(amountNode.decimalValue(), getCurrency(currencyNode.asText()));
    }

    private void validateRequiredField(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            throw new IllegalArgumentException(
                String.format("Missing required field '%s' in MonetaryAmount JSON", fieldName));
        }
    }
}
