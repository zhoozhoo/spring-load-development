package ca.zhoozhoo.loaddev.rifles.service;

import org.springframework.stereotype.Service;
import ca.zhoozhoo.loaddev.rifles.dao.RifleRepository;
import ca.zhoozhoo.loaddev.rifles.model.Rifle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RiflesService {

    private final RifleRepository rifleRepository;

    public RiflesService(RifleRepository rifleRepository) {
        this.rifleRepository = rifleRepository;
    }

    public Flux<Rifle> getAllRifles(String userId) {
        return rifleRepository.findAllByOwnerId(userId);
    }

    public Mono<Rifle> getRifleById(Long id, String userId) {
        return rifleRepository.findByIdAndOwnerId(id, userId);
    }

    public Mono<Rifle> createRifle(Rifle rifle) {
        return rifleRepository.save(rifle);
    }

    public Mono<Rifle> updateRifle(Rifle rifle) {
        return rifleRepository.save(rifle);
    }

    public Mono<Void> deleteRifle(Rifle rifle) {
        return rifleRepository.delete(rifle);
    }
}
