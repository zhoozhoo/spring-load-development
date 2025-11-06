package ca.zhoozhoo.loaddev.loads.converter;

import javax.measure.Quantity;
import javax.measure.Unit;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class QuantityModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public QuantityModule() {
        super("UnitJsonSerializationModule", new Version(2, 1, 0, null,
                QuantityModule.class.getPackage().getName(), "uom-lib-jackson"));
        addSerializer((Class) Unit.class, new UnitSerializer());
        addDeserializer((Class) Unit.class, new UnitDeserializer());
        addDeserializer((Class) Quantity.class, new QuantityDeserializer());
    }
}
