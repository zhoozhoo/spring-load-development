package ca.zhoozhoo.loaddev.loads.service;

import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.model.Load;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing Load entities.
 * <p>
 * This service provides reactive operations for Loads.
 * It handles CRUD operations ensuring data isolation by user ID.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Service
public class LoadService {

    private final LoadRepository loadRepository;

    /**
     * Constructs a new LoadService with required repository.
     *
     * @param loadRepository the repository for Load entities
     */
    public LoadService(LoadRepository loadRepository) {
        this.loadRepository = loadRepository;
    }

    /**
     * Retrieves all loads for a specific user.
     *
     * @param userId the ID of the user
     * @return a Flux of Load entities
     */
    public Flux<Load> getAllLoads(String userId) {
        return loadRepository.findAllByOwnerId(userId);
    }

    /**
     * Retrieves a specific load by ID and user ID.
     *
     * @param id     the ID of the load
     * @param userId the ID of the user
     * @return a Mono containing the Load if found, or empty
     */
    public Mono<Load> getLoadById(Long id, String userId) {
        return loadRepository.findByIdAndOwnerId(id, userId);
    }

    /**
     * Creates a new load.
     *
     * @param load the Load entity to create
     * @return a Mono containing the created Load
     */
    public Mono<Load> createLoad(Load load) {
        return loadRepository.save(load);
    }

    /**
     * Updates an existing load.
     *
     * @param load the Load entity to update
     * @return a Mono containing the updated Load
     */
    public Mono<Load> updateLoad(Load load) {
        return loadRepository.save(load);
    }

    /**
     * Deletes a load.
     *
     * @param load the Load entity to delete
     * @return a Mono<Void> that completes when deletion is finished
     */
    public Mono<Void> deleteLoad(Load load) {
        return loadRepository.delete(load);
    }
}
