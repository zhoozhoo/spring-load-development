package ca.zhoozhoo.loaddev.rifles.web;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
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

import ca.zhoozhoo.loaddev.rifles.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.rifles.dao.RifleRepository;
import ca.zhoozhoo.loaddev.rifles.model.Rifle;

/**
 * Integration tests for {@link RifleController} using JSR-385.
 * <p>
 * Tests REST API endpoints for rifle CRUD operations using WebTestClient,
 * verifying HTTP responses, JSON serialization, validation, security integration,
 * and owner-based data isolation with mocked JWT authentication.
 * Uses JSR-385 Quantity&lt;Length&gt; for type-safe measurements.
 * </p>
 *
 * @author Zhubin Salehi
 */
@SpringBootTest(properties = "spring.autoconfigure.exclude=ca.zhoozhoo.loaddev.security.SecurityAutoConfiguration")
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
    void getAllRiflesFiltersByOwner() {
        var userId = randomUUID().toString();

        // Persist one rifle for authenticated user
        rifleRepository.save(new Rifle(null, userId,
                "User Owned Rifle",
                "Primary rifle for user",
                ".308 Winchester",
                getQuantity(24.0, INCH_INTERNATIONAL),
                "Medium Palma",
                "1:10",
                "6 Groove",
                getQuantity(0.160, INCH_INTERNATIONAL))).block();

        // Persist another rifle for different user
        rifleRepository.save(new Rifle(null, randomUUID().toString(),
                "Other User Rifle",
                "Secondary rifle for other user",
                "6.5 Creedmoor",
                getQuantity(26.0, INCH_INTERNATIONAL),
                "Heavy Palma",
                "1:8",
                "5R",
                getQuantity(0.157, INCH_INTERNATIONAL))).block();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("rifles:view")))
                .get()
                .uri("/rifles")
                .header("Authorization", "Bearer " + userId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Rifle.class)
                .value(rifles -> {
                    assert rifles.size() == 1 : "Expected only rifles for authenticated user";
                    assert rifles.get(0).ownerId().equals(userId) : "Rifle ownerId should match authenticated user";
                });
    }

    @Test
    void getRifleById() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("rifles:view")))
                .get()
                .uri("/rifles/{id}", rifleRepository.save(new Rifle(null, userId,
                        "Remington 700",
                        "Custom Remington 700 in 6.5 Creedmoor with Krieger barrel",
                        "6.5 Creedmoor",
                        getQuantity(26.0, INCH_INTERNATIONAL),
                        "Heavy Palma",
                        "1:8",
                        "5R",
                        getQuantity(0.157, INCH_INTERNATIONAL))).block().id())
                .header("Authorization", "Bearer " + userId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(Rifle.class);
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

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("rifles:edit")))
                .post()
                .uri("/rifles")
                .header("Authorization", "Bearer " + userId)
                .contentType(APPLICATION_JSON)
                .body(fromValue(new Rifle(null, userId,
                        "Savage 110 Elite Precision",
                        "Long range precision rifle with MDT chassis",
                        ".308 Winchester",
                        getQuantity(24.0, INCH_INTERNATIONAL),
                        "Medium Palma",
                        "1:10",
                        "6 Groove",
                        getQuantity(0.160, INCH_INTERNATIONAL))))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Rifle.class)
                .value(createdRifle -> {
                    assert createdRifle.id() != null;
                    assert createdRifle.name().equals("Savage 110 Elite Precision");
                    assert createdRifle.description().equals("Long range precision rifle with MDT chassis");
                    assert createdRifle.caliber().equals(".308 Winchester");
                });
    }

    @Test
    void createRifleInvalidInput() {
        // Constructor validation now prevents creating rifles with invalid measurements
        // Test that constructor properly rejects barrel length and free bore out of range
        assertThrows(IllegalArgumentException.class, () -> new Rifle(null, randomUUID().toString(), "", null, null,
                getQuantity(-1.0, INCH_INTERNATIONAL), "Contour", "", "Rifling",
                getQuantity(-0.5, INCH_INTERNATIONAL)));
    }

    @Test
    void createRifleWithInvalidBarrelLength() {
        // Constructor validation now prevents creating rifles with invalid barrel length
        // Test that constructor properly requires barrel length between 4 and 50 inches
        assertThrows(IllegalArgumentException.class,
                () -> new Rifle(null, randomUUID().toString(), "ValidName", "Description",
                        ".308", getQuantity(100.0, INCH_INTERNATIONAL), // Invalid barrel length
                        "Contour", "1:10", "Rifling", getQuantity(0.020, INCH_INTERNATIONAL)));
    }

    @Test
    void updateRifle() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("rifles:edit"),
                        new SimpleGrantedAuthority("rifles:view")))
                .put()
                .uri("/rifles/{id}", rifleRepository.save(new Rifle(null, userId,
                        "Tikka T3x",
                        "Factory Tikka T3x Tactical",
                        ".308 Winchester",
                        getQuantity(20.0, INCH_INTERNATIONAL),
                        "MTU",
                        "1:11",
                        "4 Groove",
                        getQuantity(0.157, INCH_INTERNATIONAL))).block().id())
                .header("Authorization", "Bearer " + userId)
                .contentType(APPLICATION_JSON)
                .body(fromValue(new Rifle(null, userId,
                        "Tikka T3x Custom",
                        "Tikka T3x with Bartlein barrel and MDT chassis",
                        "6mm Creedmoor",
                        getQuantity(26.0, INCH_INTERNATIONAL),
                        "M24",
                        "1:7.5",
                        "6 Groove",
                        getQuantity(0.153, INCH_INTERNATIONAL))))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Rifle.class)
                .value(rifleResponse -> {
                    assert rifleResponse.name().equals("Tikka T3x Custom");
                });
    }

    @Test
    void updateRifleNotFound() {
        webTestClient.put()
                .uri("/rifles/999")
                .contentType(APPLICATION_JSON)
                .body(fromValue(new Rifle(null, randomUUID().toString(), "Test Rifle", "Description",
                        "Caliber", getQuantity(20.0, INCH_INTERNATIONAL), "Contour", "1:10", "Rifling",
                        getQuantity(0.5, INCH_INTERNATIONAL))))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteRifle() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("rifles:delete"),
                        new SimpleGrantedAuthority("rifles:view"));
        var savedRifleId = rifleRepository
                .save(new Rifle(null, userId, "Rifle to be deleted", "Description", "Caliber",
                        getQuantity(20.0, INCH_INTERNATIONAL), "Contour", "1:10", "Rifling",
                        getQuantity(0.5, INCH_INTERNATIONAL)))
                .block().id();

        webTestClient.mutateWith(jwt)
                .delete().uri("/rifles/{id}", savedRifleId)
                .header("Authorization", "Bearer " + userId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(jwt)
                .get().uri("/rifles/{id}", savedRifleId)
                .header("Authorization", "Bearer " + userId)
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
