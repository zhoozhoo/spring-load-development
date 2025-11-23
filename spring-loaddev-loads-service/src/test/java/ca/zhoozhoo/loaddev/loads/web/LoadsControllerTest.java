package ca.zhoozhoo.loaddev.loads.web;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static reactor.core.publisher.Mono.just;
import static systems.uom.ucum.UCUM.GRAIN;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.dao.GroupRepository;
import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import ca.zhoozhoo.loaddev.loads.model.Load;
import reactor.core.publisher.Flux;

/**
 * Integration tests for LoadsController.
 * <p>
 * Tests all REST endpoints for load management including CRUD operations,
 * validation, and security. Uses WebTestClient for reactive endpoint testing with
 * mock JWT authentication.
 * </p>
 *
 * @author Zhubin Salehi
 */
@SpringBootTest(properties = "spring.autoconfigure.exclude=ca.zhoozhoo.loaddev.security.SecurityAutoConfiguration")
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
@DisplayName("LoadsController Integration Tests")
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
        var bulletWeight = getQuantity(168, GRAIN);
        var distanceFromLands = getQuantity(0.020, INCH_INTERNATIONAL);
        var caseOverallLength = getQuantity(2.800, INCH_INTERNATIONAL);
        var neckTension = getQuantity(0.002, INCH_INTERNATIONAL);

        return new Load(null, ownerId, name, name + " Description",
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

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("loads:view")))
                .get()
                .uri("/loads")
                .header("Authorization", "Bearer " + userId)
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

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("loads:view")))
                .get()
                .uri("/loads/{id}", loadId)
                .header("Authorization", "Bearer " + userId)
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

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("loads:edit")))
                .post()
                .uri("/loads")
                .header("Authorization", "Bearer " + userId)
                .contentType(APPLICATION_JSON)
                .body(just(createLoad(userId, "Load1")), Load.class)
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

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("loads:edit")))
                .put()
                .uri("/loads/{id}", loadId)
                .header("Authorization", "Bearer " + userId)
                .contentType(APPLICATION_JSON)
                .body(just(new Load(loadId, userId, "UpdatedLoad", "UpdatedDescription",
                        "Alliant", "Reloder 16",
                        "Berger", "Hybrid Target", getQuantity(175, GRAIN),
                        "Federal", "210M",
                        getQuantity(0.025, INCH_INTERNATIONAL),
                        getQuantity(2.850, INCH_INTERNATIONAL),
                        getQuantity(0.003, INCH_INTERNATIONAL),
                        1L)), Load.class)
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

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("loads:delete")))
                .delete()
                .uri("/loads/{id}", loadId)
                .header("Authorization", "Bearer " + userId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("loads:view")))
                .get()
                .uri("/loads/{id}", loadId)
                .header("Authorization", "Bearer " + userId)
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

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("loads:view")))
                .get()
                .uri("/loads/999")
                .header("Authorization", "Bearer " + userId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("[Negative] Should return 404 when updating non-existent load")
    void updateNonExistentLoad() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("loads:edit")))
                .put()
                .uri("/loads/999")
                .header("Authorization", "Bearer " + userId)
                .contentType(APPLICATION_JSON)
                .body(just(createLoad(userId, "TestLoad")), Load.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("[Negative] Should return 404 when deleting non-existent load")
    void deleteNonExistentLoad() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("loads:delete")))
                .delete()
                .uri("/loads/999")
                .header("Authorization", "Bearer " + userId)
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
                new Load(null, null, "Test", "Description",
                        "Manufacturer", "Type",
                        "BulletManufacturer", "BulletType", getQuantity(168, GRAIN),
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

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("loads:edit")))
                .post()
                .uri("/loads")
                .header("Authorization", "Bearer " + userId)
                .contentType(APPLICATION_JSON)
                .body(just(new Load(null, userId, "Test", "Description",
                        "Manufacturer", "Type",
                        "BulletManufacturer", "BulletType", null,  // null bullet weight
                        "PrimerManufacturer", "PrimerType",
                        getQuantity(0.020, INCH_INTERNATIONAL),
                        null,
                        null,
                        null)), Load.class)
                .exchange()
                .expectStatus().is4xxClientError();
    }
}
