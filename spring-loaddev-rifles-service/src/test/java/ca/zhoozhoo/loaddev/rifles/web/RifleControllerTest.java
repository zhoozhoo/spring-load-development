package ca.zhoozhoo.loaddev.rifles.web;

import static java.util.UUID.randomUUID;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.rifles.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.rifles.dao.RifleRepository;
import ca.zhoozhoo.loaddev.rifles.model.Rifle;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
public class RifleControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RifleRepository rifleRepository;

    @BeforeEach
    void setUp() {
        rifleRepository.deleteAll().block();
    }

    @Test
    void getAllRifles() {
        webTestClient.get().uri("/rifles")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Rifle.class);
    }

    @Test
    void getRifleById() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedRifle = rifleRepository.save(new Rifle(null, userId, "Test Rifle", "Description", "Caliber", 20.0,
                "Contour", "1:10", "Rifling", 0.5)).block();

        webTestClient.mutateWith(jwt).get().uri("/rifles/{id}", savedRifle.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(Rifle.class)
                .isEqualTo(savedRifle);
    }

    @Test
    void getRifleByIdNotFound() {
        webTestClient.get().uri("/rifles/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createRifle() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var rifle = new Rifle(null, userId, "New Rifle", "Description", "Caliber", 20.0,
                "Contour", "1:10", "Rifling", 0.5);

        webTestClient.mutateWith(jwt).post().uri("/rifles")
                .contentType(APPLICATION_JSON)
                .body(fromValue(rifle))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Rifle.class)
                .value(createdRifle -> {
                    assert createdRifle.id() != null;
                    assert createdRifle.name().equals("New Rifle");
                });
    }

    @Test
    void createRifleInvalidInput() {
        var invalidRifle = new Rifle(null, randomUUID().toString(), "", null, null, -1.0, "Contour", "", "Rifling",
                -0.5);

        webTestClient.post().uri("/rifles")
                .contentType(APPLICATION_JSON)
                .body(fromValue(invalidRifle))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(errorMessage -> {
                    assert errorMessage.contains("Name is required");
                    assert errorMessage.contains("Caliber is required");
                    assert errorMessage.contains("Barrel length must be positive");
                    assert errorMessage.contains("Free bore must be positive");
                });
    }

    @Test
    void updateRifle() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedRifle = rifleRepository
                .save(new Rifle(null, userId, "Old Rifle", "Description", "Caliber", 20.0,
                        "Contour", "1:10", "Rifling", 0.5))
                .block();

        var updatedRifle = new Rifle(null, userId, "Updated Rifle", "Updated Description",
                "Updated Caliber", 22.0,
                "Updated Contour", "1:12", "Updated Rifling", 0.6);

        webTestClient.mutateWith(jwt).put().uri("/rifles/{id}", savedRifle.id())
                .contentType(APPLICATION_JSON)
                .body(fromValue(updatedRifle))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Rifle.class)
                .value(rifleResponse -> {
                    assert rifleResponse.id().equals(savedRifle.id());
                    assert rifleResponse.name().equals("Updated Rifle");
                });
    }

    @Test
    void updateRifleNotFound() {
        var rifle = new Rifle(null, randomUUID().toString(), "Test Rifle", "Description", "Caliber", 20.0,
                "Contour", "1:10", "Rifling", 0.5);

        webTestClient.put().uri("/rifles/999")
                .contentType(APPLICATION_JSON)
                .body(fromValue(rifle))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteRifle() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedRifle = rifleRepository.save(new Rifle(null, userId, "Rifle to be deleted", "Description", "Caliber",
                20.0, "Contour", "1:10", "Rifling", 0.5)).block();

        webTestClient.mutateWith(jwt)
                .delete().uri("/rifles/{id}", savedRifle.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(jwt)
                .get().uri("/rifles/{id}", savedRifle.id())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteRifleNotFound() {
        webTestClient.delete().uri("/rifles/999")
                .exchange()
                .expectStatus().isNotFound();
    }
}