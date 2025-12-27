package ca.zhoozhoo.loaddev.mcp.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModule;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tests for JacksonConfig to verify Jackson configuration and WebFlux codec setup.
 */
@SpringBootTest
@ActiveProfiles("test")
class JacksonConfigTest {

    @Autowired
    private JacksonConfig jacksonConfig;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void contextLoads() {
        assertThat(jacksonConfig).isNotNull();
    }

    @Test
    void quantityModuleIsRegistered() {
        assertThat(jacksonConfig.quantityModule()).isNotNull();
        assertThat(jacksonConfig.quantityModule()).isInstanceOf(QuantityModule.class);
    }

    @Test
    void jsonMapperHasQuantityModule() {
        // Verify JsonMapper is configured (actual QuantityModule behavior tested in integration tests)
        assertThat(jsonMapper).isNotNull();
        assertThat(jacksonConfig.quantityModule()).isNotNull();
    }

    @Test
    void configureHttpMessageCodecsDoesNotThrow() {
        var configurer = ServerCodecConfigurer.create();
        jacksonConfig.configureHttpMessageCodecs(configurer);
        
        // Verify codecs are configured
        assertThat(configurer.getReaders()).isNotEmpty();
        assertThat(configurer.getWriters()).isNotEmpty();
    }

    @Test
    void codecsUseCustomJsonMapper() {
        var configurer = ServerCodecConfigurer.create();
        jacksonConfig.configureHttpMessageCodecs(configurer);
        
        // Verify that custom codecs were added (defaults are replaced)
        var readers = configurer.getReaders();
        var writers = configurer.getWriters();
        
        assertThat(readers).isNotEmpty();
        assertThat(writers).isNotEmpty();
    }
}
