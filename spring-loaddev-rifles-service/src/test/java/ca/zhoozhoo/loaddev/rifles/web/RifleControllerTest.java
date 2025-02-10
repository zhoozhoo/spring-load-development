package ca.zhoozhoo.loaddev.rifles.web;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.rifles.dao.RifleRepository;
import ca.zhoozhoo.loaddev.rifles.model.Rifle;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
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
        Rifle rifle = new Rifle(null, "Test Rifle", "Description", "Caliber", 20.0, "Contour", "1:10", 0.5, "Rifling");
        Rifle savedRifle = rifleRepository.save(rifle).block();

        webTestClient.get().uri("/rifles/{id}", savedRifle.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(Rifle.class)
                .isEqualTo(savedRifle);
    }

    @Test
    void createRifle() {
        Rifle rifle = new Rifle(null, "New Rifle", "Description", "Caliber", 20.0, "Contour", "1:10", 0.5, "Rifling");

        webTestClient.post().uri("/rifles")
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
    void updateRifle() {
        Rifle rifle = new Rifle(null, "Old Rifle", "Description", "Caliber", 20.0, "Contour", "1:10", 0.5, "Rifling");
        Rifle savedRifle = rifleRepository.save(rifle).block();

        Rifle updatedRifle = new Rifle(null, "Updated Rifle", "Updated Description", "Updated Caliber", 22.0,
                "Updated Contour", "1:12", 0.6, "Updated Rifling");

        webTestClient.put().uri("/rifles/{id}", savedRifle.id())
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
    void deleteRifle() {
        Rifle rifle = new Rifle(null, "Rifle to be deleted", "Description", "Caliber", 20.0, "Contour", "1:10", 0.5,
                "Rifling");
        Rifle savedRifle = rifleRepository.save(rifle).block();

        webTestClient.delete().uri("/rifles/{id}", savedRifle.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get().uri("/rifles/{id}", savedRifle.id())
                .exchange()
                .expectStatus().isNotFound();
    }
}