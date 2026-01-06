package ca.zhoozhoo.loaddev.components.service;

import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.components.dao.ProjectileRepository;
import ca.zhoozhoo.loaddev.components.model.Projectile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing Projectile components.
 * <p>
 * This service provides reactive operations for Projectiles.
 * It handles CRUD operations and searching capabilities,
 * ensuring data isolation by user ID.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Service
public class ProjectileService {

    private final ProjectileRepository projectileRepository;

    /**
     * Constructs a new ProjectileService with required repository.
     *
     * @param projectileRepository the repository for Projectile entities
     */
    public ProjectileService(ProjectileRepository projectileRepository) {
        this.projectileRepository = projectileRepository;
    }

    /**
     * Retrieves all projectiles for a specific user.
     *
     * @param userId the ID of the user
     * @return a Flux of Projectile entities
     */
    public Flux<Projectile> getAllProjectiles(String userId) {
        return projectileRepository.findAllByOwnerId(userId);
    }

    /**
     * Searches for projectiles belonging to a user based on a query string.
     *
     * @param userId the ID of the user
     * @param query  the search query
     * @return a Flux of matching Projectile entities
     */
    public Flux<Projectile> searchProjectiles(String userId, String query) {
        return projectileRepository.searchByOwnerIdAndQuery(userId, query);
    }

    /**
     * Retrieves a specific projectile by ID and user ID.
     *
     * @param id     the ID of the projectile
     * @param userId the ID of the user
     * @return a Mono containing the Projectile if found, or empty
     */
    public Mono<Projectile> getProjectileById(Long id, String userId) {
        return projectileRepository.findByIdAndOwnerId(id, userId);
    }

    /**
     * Creates a new projectile.
     *
     * @param projectile the Projectile entity to create
     * @return a Mono containing the created Projectile
     */
    public Mono<Projectile> createProjectile(Projectile projectile) {
        return projectileRepository.save(projectile);
    }

    /**
     * Updates an existing projectile.
     *
     * @param projectile the Projectile entity to update
     * @return a Mono containing the updated Projectile
     */
    public Mono<Projectile> updateProjectile(Projectile projectile) {
        return projectileRepository.save(projectile);
    }

    /**
     * Deletes a projectile.
     *
     * @param projectile the Projectile entity to delete
     * @return a Mono<Void> that completes when deletion is finished
     */
    public Mono<Void> deleteProjectile(Projectile projectile) {
        return projectileRepository.delete(projectile);
    }
}
