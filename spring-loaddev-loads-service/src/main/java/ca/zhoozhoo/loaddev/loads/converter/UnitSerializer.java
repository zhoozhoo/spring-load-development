package ca.zhoozhoo.loaddev.loads.converter;

import static systems.uom.ucum.format.UCUMFormat.Variant.CASE_SENSITIVE;

import java.io.IOException;

import javax.measure.Unit;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import systems.uom.ucum.format.UCUMFormat;

class UnitSerializer extends StdScalarSerializer<Unit<?>> {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    private static final Class<Unit<?>> UNIT_CLASS = (Class<Unit<?>>) (Class<?>) Unit.class;

    protected UnitSerializer() {
        super(UNIT_CLASS);
    }

    @Override
    public void serialize(Unit<?> unit, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (unit == null) {
            jgen.writeNull();
            return;
        }

        jgen.writeString(UCUMFormat.getInstance(CASE_SENSITIVE)
                .format(unit, new StringBuilder())
                .toString());
    }
}
