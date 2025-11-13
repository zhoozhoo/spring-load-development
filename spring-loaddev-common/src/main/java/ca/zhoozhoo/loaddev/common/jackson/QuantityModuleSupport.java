package ca.zhoozhoo.loaddev.common.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Small helper utilities to register or create ObjectMappers preconfigured with
 * the {@link QuantityModule} for convenience and discoverability.
 * <p>
 * Examples:
 * <pre>
 * ObjectMapper mapper = QuantityModuleSupport.newObjectMapperWithQuantityModule();
 * // or
 * QuantityModuleSupport.register(mapper);
 * // or rely on auto-discovery if the module services file is on the classpath
 * ObjectMapper auto = new ObjectMapper().findAndRegisterModules();
 * </pre>
 * <p>
 * Note: This library also publishes a service manifest under
 * {@code META-INF/services/com.fasterxml.jackson.databind.Module} so that
 * {@link com.fasterxml.jackson.databind.ObjectMapper#findAndRegisterModules()} picks it up automatically.
 *
 * @author Zhubin Salehi
 */
public final class QuantityModuleSupport {

    private QuantityModuleSupport() {
        // utility class
    }

    /**
     * Create a new {@link ObjectMapper} with the {@link QuantityModule} already registered.
     *
     * @return pre-configured ObjectMapper
     */
    public static ObjectMapper newObjectMapperWithQuantityModule() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new QuantityModule());
        return mapper;
    }

    /**
     * Register the {@link QuantityModule} on an existing mapper.
     *
     * @param mapper the ObjectMapper to configure (no-op if null)
     */
    public static void register(ObjectMapper mapper) {
        if (mapper != null) {
            mapper.registerModule(new QuantityModule());
        }
    }
}
