package ca.zhoozhoo.loaddev.common.jackson;

import javax.measure.Quantity;
import javax.measure.Unit;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson module for serialization and deserialization of JSR-385 unit-of-measure types.
 * <p>
 * Registers custom serializers/deserializers for {@link Unit} and {@link Quantity} using UCUM (Unified Code for Units of
 * Measure) formatting. Once registered, {@link ObjectMapper} can seamlessly read/write unit and quantity values in concise
 * interoperable JSON.
 * <p>
 * Registration options:
 * <ul>
 *   <li>Explicit: <pre>mapper.registerModule(new QuantityModule());</pre></li>
 *   <li>Auto-discovery: place this module on the classpath and invoke <pre>ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();</pre></li>
 *   <li>Helper: <pre>ObjectMapper mapper = QuantityModuleSupport.newObjectMapperWithQuantityModule();</pre></li>
 * </ul>
 * Module name: {@code UnitJsonSerializationModule}, version: 2.1.0
 *
 * @author Zhubin Salehi
 * @see UnitSerializer
 * @see UnitDeserializer
 * @see QuantityDeserializer
 * @see QuantityModuleSupport
 */
public class QuantityModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public QuantityModule() {
        super("UnitJsonSerializationModule", new Version(2, 1, 0, null,
                QuantityModule.class.getPackage().getName(), "uom-lib-jackson"));
        addSerializer((Class) Unit.class, new UnitSerializer());
        addDeserializer((Class) Unit.class, new UnitDeserializer());
        addDeserializer((Class) Quantity.class, new QuantityDeserializer());
    }
}
