package ca.zhoozhoo.loaddev.load_development.dao;

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

    @Test
    void findById() {
        Rifle rifle = new Rifle(null, "Test Rifle", "Description", "5.56mm", 20.0, "Contour", "1:7", 0.2, "Rifling");
        Rifle savedRifle = rifleRepository.save(rifle).block();

        Load load = new Load(1L, "Test Load", "Description", "Powder Manufacturer", "Powder Type", 50.0,
                "Bullet Manufacturer", "Bullet Type", 150.0, "Primer Manufacturer", "Primer Type", savedRifle.id());
        loadRepository.save(load).block();

        Mono<Load> result = loadRepository.findById(1L);

        StepVerifier.create(result)
                .expectNextMatches(l -> l.id().equals(1L))
                .verifyComplete();
    }

    @Test
    void save() {
        Rifle rifle = new Rifle(null, "Test Rifle", "Description", "5.56mm", 20.0, "Contour", "1:7", 0.2, "Rifling");
        Rifle savedRifle = rifleRepository.save(rifle).block();

        Load load = new Load(null, "New Load", "New Description", "Powder Manufacturer", "Powder Type", 50.0,
                "Bullet Manufacturer", "Bullet Type", 150.0, "Primer Manufacturer", "Primer Type", savedRifle.id());

        Mono<Load> savedLoad = loadRepository.save(load);

        StepVerifier.create(savedLoad)
                .expectNextMatches(l -> l.id() != null && l.name().equals("New Load"))
                .verifyComplete();
    }

    @Test
    void findAll() {
        Rifle rifle1 = new Rifle(null, "Rifle 1", "Description 1", "5.56mm", 20.0, "Contour", "1:7", 0.2, "Rifling");
        Rifle savedRifle1 = rifleRepository.save(rifle1).block();

        Rifle rifle2 = new Rifle(null, "Rifle 2", "Description 2", "7.62mm", 24.0, "Heavy", "1:10", 0.3, "Polygonal");
        Rifle savedRifle2 = rifleRepository.save(rifle2).block();

        Load load1 = new Load(null, "Load 1", "Description 1", "Powder Manufacturer 1", "Powder Type 1", 50.0,
                "Bullet Manufacturer 1", "Bullet Type 1", 150.0, "Primer Manufacturer 1", "Primer Type 1",
                savedRifle1.id());
        Load load2 = new Load(null, "Load 2", "Description 2", "Powder Manufacturer 2", "Powder Type 2", 60.0,
                "Bullet Manufacturer 2", "Bullet Type 2", 160.0, "Primer Manufacturer 2", "Primer Type 2",
                savedRifle2.id());

        loadRepository.saveAll(Flux.just(load1, load2)).blockLast();

        Flux<Load> result = loadRepository.findAll();

        StepVerifier.create(result)
                .expectNextMatches(l -> l.name().equals("Load 1"))
                .expectNextMatches(l -> l.name().equals("Load 2"))
                .verifyComplete();
    }

    @Test
    void deleteById() {
        Rifle rifle = new Rifle(null, "Test Rifle", "Description", "5.56mm", 20.0, "Contour", "1:7", 0.2, "Rifling");
        Rifle savedRifle = rifleRepository.save(rifle).block();

        Load load = new Load(null, "Load to Delete", "Description", "Powder Manufacturer", "Powder Type", 50.0,
                "Bullet Manufacturer", "Bullet Type", 150.0, "Primer Manufacturer", "Primer Type", savedRifle.id());
        Load savedLoad = loadRepository.save(load).block();

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
        Rifle rifle = new Rifle(null, "Test Rifle", "Description", "5.56mm", 20.0, "Contour", "1:7", 0.2, "Rifling");
        Rifle savedRifle = rifleRepository.save(rifle).block();

        Load load = new Load(null, "Load to Update", "Initial Description", "Powder Manufacturer", "Powder Type", 50.0,
                "Bullet Manufacturer", "Bullet Type", 150.0, "Primer Manufacturer", "Primer Type", savedRifle.id());
        Load savedLoad = loadRepository.save(load).block();

        Load updatedLoad = new Load(savedLoad.id(), "Updated Load", "Updated Description",
                "Updated Powder Manufacturer", "Updated Powder Type", 60.0, "Updated Bullet Manufacturer",
                "Updated Bullet Type", 160.0, "Updated Primer Manufacturer", "Updated Primer Type", savedRifle.id());
        Mono<Load> result = loadRepository.save(updatedLoad);

        StepVerifier.create(result)
                .expectNextMatches(l -> l.id().equals(savedLoad.id()) && l.name().equals("Updated Load"))
                .verifyComplete();
    }

    @Test
    void findByName() {
        Rifle rifle = new Rifle(null, "Test Rifle", "Description", "5.56mm", 20.0, "Contour", "1:7", 0.2, "Rifling");
        Rifle savedRifle = rifleRepository.save(rifle).block();

        Load load = new Load(null, "Unique Load", "Unique Description", "Powder Manufacturer", "Powder Type", 50.0,
                "Bullet Manufacturer", "Bullet Type", 150.0, "Primer Manufacturer", "Primer Type", savedRifle.id());
        loadRepository.save(load).block();

        Flux<Load> result = loadRepository.findByName("Unique Load");

        StepVerifier.create(result)
                .expectNextMatches(l -> l.name().equals("Unique Load"))
                .verifyComplete();
    }
}