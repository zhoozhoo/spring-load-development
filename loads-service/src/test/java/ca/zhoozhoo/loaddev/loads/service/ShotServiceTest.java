package ca.zhoozhoo.loaddev.loads.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import ca.zhoozhoo.loaddev.loads.model.Shot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

@ExtendWith(MockitoExtension.class)
class ShotServiceTest {

    @Mock
    private ShotRepository shotRepository;

    @InjectMocks
    private ShotService shotService;

    @Test
    void getAllShots_ShouldReturnFluxOfShots() {
        Shot shot = new Shot(1L, "user1", 1L, Quantities.getQuantity(1000, Units.METRE_PER_SECOND));
        when(shotRepository.findByGroupIdAndOwnerId(1L, "user1")).thenReturn(Flux.just(shot));

        StepVerifier.create(shotService.getAllShots(1L, "user1"))
                .expectNext(shot)
                .verifyComplete();
    }

    @Test
    void getShotById_ShouldReturnShot_WhenFound() {
        Shot shot = new Shot(1L, "user1", 1L, Quantities.getQuantity(1000, Units.METRE_PER_SECOND));
        when(shotRepository.findByIdAndOwnerId(1L, "user1")).thenReturn(Mono.just(shot));

        StepVerifier.create(shotService.getShotById(1L, "user1"))
                .expectNext(shot)
                .verifyComplete();
    }

    @Test
    void createShot_ShouldReturnCreatedShot() {
        Shot shot = new Shot(null, "user1", 1L, Quantities.getQuantity(1000, Units.METRE_PER_SECOND));
        Shot savedShot = new Shot(1L, "user1", 1L, Quantities.getQuantity(1000, Units.METRE_PER_SECOND));
        when(shotRepository.save(any(Shot.class))).thenReturn(Mono.just(savedShot));

        StepVerifier.create(shotService.createShot(shot))
                .expectNext(savedShot)
                .verifyComplete();
    }

    @Test
    void updateShot_ShouldReturnUpdatedShot() {
        Shot shot = new Shot(1L, "user1", 1L, Quantities.getQuantity(1000, Units.METRE_PER_SECOND));
        when(shotRepository.save(any(Shot.class))).thenReturn(Mono.just(shot));

        StepVerifier.create(shotService.updateShot(shot))
                .expectNext(shot)
                .verifyComplete();
    }

    @Test
    void deleteShot_ShouldComplete() {
        Shot shot = new Shot(1L, "user1", 1L, Quantities.getQuantity(1000, Units.METRE_PER_SECOND));
        when(shotRepository.delete(shot)).thenReturn(Mono.empty());

        StepVerifier.create(shotService.deleteShot(shot))
                .verifyComplete();
    }
}
