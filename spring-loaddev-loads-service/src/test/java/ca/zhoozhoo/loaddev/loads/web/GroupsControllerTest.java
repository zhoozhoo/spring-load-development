package ca.zhoozhoo.loaddev.loads.web;

import static ca.zhoozhoo.loaddev.loads.model.Load.IMPERIAL;
import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.model.Group;
import ca.zhoozhoo.loaddev.loads.model.Load;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
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
                .save(new Group(null, ownerId, createAndSaveLoad(ownerId).id(), now(), 26.5, 100, 0.40)).block();
    }

    @Test
    public void getAllGroups() {
        var userId = randomUUID().toString();
        var group1 = createAndSaveGroup(userId);

        groupRepository
                .save(new Group(null, userId,
                        group1.loadId(),
                        now(),
                        28.0,
                        200,
                        0.50))
                .block();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).get().uri("/groups/load/" + group1.loadId())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Group.class)
                .value(groups -> assertThat(groups).hasSize(2));
    }

    @Test
    public void getGroupById() {
        var userId = randomUUID().toString();
        var group = createAndSaveGroup(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).get().uri("/groups/" + group.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Group.class)
                .value(returnedGroup -> {
                    assertThat(returnedGroup.id()).isNotNull();
                    assertThat(returnedGroup.powderCharge()).isEqualTo(group.powderCharge());
                    assertThat(returnedGroup.targetRange()).isEqualTo(group.targetRange());
                    assertThat(returnedGroup.groupSize()).isEqualTo(group.groupSize());
                });
    }

    @Test
    public void createGroup() {
        var userId = randomUUID().toString();
        var load = createAndSaveLoad(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).post().uri("/groups")
                .contentType(APPLICATION_JSON)
                .body(just(new Group(null, userId, load.id(), now(), 26.5, 100, 0.40)), Group.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Group.class)
                .value(group -> {
                    assertThat(group.id()).isNotNull();
                    assertThat(group.powderCharge()).isEqualTo(26.5);
                    assertThat(group.targetRange()).isEqualTo(100);
                    assertThat(group.groupSize()).isEqualTo(0.40);
                });
    }

    @Test
    public void updateGroup() {
        var userId = randomUUID().toString();
        var group = createAndSaveGroup(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).put().uri("/groups/" + group.id())
                .contentType(APPLICATION_JSON)
                .body(just(new Group(null, userId, group.loadId(), now(), 28.0, 200, 0.50)), Group.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Group.class)
                .value(returnedGroup -> {
                    assertThat(returnedGroup.id()).isEqualTo(group.id());
                    assertThat(returnedGroup.powderCharge()).isEqualTo(28.0);
                    assertThat(returnedGroup.targetRange()).isEqualTo(200);
                    assertThat(returnedGroup.groupSize()).isEqualTo(0.50);
                });
    }

    @Test
    public void deleteGroup() {
        var userId = randomUUID().toString();
        var group = createAndSaveGroup(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).delete().uri("/groups/" + group.id())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).get().uri("/groups/" + group.id())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void getNonExistentGroup() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).get().uri("/groups/999")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void createGroupWithInvalidData() {
        // Constructor validation now prevents creating invalid groups
        // Test that constructor properly rejects invalid powder charge
        assertThrows(IllegalArgumentException.class, () -> {
            new Group(null, randomUUID().toString(),
                    1L,
                    now(),
                    0.05,  // Invalid: below 0.1 minimum
                    100,
                    1.5);
        });
    }

    @Test
    public void createGroupWithNullData() {
        var userId = randomUUID().toString();

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).post().uri("/groups")
                .contentType(APPLICATION_JSON)
                .body(just(new Group(null, null, null, null, null, null, null)), Group.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void updateNonExistentGroup() {
        var userId = randomUUID().toString();
        var load = createAndSaveLoad(userId);

        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", userId))).put().uri("/groups/999")
                .contentType(APPLICATION_JSON)
                .body(just(new Group(null, userId, load.id(), now(), 26.5, 100, 0.40)), Group.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void updateGroupWithInvalidData() {
        // Constructor validation now prevents creating invalid groups
        // Test that constructor properly rejects invalid powder charge
        assertThrows(IllegalArgumentException.class, () -> {
            new Group(null, randomUUID().toString(),
                    1L,
                    now(),
                    0.05,  // Invalid: below 0.1 minimum
                    100,
                    1.5);
        });
    }

    @Test
    public void deleteNonExistentGroup() {
        webTestClient.mutateWith(mockJwt().jwt(token -> token.claim("sub", randomUUID().toString()))).delete().uri("/groups/999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
