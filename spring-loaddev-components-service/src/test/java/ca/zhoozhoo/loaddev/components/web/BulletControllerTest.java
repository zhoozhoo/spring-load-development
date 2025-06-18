package ca.zhoozhoo.loaddev.components.web;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static ca.zhoozhoo.loaddev.components.model.Bullet.METRIC;
import static ca.zhoozhoo.loaddev.components.model.Bullet.IMPERIAL;
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
import ca.zhoozhoo.loaddev.components.dao.BulletRepository;
import ca.zhoozhoo.loaddev.components.model.Bullet;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
public class BulletControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BulletRepository bulletRepository;

    @BeforeEach
    void setUp() {
        bulletRepository.deleteAll().block();
    }

    @Test
    void getAllBullets() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).get().uri("/bullets")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Bullet.class);
    }

    @Test
    void getBulletById() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedBullet = bulletRepository.save(createTestBullet(userId)).block();

        webTestClient.mutateWith(jwt).get().uri("/bullets/{id}", savedBullet.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(Bullet.class)
                .isEqualTo(savedBullet);
    }

    @Test
    void getBulletByIdNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).get().uri("/bullets/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createBullet() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var bullet = createTestBullet(userId);

        webTestClient.mutateWith(jwt).post().uri("/bullets")
                .contentType(APPLICATION_JSON)
                .body(just(bullet), Bullet.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Bullet.class)
                .value(b -> {
                    assertThat(b.id()).isNotNull();
                    assertThat(b.manufacturer()).isEqualTo("Hornady");
                    assertThat(b.weight()).isEqualTo(140.0);
                    assertThat(b.type()).isEqualTo("ELD-Match");
                    assertThat(b.measurementUnits()).isEqualTo(IMPERIAL);
                    assertThat(b.cost()).isEqualTo(new BigDecimal("45.99"));
                    assertThat(b.currency()).isEqualTo("CAD");
                    assertThat(b.quantityPerBox()).isEqualTo(100);
                });
    }

    @Test
    void createBulletInvalidInput() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var invalidBullet = new Bullet(null, userId, "", -1.0, "", "", 
                new BigDecimal("-1"), "", -1);

        webTestClient.mutateWith(jwt).post().uri("/bullets")
                .contentType(APPLICATION_JSON)
                .body(just(invalidBullet), Bullet.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(errorMessage -> {
                    assertThat(errorMessage).contains("Manufacturer is required");
                    assertThat(errorMessage).contains("Weight must be positive");
                    assertThat(errorMessage).contains("Type is required");
                    assertThat(errorMessage).contains("Measurement Units is required");
                    assertThat(errorMessage).contains("Cost must be positive");
                    assertThat(errorMessage).contains("Currency is required");
                    assertThat(errorMessage).contains("Quantity per box must be positive");
                });
    }

    @Test
    void updateBullet() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedBullet = bulletRepository.save(createTestBullet(userId)).block();

        var updatedBullet = new Bullet(
                savedBullet.id(),
                userId,
                "Sierra",
                150.0,
                "MatchKing",
                METRIC,
                new BigDecimal("49.99"),
                "CAD",
                50);

        webTestClient.mutateWith(jwt).put().uri("/bullets/{id}", savedBullet.id())
                .contentType(APPLICATION_JSON)
                .body(just(updatedBullet), Bullet.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Bullet.class)
                .value(b -> {
                    assertThat(b.id()).isEqualTo(savedBullet.id());
                    assertThat(b.manufacturer()).isEqualTo("Sierra");
                    assertThat(b.weight()).isEqualTo(150.0);
                    assertThat(b.type()).isEqualTo("MatchKing");
                    assertThat(b.measurementUnits()).isEqualTo(METRIC);
                    assertThat(b.cost()).isEqualTo(new BigDecimal("49.99"));
                    assertThat(b.currency()).isEqualTo("CAD");
                    assertThat(b.quantityPerBox()).isEqualTo(50);
                });
    }

    @Test
    void updateBulletNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var bullet = createTestBullet(userId);

        webTestClient.mutateWith(jwt).put().uri("/bullets/999")
                .contentType(APPLICATION_JSON)
                .body(just(bullet), Bullet.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteBullet() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var savedBullet = bulletRepository.save(createTestBullet(userId)).block();

        webTestClient.mutateWith(jwt)
                .delete().uri("/bullets/{id}", savedBullet.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(jwt)
                .get().uri("/bullets/{id}", savedBullet.id())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteBulletNotFound() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).delete().uri("/bullets/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    private Bullet createTestBullet(String ownerId) {
        return new Bullet(
                null,
                ownerId,
                "Hornady",
                140.0,
                "ELD-Match",
                IMPERIAL,
                new BigDecimal("45.99"),
                "CAD",
                100);
    }
}
