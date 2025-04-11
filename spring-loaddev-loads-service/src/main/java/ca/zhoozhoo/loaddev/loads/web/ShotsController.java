package ca.zhoozhoo.loaddev.loads.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
    @PreAuthorize("hasAuthority('shots:view') and isGroupOwner(#groupId)")
    public Flux<Shot> getShotsByGroupId(@PathVariable Long groupId) {
        return shotRepository.findByGroupId(groupId)
                .onErrorResume(e -> {
                    log.error("Error retrieving shots for group: " + groupId, e);
                    return Flux.error(new RuntimeException("Failed to retrieve shots"));
                });
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('shots:view') and isShotOwner(#id)")
    public Mono<ResponseEntity<Shot>> getShotById(@CurrentUser JwtAuthenticationToken principal,
            @PathVariable Long id) {
        return shotRepository.findById(id)
                .map(shot -> ok(shot))
                .defaultIfEmpty(notFound().build())
                .onErrorResume(e -> {
                    log.error("Error retrieving shot with id: " + id, e);
                    return just(status(INTERNAL_SERVER_ERROR).build());
                });
    }

    @PostMapping
    @PreAuthorize("hasAuthority('shots:edit')")
    public Mono<ResponseEntity<Shot>> createShot(@CurrentUser JwtAuthenticationToken principal,
            @Valid @RequestBody Shot shot) {
        return securityUtils.getCurrentUserId()
                .flatMap(ownerid -> {
                    Shot newShot = new Shot(
                            shot.id(),
                            ownerid, // Set current user as owner
                            shot.groupId(),
                            shot.velocity());
                    return shotRepository.save(newShot);
                })
                .map(savedShot -> status(CREATED).body(savedShot))
                .onErrorResume(e -> {
                    log.error("Error creating shot", e);
                    if (e instanceof jakarta.validation.ConstraintViolationException) {
                        return just(status(BAD_REQUEST).build());
                    }
                    return just(status(INTERNAL_SERVER_ERROR).build());
                });
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('shots:edit') and isShotOwner(#id)")
    public Mono<ResponseEntity<Shot>> updateShot(@CurrentUser JwtAuthenticationToken principal, @PathVariable Long id,
            @Valid @RequestBody Shot shot) {
        return shotRepository.findById(id)
                .flatMap(existingShot -> {
                    try {
                        Shot updatedShot = new Shot(
                                existingShot.id(),
                                existingShot.ownerId(), // Preserve original owner
                                shot.groupId(),
                                shot.velocity());
                        return shotRepository.save(updatedShot);
                    } catch (Exception e) {
                        log.error("Error updating shot with id: " + id, e);
                        return error(e);
                    }
                })
                .map(updatedShot -> ok(updatedShot))
                .defaultIfEmpty(notFound().build())
                .onErrorResume(e -> {
                    log.error("Error in update operation for shot with id: " + id, e);
                    return just(status(INTERNAL_SERVER_ERROR).build());
                });
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('shots:delete') and isShotOwner(#id)")
    public Mono<ResponseEntity<Void>> deleteShot(@CurrentUser JwtAuthenticationToken principal, @PathVariable Long id) {
        return shotRepository.findById(id)
                .flatMap(existingShot -> shotRepository.delete(existingShot)
                        .then(just(new ResponseEntity<Void>(NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND))
                .onErrorResume(e -> {
                    log.error("Error deleting shot with id: " + id, e);
                    return just(new ResponseEntity<>(INTERNAL_SERVER_ERROR));
                });
    }
}
