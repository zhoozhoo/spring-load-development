package ca.zhoozhoo.loaddev.loads.dao;

import static ca.zhoozhoo.loaddev.loads.model.Unit.GRAINS;
import static ca.zhoozhoo.loaddev.loads.model.Unit.INCHES;
import static java.util.UUID.randomUUID;

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
import reactor.test.StepVerifier;

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
                "Hodgdon",
                "H335",
                "Sierra",
                "MatchKing HP",
                53.0,
                GRAINS,
                "Federal",
                "205M",
                0.020,
                INCHES,
                2.260,
                INCHES,
                0.002,
                INCHES,
                1L);
    }

    @Test
    void findByIdAndOwnerId() {
        var ownerId = randomUUID().toString();
        var load = loadRepository.save(createTestLoad(ownerId)).block();

        var result = loadRepository.findByIdAndOwnerId(load.id(), load.ownerId());

        StepVerifier.create(result)
                .expectNextMatches(l -> l.id().equals(load.id()))
                .verifyComplete();
    }

    @Test
    void findAllByOwnerId() {
        var ownerId = randomUUID().toString();
        var load1 = createTestLoad(ownerId);
        var load2 = new Load(null,
                ownerId,
                "Hornady 52 BTHP 4198",
                "Hornady 52gr 4198 BTHP with IMR 4198",
                "IMR",
                "4198",
                "Hornady",
                "BTHP Match",
                52.0,
                GRAINS,
                "Federal",
                "205M",
                0.020,
                INCHES,
                2.250,
                INCHES,
                0.003,
                INCHES,
                2L);

        loadRepository.saveAll(Flux.just(load1, load2))
                .blockLast();

        var result = loadRepository.findAllByOwnerId(ownerId);

        StepVerifier.create(result)
                .expectNextMatches(l -> l.name().equals(load1.name()))
                .expectNextMatches(l -> l.name().equals("Hornady 52 BTHP 4198"))
                .verifyComplete();
    }

    @Test
    void findByNameAndOwnerId() {
        var load1 = createTestLoad(randomUUID().toString());
        var load2 = new Load(null,
                randomUUID().toString(),
                "Hornady 52 BTHP 4198",
                "Hornady 52gr 4198 BTHP with IMR 4198",
                "IMR",
                "4198",
                "Hornady",
                "BTHP Match",
                52.0,
                GRAINS,
                "Federal",
                "205M",
                0.020,
                INCHES,
                2.250,
                INCHES,
                0.003,
                INCHES,
                1L);

        loadRepository.saveAll(Flux.just(load1, load2)).blockLast();

        var result = loadRepository.findByNameAndOwnerId(load1.name(), load1.ownerId());

        StepVerifier.create(result)
                .expectNextMatches(l -> l.name().equals(load1.name()))
                .verifyComplete();
    }

    @Test
    void save() {
        var load = createTestLoad(randomUUID().toString());
        var savedLoad = loadRepository.save(load);

        StepVerifier.create(savedLoad)
                .expectNextMatches(l -> l.id() != null && l.name().equals(load.name()))
                .verifyComplete();
    }

    @Test
    void update() {
        var savedLoad = loadRepository.save(createTestLoad(randomUUID().toString())).block();

        var updatedLoad = new Load(savedLoad.id(),
                randomUUID().toString(),
                "SMK 53 HP H335 " + random.nextInt(100),
                "SMK 53gr HP with Hodgdon H335",
                "Hodgdon",
                "H335",
                "Sierra",
                "MatchKing HP",
                53.0,
                GRAINS,
                "Federal",
                "205M",
                0.020,
                INCHES,
                2.260,
                INCHES,
                0.002,
                INCHES,
                1L);

        var result = loadRepository.save(updatedLoad);

        StepVerifier.create(result)
                .expectNextMatches(
                        l -> l.id().equals(savedLoad.id()) && l.name().equals(updatedLoad.name()))
                .verifyComplete();
    }

    @Test
    void delete() {
        var savedLoad = loadRepository.save(createTestLoad(randomUUID().toString())).block();
        var result = loadRepository.deleteById(savedLoad.id());
        StepVerifier.create(result).verifyComplete();

        var deletedLoad = loadRepository.findById(savedLoad.id());
        StepVerifier.create(deletedLoad)
                .expectNextCount(0)
                .verifyComplete();
    }
}