package ca.zhoozhoo.loaddev.loads.web;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static reactor.core.publisher.Mono.just;

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
import ca.zhoozhoo.loaddev.loads.model.Group;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
public class GroupsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    public void setup() {
        groupRepository.deleteAll().block();
    }

    private Group createAndSaveGroup(String ownerId) {
        return groupRepository
                .save(new Group(null, ownerId, 5, 100, 1.5, 3000, 3000, 2900, 3100, 50, 200)).block();
    }

    @Test
    public void getAllGroups() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var group1 = createAndSaveGroup(userId);
        var group2 = groupRepository
                .save(new Group(null, userId, 10, 200, 2.5, 3100, 3100, 3000, 3200, 60, 300)).block();

        webTestClient.mutateWith(jwt).get().uri("/groups")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Group.class)
                .value(groups -> {
                    assertThat(groups).hasSize(2);
                    assertThat(groups).contains(group1, group2);
                });
    }

    @Test
    public void getGroupById() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var group = createAndSaveGroup(userId);

        webTestClient.mutateWith(jwt).get().uri("/groups/" + group.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Group.class)
                .value(returnedGroup -> {
                    assertThat(returnedGroup).isEqualTo(group);
                });
    }

    @Test
    public void createGroup() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var newGroup = new Group(null, randomUUID().toString(), 5, 100, 1.5, 3000, 3000, 2900, 3100, 50, 200);

        webTestClient.mutateWith(jwt).post().uri("/groups")
                .contentType(APPLICATION_JSON)
                .body(just(newGroup), Group.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Group.class)
                .value(group -> {
                    assertThat(group.id()).isNotNull();
                    assertThat(group.numberOfShots()).isEqualTo(newGroup.numberOfShots());
                    assertThat(group.targetRange()).isEqualTo(newGroup.targetRange());
                    assertThat(group.groupSize()).isEqualTo(newGroup.groupSize());
                    assertThat(group.mean()).isEqualTo(newGroup.mean());
                    assertThat(group.median()).isEqualTo(newGroup.median());
                    assertThat(group.min()).isEqualTo(newGroup.min());
                    assertThat(group.max()).isEqualTo(newGroup.max());
                    assertThat(group.standardDeviation()).isEqualTo(newGroup.standardDeviation());
                    assertThat(group.extremeSpread()).isEqualTo(newGroup.extremeSpread());
                });
    }

    @Test
    public void updateGroup() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var group = createAndSaveGroup(userId);

        var updatedGroup = new Group(null, randomUUID().toString(), 10, 200, 2.5, 3100, 3100, 3000, 3200, 60, 300);

        webTestClient.mutateWith(jwt).put().uri("/groups/" + group.id())
                .contentType(APPLICATION_JSON)
                .body(just(updatedGroup), Group.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Group.class)
                .value(returnedGroup -> {
                    assertThat(returnedGroup.id()).isEqualTo(group.id());
                    assertThat(returnedGroup.numberOfShots()).isEqualTo(updatedGroup.numberOfShots());
                    assertThat(returnedGroup.targetRange()).isEqualTo(updatedGroup.targetRange());
                    assertThat(returnedGroup.groupSize()).isEqualTo(updatedGroup.groupSize());
                    assertThat(returnedGroup.mean()).isEqualTo(updatedGroup.mean());
                    assertThat(returnedGroup.median()).isEqualTo(updatedGroup.median());
                    assertThat(returnedGroup.min()).isEqualTo(updatedGroup.min());
                    assertThat(returnedGroup.max()).isEqualTo(updatedGroup.max());
                    assertThat(returnedGroup.standardDeviation()).isEqualTo(updatedGroup.standardDeviation());
                    assertThat(returnedGroup.extremeSpread()).isEqualTo(updatedGroup.extremeSpread());
                });
    }

    @Test
    public void deleteGroup() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var group = createAndSaveGroup(userId);

        webTestClient.mutateWith(jwt).delete().uri("/groups/" + group.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(jwt).get().uri("/groups/" + group.id())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void getNonExistentGroup() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).get().uri("/groups/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void createGroupWithInvalidData() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var invalidGroup = new Group(null, userId, -5, -100, -1.5, 0, 0, 0, 0, -50, -200);

        webTestClient.mutateWith(jwt).post().uri("/groups")
                .contentType(APPLICATION_JSON)
                .body(just(invalidGroup), Group.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void createGroupWithNullData() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var invalidGroup = new Group(null, null, null, null, null, null, null, null, null, null, null);

        webTestClient.mutateWith(jwt).post().uri("/groups")
                .contentType(APPLICATION_JSON)
                .body(just(invalidGroup), Group.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void updateNonExistentGroup() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var group = new Group(null, userId, 5, 100, 1.5, 3000, 3000, 2900, 3100, 50, 200);

        webTestClient.mutateWith(jwt).put().uri("/groups/999")
                .contentType(APPLICATION_JSON)
                .body(just(group), Group.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void updateGroupWithInvalidData() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        var group = createAndSaveGroup(userId);
        var invalidGroup = new Group(null, userId, -5, -100, -1.5, 0, 0, 0, 0, -50, -200);

        webTestClient.mutateWith(jwt).put().uri("/groups/" + group.id())
                .contentType(APPLICATION_JSON)
                .body(just(invalidGroup), Group.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void deleteNonExistentGroup() {
        var userId = randomUUID().toString();
        var jwt = mockJwt().jwt(token -> token.claim("sub", userId));

        webTestClient.mutateWith(jwt).delete().uri("/groups/999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
