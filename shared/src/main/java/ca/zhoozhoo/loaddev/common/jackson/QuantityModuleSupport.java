package ca.zhoozhoo.loaddev.common.jackson;

import tools.jackson.databind.json.JsonMapper;

/**
 * Small helper utilities to register or create {@link ObjectMapper}s preconfigured with
 * the {@link QuantityModule} for convenience and discoverability.
 * <p>
 * Examples:
 * <pre>
 * var mapper = QuantityModuleSupport.newObjectMapperWithQuantityModule();
 * // or rebuild an existing mapper with this module (mappers are immutable in Jackson 3)
 * mapper = QuantityModuleSupport.register(mapper);
 * // or rely on auto-discovery if the module services file is on the classpath
 * var auto = new ObjectMapper().findAndRegisterModules();
 * </pre>
 * <p>
 * Note: This library also publishes a service manifest under
 * {@code META-INF/services/tools.jackson.databind.JacksonModule} so that
 * {@link tools.jackson.databind.json.JsonMapper#findAndRegisterModules()} picks it up automatically.
 *
 * @author Zhubin Salehi
 */
public final class QuantityModuleSupport {

    private QuantityModuleSupport() {
        // utility class
    }

    /**
     * Create a new {@link JsonMapper} with the {@link QuantityModule} already registered.
     *
     * @return pre-configured JsonMapper
     */
    public static JsonMapper newObjectMapperWithQuantityModule() {
        // Jackson 3 uses a builder-based registration API; use rebuild() from a default mapper
        return new JsonMapper()
            .rebuild()
            .addModule(new QuantityModule())
            .build();
    }

    /**
     * Register the {@link QuantityModule} on an existing mapper.
     * Returns a new mapper instance with the module applied (mappers are immutable in Jackson 3).
     *
     * @param mapper the ObjectMapper to configure; if {@code null}, a new mapper is created
     * @return a mapper instance with {@link QuantityModule} registered
     */
    public static JsonMapper register(JsonMapper mapper) {
        if (mapper == null) {
            return newObjectMapperWithQuantityModule();
        }
        
        return mapper.rebuild()
                .addModule(new QuantityModule())
                .build();
    }
}
