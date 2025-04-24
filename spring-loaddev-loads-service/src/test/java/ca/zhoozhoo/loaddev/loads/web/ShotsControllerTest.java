package ca.zhoozhoo.loaddev.loads.web;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

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
import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import ca.zhoozhoo.loaddev.loads.model.Group;
import ca.zhoozhoo.loaddev.loads.model.Shot;
import ca.zhoozhoo.loaddev.loads.model.Unit;
import reactor.core.publisher.Mono;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
public class ShotsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ShotRepository shotRepository;

    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    public void setup() {
        shotRepository.deleteAll().block();
        groupRepository.deleteAll().block();
    }

    private Group createAndSaveGroup(String ownerId) {
        return groupRepository
                .save(new Group(null, ownerId, 
                    26.5, Unit.GRAINS,
                    100, Unit.YARDS,
                    0.40, Unit.INCHES)).block();
    }

    private Shot createAndSaveShot(Group group, int velocity) {
        return shotRepository.save(new Shot(null, 
            group.ownerId(), 
            group.id(), 
            velocity,
            Unit.METERS)).block();
    }

    @Test
    public void getShotsByGroupId() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var group = createAndSaveGroup(userId);
        var shot1 = createAndSaveShot(group, 3000);
        var shot2 = createAndSaveShot(group, 3100);

        webTestClient.mutateWith(jwt).get().uri("/shots/group/" + group.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Shot.class)
                .value(shots -> {
                    assertThat(shots).hasSize(2);
                    assertThat(shots).contains(shot1, shot2);
                });
    }

    @Test
    public void getShotById() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var group = createAndSaveGroup(userId);
        var shot1 = createAndSaveShot(group, 3000);

        webTestClient.mutateWith(jwt).get().uri("/shots/" + shot1.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Shot.class)
                .value(shot -> {
                    assertThat(shot).isEqualTo(shot1);
                });
    }

    @Test
    public void createShot() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var group = createAndSaveGroup(userId);
        var newShot = new Shot(null, userId, group.id(), 3200, Unit.METERS);

        webTestClient.mutateWith(jwt).post().uri("/shots")
                .contentType(APPLICATION_JSON)
                .body(Mono.just(newShot), Shot.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Shot.class)
                .value(shot -> {
                    assertThat(shot.id()).isNotNull();
                    assertThat(shot.groupId()).isEqualTo(newShot.groupId());
                    assertThat(shot.velocity()).isEqualTo(newShot.velocity());
                    assertThat(shot.velocityUnit()).isEqualTo(Unit.METERS);
                });
    }

    @Test
    public void updateShot() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var group = createAndSaveGroup(userId);
        var shot1 = createAndSaveShot(group, 3000);

        var updatedShot = new Shot(null, userId, group.id(), 3300, Unit.METERS);

        webTestClient.mutateWith(jwt).put().uri("/shots/" + shot1.id())
                .contentType(APPLICATION_JSON)
                .body(Mono.just(updatedShot), Shot.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Shot.class)
                .value(shot -> {
                    assertThat(shot.id()).isEqualTo(shot1.id());
                    assertThat(shot.groupId()).isEqualTo(updatedShot.groupId());
                    assertThat(shot.velocity()).isEqualTo(updatedShot.velocity());
                    assertThat(shot.velocityUnit()).isEqualTo(Unit.METERS);
                });
    }

    @Test
    public void deleteShot() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var group = createAndSaveGroup(userId);
        var shot1 = createAndSaveShot(group, 3000);

        webTestClient.mutateWith(jwt).delete().uri("/shots/" + shot1.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(jwt).get().uri("/shots/" + shot1.id())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void getNonExistentShot() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).get().uri("/shots/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void getShotsByNonExistentGroup() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).get().uri("/shots/group/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Shot.class).hasSize(0);
    }

    @Test
    public void createShotWithInvalidData() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var invalidShot = new Shot(null, userId, null, -100, null);

        webTestClient.mutateWith(jwt).post().uri("/shots")
                .contentType(APPLICATION_JSON)
                .body(Mono.just(invalidShot), Shot.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void createShotWithNullData() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var invalidShot = new Shot(null, userId, null, null, null);

        webTestClient.mutateWith(jwt).post().uri("/shots")
                .contentType(APPLICATION_JSON)
                .body(Mono.just(invalidShot), Shot.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void updateNonExistentShot() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var group = createAndSaveGroup(userId);
        var shot = new Shot(null, randomUUID().toString(), group.id(), 3000, Unit.METERS);

        webTestClient.mutateWith(jwt).put().uri("/shots/999")
                .contentType(APPLICATION_JSON)
                .body(Mono.just(shot), Shot.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void updateShotWithInvalidData() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var group = createAndSaveGroup(userId);
        var shot = createAndSaveShot(group, 3000);
        var invalidShot = new Shot(shot.id(), userId, null, -100, null);

        webTestClient.mutateWith(jwt).put().uri("/shots/{id}", shot.id())
                .contentType(APPLICATION_JSON)
                .body(Mono.just(invalidShot), Shot.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void deleteNonExistentShot() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).delete().uri("/shots/999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
