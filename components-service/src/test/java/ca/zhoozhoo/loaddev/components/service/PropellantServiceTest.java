package ca.zhoozhoo.loaddev.components.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ca.zhoozhoo.loaddev.components.dao.PropellantRepository;
import ca.zhoozhoo.loaddev.components.model.Propellant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PropellantServiceTest {

    @Mock
    private PropellantRepository propellantRepository;

    private PropellantService propellantService;

    @BeforeEach
    void setUp() {
        propellantService = new PropellantService(propellantRepository);
    }

    @Test
    void getAllPropellants_ShouldReturnFluxOfPropellants() {
        Propellant propellant = new Propellant(1L, "user1", "Brand", "Model", null, null);
        when(propellantRepository.findAllByOwnerId(anyString())).thenReturn(Flux.just(propellant));

        StepVerifier.create(propellantService.getAllPropellants("user1"))
                .expectNext(propellant)
                .verifyComplete();

        verify(propellantRepository).findAllByOwnerId("user1");
    }

    @Test
    void searchPropellants_ShouldReturnFluxOfPropellants() {
        Propellant propellant = new Propellant(1L, "user1", "Brand", "Model", null, null);
        when(propellantRepository.searchByOwnerIdAndQuery(anyString(), anyString())).thenReturn(Flux.just(propellant));

        StepVerifier.create(propellantService.searchPropellants("user1", "query"))
                .expectNext(propellant)
                .verifyComplete();

        verify(propellantRepository).searchByOwnerIdAndQuery("user1", "query");
    }

    @Test
    void getPropellantById_ShouldReturnPropellant() {
        Propellant propellant = new Propellant(1L, "user1", "Brand", "Model", null, null);
        when(propellantRepository.findByIdAndOwnerId(anyLong(), anyString())).thenReturn(Mono.just(propellant));

        StepVerifier.create(propellantService.getPropellantById(1L, "user1"))
                .expectNext(propellant)
                .verifyComplete();

        verify(propellantRepository).findByIdAndOwnerId(1L, "user1");
    }

    @Test
    void createPropellant_ShouldReturnSavedPropellant() {
        Propellant propellant = new Propellant(1L, "user1", "Brand", "Model", null, null);
        when(propellantRepository.save(any(Propellant.class))).thenReturn(Mono.just(propellant));

        StepVerifier.create(propellantService.createPropellant(propellant))
                .expectNext(propellant)
                .verifyComplete();

        verify(propellantRepository).save(propellant);
    }

    @Test
    void updatePropellant_ShouldReturnUpdatedPropellant() {
        Propellant propellant = new Propellant(1L, "user1", "Brand", "Model", null, null);
        when(propellantRepository.save(any(Propellant.class))).thenReturn(Mono.just(propellant));

        StepVerifier.create(propellantService.updatePropellant(propellant))
                .expectNext(propellant)
                .verifyComplete();

        verify(propellantRepository).save(propellant);
    }

    @Test
    void deletePropellant_ShouldComplete() {
        Propellant propellant = new Propellant(1L, "user1", "Brand", "Model", null, null);
        when(propellantRepository.delete(any(Propellant.class))).thenReturn(Mono.empty());

        StepVerifier.create(propellantService.deletePropellant(propellant))
                .verifyComplete();

        verify(propellantRepository).delete(propellant);
    }
}
