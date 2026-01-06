package ca.zhoozhoo.loaddev.components.service;

import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.components.dao.PropellantRepository;
import ca.zhoozhoo.loaddev.components.model.Propellant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing Propellant components.
 * <p>
 * This service provides reactive operations for Propellants.
 * It handles CRUD operations and searching capabilities,
 * ensuring data isolation by user ID.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Service
public class PropellantService {

    private final PropellantRepository propellantRepository;

    /**
     * Constructs a new PropellantService with required repository.
     *
     * @param propellantRepository the repository for Propellant entities
     */
    public PropellantService(PropellantRepository propellantRepository) {
        this.propellantRepository = propellantRepository;
    }

    /**
     * Retrieves all propellants for a specific user.
     *
     * @param userId the ID of the user
     * @return a Flux of Propellant entities
     */
    public Flux<Propellant> getAllPropellants(String userId) {
        return propellantRepository.findAllByOwnerId(userId);
    }

    /**
     * Searches for propellants belonging to a user based on a query string.
     *
     * @param userId the ID of the user
     * @param query  the search query
     * @return a Flux of matching Propellant entities
     */
    public Flux<Propellant> searchPropellants(String userId, String query) {
        return propellantRepository.searchByOwnerIdAndQuery(userId, query);
    }

    /**
     * Retrieves a specific propellant by ID and user ID.
     *
     * @param id     the ID of the propellant
     * @param userId the ID of the user
     * @return a Mono containing the Propellant if found, or empty
     */
    public Mono<Propellant> getPropellantById(Long id, String userId) {
        return propellantRepository.findByIdAndOwnerId(id, userId);
    }

    /**
     * Creates a new propellant.
     *
     * @param propellant the Propellant entity to create
     * @return a Mono containing the created Propellant
     */
    public Mono<Propellant> createPropellant(Propellant propellant) {
        return propellantRepository.save(propellant);
    }

    /**
     * Updates an existing propellant.
     *
     * @param propellant the Propellant entity to update
     * @return a Mono containing the updated Propellant
     */
    public Mono<Propellant> updatePropellant(Propellant propellant) {
        return propellantRepository.save(propellant);
    }

    /**
     * Deletes a propellant.
     *
     * @param propellant the Propellant entity to delete
     * @return a Mono<Void> that completes when deletion is finished
     */
    public Mono<Void> deletePropellant(Propellant propellant) {
        return propellantRepository.delete(propellant);
    }
}
