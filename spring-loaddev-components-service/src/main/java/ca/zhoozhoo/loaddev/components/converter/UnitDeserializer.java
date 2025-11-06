package ca.zhoozhoo.loaddev.components.converter;

import static systems.uom.ucum.format.UCUMFormat.Variant.CASE_SENSITIVE;

import java.io.IOException;
import java.text.ParsePosition;

import javax.measure.Unit;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import systems.uom.ucum.format.UCUMFormat;

class UnitDeserializer extends StdScalarDeserializer<Unit<?>> {

    private static final long serialVersionUID = 1L;

    protected UnitDeserializer() {
        super(Unit.class);
    }

    @Override
    public Unit<?> deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
        if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING) {
            return UCUMFormat.getInstance(CASE_SENSITIVE)
                    .parse(jsonParser.getText(), new ParsePosition(0));
        }

        throw ctx.wrongTokenException(jsonParser, Unit.class,
                JsonToken.VALUE_STRING,
                "Expected unit value in String format");
    }
}
