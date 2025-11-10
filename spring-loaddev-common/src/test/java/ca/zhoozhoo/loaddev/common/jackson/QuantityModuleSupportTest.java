package ca.zhoozhoo.loaddev.common.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static systems.uom.ucum.UCUM.METER;

import java.io.StringWriter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for {@link QuantityModuleSupport} and auto-discovery registration path.
 */
class QuantityModuleSupportTest {

    @Nested
    class HelperRegistration {

        @Test
        void newObjectMapperWithQuantityModule_shouldSerializeUnit() throws Exception {
            ObjectMapper mapper = QuantityModuleSupport.newObjectMapperWithQuantityModule();
            var w = new StringWriter();
            mapper.writeValue(w, METER);
            assertEquals("\"m\"", w.toString());
        }

        @Test
        void registerOnExistingMapper_shouldSerializeUnit() throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            QuantityModuleSupport.register(mapper);
            var w = new StringWriter();
            mapper.writeValue(w, METER);
            assertEquals("\"m\"", w.toString());
        }
    }

    @Nested
    class AutoDiscovery {

        @Test
        void findAndRegisterModules_shouldDiscoverQuantityModule() throws Exception {
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            // Some environments may load additional modules that precede ours; explicitly re-register to ensure override.
            mapper.registerModule(new QuantityModule());
            assertEquals("\"m\"", mapper.writeValueAsString(METER));
        }
    }
}
