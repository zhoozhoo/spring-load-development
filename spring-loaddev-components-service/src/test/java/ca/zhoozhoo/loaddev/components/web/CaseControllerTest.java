package ca.zhoozhoo.loaddev.components.web;

import static java.util.UUID.randomUUID;
import static javax.money.Monetary.getCurrency;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javamoney.moneta.Money.of;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static reactor.core.publisher.Mono.just;
import static tech.units.indriya.AbstractUnit.ONE;
import static tech.units.indriya.quantity.Quantities.getQuantity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.components.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.components.dao.CaseRepository;
import ca.zhoozhoo.loaddev.components.model.Case;
import ca.zhoozhoo.loaddev.components.model.PrimerSize;

@SpringBootTest(properties = "spring.autoconfigure.exclude=ca.zhoozhoo.loaddev.security.SecurityAutoConfiguration")
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

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri("/cases")
                .header("Authorization", "Bearer " + userId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Case.class);
    }

    @Test
    void searchCases() {
        var userId = randomUUID().toString();

        var savedCase = caseRepository.save(createTestCase(userId)).block();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri(uriBuilder -> uriBuilder.path("/cases/search").queryParam("query", "Lapua 6.5 Creedmoor").build())
                .header("Authorization", "Bearer " + userId)
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

        var savedCase = caseRepository.save(createTestCase(userId)).block();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri("/cases/{id}", savedCase.id())
                .header("Authorization", "Bearer " + userId)
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

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri("/cases/999")
                .header("Authorization", "Bearer " + userId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createCase() {
        var userId = randomUUID().toString();

        var casing = createTestCase(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .post()
                .uri("/cases")
                .header("Authorization", "Bearer " + userId)
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
                    assertThat(c.cost()).isEqualTo(of(89.99, getCurrency("CAD")));
                    assertThat(c.quantityPerBox().getValue().doubleValue()).isEqualTo(100.0);
                    assertThat(c.quantityPerBox().getUnit()).isEqualTo(ONE);
                });
    }

    @Test
    void createCaseInvalidInput() {
        var userId = randomUUID().toString();

        var invalidCase = new Case(null, userId, "", "", null,
                of(-1, getCurrency("CAD")),
                getQuantity(-1, ONE));

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .post()
                .uri("/cases")
                .header("Authorization", "Bearer " + userId)
                .body(just(invalidCase), Case.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(errorMessage -> {
                    assertThat(errorMessage).contains("Manufacturer is required");
                    assertThat(errorMessage).contains("Caliber is required");
                    assertThat(errorMessage).contains("Primer size is required");
                    assertThat(errorMessage).contains("Cost must be non-negative");
                    assertThat(errorMessage).contains("Quantity per box must be positive");
                });
    }

    @Test
    void updateCase() {
        var userId = randomUUID().toString();

        var savedCase = caseRepository.save(createTestCase(userId)).block();

        var updatedCase = new Case(
                savedCase.id(),
                userId,
                "Peterson",
                "308 Winchester",
                PrimerSize.LARGE_RIFLE,
                of(99.99, getCurrency("CAD")),
                getQuantity(50, ONE));

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .put()
                .uri("/cases/{id}", savedCase.id())
                .header("Authorization", "Bearer " + userId)
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
                    assertThat(c.cost()).isEqualTo(of(99.99, getCurrency("CAD")));
                    assertThat(c.quantityPerBox().getValue().doubleValue()).isEqualTo(50.0);
                    assertThat(c.quantityPerBox().getUnit()).isEqualTo(ONE);
                });
    }

    @Test
    void updateCaseNotFound() {
        var userId = randomUUID().toString();

        var casing = createTestCase(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .put()
                .uri("/cases/999")
                .header("Authorization", "Bearer " + userId)
                .contentType(APPLICATION_JSON)
                .body(just(casing), Case.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteCase() {
        var userId = randomUUID().toString();

        var savedCase = caseRepository.save(createTestCase(userId)).block();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:delete")))
                .delete()
                .uri("/cases/{id}", savedCase.id())
                .header("Authorization", "Bearer " + userId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri("/cases/{id}", savedCase.id())
                .header("Authorization", "Bearer " + userId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteCaseNotFound() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:delete")))
                .delete()
                .uri("/cases/999")
                .header("Authorization", "Bearer " + userId)
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
                of(89.99, getCurrency("CAD")),
                getQuantity(100, ONE));
    }
}
