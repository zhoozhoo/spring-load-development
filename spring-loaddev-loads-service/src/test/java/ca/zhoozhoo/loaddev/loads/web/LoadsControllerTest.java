package ca.zhoozhoo.loaddev.loads.web;

import static java.util.UUID.randomUUID;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.model.Load;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
class LoadsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private LoadRepository loadRepository;

    @BeforeEach
    void setUp() {
        loadRepository.deleteAll().block();
    }

    private Load createLoad(String name, Long rifleId) {
        return new Load(null, randomUUID().toString(), name, name + " Description", "Manufacturer", "Type", 10.0,
                "BulletManufacturer", "BulletType", 100.0, "PrimerManufacturer", "PrimerType", 0.020, 1L);
    }

    @Test
    void getAllLoads() {
        loadRepository.saveAll(Flux.just(createLoad("Load1", 1L), createLoad("Load2", 2L))).blockLast();

        webTestClient.get().uri("/loads")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Load1")
                .jsonPath("$[1].name").isEqualTo("Load2");
    }

    @Test
    void getLoadById() {
        var load = loadRepository.save(createLoad("Load1", 1L)).block();

        webTestClient.get().uri("/loads/{id}", load.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Load1");
    }

    @Test
    void createLoad() {
        var load = createLoad("Load1", 1L);

        webTestClient.post().uri("/loads")
                .contentType(APPLICATION_JSON)
                .body(Mono.just(load), Load.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Load1")
                .jsonPath("$.id").exists()
                .jsonPath("$.rifleId").isEqualTo(1);
    }

    @Test
    void updateLoad() {
        var load = loadRepository.save(createLoad("Load1", 1L)).block();

        var updatedLoad = new Load(load.id(), randomUUID().toString(), "UpdatedLoad", "UpdatedDescription",
                "UpdatedManufacturer",
                "UpdatedType", 15.0,
                "UpdatedBulletManufacturer", "UpdatedBulletType", 150.0, "UpdatedPrimerManufacturer",
                "UpdatedPrimerType", 0.025, 1L);

        webTestClient.put().uri("/loads/{id}", load.id())
                .contentType(APPLICATION_JSON)
                .body(Mono.just(updatedLoad), Load.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("UpdatedLoad");
    }

    @Test
    void deleteLoad() {
        var load = loadRepository.save(createLoad("Load1", 1L)).block();

        webTestClient.delete().uri("/loads/{id}", load.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get().uri("/loads/{id}", load.id())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getNonExistentLoad() {
        webTestClient.get().uri("/loads/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createLoadWithInvalidData() {
        var invalidLoad = new Load(null, "", "", "", "", "", -1.0,
                "", "", -1.0, "", "", -1.0, null);

        webTestClient.post().uri("/loads")
                .contentType(APPLICATION_JSON)
                .body(Mono.just(invalidLoad), Load.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createLoadWithNullData() {
        var invalidLoad = new Load(null, null, null, null, null, null, null,
                null, null, null, null, null, null, null);

        webTestClient.post().uri("/loads")
                .contentType(APPLICATION_JSON)
                .body(Mono.just(invalidLoad), Load.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateNonExistentLoad() {
        var load = createLoad("TestLoad", 1L);

        webTestClient.put().uri("/loads/999")
                .contentType(APPLICATION_JSON)
                .body(Mono.just(load), Load.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateLoadWithInvalidData() {
        var load = loadRepository.save(createLoad("Load1", 1L)).block();
        var invalidLoad = new Load(load.id(), randomUUID().toString(), "", "", "", "", -1.0,
                "", "", -1.0, "", "", -1.0, null);

        webTestClient.put().uri("/loads/{id}", load.id())
                .contentType(APPLICATION_JSON)
                .body(Mono.just(invalidLoad), Load.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void deleteNonExistentLoad() {
        webTestClient.delete().uri("/loads/999")
                .exchange()
                .expectStatus().isNotFound();
    }
}