package ca.zhoozhoo.loaddev.components.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.zhoozhoo.loaddev.components.dao.ProjectileRepository;
import ca.zhoozhoo.loaddev.components.events.ProjectileEventPublisher;
import ca.zhoozhoo.loaddev.components.model.Projectile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/// Service class for managing Projectile components.
///
/// This service provides reactive operations for Projectiles.
/// It handles CRUD operations and searching capabilities,
/// ensuring data isolation by user ID.
///
/// @author Zhubin Salehi
@Service
public class ProjectileService {

    private final ProjectileRepository projectileRepository;
    private final ProjectileEventPublisher projectileEventPublisher;

    /// Constructs a new ProjectileService with required repository.
    ///
    /// @param projectileRepository the repository for Projectile entities
    /// @param projectileEventPublisher the publisher for Projectile domain events
    public ProjectileService(ProjectileRepository projectileRepository, ProjectileEventPublisher projectileEventPublisher) {
        this.projectileRepository = projectileRepository;
        this.projectileEventPublisher = projectileEventPublisher;
    }

    /// Retrieves all projectiles for a specific user with pagination.
    ///
    /// @param userId   the ID of the user
    /// @param pageable pagination parameters
    /// @return a Flux of Projectile entities
    public Flux<Projectile> getAllProjectiles(String userId, Pageable pageable) {
        return projectileRepository.findAllByOwnerId(userId, pageable);
    }

    /// Searches for projectiles belonging to a user based on a query string with pagination.
    ///
    /// @param userId   the ID of the user
    /// @param query    the search query
    /// @param pageable pagination parameters
    /// @return a Flux of matching Projectile entities
    public Flux<Projectile> searchProjectiles(String userId, String query, Pageable pageable) {
        return projectileRepository.searchByOwnerIdAndQuery(userId, query, pageable.getPageSize(), pageable.getOffset());
    }

    /// Retrieves a specific projectile by ID and user ID.
    ///
    /// @param id     the ID of the projectile
    /// @param userId the ID of the user
    /// @return a Mono containing the Projectile if found, or empty
    public Mono<Projectile> getProjectileById(Long id, String userId) {
        return projectileRepository.findByIdAndOwnerId(id, userId);
    }

    /// Creates a new projectile.
    ///
    /// @param projectile the Projectile entity to create
    /// @return a Mono containing the created Projectile
    @Transactional
    public Mono<Projectile> createProjectile(Projectile projectile) {
        return projectileRepository.save(projectile)
                .flatMap(saved -> projectileEventPublisher.publishCreated(saved).thenReturn(saved));
    }

    /// Updates an existing projectile.
    ///
    /// @param projectile the Projectile entity to update
    /// @return a Mono containing the updated Projectile
    @Transactional
    public Mono<Projectile> updateProjectile(Projectile projectile) {
        return projectileRepository.save(projectile)
                .flatMap(saved -> projectileEventPublisher.publishUpdated(saved).thenReturn(saved));
    }

    /// Deletes a projectile.
    ///
    /// @param projectile the Projectile entity to delete
    /// @return a Mono<Void> that completes when deletion is finished
    @Transactional
    public Mono<Void> deleteProjectile(Projectile projectile) {
        return projectileRepository.delete(projectile)
                .then(projectileEventPublisher.publishDeleted(projectile));
    }
}
