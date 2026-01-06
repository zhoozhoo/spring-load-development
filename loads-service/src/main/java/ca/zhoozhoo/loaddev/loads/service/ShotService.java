package ca.zhoozhoo.loaddev.loads.service;

import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import ca.zhoozhoo.loaddev.loads.model.Shot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing Shot entities.
 * <p>
 * This service provides reactive operations for Shots.
 * It handles CRUD operations ensuring data isolation by user ID.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Service
public class ShotService {

    private final ShotRepository shotRepository;

    /**
     * Constructs a new ShotService with required repository.
     *
     * @param shotRepository the repository for Shot entities
     */
    public ShotService(ShotRepository shotRepository) {
        this.shotRepository = shotRepository;
    }

    /**
     * Retrieves all shots for a specific group and user.
     *
     * @param groupId the ID of the group
     * @param userId  the ID of the user
     * @return a Flux of Shot entities
     */
    public Flux<Shot> getAllShots(Long groupId, String userId) {
        return shotRepository.findByGroupIdAndOwnerId(groupId, userId);
    }

    /**
     * Retrieves a specific shot by ID and user ID.
     *
     * @param id     the ID of the shot
     * @param userId the ID of the user
     * @return a Mono containing the Shot if found, or empty
     */
    public Mono<Shot> getShotById(Long id, String userId) {
        return shotRepository.findByIdAndOwnerId(id, userId);
    }

    /**
     * Creates a new shot.
     *
     * @param shot the Shot entity to create
     * @return a Mono containing the created Shot
     */
    public Mono<Shot> createShot(Shot shot) {
        return shotRepository.save(shot);
    }

    /**
     * Updates an existing shot.
     *
     * @param shot the Shot entity to update
     * @return a Mono containing the updated Shot
     */
    public Mono<Shot> updateShot(Shot shot) {
        return shotRepository.save(shot);
    }

    /**
     * Deletes a shot.
     *
     * @param shot the Shot entity to delete
     * @return a Mono<Void> that completes when deletion is finished
     */
    public Mono<Void> deleteShot(Shot shot) {
        return shotRepository.delete(shot);
    }
}
