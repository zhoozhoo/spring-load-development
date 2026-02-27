package ca.zhoozhoo.loaddev.common.jackson;

import tools.jackson.databind.json.JsonMapper;

/// Small helper utilities to register or create [ObjectMapper]s preconfigured with
/// the [QuantityModule] for convenience and discoverability.
///
/// Examples:
/// ```
/// var mapper = QuantityModuleSupport.newObjectMapperWithQuantityModule();
/// // or rebuild an existing mapper with this module (mappers are immutable in Jackson 3)
/// mapper = QuantityModuleSupport.register(mapper);
/// // or rely on auto-discovery if the module services file is on the classpath
/// var auto = new ObjectMapper().findAndRegisterModules();
/// ```
///
/// Note: This library also publishes a service manifest under
/// `META-INF/services/tools.jackson.databind.JacksonModule` so that
/// [tools.jackson.databind.json.JsonMapper#findAndRegisterModules()] picks it up automatically.
///
/// @author Zhubin Salehi
public final class QuantityModuleSupport {

    private QuantityModuleSupport() {
        // utility class
    }

    /// Create a new [JsonMapper] with the [QuantityModule] already registered.
    ///
    /// @return pre-configured JsonMapper
    public static JsonMapper newObjectMapperWithQuantityModule() {
        // Jackson 3 uses a builder-based registration API; use rebuild() from a default mapper
        return new JsonMapper()
            .rebuild()
            .addModule(new QuantityModule())
            .build();
    }

    /// Register the [QuantityModule] on an existing mapper.
    /// Returns a new mapper instance with the module applied (mappers are immutable in Jackson 3).
    ///
    /// @param mapper the ObjectMapper to configure; if `null`, a new mapper is created
    /// @return a mapper instance with [QuantityModule] registered
    public static JsonMapper register(JsonMapper mapper) {
        if (mapper == null) {
            return newObjectMapperWithQuantityModule();
        }
        
        return mapper.rebuild()
                .addModule(new QuantityModule())
                .build();
    }
}
