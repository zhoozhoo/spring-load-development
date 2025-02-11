package ca.zhoozhoo.loaddev.loads.web;

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

import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import ca.zhoozhoo.loaddev.loads.model.Shot;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/shots")
public class ShotsController {

    @Autowired
    private ShotRepository shotRepository;

    @GetMapping("/group/{groupId}")
    public Flux<Shot> getShotsByGroupId(@PathVariable Long groupId) {
        return shotRepository.findByGroupId(groupId);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Shot>> getShotById(@PathVariable Long id) {
        return shotRepository.findById(id)
                .map(shot -> ResponseEntity.ok(shot))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Shot> createShot(@Valid @RequestBody Shot shot) {
        return shotRepository.save(shot);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Shot>> updateShot(@PathVariable Long id, @Valid @RequestBody Shot shot) {
        return shotRepository.findById(id)
                .flatMap(existingShot -> {
                    Shot updatedShot = new Shot(
                            existingShot.id(),
                            shot.groupId(),
                            shot.velocity());
                    return shotRepository.save(updatedShot);
                })
                .map(updatedShot -> ResponseEntity.ok(updatedShot))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteShot(@PathVariable Long id) {
        return shotRepository.findById(id)
                .flatMap(existingShot -> shotRepository.delete(existingShot)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
