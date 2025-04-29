package ca.zhoozhoo.loaddev.loads.web;

import static ca.zhoozhoo.loaddev.loads.model.Unit.GRAINS;
import static ca.zhoozhoo.loaddev.loads.model.Unit.INCHES;
import static java.util.UUID.randomUUID;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static reactor.core.publisher.Mono.just;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.dao.GroupRepository;
import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import ca.zhoozhoo.loaddev.loads.model.Load;
import reactor.core.publisher.Flux;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
class LoadsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private LoadRepository loadRepository;

    @Autowired
    private ShotRepository shotRepository;

    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    void setUp() {
        shotRepository.deleteAll().block();
        groupRepository.deleteAll().block();
        loadRepository.deleteAll().block();
    }

    private Load createLoad(String ownerId, String name) {
        return new Load(null, ownerId, name, name + " Description",
                "Manufacturer", "Type",
                "BulletManufacturer", "BulletType", 100.0, GRAINS,
                "PrimerManufacturer", "PrimerType",
                0.020, INCHES,
                2.800, INCHES,
                0.002, INCHES,
                null);
    }

    @Test
    void getAllLoads() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        loadRepository.saveAll(Flux.just(createLoad(userId, "Load1"), createLoad(userId, "Load2"))).blockLast();

        webTestClient.mutateWith(jwt).get().uri("/loads")
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
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));
        var load = loadRepository.save(createLoad(userId, "Load1")).block();

        webTestClient.mutateWith(jwt).get().uri("/loads/{id}", load.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Load1");
    }

    @Test
    void createLoad() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));
        var load = createLoad(userId, "Load1");

        webTestClient.mutateWith(jwt).post().uri("/loads")
                .contentType(APPLICATION_JSON)
                .body(just(load), Load.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Load1")
                .jsonPath("$.id").exists();
    }

    @Test
    void updateLoad() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));
        var load = loadRepository.save(createLoad(userId, "Load1")).block();

        var updatedLoad = new Load(load.id(), userId, "UpdatedLoad", "UpdatedDescription",
                "UpdatedManufacturer", "UpdatedType",
                "UpdatedBulletManufacturer", "UpdatedBulletType", 150.0, GRAINS,
                "UpdatedPrimerManufacturer", "UpdatedPrimerType",
                0.025, INCHES,
                2.850, INCHES,
                0.003, INCHES,
                1L);

        webTestClient.mutateWith(jwt).put().uri("/loads/{id}", load.id())
                .contentType(APPLICATION_JSON)
                .body(just(updatedLoad), Load.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("UpdatedLoad");
    }

    @Test
    void deleteLoad() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));
        var load = loadRepository.save(createLoad(userId, "Load1")).block();

        webTestClient.mutateWith(jwt).delete().uri("/loads/{id}", load.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(jwt).get().uri("/loads/{id}", load.id())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getNonExistentLoad() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).get().uri("/loads/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createLoadWithInvalidData() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));
        var invalidLoad = new Load(null, userId, "", "",
                "", "",
                "", "", -1.0, null,
                "", "",
                -1.0, null,
                -1.0, null,
                -1.0, null,
                null);

        webTestClient.mutateWith(jwt).post().uri("/loads")
                .contentType(APPLICATION_JSON)
                .body(just(invalidLoad), Load.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createLoadWithNullData() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));
        var invalidLoad = new Load(null, null, null, null,
                null, null,
                null, null, null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null);

        webTestClient.mutateWith(jwt).post().uri("/loads")
                .contentType(APPLICATION_JSON)
                .body(just(invalidLoad), Load.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateNonExistentLoad() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));
        var load = createLoad(userId, "TestLoad");

        webTestClient.mutateWith(jwt).put().uri("/loads/999")
                .contentType(APPLICATION_JSON)
                .body(just(load), Load.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateLoadWithInvalidData() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));
        var load = loadRepository.save(createLoad(userId, "Load1")).block();
        var invalidLoad = new Load(load.id(), userId, "", "",
                "", "",
                "", "", -1.0, null,
                "", "",
                -1.0, null,
                -1.0, null,
                -1.0, null,
                null);

        webTestClient.mutateWith(jwt).put().uri("/loads/{id}", load.id())
                .contentType(APPLICATION_JSON)
                .body(just(invalidLoad), Load.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void deleteNonExistentLoad() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).delete().uri("/loads/999")
                .exchange()
                .expectStatus().isNotFound();
    }
}