package ca.zhoozhoo.loaddev.loads.web;

import static ca.zhoozhoo.loaddev.loads.model.Load.IMPERIAL;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static reactor.core.publisher.Mono.just;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
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
        return loadRepository.save(new Load(null, ownerId, "Load", "Description", IMPERIAL,
                "Manufacturer", "Type",
                "BulletManufacturer", "BulletType", 100.0,
                "PrimerManufacturer", "PrimerType",
                0.020,
                2.800,
                0.002,
                null)).block();
    }

    private Group createAndSaveGroup(String ownerId) {
        return groupRepository
                .save(new Group(null, ownerId, createAndSaveLoad(ownerId).id(), LocalDate.now(), 26.5, 100, 0.40)).block();
    }

    private Shot createAndSaveShot(Group group, int velocity) {
        return shotRepository.save(new Shot(null,
                group.ownerId(),
                group.id(),
                velocity)).block();
    }

    @Test
    public void getShotsByGroupId() {
        var userId = randomUUID().toString();
        var group = createAndSaveGroup(userId);

        createAndSaveShot(group, 3000);
        createAndSaveShot(group, 3100);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).get().uri("/shots/group/" + group.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Shot.class)
                .value(shots -> assertThat(shots).hasSize(2));
    }

    @Test
    public void getShotById() {
        var userId = randomUUID().toString();
        var shot1 = createAndSaveShot(createAndSaveGroup(userId), 3000);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).get().uri("/shots/" + shot1.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Shot.class)
                .value(shot -> {
                    assertThat(shot.id()).isNotNull();
                    assertThat(shot.groupId()).isEqualTo(shot1.groupId());
                    assertThat(shot.velocity()).isEqualTo(shot1.velocity());
                });
    }

    @Test
    public void createShot() {
        var userId = randomUUID().toString();
        var group = createAndSaveGroup(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).post().uri("/shots")
                .contentType(APPLICATION_JSON)
                .body(just(new Shot(null, userId, group.id(), 3200)), Shot.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Shot.class)
                .value(shot -> {
                    assertThat(shot.id()).isNotNull();
                    assertThat(shot.groupId()).isEqualTo(group.id());
                    assertThat(shot.velocity()).isEqualTo(3200);
                });
    }

    @Test
    public void updateShot() {
        var userId = randomUUID().toString();
        var group = createAndSaveGroup(userId);
        var shot1 = createAndSaveShot(group, 3000);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).put().uri("/shots/" + shot1.id())
                .contentType(APPLICATION_JSON)
                .body(just(new Shot(null, userId, group.id(), 3300)), Shot.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Shot.class)
                .value(shot -> {
                    assertThat(shot.id()).isEqualTo(shot1.id());
                    assertThat(shot.groupId()).isEqualTo(group.id());
                    assertThat(shot.velocity()).isEqualTo(3300);
                });
    }

    @Test
    public void deleteShot() {
        var userId = randomUUID().toString();
        var shot1 = createAndSaveShot(createAndSaveGroup(userId), 3000);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).delete().uri("/shots/" + shot1.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).get().uri("/shots/" + shot1.id())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void getNonExistentShot() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).get().uri("/shots/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void getShotsByNonExistentGroup() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).get().uri("/shots/group/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Shot.class).hasSize(0);
    }

    @Test
    public void createShotWithInvalidData() {
        // Constructor validation now prevents creating invalid shots
        // Test that constructor properly rejects invalid velocity
        assertThrows(IllegalArgumentException.class, () -> {
            new Shot(null, randomUUID().toString(), null, 100);  // Invalid: below 500 fps minimum
        });
    }

    @Test
    public void createShotWithNullData() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).post().uri("/shots")
                .contentType(APPLICATION_JSON)
                .body(just(new Shot(null, userId, null, null)), Shot.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void updateNonExistentShot() {
        var userId = randomUUID().toString();
        var group = createAndSaveGroup(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).put().uri("/shots/999")
                .contentType(APPLICATION_JSON)
                .body(just(new Shot(null, randomUUID().toString(), group.id(), 3000)), Shot.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void updateShotWithInvalidData() {
        // Constructor validation now prevents creating invalid shots
        // Test that constructor properly rejects invalid velocity
        assertThrows(IllegalArgumentException.class, () -> {
            new Shot(1L, randomUUID().toString(), null, 100);  // Invalid: below 500 fps minimum
        });
    }

    @Test
    public void deleteNonExistentShot() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).delete().uri("/shots/999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
