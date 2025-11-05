package ca.zhoozhoo.loaddev.loads.web;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static reactor.core.publisher.Mono.just;
import static systems.uom.ucum.UCUM.GRAIN;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.dao.GroupJsr385Repository;
import ca.zhoozhoo.loaddev.loads.dao.LoadJsr385Repository;
import ca.zhoozhoo.loaddev.loads.dao.ShotJsr385Repository;
import ca.zhoozhoo.loaddev.loads.model.LoadJsr385;
import reactor.core.publisher.Flux;
import tech.units.indriya.quantity.Quantities;

/**
 * Integration tests for LoadsJsr385Controller.
 * <p>
 * Tests all REST endpoints for JSR-385 load management including CRUD operations,
 * validation, and security. Uses WebTestClient for reactive endpoint testing with
 * mock JWT authentication.
 * </p>
 *
 * @author Zhubin Salehi
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
@DisplayName("LoadsJsr385Controller Integration Tests")
class LoadsJsr385ControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private LoadJsr385Repository loadRepository;

    @Autowired
    private ShotJsr385Repository shotRepository;

    @Autowired
    private GroupJsr385Repository groupRepository;

    @BeforeEach
    void setUp() {
        shotRepository.deleteAll().block();
        groupRepository.deleteAll().block();
        loadRepository.deleteAll().block();
    }

    private LoadJsr385 createLoad(String ownerId, String name) {
        var bulletWeight = Quantities.getQuantity(168, GRAIN);
        var distanceFromLands = Quantities.getQuantity(0.020, INCH_INTERNATIONAL);
        var caseOverallLength = Quantities.getQuantity(2.800, INCH_INTERNATIONAL);
        var neckTension = Quantities.getQuantity(0.002, INCH_INTERNATIONAL);

        return new LoadJsr385(null, ownerId, name, name + " Description",
                "Hodgdon", "H4350",
                "Hornady", "ELD-M", bulletWeight,
                "CCI", "BR-2",
                distanceFromLands,
                caseOverallLength,
                neckTension,
                null);
    }

    // ========================================
    // Positive Test Cases
    // ========================================

    @Test
    @DisplayName("[Positive] Should get all loads for a user")
    void getAllLoads() {
        var userId = randomUUID().toString();

        loadRepository.saveAll(Flux.just(
                createLoad(userId, "Load1"), 
                createLoad(userId, "Load2")
        )).blockLast();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .get().uri("/jsr385/loads")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Load1")
                .jsonPath("$[1].name").isEqualTo("Load2");
    }

    @Test
    @DisplayName("[Positive] Should get load by ID")
    void getLoadById() {
        var userId = randomUUID().toString();
        var loadId = loadRepository.save(createLoad(userId, "Load1")).block().id();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .get().uri("/jsr385/loads/{id}", loadId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Load1");
    }

    @Test
    @DisplayName("[Positive] Should create a new load")
    void createLoad() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .post().uri("/jsr385/loads")
                .contentType(APPLICATION_JSON)
                .body(just(createLoad(userId, "Load1")), LoadJsr385.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Load1")
                .jsonPath("$.id").exists();
    }

    @Test
    @DisplayName("[Positive] Should update an existing load")
    void updateLoad() {
        var userId = randomUUID().toString();
        var loadId = loadRepository.save(createLoad(userId, "Load1")).block().id();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .put().uri("/jsr385/loads/{id}", loadId)
                .contentType(APPLICATION_JSON)
                .body(just(new LoadJsr385(loadId, userId, "UpdatedLoad", "UpdatedDescription",
                        "Alliant", "Reloder 16",
                        "Berger", "Hybrid Target", Quantities.getQuantity(175, GRAIN),
                        "Federal", "210M",
                        Quantities.getQuantity(0.025, INCH_INTERNATIONAL),
                        Quantities.getQuantity(2.850, INCH_INTERNATIONAL),
                        Quantities.getQuantity(0.003, INCH_INTERNATIONAL),
                        1L)), LoadJsr385.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("UpdatedLoad");
    }

    @Test
    @DisplayName("[Positive] Should delete an existing load")
    void deleteLoad() {
        var userId = randomUUID().toString();
        var loadId = loadRepository.save(createLoad(userId, "Load1")).block().id();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .delete().uri("/jsr385/loads/{id}", loadId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .get().uri("/jsr385/loads/{id}", loadId)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ========================================
    // Negative Test Cases - Not Found
    // ========================================

    @Test
    @DisplayName("[Negative] Should return 404 when getting non-existent load")
    void getNonExistentLoad() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .get().uri("/jsr385/loads/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("[Negative] Should return 404 when updating non-existent load")
    void updateNonExistentLoad() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .put().uri("/jsr385/loads/999")
                .contentType(APPLICATION_JSON)
                .body(just(createLoad(userId, "TestLoad")), LoadJsr385.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("[Negative] Should return 404 when deleting non-existent load")
    void deleteNonExistentLoad() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .delete().uri("/jsr385/loads/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    // ========================================
    // Negative Test Cases - Validation
    // ========================================

    @Test
    @DisplayName("[Validation] Should throw exception when creating load with invalid data")
    void createLoadWithInvalidData() {
        assertThrows(IllegalArgumentException.class, () ->
                new LoadJsr385(null, null, "Test", "Description",
                        "Manufacturer", "Type",
                        "BulletManufacturer", "BulletType", Quantities.getQuantity(168, GRAIN),
                        "PrimerManufacturer", "PrimerType",
                        null,  // No distanceFromLands
                        null,  // No caseOverallLength - violates constraint
                        null,
                        null));
    }

    @Test
    @DisplayName("[Validation] Should return 4xx when creating load with null bullet weight")
    void createLoadWithNullBulletWeight() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .post().uri("/jsr385/loads")
                .contentType(APPLICATION_JSON)
                .body(just(new LoadJsr385(null, userId, "Test", "Description",
                        "Manufacturer", "Type",
                        "BulletManufacturer", "BulletType", null,  // null bullet weight
                        "PrimerManufacturer", "PrimerType",
                        Quantities.getQuantity(0.020, INCH_INTERNATIONAL),
                        null,
                        null,
                        null)), LoadJsr385.class)
                .exchange()
                .expectStatus().is4xxClientError();
    }
}
