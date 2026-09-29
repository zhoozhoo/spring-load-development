package ca.zhoozhoo.loaddev.rifles.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ca.zhoozhoo.loaddev.rifles.dao.RifleRepository;
import ca.zhoozhoo.loaddev.rifles.events.RifleEventPublisher;
import ca.zhoozhoo.loaddev.rifles.model.Rifle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.units.indriya.quantity.Quantities;

@ExtendWith(MockitoExtension.class)
class RiflesServiceTest {

    @Mock
    private RifleRepository rifleRepository;

    @Mock
    private RifleEventPublisher rifleEventPublisher;

    @InjectMocks
    private RiflesService riflesService;

    @Test
    void getAllRifles_ShouldReturnFluxOfRifles() {
        var rifle = new Rifle(1L, "user1", "Rifle 1", "Description", ".308 Win",
                Quantities.getQuantity(24.0, INCH_INTERNATIONAL), "Heavy", null, null);
        when(rifleRepository.findAllByOwnerId(eq("user1"), any(Pageable.class))).thenReturn(Flux.just(rifle));

        StepVerifier.create(riflesService.getAllRifles("user1", PageRequest.of(0, 20)))
                .expectNext(rifle)
                .verifyComplete();
    }

    @Test
    void getRifleById_ShouldReturnRifle_WhenFound() {
        var rifle = new Rifle(1L, "user1", "Rifle 1", "Description", ".308 Win",
                Quantities.getQuantity(24.0, INCH_INTERNATIONAL), "Heavy", null, null);
        when(rifleRepository.findByIdAndOwnerId(1L, "user1")).thenReturn(Mono.just(rifle));

        StepVerifier.create(riflesService.getRifleById(1L, "user1"))
                .expectNext(rifle)
                .verifyComplete();
    }

    @Test
    void createRifle_ShouldReturnCreatedRifle() {
        var rifle = new Rifle(null, "user1", "Rifle 1", "Description", ".308 Win",
                Quantities.getQuantity(24.0, INCH_INTERNATIONAL), "Heavy", null, null);
        var savedRifle = new Rifle(1L, "user1", "Rifle 1", "Description", ".308 Win",
                Quantities.getQuantity(24.0, INCH_INTERNATIONAL), "Heavy", null, null);
        when(rifleRepository.save(any(Rifle.class))).thenReturn(Mono.just(savedRifle));
        when(rifleEventPublisher.publishCreated(any(Rifle.class))).thenReturn(Mono.empty());

        StepVerifier.create(riflesService.createRifle(rifle))
                .expectNext(savedRifle)
                .verifyComplete();
    }

    @Test
    void updateRifle_ShouldReturnUpdatedRifle() {
        var rifle = new Rifle(1L, "user1", "Rifle 1", "Description", ".308 Win",
                Quantities.getQuantity(24.0, INCH_INTERNATIONAL), "Heavy", null, null);
        when(rifleRepository.save(any(Rifle.class))).thenReturn(Mono.just(rifle));
        when(rifleEventPublisher.publishUpdated(any(Rifle.class))).thenReturn(Mono.empty());

        StepVerifier.create(riflesService.updateRifle(rifle))
                .expectNext(rifle)
                .verifyComplete();
    }

    @Test
    void deleteRifle_ShouldComplete() {
        var rifle = new Rifle(1L, "user1", "Rifle 1", "Description", ".308 Win",
                Quantities.getQuantity(24.0, INCH_INTERNATIONAL), "Heavy", null, null);
        when(rifleRepository.delete(rifle)).thenReturn(Mono.empty());
        when(rifleEventPublisher.publishDeleted(any(Rifle.class))).thenReturn(Mono.empty());

        StepVerifier.create(riflesService.deleteRifle(rifle))
                .verifyComplete();
    }
}
