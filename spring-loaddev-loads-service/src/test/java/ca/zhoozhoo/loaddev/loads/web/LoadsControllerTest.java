package ca.zhoozhoo.loaddev.loads.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.model.Load;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
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
        return new Load(null, name, name + " Description", "Manufacturer", "Type", 10.0,
                "BulletManufacturer", "BulletType", 100.0, "PrimerManufacturer", "PrimerType", 0.020, 1L);
    }

    @Test
    void shouldGetAllLoads() {
        Load load1 = createLoad("Load1", 1L);
        Load load2 = createLoad("Load2", 2L);

        loadRepository.saveAll(Flux.just(load1, load2)).blockLast();

        webTestClient.get().uri("/loads")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Load1")
                .jsonPath("$[1].name").isEqualTo("Load2");
    }

    @Test
    void shouldGetLoadById() {
        Load load = createLoad("Load1", 1L);
        Load savedLoad = loadRepository.save(load).block();

        webTestClient.get().uri("/loads/{id}", savedLoad.id())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Load1");
    }

    @Test
    void shouldCreateLoad() {
        Load load = createLoad("Load1", 1L);

        webTestClient.post().uri("/loads")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(load), Load.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Load1");
    }

    @Test
    void shouldUpdateLoad() {
        Load load = createLoad("Load1", 1L);
        Load savedLoad = loadRepository.save(load).block();

        Load updatedLoad = new Load(savedLoad.id(), "UpdatedLoad", "UpdatedDescription", "UpdatedManufacturer",
                "UpdatedType", 15.0,
                "UpdatedBulletManufacturer", "UpdatedBulletType", 150.0, "UpdatedPrimerManufacturer",
                "UpdatedPrimerType", 0.025, 1L);

        webTestClient.put().uri("/loads/{id}", savedLoad.id())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updatedLoad), Load.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("UpdatedLoad");
    }

    @Test
    void shouldDeleteLoad() {
        Load load = createLoad("Load1", 1L);
        Load savedLoad = loadRepository.save(load).block();

        webTestClient.delete().uri("/loads/{id}", savedLoad.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get().uri("/loads/{id}", savedLoad.id())
                .exchange()
                .expectStatus().isNotFound();
    }
}