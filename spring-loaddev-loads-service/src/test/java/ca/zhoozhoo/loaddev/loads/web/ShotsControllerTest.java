package ca.zhoozhoo.loaddev.loads.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

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

    private Group createAndSaveGroup() {
        return groupRepository.save(new Group(null, 5, 100, 1.5, 3000, 3000, 2900, 3100, 50, 200)).block();
    }

    private Shot createAndSaveShot(Group group, int velocity) {
        return shotRepository.save(new Shot(null, group.id(), velocity)).block();
    }

    @Test
    public void getShotsByGroupId() {
        var group = createAndSaveGroup();
        var shot1 = createAndSaveShot(group, 3000);
        var shot2 = createAndSaveShot(group, 3100);

        webTestClient.get().uri("/shots/group/" + group.id())
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
        var group = createAndSaveGroup();
        var shot1 = createAndSaveShot(group, 3000);

        webTestClient.get().uri("/shots/" + shot1.id())
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
        var group = createAndSaveGroup();
        var newShot = new Shot(null, group.id(), 3200);

        webTestClient.post().uri("/shots")
                .contentType(APPLICATION_JSON)
                .body(Mono.just(newShot), Shot.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Shot.class)
                .value(shot -> {
                    assertThat(shot.id()).isNotNull();
                    assertThat(shot.groupId()).isEqualTo(newShot.groupId());
                    assertThat(shot.velocity()).isEqualTo(newShot.velocity());
                });
    }

    @Test
    public void updateShot() {
        var group = createAndSaveGroup();
        var shot1 = createAndSaveShot(group, 3000);

        var updatedShot = new Shot(null, group.id(), 3300);

        webTestClient.put().uri("/shots/" + shot1.id())
                .contentType(APPLICATION_JSON)
                .body(Mono.just(updatedShot), Shot.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Shot.class)
                .value(shot -> {
                    assertThat(shot.id()).isEqualTo(shot1.id());
                    assertThat(shot.groupId()).isEqualTo(updatedShot.groupId());
                    assertThat(shot.velocity()).isEqualTo(updatedShot.velocity());
                });
    }

    @Test
    public void deleteShot() {
        var group = createAndSaveGroup();
        var shot1 = createAndSaveShot(group, 3000);

        webTestClient.delete().uri("/shots/" + shot1.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get().uri("/shots/" + shot1.id())
                .exchange()
                .expectStatus().isNotFound();
    }
}
