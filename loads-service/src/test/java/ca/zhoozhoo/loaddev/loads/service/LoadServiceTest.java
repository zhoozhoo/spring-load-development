package ca.zhoozhoo.loaddev.loads.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static systems.uom.ucum.UCUM.GRAIN;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.model.Load;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.units.indriya.quantity.Quantities;

@ExtendWith(MockitoExtension.class)
class LoadServiceTest {

    @Mock
    private LoadRepository loadRepository;

    @InjectMocks
    private LoadService loadService;

    @Test
    void getAllLoads_ShouldReturnFluxOfLoads() {
        Load load = new Load(1L, "user1", "Load 1", "Desc", "Powder", "Type", "Bullet", "Type", Quantities.getQuantity(100.0, GRAIN), "Primer", "Type", Quantities.getQuantity(0.020, INCH_INTERNATIONAL), Quantities.getQuantity(2.800, INCH_INTERNATIONAL), Quantities.getQuantity(0.002, INCH_INTERNATIONAL), 1L);
        when(loadRepository.findAllByOwnerId("user1")).thenReturn(Flux.just(load));

        StepVerifier.create(loadService.getAllLoads("user1"))
                .expectNext(load)
                .verifyComplete();
    }

    @Test
    void getLoadById_ShouldReturnLoad_WhenFound() {
        Load load = new Load(1L, "user1", "Load 1", "Desc", "Powder", "Type", "Bullet", "Type", Quantities.getQuantity(100.0, GRAIN), "Primer", "Type", Quantities.getQuantity(0.020, INCH_INTERNATIONAL), Quantities.getQuantity(2.800, INCH_INTERNATIONAL), Quantities.getQuantity(0.002, INCH_INTERNATIONAL), 1L);
        when(loadRepository.findByIdAndOwnerId(1L, "user1")).thenReturn(Mono.just(load));

        StepVerifier.create(loadService.getLoadById(1L, "user1"))
                .expectNext(load)
                .verifyComplete();
    }

    @Test
    void createLoad_ShouldReturnCreatedLoad() {
        Load load = new Load(null, "user1", "Load 1", "Desc", "Powder", "Type", "Bullet", "Type", Quantities.getQuantity(100.0, GRAIN), "Primer", "Type", Quantities.getQuantity(0.020, INCH_INTERNATIONAL), Quantities.getQuantity(2.800, INCH_INTERNATIONAL), Quantities.getQuantity(0.002, INCH_INTERNATIONAL), 1L);
        Load savedLoad = new Load(1L, "user1", "Load 1", "Desc", "Powder", "Type", "Bullet", "Type", Quantities.getQuantity(100.0, GRAIN), "Primer", "Type", Quantities.getQuantity(0.020, INCH_INTERNATIONAL), Quantities.getQuantity(2.800, INCH_INTERNATIONAL), Quantities.getQuantity(0.002, INCH_INTERNATIONAL), 1L);
        when(loadRepository.save(any(Load.class))).thenReturn(Mono.just(savedLoad));

        StepVerifier.create(loadService.createLoad(load))
                .expectNext(savedLoad)
                .verifyComplete();
    }

    @Test
    void updateLoad_ShouldReturnUpdatedLoad() {
        Load load = new Load(1L, "user1", "Load 1", "Desc", "Powder", "Type", "Bullet", "Type", Quantities.getQuantity(100.0, GRAIN), "Primer", "Type", Quantities.getQuantity(0.020, INCH_INTERNATIONAL), Quantities.getQuantity(2.800, INCH_INTERNATIONAL), Quantities.getQuantity(0.002, INCH_INTERNATIONAL), 1L);
        when(loadRepository.save(any(Load.class))).thenReturn(Mono.just(load));

        StepVerifier.create(loadService.updateLoad(load))
                .expectNext(load)
                .verifyComplete();
    }

    @Test
    void deleteLoad_ShouldComplete() {
        Load load = new Load(1L, "user1", "Load 1", "Desc", "Powder", "Type", "Bullet", "Type", Quantities.getQuantity(100.0, GRAIN), "Primer", "Type", Quantities.getQuantity(0.020, INCH_INTERNATIONAL), Quantities.getQuantity(2.800, INCH_INTERNATIONAL), Quantities.getQuantity(0.002, INCH_INTERNATIONAL), 1L);
        when(loadRepository.delete(load)).thenReturn(Mono.empty());

        StepVerifier.create(loadService.deleteLoad(load))
                .verifyComplete();
    }
}
