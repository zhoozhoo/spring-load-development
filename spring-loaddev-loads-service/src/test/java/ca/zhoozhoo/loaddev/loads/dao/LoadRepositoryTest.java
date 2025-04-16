package ca.zhoozhoo.loaddev.loads.dao;

import static java.util.UUID.randomUUID;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.model.Load;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class LoadRepositoryTest {

    @Autowired
    private LoadRepository loadRepository;

    private Random random = new Random();

    @Test
    void findById() {
        Load load = new Load(1L,
                randomUUID().toString(),
                "SMK 53 HP H335 " + random.nextInt(100),
                "SMK 53gr HP with Hodgdon H335",
                "Hodgdon", "H335",
                26.9,
                "Sierra",
                "MatchKing HP",
                53.0,
                "Federal",
                "205M",
                0.020,
                1L);
        loadRepository.save(load).block();

        Mono<Load> result = loadRepository.findById(1L);

        StepVerifier.create(result)
                .expectNextMatches(l -> l.id().equals(1L))
                .verifyComplete();
    }

    @Test
    void save() {
        Load load = new Load(null,
                randomUUID().toString(),
                "SMK 53 HP H335 " + random.nextInt(100),
                "SMK 53gr HP with Hodgdon H335",
                "Hodgdon", "H335",
                26.9,
                "Sierra",
                "MatchKing HP",
                53.0,
                "Federal",
                "205M",
                0.020,
                1L);

        Mono<Load> savedLoad = loadRepository.save(load);

        StepVerifier.create(savedLoad)
                .expectNextMatches(l -> l.id() != null && l.name().equals(load.name()))
                .verifyComplete();
    }

    @Test
    void findAll() {

        Load load1 = new Load(null,
                randomUUID().toString(),
                "SMK 53 HP H335 " + random.nextInt(100),
                "SMK 53gr HP with Hodgdon H335",
                "Hodgdon", "H335",
                26.9,
                "Sierra",
                "MatchKing HP",
                53.0,
                "Federal",
                "205M",
                0.020,
                1L);

        Load load2 = new Load(null,
                randomUUID().toString(),
                "Hornady 52 BTHP 4198",
                "Hornady 52gr 4198 BTHP with IMR 4198",
                "IMR", "4198",
                20.0,
                "Hornady",
                "BTHP Match",
                52.0,
                "Federal",
                "205M",
                0.020,
                2L);

        loadRepository.saveAll(Flux.just(load1, load2))
                .blockLast();

        Flux<Load> result = loadRepository.findAll();

        StepVerifier.create(result)
                .expectNextMatches(l -> l.name().equals(load1.name()))
                .expectNextMatches(l -> l.name().equals("Hornady 52 BTHP 4198"))
                .verifyComplete();
    }

    @Test
    void deleteById() {
        Load load = new Load(null,
                randomUUID().toString(),
                "SMK 53 HP H335 " + random.nextInt(100),
                "SMK 53gr HP with Hodgdon H335",
                "Hodgdon", "H335",
                26.9,
                "Sierra",
                "MatchKing HP",
                53.0,
                "Federal",
                "205M",
                0.020,
                1L);

        Load savedLoad = loadRepository.save(load)
                .block();

        Mono<Void> result = loadRepository.deleteById(savedLoad.id());

        StepVerifier.create(result)
                .verifyComplete();

        Mono<Load> deletedLoad = loadRepository.findById(savedLoad.id());

        StepVerifier.create(deletedLoad)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void update() {
        Load load = new Load(null,
                randomUUID().toString(),
                "SMK 53 HP H335 " + random.nextInt(100),
                "SMK 53gr HP with Hodgdon H335",
                "Hodgdon", "H335",
                26.9,
                "Sierra",
                "MatchKing HP",
                53.0,
                "Federal",
                "205M",
                0.020,
                1L);
        Load savedLoad = loadRepository.save(load)
                .block();

        Load updatedLoad = new Load(savedLoad.id(),
                randomUUID().toString(),
                "SMK 53 HP H335 " + random.nextInt(100),
                "SMK 53gr HP with Hodgdon H335",
                "Hodgdon", "H335",
                26.5,
                "Sierra",
                "MatchKing HP",
                53.0,
                "Federal",
                "205M",
                0.020,
                1L);

        Mono<Load> result = loadRepository.save(updatedLoad);

        StepVerifier.create(result)
                .expectNextMatches(
                        l -> l.id().equals(savedLoad.id()) && l.name().equals(updatedLoad.name()))
                .verifyComplete();
    }

    @Test
    void findByName() {
        Load load1 = new Load(null,
                randomUUID().toString(),
                "SMK 53 HP H335 " + random.nextInt(100),
                "SMK 53gr HP with Hodgdon H335",
                "Hodgdon", "H335",
                26.9,
                "Sierra",
                "MatchKing HP",
                53.0,
                "Federal",
                "205M",
                0.020,
                1L);

        Load load2 = new Load(null,
                randomUUID().toString(),
                "Hornady 52 BTHP 4198",
                "Hornady 52gr 4198 BTHP with IMR 4198",
                "IMR", "4198",
                20.0,
                "Hornady",
                "BTHP Match",
                52.0,
                "Federal",
                "205M",
                0.020,
                1L);

        loadRepository.saveAll(Flux.just(load1, load2))
                .blockLast();

        Flux<Load> result = loadRepository.findByNameAndOwnerId(load1.name(), load1.ownerId());

        StepVerifier.create(result)
                .expectNextMatches(l -> l.name().equals(load1.name()))
                .verifyComplete();
    }
}