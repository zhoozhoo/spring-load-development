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

import ca.zhoozhoo.loaddev.components.dao.ProjectileRepository;
import ca.zhoozhoo.loaddev.components.model.Projectile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ProjectileServiceTest {

    @Mock
    private ProjectileRepository projectileRepository;

    private ProjectileService projectileService;

    @BeforeEach
    void setUp() {
        projectileService = new ProjectileService(projectileRepository);
    }

    @Test
    void getAllProjectiles_ShouldReturnFluxOfProjectiles() {
        Projectile projectile = new Projectile(1L, "user1", "Brand", null, "Model", null, 100);
        when(projectileRepository.findAllByOwnerId(anyString())).thenReturn(Flux.just(projectile));

        StepVerifier.create(projectileService.getAllProjectiles("user1"))
                .expectNext(projectile)
                .verifyComplete();

        verify(projectileRepository).findAllByOwnerId("user1");
    }

    @Test
    void searchProjectiles_ShouldReturnFluxOfProjectiles() {
        Projectile projectile = new Projectile(1L, "user1", "Brand", null, "Model", null, 100);
        when(projectileRepository.searchByOwnerIdAndQuery(anyString(), anyString())).thenReturn(Flux.just(projectile));

        StepVerifier.create(projectileService.searchProjectiles("user1", "query"))
                .expectNext(projectile)
                .verifyComplete();

        verify(projectileRepository).searchByOwnerIdAndQuery("user1", "query");
    }

    @Test
    void getProjectileById_ShouldReturnProjectile() {
        Projectile projectile = new Projectile(1L, "user1", "Brand", null, "Model", null, 100);
        when(projectileRepository.findByIdAndOwnerId(anyLong(), anyString())).thenReturn(Mono.just(projectile));

        StepVerifier.create(projectileService.getProjectileById(1L, "user1"))
                .expectNext(projectile)
                .verifyComplete();

        verify(projectileRepository).findByIdAndOwnerId(1L, "user1");
    }

    @Test
    void createProjectile_ShouldReturnSavedProjectile() {
        Projectile projectile = new Projectile(1L, "user1", "Brand", null, "Model", null, 100);
        when(projectileRepository.save(any(Projectile.class))).thenReturn(Mono.just(projectile));

        StepVerifier.create(projectileService.createProjectile(projectile))
                .expectNext(projectile)
                .verifyComplete();

        verify(projectileRepository).save(projectile);
    }

    @Test
    void updateProjectile_ShouldReturnUpdatedProjectile() {
        Projectile projectile = new Projectile(1L, "user1", "Brand", null, "Model", null, 100);
        when(projectileRepository.save(any(Projectile.class))).thenReturn(Mono.just(projectile));

        StepVerifier.create(projectileService.updateProjectile(projectile))
                .expectNext(projectile)
                .verifyComplete();

        verify(projectileRepository).save(projectile);
    }

    @Test
    void deleteProjectile_ShouldComplete() {
        Projectile projectile = new Projectile(1L, "user1", "Brand", null, "Model", null, 100);
        when(projectileRepository.delete(any(Projectile.class))).thenReturn(Mono.empty());

        StepVerifier.create(projectileService.deleteProjectile(projectile))
                .verifyComplete();

        verify(projectileRepository).delete(projectile);
    }
}
