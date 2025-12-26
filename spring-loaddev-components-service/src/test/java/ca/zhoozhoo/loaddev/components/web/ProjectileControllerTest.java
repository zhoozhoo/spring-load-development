package ca.zhoozhoo.loaddev.components.web;

import static java.util.UUID.randomUUID;
import static javax.money.Monetary.getCurrency;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javamoney.moneta.Money.of;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static reactor.core.publisher.Mono.just;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.GRAM;
import static tech.units.indriya.unit.Units.KILOGRAM;

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
import ca.zhoozhoo.loaddev.components.dao.ProjectileRepository;
import ca.zhoozhoo.loaddev.components.model.Projectile;

@SpringBootTest(properties = "spring.autoconfigure.exclude=ca.zhoozhoo.loaddev.security.SecurityAutoConfiguration")
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
public class ProjectileControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProjectileRepository projectileRepository;

    @BeforeEach
    void setUp() {
        projectileRepository.deleteAll().block();
    }

    @Test
    void getAllProjectiles() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri("/projectiles")
                .header("Authorization", "Bearer " + userId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Projectile.class);
    }

    @Test
    void searchProjectiles() {
        var userId = randomUUID().toString();

        var savedProjectile = projectileRepository.save(createTestProjectile(userId)).block();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri(uriBuilder -> uriBuilder.path("/projectiles/search").queryParam("query", "Hornady ELD-X").build())
                .header("Authorization", "Bearer " + userId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Projectile.class)
                .value(list -> {
                    assertThat(list).isNotEmpty();
                    assertThat(list).contains(savedProjectile);
                });
    }

    @Test
    void getProjectileById() {
        var userId = randomUUID().toString();

        var savedProjectile = projectileRepository.save(createTestProjectile(userId)).block();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri("/projectiles/{id}", savedProjectile.id())
                .header("Authorization", "Bearer " + userId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(Projectile.class)
                .isEqualTo(savedProjectile);
    }

    @Test
    void getProjectileByIdNotFound() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri("/projectiles/999")
                .header("Authorization", "Bearer " + userId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createProjectile() {
        var userId = randomUUID().toString();

        var projectile = createTestProjectile(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .post()
                .uri("/projectiles")
                .header("Authorization", "Bearer " + userId)
                .contentType(APPLICATION_JSON)
                .body(just(projectile), Projectile.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Projectile.class)
                .value(p -> {
                    assertThat(p.id()).isNotNull();
                    assertThat(p.manufacturer()).isEqualTo("Hornady");
                    assertThat(p.type()).isEqualTo("ELD-X");
                    assertThat(p.weight().getValue().doubleValue()).isEqualTo(178.0);
                    assertThat(p.weight().getUnit()).isEqualTo(GRAM);
                    assertThat(p.cost()).isEqualTo(of(52.99, getCurrency("CAD")));
                    assertThat(p.quantityPerBox()).isEqualTo(100);
                });
    }

    @Test
    void createProjectileInvalidInput() {
        var userId = randomUUID().toString();

        var invalidProjectile = new Projectile(null, userId, "",
                getQuantity(-1, GRAM), "",
                of(-1, getCurrency("CAD")), -1);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .post()
                .uri("/projectiles")
                .header("Authorization", "Bearer " + userId)
                .body(just(invalidProjectile), Projectile.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(errorMessage -> {
                    assertThat(errorMessage).contains("Manufacturer is required");
                    assertThat(errorMessage).contains("Type is required");
                    assertThat(errorMessage).contains("Weight must be positive");
                    assertThat(errorMessage).contains("Cost must be non-negative");
                    assertThat(errorMessage).contains("Quantity per box must be positive");
                });
    }

    @Test
    void updateProjectile() {
        var userId = randomUUID().toString();

        var savedProjectile = projectileRepository.save(createTestProjectile(userId)).block();

        var updatedProjectile = new Projectile(
                savedProjectile.id(),
                userId,
                "Sierra",
                getQuantity(0.168, KILOGRAM),
                "MatchKing",
                of(49.99, getCurrency("CAD")),
                50);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .put()
                .uri("/projectiles/{id}", savedProjectile.id())
                .header("Authorization", "Bearer " + userId)
                .contentType(APPLICATION_JSON)
                .body(just(updatedProjectile), Projectile.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Projectile.class)
                .value(p -> {
                    assertThat(p.id()).isEqualTo(savedProjectile.id());
                    assertThat(p.manufacturer()).isEqualTo("Sierra");
                    assertThat(p.type()).isEqualTo("MatchKing");
                    assertThat(p.weight().getValue().doubleValue()).isEqualTo(0.168);
                    assertThat(p.weight().getUnit().toString()).isEqualTo(KILOGRAM.toString());
                    assertThat(p.cost()).isEqualTo(of(49.99, getCurrency("CAD")));
                    assertThat(p.quantityPerBox()).isEqualTo(50);
                });
    }

    @Test
    void updateProjectileNotFound() {
        var userId = randomUUID().toString();

        var projectile = createTestProjectile(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .put()
                .uri("/projectiles/999")
                .header("Authorization", "Bearer " + userId)
                .contentType(APPLICATION_JSON)
                .body(just(projectile), Projectile.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteProjectile() {
        var userId = randomUUID().toString();

        var savedProjectile = projectileRepository.save(createTestProjectile(userId)).block();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .delete()
                .uri("/projectiles/{id}", savedProjectile.id())
                .header("Authorization", "Bearer " + userId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:view")))
                .get()
                .uri("/projectiles/{id}", savedProjectile.id())
                .header("Authorization", "Bearer " + userId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteProjectileNotFound() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"),
                        new SimpleGrantedAuthority("components:edit")))
                .delete()
                .uri("/projectiles/999")
                .header("Authorization", "Bearer " + userId)
                .exchange()
                .expectStatus().isNotFound();
    }

    private Projectile createTestProjectile(String ownerId) {
        return new Projectile(
                null,
                ownerId,
                "Hornady",
                getQuantity(178.0, GRAM),
                "ELD-X",
                of(52.99, getCurrency("CAD")),
                100);
    }
}
