package ca.zhoozhoo.loaddev.components.service;

import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.components.dao.CaseRepository;
import ca.zhoozhoo.loaddev.components.model.Case;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing Case components.
 * <p>
 * This service provides reactive operations for Cases.
 * It handles CRUD operations and searching capabilities,
 * ensuring data isolation by user ID.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Service
public class CaseService {

    private final CaseRepository caseRepository;

    /**
     * Constructs a new CaseService with required repository.
     *
     * @param caseRepository the repository for Case entities
     */
    public CaseService(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    /**
     * Retrieves all cases for a specific user.
     *
     * @param userId the ID of the user
     * @return a Flux of Case entities
     */
    public Flux<Case> getAllCases(String userId) {
        return caseRepository.findAllByOwnerId(userId);
    }

    /**
     * Searches for cases belonging to a user based on a query string.
     *
     * @param userId the ID of the user
     * @param query  the search query
     * @return a Flux of matching Case entities
     */
    public Flux<Case> searchCases(String userId, String query) {
        return caseRepository.searchByOwnerIdAndQuery(userId, query);
    }

    /**
     * Retrieves a specific case by ID and user ID.
     *
     * @param id     the ID of the case
     * @param userId the ID of the user
     * @return a Mono containing the Case if found, or empty
     */
    public Mono<Case> getCaseById(Long id, String userId) {
        return caseRepository.findByIdAndOwnerId(id, userId);
    }

    /**
     * Creates a new case.
     *
     * @param caseItem the Case entity to create
     * @return a Mono containing the created Case
     */
    public Mono<Case> createCase(Case caseItem) {
        return caseRepository.save(caseItem);
    }

    /**
     * Updates an existing case.
     *
     * @param caseItem the Case entity to update
     * @return a Mono containing the updated Case
     */
    public Mono<Case> updateCase(Case caseItem) {
        return caseRepository.save(caseItem);
    }

    /**
     * Deletes a case.
     *
     * @param caseItem the Case entity to delete
     * @return a Mono<Void> that completes when deletion is finished
     */
    public Mono<Void> deleteCase(Case caseItem) {
        return caseRepository.delete(caseItem);
    }
}
