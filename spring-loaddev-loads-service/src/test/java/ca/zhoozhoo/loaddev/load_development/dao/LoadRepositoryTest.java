package ca.zhoozhoo.loaddev.load_development.dao;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.load_development.model.Load;
import ca.zhoozhoo.loaddev.load_development.model.Rifle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
class LoadRepositoryTest {

    @Autowired
    private LoadRepository loadRepository;

    @Autowired
    private RifleRepository rifleRepository;

    private Random random = new Random();

    @Test
    void findById() {
        Rifle rifle = new Rifle(null,
                "Tikka T3X Tact A1 223 Rem.",
                "Tikka T3X Tact A1 223 Rem. Rifle",
                "223 Rem.",
                24.0,
                "Mideum",
                "1:8",
                0.068,
                "4");
        Rifle savedRifle = rifleRepository.save(rifle).block();

        Load load = new Load(1L,
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
                savedRifle.id());
        loadRepository.save(load).block();

        Mono<Load> result = loadRepository.findById(1L);

        StepVerifier.create(result)
                .expectNextMatches(l -> l.id().equals(1L))
                .verifyComplete();
    }

    @Test
    void save() {
        Rifle rifle = new Rifle(null,
                "Tikka T3X Tact A1 223 Rem.",
                "Tikka T3X Tact A1 223 Rem. Rifle",
                "223 Rem.",
                24.0,
                "Medium",
                "1:8",
                0.068,
                "4");
        Rifle savedRifle = rifleRepository.save(rifle).block();

        Load load = new Load(null,
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
                savedRifle.id());

        Mono<Load> savedLoad = loadRepository.save(load);

        StepVerifier.create(savedLoad)
                .expectNextMatches(l -> l.id() != null && l.name().equals(load.name()))
                .verifyComplete();
    }

    @Test
    void findAll() {
        Rifle rifle1 = new Rifle(null,
                "Tikka T3X Tact A1 223 Rem.",
                "Tikka T3X Tact A1 223 Rem. Rifle",
                "223 Rem.",
                24.0,
                "Medium",
                "1:8",
                0.068,
                "4");
        Rifle savedRifle1 = rifleRepository.save(rifle1).block();

        Rifle rifle2 = new Rifle(null,
                "Tikka T3X CTR 223 Rem.",
                "Tikka T3X CTR 223 Rem. Rifle",
                "223 Rem.",
                20.0,
                "Medium",
                "1:8",
                0.068,
                "4");
        Rifle savedRifle2 = rifleRepository.save(rifle2).block();

        Load load1 = new Load(null,
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
                savedRifle1.id());

        Load load2 = new Load(null,
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
                savedRifle2.id());

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
        Rifle rifle = new Rifle(null,
                "Tikka T3X Tact A1 223 Rem.",
                "Tikka T3X Tact A1 223 Rem. Rifle",
                "223 Rem.",
                24.0,
                "Medium",
                "1:8",
                0.068,
                "4");
        Rifle savedRifle = rifleRepository.save(rifle).block();

        Load load = new Load(null,
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
                savedRifle.id());

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
        Rifle rifle = new Rifle(null,
                "Tikka T3X Tact A1 223 Rem.",
                "Tikka T3X Tact A1 223 Rem. Rifle",
                "223 Rem.",
                24.0,
                "Medium",
                "1:8",
                0.068,
                "4");
        Rifle savedRifle = rifleRepository.save(rifle).block();

        Load load = new Load(null,
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
                savedRifle.id());
        Load savedLoad = loadRepository.save(load)
                .block();

        Load updatedLoad = new Load(savedLoad.id(),
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
                savedRifle.id());

        Mono<Load> result = loadRepository.save(updatedLoad);

        StepVerifier.create(result)
                .expectNextMatches(
                        l -> l.id().equals(savedLoad.id()) && l.name().equals(updatedLoad.name()))
                .verifyComplete();
    }

    @Test
    void findByName() {
        Rifle rifle = new Rifle(null,
                "Tikka T3X Tact A1 223 Rem.",
                "Tikka T3X Tact A1 223 Rem. Rifle",
                "223 Rem.",
                24.0,
                "Medium",
                "1:8",
                0.068,
                "4");
        Rifle savedRifle = rifleRepository.save(rifle).block();

        Load load1 = new Load(null,
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
                savedRifle.id());

        Load load2 = new Load(null,
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
                savedRifle.id());

        loadRepository.saveAll(Flux.just(load1, load2))
                .blockLast();

        Flux<Load> result = loadRepository.findByName(load1.name());

        StepVerifier.create(result)
                .expectNextMatches(l -> l.name().equals(load1.name()))
                .verifyComplete();
    }
}