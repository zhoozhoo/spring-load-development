package ca.zhoozhoo.loaddev.rifles.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ca.zhoozhoo.loaddev.rifles.dao.RifleRepository;
import ca.zhoozhoo.loaddev.rifles.model.Rifle;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rifles")
public class RifleController {

    @Autowired
    private RifleRepository rifleRepository;

    @GetMapping
    public Flux<Rifle> getAllRifles() {
        return rifleRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Rifle>> getRifleById(@PathVariable Long id) {
        return rifleRepository.findById(id)
                .map(rifle -> ResponseEntity.ok(rifle))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Rifle> createRifle(@Valid @RequestBody Rifle rifle) {
        return rifleRepository.save(rifle);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Rifle>> updateRifle(@PathVariable Long id, @Valid @RequestBody Rifle rifle) {
        return rifleRepository.findById(id)
                .flatMap(existingRifle -> {
                    Rifle updatedRifle = new Rifle(
                            existingRifle.id(),
                            rifle.name(),
                            rifle.description(),
                            rifle.caliber(),
                            rifle.barrelLength(),
                            rifle.barrelContour(),
                            rifle.twistRate(),
                            rifle.freeBore(),
                            rifle.rifling());
                    return rifleRepository.save(updatedRifle);
                })
                .map(updatedRifle -> ResponseEntity.ok(updatedRifle))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteRifle(@PathVariable Long id) {
        return rifleRepository.findById(id)
                .flatMap(existingRifle -> rifleRepository.delete(existingRifle)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}