package ca.zhoozhoo.loaddev.loads.converter;

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

    @SuppressWarnings("unchecked")
    private static final Class<Unit<?>> UNIT_CLASS = (Class<Unit<?>>) (Class<?>) Unit.class;

    protected UnitDeserializer() {
        super(UNIT_CLASS);
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
