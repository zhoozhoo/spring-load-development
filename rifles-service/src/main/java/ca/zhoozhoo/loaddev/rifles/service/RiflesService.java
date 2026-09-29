package ca.zhoozhoo.loaddev.rifles.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.zhoozhoo.loaddev.rifles.dao.RifleRepository;
import ca.zhoozhoo.loaddev.rifles.events.RifleEventPublisher;
import ca.zhoozhoo.loaddev.rifles.model.Rifle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/// Service class for managing Rifle entities.
///
/// This service provides reactive CRUD operations for rifles and publishes
/// domain events after successful writes via the transactional outbox pattern, so event
/// delivery survives Kafka outages without failing or blocking the write path.
@Service
public class RiflesService {

    private final RifleRepository rifleRepository;
    private final RifleEventPublisher rifleEventPublisher;

    public RiflesService(RifleRepository rifleRepository, RifleEventPublisher rifleEventPublisher) {
        this.rifleRepository = rifleRepository;
        this.rifleEventPublisher = rifleEventPublisher;
    }

    public Flux<Rifle> getAllRifles(String userId, Pageable pageable) {
        return rifleRepository.findAllByOwnerId(userId, pageable);
    }

    public Mono<Rifle> getRifleById(Long id, String userId) {
        return rifleRepository.findByIdAndOwnerId(id, userId);
    }

    @Transactional
    public Mono<Rifle> createRifle(Rifle rifle) {
        return rifleRepository.save(rifle)
                .flatMap(saved -> rifleEventPublisher.publishCreated(saved).thenReturn(saved));
    }

    @Transactional
    public Mono<Rifle> updateRifle(Rifle rifle) {
        return rifleRepository.save(rifle)
                .flatMap(saved -> rifleEventPublisher.publishUpdated(saved).thenReturn(saved));
    }

    @Transactional
    public Mono<Void> deleteRifle(Rifle rifle) {
        return rifleRepository.delete(rifle)
                .then(rifleEventPublisher.publishDeleted(rifle));
    }
}
