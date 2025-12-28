package ca.zhoozhoo.loaddev.common.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static systems.uom.ucum.UCUM.METER;

import java.io.StringWriter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

/**
 * Tests for {@link QuantityModuleSupport} and auto-discovery registration path.
 */
class QuantityModuleSupportTest {

    @Nested
    class HelperRegistration {

        @Test
        void newObjectMapperWithQuantityModule_shouldSerializeUnit() throws Exception {
            var w = new StringWriter();
            QuantityModuleSupport.newObjectMapperWithQuantityModule().writeValue(w, METER);
            assertEquals("\"m\"", w.toString());
        }

        @Test
        void registerOnExistingMapper_shouldSerializeUnit() throws Exception {
            var mapper = QuantityModuleSupport.register(new JsonMapper());
            var w = new StringWriter();
            mapper.writeValue(w, METER);
            assertEquals("\"m\"", w.toString());
        }

        @Test
        void register_withNullMapper_shouldBeNoOp() {
            // Ensures the false branch in QuantityModuleSupport.register is covered
            QuantityModuleSupport.register(null);
        }
    }

    @Nested
    class AutoDiscovery {

        @Test
        void findAndRegisterModules_shouldDiscoverQuantityModule() throws Exception {
                var mapper = new JsonMapper()
                    .rebuild()
                    .findAndAddModules()
                    .build();
            // Some environments may load additional modules that precede ours; explicitly re-register to ensure override.
            mapper = mapper.rebuild().addModule(new QuantityModule()).build();
            assertEquals("\"m\"", mapper.writeValueAsString(METER));
        }
    }
}
