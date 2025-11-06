package ca.zhoozhoo.loaddev.components.converter;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.money.MonetaryAmount;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson module for JSR-385 Unit/Quantity and JSR-354 MonetaryAmount serialization.
 * <p>
 * Registers custom serializers and deserializers for Units of Measurement (JSR-385)
 * and Money and Currency (JSR-354) types, enabling JSON serialization/deserialization.
 * </p>
 *
 * @author Zhubin Salehi
 */
public class Jsr385Jsr354Module extends SimpleModule {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public Jsr385Jsr354Module() {
        super("Jsr385Jsr354Module", new Version(1, 0, 0, null,
                Jsr385Jsr354Module.class.getPackage().getName(), "spring-loaddev-components"));
        // JSR-385 converters
        addSerializer((Class) Unit.class, new UnitSerializer());
        addDeserializer((Class) Unit.class, new UnitDeserializer());
        addDeserializer((Class) Quantity.class, new QuantityDeserializer());
        // JSR-354 converters
        addSerializer((Class) MonetaryAmount.class, new MonetaryAmountSerializer());
        addDeserializer((Class) MonetaryAmount.class, new MonetaryAmountDeserializer());
    }
}
