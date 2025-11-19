package ca.zhoozhoo.loaddev.loads.web;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static systems.uom.ucum.UCUM.GRAIN;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static systems.uom.ucum.UCUM.YARD_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.dao.GroupRepository;
import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.model.Group;
import ca.zhoozhoo.loaddev.loads.model.Load;

/**
 * Integration tests for GroupsController.
 * <p>
 * Tests all REST endpoints for group management including CRUD operations,
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
@DisplayName("GroupsController Integration Tests")
public class GroupsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private LoadRepository loadRepository;

    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    public void setup() {
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
                        getQuantity(0.75, INCH_INTERNATIONAL)))
                .block();
    }

    // ========================================
    // Positive Test Cases
    // ========================================

    @Test
    @DisplayName("[Positive] Should get all groups for a load")
    public void getAllGroups() {
        var userId = randomUUID().toString();
        var group1 = createAndSaveGroup(userId);

        groupRepository
                .save(new Group(null, userId,
                        group1.loadId(),
                        now(),
                        getQuantity(44.0, GRAIN),
                        getQuantity(200, YARD_INTERNATIONAL),
                        getQuantity(0.85, INCH_INTERNATIONAL)))
                .block();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("groups:view")))
                .get()
                .uri("/groups/load/" + group1.loadId())
                .header("Authorization", "Bearer " + userId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Group.class)
                .value(groups -> assertThat(groups).hasSize(2));
    }

    @Test
    @DisplayName("[Positive] Should get group by ID")
    public void getGroupById() {
        var userId = randomUUID().toString();
        var group = createAndSaveGroup(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("groups:view")))
                .get()
                .uri("/groups/" + group.id())
                .header("Authorization", "Bearer " + userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Group.class)
                .value(result -> {
                    assertThat(result.id()).isEqualTo(group.id());
                    assertThat(result.loadId()).isEqualTo(group.loadId());
                    // Compare Quantity values using double comparison
                    assertThat(result.powderCharge().getValue().doubleValue())
                            .isCloseTo(group.powderCharge().getValue().doubleValue(), within(0.01));
                    assertThat(result.targetRange().getValue().doubleValue())
                            .isCloseTo(group.targetRange().getValue().doubleValue(), within(0.01));
                    assertThat(result.groupSize().getValue().doubleValue())
                            .isCloseTo(group.groupSize().getValue().doubleValue(), within(0.01));
                });
    }

    @Test
    @DisplayName("[Positive] Should create a new group")
    public void createGroup() {
        var userId = randomUUID().toString();
        var load = createAndSaveLoad(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("groups:edit")))
                .post()
                .uri("/groups")
                .header("Authorization", "Bearer " + userId)
                .contentType(APPLICATION_JSON)
                .bodyValue(new Group(null, userId, load.id(), now(),
                        getQuantity(43.5, GRAIN),
                        getQuantity(100, YARD_INTERNATIONAL),
                        getQuantity(0.75, INCH_INTERNATIONAL)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Group.class)
                .value(createdGroup -> {
                    assertThat(createdGroup.id()).isNotNull();
                    assertThat(createdGroup.loadId()).isEqualTo(load.id());
                });
    }

    @Test
    @DisplayName("[Positive] Should update an existing group")
    public void updateGroup() {
        var userId = randomUUID().toString();
        var group = createAndSaveGroup(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("groups:edit")))
                .put()
                .uri("/groups/" + group.id())
                .header("Authorization", "Bearer " + userId)
                .contentType(APPLICATION_JSON)
                .bodyValue(new Group(group.id(), group.ownerId(), group.loadId(),
                        now(),
                        getQuantity(44.0, GRAIN),
                        getQuantity(200, YARD_INTERNATIONAL),
                        getQuantity(0.85, INCH_INTERNATIONAL)))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Group.class)
                .value(result -> {
                    assertThat(result.powderCharge().getValue().doubleValue())
                            .isCloseTo(44.0, within(0.01));
                    assertThat(result.targetRange().getValue().doubleValue())
                            .isCloseTo(200, within(0.01));
                    assertThat(result.groupSize().getValue().doubleValue())
                            .isCloseTo(0.85, within(0.01));
                });
    }

    @Test
    @DisplayName("[Positive] Should delete an existing group")
    public void deleteGroup() {
        var userId = randomUUID().toString();
        var group = createAndSaveGroup(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("groups:delete")))
                .delete()
                .uri("/groups/" + group.id())
                .header("Authorization", "Bearer " + userId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("groups:view")))
                .get()
                .uri("/groups/" + group.id())
                .header("Authorization", "Bearer " + userId)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ========================================
    // Negative Test Cases - Not Found
    // ========================================

    @Test
    @DisplayName("[Negative] Should return 404 when getting non-existent group")
    public void getGroupByIdNotFound() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("groups:view")))
                .get()
                .uri("/groups/999")
                .header("Authorization", "Bearer " + userId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("[Negative] Should return 404 when updating non-existent group")
    public void updateGroupNotFound() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("groups:edit")))
                .put()
                .uri("/groups/999")
                .header("Authorization", "Bearer " + userId)
                .contentType(APPLICATION_JSON)
                .bodyValue(new Group(999L, userId, 1L, now(),
                        getQuantity(43.5, GRAIN),
                        getQuantity(100, YARD_INTERNATIONAL),
                        getQuantity(0.75, INCH_INTERNATIONAL)))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("[Negative] Should return 404 when deleting non-existent group")
    public void deleteGroupNotFound() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))
                .authorities(new SimpleGrantedAuthority("ROLE_RELOADER"), new SimpleGrantedAuthority("groups:delete")))
                .delete()
                .uri("/groups/999")
                .header("Authorization", "Bearer " + userId)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ========================================
    // Negative Test Cases - Validation
    // ========================================

    @Test
    @DisplayName("[Validation] Should throw exception when powder charge is too high")
    public void createGroupWithInvalidPowderCharge() {
        assertThrows(IllegalArgumentException.class, () -> new Group(null, "userId", 1L, now(),
                getQuantity(200, GRAIN), // Too high
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.75, INCH_INTERNATIONAL)));
    }

    @Test
    @DisplayName("[Validation] Should throw exception when target range is too far")
    public void createGroupWithInvalidTargetRange() {
        assertThrows(IllegalArgumentException.class, () -> new Group(null, "userId", 1L, now(),
                getQuantity(43.5, GRAIN),
                getQuantity(5000, YARD_INTERNATIONAL), // Too far
                getQuantity(0.75, INCH_INTERNATIONAL)));
    }

    @Test
    @DisplayName("[Validation] Should throw exception when group date is in the future")
    public void createGroupWithFutureDate() {
        assertThrows(IllegalArgumentException.class, () -> new Group(null, "userId", 1L, LocalDate.now().plusDays(1),
                getQuantity(43.5, GRAIN),
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.75, INCH_INTERNATIONAL)));
    }
}
