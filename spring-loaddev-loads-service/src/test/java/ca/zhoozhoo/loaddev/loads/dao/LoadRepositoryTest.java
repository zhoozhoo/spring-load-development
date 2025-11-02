package ca.zhoozhoo.loaddev.loads.dao;

import static ca.zhoozhoo.loaddev.loads.model.Load.IMPERIAL;
import static java.util.UUID.randomUUID;
import static reactor.test.StepVerifier.create;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.model.Load;
import reactor.core.publisher.Flux;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class LoadRepositoryTest {

    @Autowired
    private LoadRepository loadRepository;

    @Autowired
    private ShotRepository shotRepository;

    @Autowired
    private GroupRepository groupRepository;

    private Random random = new Random();

    @BeforeEach
    public void setup() {
        shotRepository.deleteAll().block();
        groupRepository.deleteAll().block();
        loadRepository.deleteAll().block();
    }

    private Load createTestLoad(String ownerId) {
        return new Load(null,
                ownerId,
                "SMK 53 HP H335 " + random.nextInt(100),
                "SMK 53gr HP with Hodgdon H335",
                IMPERIAL,
                "Hodgdon",
                "H335",
                "Sierra",
                "MatchKing HP",
                53.0,
                "Federal",
                "205M",
                0.020,
                2.260,
                0.002,
                1L);
    }

    @Test
    void findByIdAndOwnerId() {
        var ownerId = randomUUID().toString();
        var savedLoad = loadRepository.save(createTestLoad(ownerId)).block();

        create(loadRepository.findByIdAndOwnerId(savedLoad.id(), savedLoad.ownerId()))
                .expectNextMatches(l -> l.id().equals(savedLoad.id()))
                .verifyComplete();
    }

    @Test
    void findAllByOwnerId() {
        var ownerId = randomUUID().toString();
        loadRepository.saveAll(Flux.just(
                createTestLoad(ownerId),
                new Load(null,
                        ownerId,
                        "Hornady 52 BTHP 4198",
                        "Hornady 52gr 4198 BTHP with IMR 4198",
                        IMPERIAL,
                        "IMR",
                        "4198",
                        "Hornady",
                        "BTHP Match",
                        52.0,
                        "Federal",
                        "205M",
                        0.020,
                        2.250,
                        0.003,
                        2L)))
                .blockLast();

        create(loadRepository.findAllByOwnerId(ownerId))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByNameAndOwnerId() {
        var savedLoad = loadRepository.saveAll(Flux.just(
                createTestLoad(randomUUID().toString()),
                new Load(null,
                        randomUUID().toString(),
                        "Hornady 52 BTHP 4198",
                        "Hornady 52gr 4198 BTHP with IMR 4198",
                        IMPERIAL,
                        "IMR",
                        "4198",
                        "Hornady",
                        "BTHP Match",
                        52.0,
                        "Federal",
                        "205M",
                        0.020,
                        2.250,
                        0.003,
                        1L)))
                .blockFirst();

        create(loadRepository.findByNameAndOwnerId(savedLoad.name(), savedLoad.ownerId()))
                .expectNextMatches(l -> l.name().equals(savedLoad.name()))
                .verifyComplete();
    }

    @Test
    void save() {
        create(loadRepository.save(createTestLoad(randomUUID().toString())))
                .expectNextMatches(l -> l.id() != null)
                .verifyComplete();
    }

    @Test
    void update() {
        var savedLoad = loadRepository.save(createTestLoad(randomUUID().toString())).block();

        create(loadRepository.save(new Load(savedLoad.id(),
                randomUUID().toString(),
                "SMK 53 HP H335 " + random.nextInt(100),
                "SMK 53gr HP with Hodgdon H335",
                IMPERIAL,
                "Hodgdon",
                "H335",
                "Sierra",
                "MatchKing HP",
                53.0,
                "Federal",
                "205M",
                0.020,
                2.260,
                0.002,
                1L)))
                .expectNextMatches(
                        l -> l.id().equals(savedLoad.id()) && l.name().startsWith("SMK 53 HP H335"))
                .verifyComplete();
    }

    @Test
    void delete() {
        var savedLoad = loadRepository.save(createTestLoad(randomUUID().toString())).block();

        create(loadRepository.deleteById(savedLoad.id())).verifyComplete();
        create(loadRepository.findById(savedLoad.id())).expectNextCount(0).verifyComplete();
    }
}