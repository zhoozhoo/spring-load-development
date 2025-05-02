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
import static ca.zhoozhoo.loaddev.rifles.model.Rifle.IMPERIAL;

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

        var savedRifle = rifleRepository.save(new Rifle(null, userId,
                "Remington 700",
                "Custom Remington 700 in 6.5 Creedmoor with Krieger barrel",
                IMPERIAL,
                "6.5 Creedmoor",
                26.0,
                "Heavy Palma",
                "1:8",
                "5R",
                0.157)).block();

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

        var rifle = new Rifle(null, userId,
                "Savage 110 Elite Precision",
                "Long range precision rifle with MDT chassis",
                IMPERIAL,
                ".308 Winchester",
                24.0,
                "Medium Palma",
                "1:10",
                "6 Groove",
                0.160);

        webTestClient.mutateWith(jwt).post().uri("/rifles")
                .contentType(APPLICATION_JSON)
                .body(fromValue(rifle))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Rifle.class)
                .value(createdRifle -> {
                    assert createdRifle.id() != null;
                    assert createdRifle.name().equals("Savage 110 Elite Precision");
                    assert createdRifle.description().equals("Long range precision rifle with MDT chassis");
                    assert createdRifle.measurementUnits().equals(IMPERIAL);
                    assert createdRifle.caliber().equals(".308 Winchester");
                });
    }

    @Test
    void createRifleInvalidInput() {
        var invalidRifle = new Rifle(null, randomUUID().toString(), "", null, null, null, -1.0,
                "Contour", "", "Rifling", -0.5);

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
                    assert errorMessage.contains("Measurement Units is required");
                });
    }

    @Test
    void updateRifle() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedRifle = rifleRepository.save(new Rifle(null, userId,
                "Tikka T3x",
                "Factory Tikka T3x Tactical",
                IMPERIAL,
                ".308 Winchester",
                20.0,
                "MTU",
                "1:11",
                "4 Groove",
                0.157)).block();

        var updatedRifle = new Rifle(null, userId,
                "Tikka T3x Custom",
                "Tikka T3x with Bartlein barrel and MDT chassis",
                IMPERIAL,
                "6mm Creedmoor",
                26.0,
                "M24",
                "1:7.5",
                "6 Groove",
                0.153);

        webTestClient.mutateWith(jwt).put().uri("/rifles/{id}", savedRifle.id())
                .contentType(APPLICATION_JSON)
                .body(fromValue(updatedRifle))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Rifle.class)
                .value(rifleResponse -> {
                    assert rifleResponse.id().equals(savedRifle.id());
                    assert rifleResponse.name().equals("Tikka T3x Custom");
                });
    }

    @Test
    void updateRifleNotFound() {
        var rifle = new Rifle(null, randomUUID().toString(), "Test Rifle", "Description", IMPERIAL, "Caliber", 20.0,
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

        var savedRifle = rifleRepository
                .save(new Rifle(null, userId, "Rifle to be deleted", "Description", IMPERIAL, "Caliber",
                        20.0, "Contour", "1:10", "Rifling", 0.5))
                .block();

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