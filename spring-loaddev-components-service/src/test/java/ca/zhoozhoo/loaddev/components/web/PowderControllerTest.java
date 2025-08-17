package ca.zhoozhoo.loaddev.components.web;

import static ca.zhoozhoo.loaddev.components.model.Powder.IMPERIAL;
import static ca.zhoozhoo.loaddev.components.model.Powder.METRIC;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static reactor.core.publisher.Mono.just;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.components.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.components.dao.PowderRepository;
import ca.zhoozhoo.loaddev.components.model.Powder;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
public class PowderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PowderRepository powderRepository;

    @BeforeEach
    void setUp() {
        powderRepository.deleteAll().block();
    }

    @Test
    void getAllPowders() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).get().uri("/powders")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Powder.class);
    }

    @Test
    void searchPowders() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedPowder = powderRepository.save(createTestPowder(userId)).block();

        webTestClient.mutateWith(jwt).get().uri(uriBuilder -> uriBuilder.path("/powders/search").queryParam("query", "Hodgdon H4350").build())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Powder.class)
                .value(list -> {
                    assertThat(list).isNotEmpty();
                    assertThat(list).contains(savedPowder);
                });
    }

    @Test
    void getPowderById() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedPowder = powderRepository.save(createTestPowder(userId)).block();

        webTestClient.mutateWith(jwt).get().uri("/powders/{id}", savedPowder.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(Powder.class)
                .isEqualTo(savedPowder);
    }

    @Test
    void getPowderByIdNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).get().uri("/powders/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createPowder() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var powder = createTestPowder(userId);

        webTestClient.mutateWith(jwt).post().uri("/powders")
                .contentType(APPLICATION_JSON)
                .body(just(powder), Powder.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Powder.class)
                .value(p -> {
                    assertThat(p.id()).isNotNull();
                    assertThat(p.manufacturer()).isEqualTo("Hodgdon");
                    assertThat(p.type()).isEqualTo("H4350");
                    assertThat(p.measurementUnits()).isEqualTo(IMPERIAL);
                    assertThat(p.cost()).isEqualTo(new BigDecimal("45.99"));
                    assertThat(p.currency()).isEqualTo("CAD");
                    assertThat(p.weightPerContainer()).isEqualTo(1.0);
                });
    }

    @Test
    void createPowderInvalidInput() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var invalidPowder = new Powder(null, userId, "", "", "", 
                new BigDecimal("-1"), "", -1.0);

        webTestClient.mutateWith(jwt).post().uri("/powders")
                .contentType(APPLICATION_JSON)
                .body(just(invalidPowder), Powder.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(errorMessage -> {
                    assertThat(errorMessage).contains("Manufacturer is required");
                    assertThat(errorMessage).contains("Type is required");
                    assertThat(errorMessage).contains("Measurement Units is required");
                    assertThat(errorMessage).contains("Cost must be positive");
                    assertThat(errorMessage).contains("Currency is required");
                    assertThat(errorMessage).contains("Weight per container must be positive");
                });
    }

    @Test
    void updatePowder() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedPowder = powderRepository.save(createTestPowder(userId)).block();

        var updatedPowder = new Powder(
                savedPowder.id(),
                userId,
                "IMR",
                "4895",
                METRIC,
                new BigDecimal("49.99"),
                "CAD",
                0.5);

        webTestClient.mutateWith(jwt).put().uri("/powders/{id}", savedPowder.id())
                .contentType(APPLICATION_JSON)
                .body(just(updatedPowder), Powder.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Powder.class)
                .value(p -> {
                    assertThat(p.id()).isEqualTo(savedPowder.id());
                    assertThat(p.manufacturer()).isEqualTo("IMR");
                    assertThat(p.type()).isEqualTo("4895");
                    assertThat(p.measurementUnits()).isEqualTo(METRIC);
                    assertThat(p.cost()).isEqualTo(new BigDecimal("49.99"));
                    assertThat(p.currency()).isEqualTo("CAD");
                    assertThat(p.weightPerContainer()).isEqualTo(0.5);
                });
    }

    @Test
    void updatePowderNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var powder = createTestPowder(userId);

        webTestClient.mutateWith(jwt).put().uri("/powders/999")
                .contentType(APPLICATION_JSON)
                .body(just(powder), Powder.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePowder() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedPowder = powderRepository.save(createTestPowder(userId)).block();

        webTestClient.mutateWith(jwt)
                .delete().uri("/powders/{id}", savedPowder.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(jwt)
                .get().uri("/powders/{id}", savedPowder.id())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePowderNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).delete().uri("/powders/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    private Powder createTestPowder(String ownerId) {
        return new Powder(
                null,
                ownerId,
                "Hodgdon",
                "H4350",
                IMPERIAL,
                new BigDecimal("45.99"),
                "CAD",
                1.0);
    }
}
