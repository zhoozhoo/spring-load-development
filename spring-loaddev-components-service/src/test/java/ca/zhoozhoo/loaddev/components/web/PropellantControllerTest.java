package ca.zhoozhoo.loaddev.components.web;

import static java.util.UUID.randomUUID;
import static javax.money.Monetary.getCurrency;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javamoney.moneta.Money.of;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static reactor.core.publisher.Mono.just;
import static systems.uom.ucum.UCUM.POUND;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.components.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.components.dao.PropellantRepository;
import ca.zhoozhoo.loaddev.components.model.Propellant;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
public class PropellantControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PropellantRepository propellantRepository;

    @BeforeEach
    void setUp() {
        propellantRepository.deleteAll().block();
    }

    @Test
    void getAllPropellants() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).get().uri("/propellants")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Propellant.class);
    }

    @Test
    void searchPropellants() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedPropellant = propellantRepository.save(createTestPropellant(userId)).block();

        webTestClient.mutateWith(jwt).get().uri(uriBuilder -> uriBuilder.path("/propellants/search").queryParam("query", "Hodgdon H4350").build())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Propellant.class)
                .value(list -> {
                    assertThat(list).isNotEmpty();
                    assertThat(list).contains(savedPropellant);
                });
    }

    @Test
    void getPropellantById() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedPropellant = propellantRepository.save(createTestPropellant(userId)).block();

        webTestClient.mutateWith(jwt).get().uri("/propellants/{id}", savedPropellant.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(Propellant.class)
                .isEqualTo(savedPropellant);
    }

    @Test
    void getPropellantByIdNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).get().uri("/propellants/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createPropellant() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var propellant = createTestPropellant(userId);

        webTestClient.mutateWith(jwt).post().uri("/propellants")
                .contentType(APPLICATION_JSON)
                .body(just(propellant), Propellant.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Propellant.class)
                .value(p -> {
                    assertThat(p.id()).isNotNull();
                    assertThat(p.manufacturer()).isEqualTo("Hodgdon");
                    assertThat(p.type()).isEqualTo("H4350");
                    assertThat(p.cost()).isEqualTo(of(45.99, getCurrency("CAD")));
                    assertThat(p.weightPerContainer().getValue().doubleValue()).isEqualTo(1.0);
                    assertThat(p.weightPerContainer().getUnit()).isEqualTo(POUND);
                });
    }

    @Test
    void createPropellantInvalidInput() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var invalidPropellant = new Propellant(null, userId, "", "", 
                of(-1, getCurrency("CAD")), 
                getQuantity(-1, KILOGRAM));

        webTestClient.mutateWith(jwt).post().uri("/propellants")
                .contentType(APPLICATION_JSON)
                .body(just(invalidPropellant), Propellant.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(errorMessage -> {
                    assertThat(errorMessage).contains("Manufacturer is required");
                    assertThat(errorMessage).contains("Type is required");
                    assertThat(errorMessage).contains("Cost must be non-negative");
                    assertThat(errorMessage).contains("Weight per container must be positive");
                });
    }

    @Test
    void updatePropellant() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedPropellant = propellantRepository.save(createTestPropellant(userId)).block();

        var updatedPropellant = new Propellant(
                savedPropellant.id(),
                userId,
                "IMR",
                "4895",
                of(49.99, getCurrency("CAD")),
                getQuantity(0.5, KILOGRAM));

        webTestClient.mutateWith(jwt).put().uri("/propellants/{id}", savedPropellant.id())
                .contentType(APPLICATION_JSON)
                .body(just(updatedPropellant), Propellant.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Propellant.class)
                .value(p -> {
                    assertThat(p.id()).isEqualTo(savedPropellant.id());
                    assertThat(p.manufacturer()).isEqualTo("IMR");
                    assertThat(p.type()).isEqualTo("4895");
                    assertThat(p.cost()).isEqualTo(of(49.99, getCurrency("CAD")));
                    assertThat(p.weightPerContainer().getValue().doubleValue()).isEqualTo(0.5);
                    assertThat(p.weightPerContainer().getUnit().toString()).isEqualTo(KILOGRAM.toString());
                });
    }

    @Test
    void updatePropellantNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var propellant = createTestPropellant(userId);

        webTestClient.mutateWith(jwt).put().uri("/propellants/999")
                .contentType(APPLICATION_JSON)
                .body(just(propellant), Propellant.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePropellant() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedPropellant = propellantRepository.save(createTestPropellant(userId)).block();

        webTestClient.mutateWith(jwt)
                .delete().uri("/propellants/{id}", savedPropellant.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(jwt)
                .get().uri("/propellants/{id}", savedPropellant.id())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePropellantNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).delete().uri("/propellants/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    private Propellant createTestPropellant(String ownerId) {
        return new Propellant(
                null,
                ownerId,
                "Hodgdon",
                "H4350",
                of(45.99, getCurrency("CAD")),
                getQuantity(1.0, POUND));
    }
}
