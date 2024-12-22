package ca.zhoozhoo.loaddev.load_development.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.load_development.model.Load;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
class LoadRepositoryTest {

    @Autowired
    private LoadRepository loadRepository;

    @Test
    void testFindById() {
        Load load = new Load(1L, "Test Load", "Description", 100.0);
        loadRepository.save(load).block();

        Mono<Load> result = loadRepository.findById(1L);

        StepVerifier.create(result)
                .expectNextMatches(l -> l.id().equals(1L))
                .verifyComplete();
    }

    @Test
    void testSave() {
        Load load = new Load(null, "New Load", "New Description", 200.0);

        Mono<Load> savedLoad = loadRepository.save(load);

        StepVerifier.create(savedLoad)
                .expectNextMatches(l -> l.id() != null && l.name().equals("New Load"))
                .verifyComplete();
    }

    @Test
    void testFindAll() {
        Load load1 = new Load(null, "Load 1", "Description 1", 100.0);
        Load load2 = new Load(null, "Load 2", "Description 2", 200.0);

        loadRepository.saveAll(Flux.just(load1, load2)).blockLast();

        Flux<Load> result = loadRepository.findAll();

        StepVerifier.create(result)
                .expectNextMatches(l -> l.name().equals("Load 1"))
                .expectNextMatches(l -> l.name().equals("Load 2"))
                .verifyComplete();
    }

    @Test
    void testDeleteById() {
        Load load = new Load(null, "Load to Delete", "Description", 100.0);
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
    void testUpdate() {
        Load load = new Load(null, "Load to Update", "Initial Description", 150.0);
        Load savedLoad = loadRepository.save(load).block();

        Load updatedLoad = new Load(savedLoad.id(), "Updated Load", "Updated Description", 150.0);
        Mono<Load> result = loadRepository.save(updatedLoad);

        StepVerifier.create(result)
                .expectNextMatches(l -> l.id().equals(savedLoad.id()) && l.name().equals("Updated Load"))
                .verifyComplete();
    }
}
