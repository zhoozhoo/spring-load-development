package ca.zhoozhoo.loaddev.components.service;

import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.components.dao.PrimerRepository;
import ca.zhoozhoo.loaddev.components.model.Primer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing Primer components.
 * <p>
 * This service provides reactive operations for Primers.
 * It handles CRUD operations and searching capabilities,
 * ensuring data isolation by user ID.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Service
public class PrimerService {

    private final PrimerRepository primerRepository;

    /**
     * Constructs a new PrimerService with required repository.
     *
     * @param primerRepository the repository for Primer entities
     */
    public PrimerService(PrimerRepository primerRepository) {
        this.primerRepository = primerRepository;
    }

    /**
     * Retrieves all primers for a specific user.
     *
     * @param userId the ID of the user
     * @return a Flux of Primer entities
     */
    public Flux<Primer> getAllPrimers(String userId) {
        return primerRepository.findAllByOwnerId(userId);
    }

    /**
     * Searches for primers belonging to a user based on a query string.
     *
     * @param userId the ID of the user
     * @param query  the search query
     * @return a Flux of matching Primer entities
     */
    public Flux<Primer> searchPrimers(String userId, String query) {
        return primerRepository.searchByOwnerIdAndQuery(userId, query);
    }

    /**
     * Retrieves a specific primer by ID and user ID.
     *
     * @param id     the ID of the primer
     * @param userId the ID of the user
     * @return a Mono containing the Primer if found, or empty
     */
    public Mono<Primer> getPrimerById(Long id, String userId) {
        return primerRepository.findByIdAndOwnerId(id, userId);
    }

    /**
     * Creates a new primer.
     *
     * @param primer the Primer entity to create
     * @return a Mono containing the created Primer
     */
    public Mono<Primer> createPrimer(Primer primer) {
        return primerRepository.save(primer);
    }

    /**
     * Updates an existing primer.
     *
     * @param primer the Primer entity to update
     * @return a Mono containing the updated Primer
     */
    public Mono<Primer> updatePrimer(Primer primer) {
        return primerRepository.save(primer);
    }

    /**
     * Deletes a primer.
     *
     * @param primer the Primer entity to delete
     * @return a Mono<Void> that completes when deletion is finished
     */
    public Mono<Void> deletePrimer(Primer primer) {
        return primerRepository.delete(primer);
    }
}
