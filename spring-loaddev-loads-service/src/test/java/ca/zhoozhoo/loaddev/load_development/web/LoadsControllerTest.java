package ca.zhoozhoo.loaddev.load_development.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.load_development.dao.LoadRepository;
import ca.zhoozhoo.loaddev.load_development.dao.RifleRepository;
import ca.zhoozhoo.loaddev.load_development.model.Load;
import ca.zhoozhoo.loaddev.load_development.model.Rifle;
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

    @Autowired
    private RifleRepository rifleRepository;

    @BeforeEach
    void setUp() {
        loadRepository.deleteAll().block();
        rifleRepository.deleteAll().block();
    }

    private Rifle createRifle(String name) {
        Rifle rifle = new Rifle(null, name, name + " Rifle", "223 Rem.", 24.0, "Medium", "1:8", 0.068, "4");
        return rifleRepository.save(rifle).block();
    }

    private Load createLoad(String name, Long rifleId) {
        return new Load(null, name, name + " Description", "Manufacturer", "Type", 10.0,
                "BulletManufacturer", "BulletType", 100.0, "PrimerManufacturer", "PrimerType", 0.020, rifleId);
    }

    @Test
    void shouldGetAllLoads() {
        Rifle savedRifle1 = createRifle("Tikka T3X Tact A1 223 Rem.");
        Rifle savedRifle2 = createRifle("Tikka T3X CTR 223 Rem.");

        Load load1 = createLoad("Load1", savedRifle1.id());
        Load load2 = createLoad("Load2", savedRifle2.id());

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
        Rifle savedRifle = createRifle("Tikka T3X Tact A1 223 Rem.");

        Load load = createLoad("Load1", savedRifle.id());
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
        Rifle savedRifle = createRifle("Tikka T3X Tact A1 223 Rem.");

        Load load = createLoad("Load1", savedRifle.id());

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
        Rifle savedRifle = createRifle("Tikka T3X Tact A1 223 Rem.");

        Load load = createLoad("Load1", savedRifle.id());
        Load savedLoad = loadRepository.save(load).block();

        Load updatedLoad = new Load(savedLoad.id(), "UpdatedLoad", "UpdatedDescription", "UpdatedManufacturer",
                "UpdatedType", 15.0,
                "UpdatedBulletManufacturer", "UpdatedBulletType", 150.0, "UpdatedPrimerManufacturer",
                "UpdatedPrimerType", 0.025, savedRifle.id());

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
        Rifle savedRifle = createRifle("Tikka T3X Tact A1 223 Rem.");

        Load load = createLoad("Load1", savedRifle.id());
        Load savedLoad = loadRepository.save(load).block();

        webTestClient.delete().uri("/loads/{id}", savedLoad.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get().uri("/loads/{id}", savedLoad.id())
                .exchange()
                .expectStatus().isNotFound();
    }
}