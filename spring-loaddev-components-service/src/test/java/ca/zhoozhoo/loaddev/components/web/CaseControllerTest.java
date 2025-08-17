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
import ca.zhoozhoo.loaddev.components.dao.CaseRepository;
import ca.zhoozhoo.loaddev.components.model.Case;
import ca.zhoozhoo.loaddev.components.model.PrimerSize;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
public class CaseControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CaseRepository caseRepository;

    @BeforeEach
    void setUp() {
        caseRepository.deleteAll().block();
    }

    @Test
    void getAllCases() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).get().uri("/cases")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Case.class);
    }

    @Test
    void searchCases() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedCase = caseRepository.save(createTestCase(userId)).block();

        webTestClient.mutateWith(jwt).get().uri(uriBuilder -> uriBuilder.path("/cases/search").queryParam("query", "Lapua 6.5 Creedmoor").build())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Case.class)
                .value(list -> {
                    assertThat(list).isNotEmpty();
                    assertThat(list).contains(savedCase);
                });
    }

    @Test
    void getCaseById() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedCase = caseRepository.save(createTestCase(userId)).block();

        webTestClient.mutateWith(jwt).get().uri("/cases/{id}", savedCase.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(Case.class)
                .isEqualTo(savedCase);
    }

    @Test
    void getCaseByIdNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).get().uri("/cases/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createCase() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var casing = createTestCase(userId);

        webTestClient.mutateWith(jwt).post().uri("/cases")
                .contentType(APPLICATION_JSON)
                .body(just(casing), Case.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Case.class)
                .value(c -> {
                    assertThat(c.id()).isNotNull();
                    assertThat(c.manufacturer()).isEqualTo("Lapua");
                    assertThat(c.caliber()).isEqualTo("6.5 Creedmoor");
                    assertThat(c.primerSize()).isEqualTo(PrimerSize.LARGE_RIFLE);
                    assertThat(c.cost()).isEqualTo(new BigDecimal("89.99"));
                    assertThat(c.currency()).isEqualTo("CAD");
                    assertThat(c.quantityPerBox()).isEqualTo(100);
                });
    }

    @Test
    void createCaseInvalidInput() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var invalidCase = new Case(null, userId, "", "", null, 
                new BigDecimal("-1"), "", -1);

        webTestClient.mutateWith(jwt).post().uri("/cases")
                .contentType(APPLICATION_JSON)
                .body(just(invalidCase), Case.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(errorMessage -> {
                    assertThat(errorMessage).contains("Manufacturer is required");
                    assertThat(errorMessage).contains("Caliber is required");
                    assertThat(errorMessage).contains("Primer size is required");
                    assertThat(errorMessage).contains("Cost must be greater than or equal to 0");
                    assertThat(errorMessage).contains("Currency is required");
                    assertThat(errorMessage).contains("Quantity per box must be greater than 0");
                });
    }

    @Test
    void updateCase() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedCase = caseRepository.save(createTestCase(userId)).block();

        var updatedCase = new Case(
                savedCase.id(),
                userId,
                "Peterson",
                "308 Winchester",
                PrimerSize.LARGE_RIFLE,
                new BigDecimal("99.99"),
                "CAD",
                50);

        webTestClient.mutateWith(jwt).put().uri("/cases/{id}", savedCase.id())
                .contentType(APPLICATION_JSON)
                .body(just(updatedCase), Case.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Case.class)
                .value(c -> {
                    assertThat(c.id()).isEqualTo(savedCase.id());
                    assertThat(c.manufacturer()).isEqualTo("Peterson");
                    assertThat(c.caliber()).isEqualTo("308 Winchester");
                    assertThat(c.primerSize()).isEqualTo(PrimerSize.LARGE_RIFLE);
                    assertThat(c.cost()).isEqualTo(new BigDecimal("99.99"));
                    assertThat(c.currency()).isEqualTo("CAD");
                    assertThat(c.quantityPerBox()).isEqualTo(50);
                });
    }

    @Test
    void updateCaseNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var casing = createTestCase(userId);

        webTestClient.mutateWith(jwt).put().uri("/cases/999")
                .contentType(APPLICATION_JSON)
                .body(just(casing), Case.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteCase() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedCase = caseRepository.save(createTestCase(userId)).block();

        webTestClient.mutateWith(jwt)
                .delete().uri("/cases/{id}", savedCase.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(jwt)
                .get().uri("/cases/{id}", savedCase.id())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteCaseNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).delete().uri("/cases/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    private Case createTestCase(String ownerId) {
        return new Case(
                null,
                ownerId,
                "Lapua",
                "6.5 Creedmoor",
                PrimerSize.LARGE_RIFLE,
                new BigDecimal("89.99"),
                "CAD",
                100);
    }
}
