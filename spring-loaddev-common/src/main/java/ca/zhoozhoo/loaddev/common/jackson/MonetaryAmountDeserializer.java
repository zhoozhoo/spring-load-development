package ca.zhoozhoo.loaddev.common.jackson;

import static javax.money.Monetary.getCurrency;
import static org.javamoney.moneta.Money.of;

import javax.money.MonetaryAmount;

// (Using static import org.javamoney.moneta.Money.of)

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;


/**
 * Jackson deserializer for JSR-354 {@link MonetaryAmount} types.
 * <p>
 * Deserializes JSON objects of the form {@code {"amount": 45.99, "currency": "USD"}}
 * into {@link MonetaryAmount}.
 * </p>
 * <p>
 * Validation: both {@code amount} and {@code currency} must be present and non-null.
 * </p>
 *
 * @author Zhubin Salehi
 * @see MonetaryAmountSerializer
 */
public class MonetaryAmountDeserializer extends StdDeserializer<MonetaryAmount> {

    public MonetaryAmountDeserializer() {
        super(MonetaryAmount.class);
    }

    @Override
    public MonetaryAmount deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        JsonNode root = parser.objectReadContext().readTree(parser);

        var amountNode = root.get("amount");
        var currencyNode = root.get("currency");

        validateRequiredField(amountNode, "amount");
        validateRequiredField(currencyNode, "currency");

        return of(amountNode.decimalValue(), getCurrency(currencyNode.asString()));
    }

    private void validateRequiredField(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            throw new IllegalArgumentException(
                    "Missing required field '%s' in MonetaryAmount JSON".formatted(fieldName));
        }
    }
}
