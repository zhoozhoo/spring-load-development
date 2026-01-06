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

import ca.zhoozhoo.loaddev.components.dao.CaseRepository;
import ca.zhoozhoo.loaddev.components.model.Case;
import ca.zhoozhoo.loaddev.components.model.PrimerSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    private CaseRepository caseRepository;

    private CaseService caseService;

    @BeforeEach
    void setUp() {
        caseService = new CaseService(caseRepository);
    }

    @Test
    void getAllCases_ShouldReturnFluxOfCases() {
        Case caseItem = new Case(1L, "user1", "Brand", "Caliber", PrimerSize.LARGE_RIFLE, null, null);
        when(caseRepository.findAllByOwnerId(anyString())).thenReturn(Flux.just(caseItem));

        StepVerifier.create(caseService.getAllCases("user1"))
                .expectNext(caseItem)
                .verifyComplete();
        
        verify(caseRepository).findAllByOwnerId("user1");
    }

    @Test
    void searchCases_ShouldReturnFluxOfCases() {
        Case caseItem = new Case(1L, "user1", "Brand", "Caliber", PrimerSize.LARGE_RIFLE, null, null);
        when(caseRepository.searchByOwnerIdAndQuery(anyString(), anyString())).thenReturn(Flux.just(caseItem));

        StepVerifier.create(caseService.searchCases("user1", "query"))
                .expectNext(caseItem)
                .verifyComplete();

        verify(caseRepository).searchByOwnerIdAndQuery("user1", "query");
    }

    @Test
    void getCaseById_ShouldReturnCase() {
        Case caseItem = new Case(1L, "user1", "Brand", "Caliber", PrimerSize.LARGE_RIFLE, null, null);
        when(caseRepository.findByIdAndOwnerId(anyLong(), anyString())).thenReturn(Mono.just(caseItem));

        StepVerifier.create(caseService.getCaseById(1L, "user1"))
                .expectNext(caseItem)
                .verifyComplete();

        verify(caseRepository).findByIdAndOwnerId(1L, "user1");
    }

    @Test
    void createCase_ShouldReturnSavedCase() {
        Case caseItem = new Case(1L, "user1", "Brand", "Caliber", PrimerSize.LARGE_RIFLE, null, null);
        when(caseRepository.save(any(Case.class))).thenReturn(Mono.just(caseItem));

        StepVerifier.create(caseService.createCase(caseItem))
                .expectNext(caseItem)
                .verifyComplete();

        verify(caseRepository).save(caseItem);
    }

    @Test
    void updateCase_ShouldReturnUpdatedCase() {
        Case caseItem = new Case(1L, "user1", "Brand", "Caliber", PrimerSize.LARGE_RIFLE, null, null);
        when(caseRepository.save(any(Case.class))).thenReturn(Mono.just(caseItem));

        StepVerifier.create(caseService.updateCase(caseItem))
                .expectNext(caseItem)
                .verifyComplete();

        verify(caseRepository).save(caseItem);
    }

    @Test
    void deleteCase_ShouldComplete() {
        Case caseItem = new Case(1L, "user1", "Brand", "Caliber", PrimerSize.LARGE_RIFLE, null, null);
        when(caseRepository.delete(any(Case.class))).thenReturn(Mono.empty());

        StepVerifier.create(caseService.deleteCase(caseItem))
                .verifyComplete();

        verify(caseRepository).delete(caseItem);
    }
}
