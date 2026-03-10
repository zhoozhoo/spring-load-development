package ca.zhoozhoo.loaddev.components.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ca.zhoozhoo.loaddev.components.dao.PrimerRepository;
import ca.zhoozhoo.loaddev.components.model.Primer;
import ca.zhoozhoo.loaddev.components.model.PrimerSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PrimerServiceTest {

    @Mock
    private PrimerRepository primerRepository;

    private PrimerService primerService;

    @BeforeEach
    void setUp() {
        primerService = new PrimerService(primerRepository);
    }

    @Test
    void getAllPrimers_ShouldReturnFluxOfPrimers() {
        Primer primer = new Primer(1L, "user1", "Brand", "Model", PrimerSize.LARGE_RIFLE, null, null);
        when(primerRepository.findAllByOwnerId(anyString(), any(Pageable.class))).thenReturn(Flux.just(primer));

        StepVerifier.create(primerService.getAllPrimers("user1", PageRequest.of(0, 20)))
                .expectNext(primer)
                .verifyComplete();

        verify(primerRepository).findAllByOwnerId(eq("user1"), any(Pageable.class));
    }

    @Test
    void searchPrimers_ShouldReturnFluxOfPrimers() {
        Primer primer = new Primer(1L, "user1", "Brand", "Model", PrimerSize.LARGE_RIFLE, null, null);
        when(primerRepository.searchByOwnerIdAndQuery(anyString(), anyString(), anyInt(), anyLong())).thenReturn(Flux.just(primer));

        StepVerifier.create(primerService.searchPrimers("user1", "query", PageRequest.of(0, 20)))
                .expectNext(primer)
                .verifyComplete();

        verify(primerRepository).searchByOwnerIdAndQuery(eq("user1"), eq("query"), anyInt(), anyLong());
    }

    @Test
    void getPrimerById_ShouldReturnPrimer() {
        Primer primer = new Primer(1L, "user1", "Brand", "Model", PrimerSize.LARGE_RIFLE, null, null);
        when(primerRepository.findByIdAndOwnerId(anyLong(), anyString())).thenReturn(Mono.just(primer));

        StepVerifier.create(primerService.getPrimerById(1L, "user1"))
                .expectNext(primer)
                .verifyComplete();

        verify(primerRepository).findByIdAndOwnerId(1L, "user1");
    }

    @Test
    void createPrimer_ShouldReturnSavedPrimer() {
        Primer primer = new Primer(1L, "user1", "Brand", "Model", PrimerSize.LARGE_RIFLE, null, null);
        when(primerRepository.save(any(Primer.class))).thenReturn(Mono.just(primer));

        StepVerifier.create(primerService.createPrimer(primer))
                .expectNext(primer)
                .verifyComplete();

        verify(primerRepository).save(primer);
    }

    @Test
    void updatePrimer_ShouldReturnUpdatedPrimer() {
        Primer primer = new Primer(1L, "user1", "Brand", "Model", PrimerSize.LARGE_RIFLE, null, null);
        when(primerRepository.save(any(Primer.class))).thenReturn(Mono.just(primer));

        StepVerifier.create(primerService.updatePrimer(primer))
                .expectNext(primer)
                .verifyComplete();

        verify(primerRepository).save(primer);
    }

    @Test
    void deletePrimer_ShouldComplete() {
        Primer primer = new Primer(1L, "user1", "Brand", "Model", PrimerSize.LARGE_RIFLE, null, null);
        when(primerRepository.delete(any(Primer.class))).thenReturn(Mono.empty());

        StepVerifier.create(primerService.deletePrimer(primer))
                .verifyComplete();

        verify(primerRepository).delete(primer);
    }
}
