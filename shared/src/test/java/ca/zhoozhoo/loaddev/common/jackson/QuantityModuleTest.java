package ca.zhoozhoo.loaddev.common.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for QuantityModule.
 * 
 * @author Zhubin Salehi
 */
class QuantityModuleTest {

    @Nested
    class ModuleMetadata {

        @Test
        void constructor_shouldCreateModuleWithCorrectName() {
            assertEquals("UnitJsonSerializationModule", new QuantityModule().getModuleName());
        }

        @Test
        void constructor_shouldCreateModuleWithCorrectVersion() {
            var version = new QuantityModule().version();
            
            assertEquals(2, version.getMajorVersion());
            assertEquals(1, version.getMinorVersion());
            assertEquals(0, version.getPatchLevel());
        }

        @Test
        void module_shouldHaveCorrectPackageGroup() {
            assertEquals("ca.zhoozhoo.loaddev.common.jackson", 
                    new QuantityModule().version().getGroupId());
        }

        @Test
        void module_shouldHaveCorrectArtifactId() {
            assertEquals("uom-lib-jackson", new QuantityModule().version().getArtifactId());
        }
    }

    @Nested
    class TypeVerification {

        @Test
        void module_shouldBeSerializable() {
            assertInstanceOf(java.io.Serializable.class, new QuantityModule());
        }
    }
}
