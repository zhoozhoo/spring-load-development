package ca.zhoozhoo.loaddev.loads.web;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static systems.uom.ucum.UCUM.FOOT_INTERNATIONAL;
import static systems.uom.ucum.UCUM.GRAIN;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static systems.uom.ucum.UCUM.YARD_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;

import javax.measure.Unit;
import javax.measure.quantity.Speed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.dao.GroupRepository;
import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import ca.zhoozhoo.loaddev.loads.model.Group;
import ca.zhoozhoo.loaddev.loads.model.Load;
import ca.zhoozhoo.loaddev.loads.model.Shot;
import tech.units.indriya.unit.Units;

/**
 * Integration tests for ShotsController.
 * <p>
 * Tests all REST endpoints for shot management including CRUD operations,
 * validation, and security. Uses WebTestClient for reactive endpoint testing with
 * mock JWT authentication.
 * </p>
 *
 * @author Zhubin Salehi
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
@DisplayName("ShotsController Integration Tests")
public class ShotsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private LoadRepository loadRepository;

    @Autowired
    private ShotRepository shotRepository;

    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    public void setup() {
        shotRepository.deleteAll().block();
        groupRepository.deleteAll().block();
    }

    private Load createAndSaveLoad(String ownerId) {
        return loadRepository.save(new Load(null, ownerId, "Load", "Description",
                "Hodgdon", "H4350",
                "Hornady", "ELD-M", getQuantity(168, GRAIN),
                "CCI", "BR-2",
                getQuantity(0.020, INCH_INTERNATIONAL),
                getQuantity(2.800, INCH_INTERNATIONAL),
                getQuantity(0.002, INCH_INTERNATIONAL),
                null)).block();
    }

    private Group createAndSaveGroup(String ownerId) {
        return groupRepository
                .save(new Group(null, ownerId, createAndSaveLoad(ownerId).id(), now(),
                        getQuantity(43.5, GRAIN),
                        getQuantity(100, YARD_INTERNATIONAL),
                        getQuantity(0.75, INCH_INTERNATIONAL))).block();
    }

    @SuppressWarnings("unchecked")
    private Shot createAndSaveShot(Group group, double velocityInFps) {
        return shotRepository.save(new Shot(null,
                group.ownerId(),
                group.id(),
                getQuantity(velocityInFps, (Unit<Speed>) FOOT_INTERNATIONAL.divide(Units.SECOND)))).block();
    }

    // ========================================
    // Positive Test Cases
    // ========================================

    @Test
    @DisplayName("[Positive] Should get all shots for a group")
    public void getShotsByGroupId() {
        var userId = randomUUID().toString();
        var group = createAndSaveGroup(userId);

        createAndSaveShot(group, 2800.0);
        createAndSaveShot(group, 2810.0);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .get().uri("/shots/group/" + group.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Shot.class)
                .value(shots -> assertThat(shots).hasSize(2));
    }

    @Test
    @DisplayName("[Positive] Should get shot by ID")
    public void getShotById() {
        var userId = randomUUID().toString();
        var shot = createAndSaveShot(createAndSaveGroup(userId), 2800.0);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .get().uri("/shots/" + shot.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Shot.class)
                .value(result -> {
                    assertThat(result.id()).isEqualTo(shot.id());
                    assertThat(result.velocity()).isEqualTo(shot.velocity());
                });
    }

    @Test
    @DisplayName("[Positive] Should create a new shot")
    public void createShot() {
        var userId = randomUUID().toString();
        var groupId = createAndSaveGroup(userId).id();

        @SuppressWarnings("unchecked")
        var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(Units.SECOND);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .post().uri("/shots")
                .contentType(APPLICATION_JSON)
                .bodyValue(new Shot(null, userId, groupId, getQuantity(2800.0, feetPerSecond)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Shot.class)
                .value(createdShot -> {
                    assertThat(createdShot.id()).isNotNull();
                    assertThat(createdShot.groupId()).isEqualTo(groupId);
                    assertThat(createdShot.velocity()).isEqualTo(getQuantity(2800.0, feetPerSecond));
                });
    }

    @Test
    @DisplayName("[Positive] Should update an existing shot")
    public void updateShot() {
        var userId = randomUUID().toString();
        var shot = createAndSaveShot(createAndSaveGroup(userId), 2800.0);

        @SuppressWarnings("unchecked")
        var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(Units.SECOND);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .put().uri("/shots/" + shot.id())
                .contentType(APPLICATION_JSON)
                .bodyValue(new Shot(shot.id(), shot.ownerId(), shot.groupId(), getQuantity(2850.0, feetPerSecond)))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Shot.class)
                .value(result -> assertThat(result.velocity()).isEqualTo(getQuantity(2850.0, feetPerSecond)));
    }

    @Test
    @DisplayName("[Positive] Should delete an existing shot")
    public void deleteShot() {
        var userId = randomUUID().toString();
        var shot = createAndSaveShot(createAndSaveGroup(userId), 2800.0);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .delete().uri("/shots/" + shot.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .get().uri("/shots/" + shot.id())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("[Positive] Should get empty list for non-existent group")
    public void getShotsByNonExistentGroup() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .get().uri("/shots/group/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Shot.class)
                .value(shots -> assertThat(shots).isEmpty());
    }

    @Test
    @DisplayName("[Positive] Should create multiple shots for same group")
    public void createMultipleShotsForSameGroup() {
        var userId = randomUUID().toString();
        var group = createAndSaveGroup(userId);

        createAndSaveShot(group, 2800.0);
        createAndSaveShot(group, 2810.0);
        createAndSaveShot(group, 2805.0);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .get().uri("/shots/group/" + group.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Shot.class)
                .value(shots -> {
                    assertThat(shots).hasSize(3);
                    assertThat(shots).allMatch(shot -> shot.groupId().equals(group.id()));
                });
    }

    // ========================================
    // Negative Test Cases - Not Found
    // ========================================

    @Test
    @DisplayName("[Negative] Should return 404 when getting non-existent shot")
    public void getShotByIdNotFound() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .get().uri("/shots/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("[Negative] Should return 404 when updating non-existent shot")
    public void updateShotNotFound() {
        var userId = randomUUID().toString();

        @SuppressWarnings("unchecked")
        var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(Units.SECOND);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .put().uri("/shots/999")
                .contentType(APPLICATION_JSON)
                .bodyValue(new Shot(999L, userId, 1L, getQuantity(2800.0, feetPerSecond)))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("[Negative] Should return 404 when deleting non-existent shot")
    public void deleteShotNotFound() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId)))
                .delete().uri("/shots/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    // ========================================
    // Negative Test Cases - Validation
    // ========================================

    @Test
    @DisplayName("[Validation] Should throw exception when velocity is too high")
    public void createShotWithInvalidVelocity() {
        @SuppressWarnings("unchecked")
        var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(Units.SECOND);

        assertThrows(IllegalArgumentException.class, () ->
                new Shot(null, "userId", 1L, getQuantity(10000.0, feetPerSecond)));
    }

    @Test
    @DisplayName("[Validation] Should throw exception when velocity is too low")
    public void createShotWithTooLowVelocity() {
        @SuppressWarnings("unchecked")
        var feetPerSecond = (Unit<Speed>) FOOT_INTERNATIONAL.divide(Units.SECOND);

        assertThrows(IllegalArgumentException.class, () ->
                new Shot(null, "userId", 1L, getQuantity(100.0, feetPerSecond)));
    }
}
