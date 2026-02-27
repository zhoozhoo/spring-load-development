package ca.zhoozhoo.loaddev.common.jackson;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.money.MonetaryAmount;

import tools.jackson.core.Version;
import tools.jackson.databind.module.SimpleModule;

/// Jackson module for serialization and deserialization of JSR-385 unit-of-measure types.
///
/// Registers custom serializers/deserializers for [Unit] and [Quantity] using UCUM (Unified Code for Units of
/// Measure) formatting. Once registered, [tools.jackson.databind.ObjectMapper] can read/write unit and quantity
/// values in concise interoperable JSON.
///
/// Registration options:
///
/// - Explicit (builder): ```{@code var mapper = new ObjectMapper().rebuild().addModule(new QuantityModule()).build();```
/// - Auto-discovery: place this module on the classpath and invoke ```{@code var mapper = new ObjectMapper().findAndRegisterModules();```
/// - Helper: ```{@code var mapper = QuantityModuleSupport.newObjectMapperWithQuantityModule();```
///
/// Module name: `UnitJsonSerializationModule`, version: 2.1.0
///
/// @author Zhubin Salehi
/// @see UnitSerializer
/// @see UnitDeserializer
/// @see QuantityDeserializer
/// @see QuantityModuleSupport
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
