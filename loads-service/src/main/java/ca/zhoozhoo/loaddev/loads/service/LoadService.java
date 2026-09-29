package ca.zhoozhoo.loaddev.loads.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.events.LoadEventPublisher;
import ca.zhoozhoo.loaddev.loads.model.Load;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/// Service class for managing Load entities.
///
/// This service provides reactive operations for Loads.
/// It handles CRUD operations ensuring data isolation by user ID, and
/// publishes domain events for created, updated, and deleted loads.
///
/// @author Zhubin Salehi
@Service
public class LoadService {

    private final LoadRepository loadRepository;

    private final LoadEventPublisher loadEventPublisher;

    /// Constructs a new LoadService with required repository.
    ///
    /// @param loadRepository the repository for Load entities
    /// @param loadEventPublisher the publisher for Load domain events
    public LoadService(LoadRepository loadRepository, LoadEventPublisher loadEventPublisher) {
        this.loadRepository = loadRepository;
        this.loadEventPublisher = loadEventPublisher;
    }

    /// Retrieves all loads for a specific user with pagination.
    ///
    /// @param userId   the ID of the user
    /// @param pageable pagination parameters
    /// @return a Flux of Load entities
    public Flux<Load> getAllLoads(String userId, Pageable pageable) {
        return loadRepository.findAllByOwnerId(userId, pageable);
    }

    /// Retrieves a specific load by ID and user ID.
    ///
    /// @param id     the ID of the load
    /// @param userId the ID of the user
    /// @return a Mono containing the Load if found, or empty
    public Mono<Load> getLoadById(Long id, String userId) {
        return loadRepository.findByIdAndOwnerId(id, userId);
    }

    /// Creates a new load.
    ///
    /// @param load the Load entity to create
    /// @return a Mono containing the created Load
    @Transactional
    public Mono<Load> createLoad(Load load) {
        return loadRepository.save(load)
                .flatMap(saved -> loadEventPublisher.publishCreated(saved).thenReturn(saved));
    }

    /// Updates an existing load.
    ///
    /// @param load the Load entity to update
    /// @return a Mono containing the updated Load
    @Transactional
    public Mono<Load> updateLoad(Load load) {
        return loadRepository.save(load)
                .flatMap(saved -> loadEventPublisher.publishUpdated(saved).thenReturn(saved));
    }

    /// Deletes a load.
    ///
    /// @param load the Load entity to delete
    /// @return a Mono<Void> that completes when deletion is finished
    @Transactional
    public Mono<Void> deleteLoad(Load load) {
        return loadRepository.delete(load)
                .then(loadEventPublisher.publishDeleted(load));
    }
}
