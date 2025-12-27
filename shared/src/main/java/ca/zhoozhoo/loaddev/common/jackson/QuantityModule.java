package ca.zhoozhoo.loaddev.common.jackson;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.money.MonetaryAmount;

import tools.jackson.core.Version;
import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson module for serialization and deserialization of JSR-385 unit-of-measure types.
 * <p>
 * Registers custom serializers/deserializers for {@link Unit} and {@link Quantity} using UCUM (Unified Code for Units of
 * Measure) formatting. Once registered, {@link tools.jackson.databind.ObjectMapper} can read/write unit and quantity
 * values in concise interoperable JSON.
 * <p>
 * Registration options:
 * <ul>
 *   <li>Explicit (builder): <pre>{@code var mapper = new ObjectMapper().rebuild().addModule(new QuantityModule()).build();}</pre></li>
 *   <li>Auto-discovery: place this module on the classpath and invoke <pre>{@code var mapper = new ObjectMapper().findAndRegisterModules();}</pre></li>
 *   <li>Helper: <pre>{@code var mapper = QuantityModuleSupport.newObjectMapperWithQuantityModule();}</pre></li>
 * </ul>
 * Module name: {@code UnitJsonSerializationModule}, version: 2.1.0
 *
 * @author Zhubin Salehi
 * @see UnitSerializer
 * @see UnitDeserializer
 * @see QuantityDeserializer
 * @see QuantityModuleSupport
 */
public final class QuantityModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public QuantityModule() {
        super("UnitJsonSerializationModule", new Version(2, 1, 0, null,
                QuantityModule.class.getPackage().getName(), "uom-lib-jackson"));
        addSerializer((Class) Unit.class, new UnitSerializer());
        addDeserializer((Class) Unit.class, new UnitDeserializer());
        addDeserializer((Class) Quantity.class, new QuantityDeserializer());
        addSerializer((Class) Quantity.class, new QuantitySerializer());
        // MonetaryAmount (JSR-354) support
        addSerializer((Class) MonetaryAmount.class, new MonetaryAmountSerializer());
        addDeserializer((Class) MonetaryAmount.class, new MonetaryAmountDeserializer());
    }
}
