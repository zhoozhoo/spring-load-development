package ca.zhoozhoo.loaddev.components.web;

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
import ca.zhoozhoo.loaddev.components.dao.PrimerRepository;
import ca.zhoozhoo.loaddev.components.model.Primer;
import ca.zhoozhoo.loaddev.components.model.PrimerSize;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
public class PrimerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PrimerRepository primerRepository;

    @BeforeEach
    void setUp() {
        primerRepository.deleteAll().block();
    }

    @Test
    void getAllPrimers() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).get().uri("/primers")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Primer.class);
    }

    @Test
    void searchPrimers() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedPrimer = primerRepository.save(createTestPrimer(userId)).block();

        webTestClient.mutateWith(jwt).get().uri(uriBuilder -> uriBuilder.path("/primers/search").queryParam("query", "CCI BR-4").build())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Primer.class)
                .value(list -> {
                    assertThat(list).isNotEmpty();
                    assertThat(list).contains(savedPrimer);
                });
    }

    @Test
    void getPrimerById() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedPrimer = primerRepository.save(createTestPrimer(userId)).block();

        webTestClient.mutateWith(jwt).get().uri("/primers/{id}", savedPrimer.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(Primer.class)
                .isEqualTo(savedPrimer);
    }

    @Test
    void getPrimerByIdNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).get().uri("/primers/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createPrimer() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var primer = createTestPrimer(userId);

        webTestClient.mutateWith(jwt).post().uri("/primers")
                .contentType(APPLICATION_JSON)
                .body(just(primer), Primer.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Primer.class)
                .value(p -> {
                    assertThat(p.id()).isNotNull();
                    assertThat(p.manufacturer()).isEqualTo("CCI");
                    assertThat(p.type()).isEqualTo("BR-4");
                    assertThat(p.primerSize()).isEqualTo(PrimerSize.LARGE_RIFLE);
                    assertThat(p.cost()).isEqualTo(new BigDecimal("89.99"));
                    assertThat(p.currency()).isEqualTo("CAD");
                    assertThat(p.quantityPerBox()).isEqualTo(1000);
                });
    }

    @Test
    void createPrimerInvalidInput() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var invalidPrimer = new Primer(null, userId, "", "", null,
                new BigDecimal("-1"), "", -1);

        webTestClient.mutateWith(jwt).post().uri("/primers")
                .contentType(APPLICATION_JSON)
                .body(just(invalidPrimer), Primer.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(errorMessage -> {
                    assertThat(errorMessage).contains("Manufacturer is required");
                    assertThat(errorMessage).contains("Type is required");
                    assertThat(errorMessage).contains("Primer size is required");
                    assertThat(errorMessage).contains("Cost must be greater than or equal to 0");
                    assertThat(errorMessage).contains("Currency is required");
                    assertThat(errorMessage).contains("Quantity per box must be greater than 0");
                });
    }

    @Test
    void updatePrimer() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedPrimer = primerRepository.save(createTestPrimer(userId)).block();

        var updatedPrimer = new Primer(
                savedPrimer.id(),
                userId,
                "Federal",
                "205M",
                PrimerSize.LARGE_RIFLE_MAGNUM,
                new BigDecimal("99.99"),
                "CAD",
                500);

        webTestClient.mutateWith(jwt).put().uri("/primers/{id}", savedPrimer.id())
                .contentType(APPLICATION_JSON)
                .body(just(updatedPrimer), Primer.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Primer.class)
                .value(p -> {
                    assertThat(p.id()).isEqualTo(savedPrimer.id());
                    assertThat(p.manufacturer()).isEqualTo("Federal");
                    assertThat(p.type()).isEqualTo("205M");
                    assertThat(p.primerSize()).isEqualTo(PrimerSize.LARGE_RIFLE_MAGNUM);
                    assertThat(p.cost()).isEqualTo(new BigDecimal("99.99"));
                    assertThat(p.currency()).isEqualTo("CAD");
                    assertThat(p.quantityPerBox()).isEqualTo(500);
                });
    }

    @Test
    void updatePrimerNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var primer = createTestPrimer(userId);

        webTestClient.mutateWith(jwt).put().uri("/primers/999")
                .contentType(APPLICATION_JSON)
                .body(just(primer), Primer.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePrimer() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedPrimer = primerRepository.save(createTestPrimer(userId)).block();

        webTestClient.mutateWith(jwt)
                .delete().uri("/primers/{id}", savedPrimer.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(jwt)
                .get().uri("/primers/{id}", savedPrimer.id())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePrimerNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).delete().uri("/primers/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    private Primer createTestPrimer(String ownerId) {
        return new Primer(
                null,
                ownerId,
                "CCI",
                "BR-4",
                PrimerSize.LARGE_RIFLE,
                new BigDecimal("89.99"),
                "CAD",
                1000);
    }
}
