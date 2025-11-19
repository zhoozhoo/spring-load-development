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

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri("/primers")
                .header("Authorization", "Bearer " + userId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Primer.class);
    }

    @Test
    void searchPrimers() {
        var userId = randomUUID().toString();

        var savedPrimer = primerRepository.save(createTestPrimer(userId)).block();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri(uriBuilder -> uriBuilder.path("/primers/search").queryParam("query", "CCI BR-4").build())
                .header("Authorization", "Bearer " + userId)
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

        var savedPrimer = primerRepository.save(createTestPrimer(userId)).block();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri("/primers/{id}", savedPrimer.id())
                .header("Authorization", "Bearer " + userId)
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

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri("/primers/999")
                .header("Authorization", "Bearer " + userId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createPrimer() {
        var userId = randomUUID().toString();

        var primer = createTestPrimer(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .post()
                .uri("/primers")
                .header("Authorization", "Bearer " + userId)
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
                    assertThat(p.cost()).isEqualTo(of(89.99, getCurrency("CAD")));
                    assertThat(p.quantityPerBox().getValue().doubleValue()).isEqualTo(1000.0);
                    assertThat(p.quantityPerBox().getUnit()).isEqualTo(ONE);
                });
    }

    @Test
    void createPrimerInvalidInput() {
        var userId = randomUUID().toString();

        var invalidPrimer = new Primer(null, userId, "", "", null,
                of(-1, getCurrency("CAD")),
                getQuantity(-1, ONE));

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .post()
                .uri("/primers")
                .header("Authorization", "Bearer " + userId)
                .body(just(invalidPrimer), Primer.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(errorMessage -> {
                    assertThat(errorMessage).contains("Manufacturer is required");
                    assertThat(errorMessage).contains("Type is required");
                    assertThat(errorMessage).contains("Primer size is required");
                    assertThat(errorMessage).contains("Cost must be non-negative");
                    assertThat(errorMessage).contains("Quantity per box must be positive");
                });
    }

    @Test
    void updatePrimer() {
        var userId = randomUUID().toString();

        var savedPrimer = primerRepository.save(createTestPrimer(userId)).block();

        var updatedPrimer = new Primer(
                savedPrimer.id(),
                userId,
                "Federal",
                "205M",
                PrimerSize.LARGE_RIFLE_MAGNUM,
                of(99.99, getCurrency("CAD")),
                getQuantity(500, ONE));

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .put()
                .uri("/primers/{id}", savedPrimer.id())
                .header("Authorization", "Bearer " + userId)
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
                    assertThat(p.cost()).isEqualTo(of(99.99, getCurrency("CAD")));
                    assertThat(p.quantityPerBox().getValue().doubleValue()).isEqualTo(500.0);
                    assertThat(p.quantityPerBox().getUnit()).isEqualTo(ONE);
                });
    }

    @Test
    void updatePrimerNotFound() {
        var userId = randomUUID().toString();

        var primer = createTestPrimer(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .put()
                .uri("/primers/999")
                .header("Authorization", "Bearer " + userId)
                .contentType(APPLICATION_JSON)
                .body(just(primer), Primer.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePrimer() {
        var userId = randomUUID().toString();

        var savedPrimer = primerRepository.save(createTestPrimer(userId)).block();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .delete()
                .uri("/primers/{id}", savedPrimer.id())
                .header("Authorization", "Bearer " + userId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri("/primers/{id}", savedPrimer.id())
                .header("Authorization", "Bearer " + userId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePrimerNotFound() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .delete()
                .uri("/primers/999")
                .header("Authorization", "Bearer " + userId)
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
                of(89.99, getCurrency("CAD")),
                getQuantity(1000, ONE));
    }
}
