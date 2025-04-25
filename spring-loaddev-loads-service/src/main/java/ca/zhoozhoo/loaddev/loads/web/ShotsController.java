package ca.zhoozhoo.loaddev.loads.web;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import ca.zhoozhoo.loaddev.loads.model.Shot;
import ca.zhoozhoo.loaddev.loads.security.CurrentUser;
import ca.zhoozhoo.loaddev.loads.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/shots")
@Log4j2
@PreAuthorize("hasRole('RELOADER')")
public class ShotsController {

    @Autowired
    private ShotRepository shotRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasAuthority('shots:view')")
    public Flux<Shot> getShotsByGroupId(@CurrentUser String userId, @PathVariable Long groupId) {
        return shotRepository.findByGroupIdAndOwnerId(groupId, userId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('shots:view')")
    public Mono<ResponseEntity<Shot>> getShotById(@CurrentUser String userId, @PathVariable Long id) {
        return shotRepository.findById(id)
                .map(shot -> ok(shot))
                .defaultIfEmpty(notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('shots:edit')")
    public Mono<ResponseEntity<Shot>> createShot(@CurrentUser String userId, @Valid @RequestBody Shot shot) {
        return securityUtils.getCurrentUserId()
                .flatMap(ownerid -> {
                    Shot newShot = new Shot(
                            shot.id(),
                            ownerid,
                            shot.groupId(),
                            shot.velocity(),
                            shot.velocityUnit());
                    return shotRepository.save(newShot);
                })
                .map(savedShot -> status(CREATED).body(savedShot));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('shots:edit')")
    public Mono<ResponseEntity<Shot>> updateShot(@CurrentUser String userId, @PathVariable Long id,
            @Valid @RequestBody Shot shot) {
        return shotRepository.findById(id)
                .flatMap(existingShot -> {
                    Shot updatedShot = new Shot(
                            existingShot.id(),
                            existingShot.ownerId(),
                            shot.groupId(),
                            shot.velocity(),
                            shot.velocityUnit());
                    return shotRepository.save(updatedShot);
                })
                .map(updatedShot -> ok(updatedShot))
                .defaultIfEmpty(notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('shots:delete')")
    public Mono<ResponseEntity<Void>> deleteShot(@CurrentUser String userId, @PathVariable Long id) {
        return shotRepository.findById(id)
                .flatMap(existingShot -> shotRepository.delete(existingShot)
                        .then(Mono.just(new ResponseEntity<Void>(NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }
}
