package ca.zhoozhoo.loaddev.rifles.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import ca.zhoozhoo.loaddev.rifles.dao.RifleRepository;
import ca.zhoozhoo.loaddev.rifles.model.Rifle;
import ca.zhoozhoo.loaddev.rifles.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rifles")
@Log4j2
public class RifleController {

    @Autowired
    private RifleRepository rifleRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('rifles:view')")
    public Flux<Rifle> getAllRifles(@CurrentUser String userId) {
        return rifleRepository.findAllByOwnerId(userId);
    }

    @PreAuthorize("hasAuthority('rifles:view')")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Rifle>> getRifleById(@CurrentUser String userId, @PathVariable Long id) {
        return rifleRepository.findByIdAndOwnerId(id, userId)
                .map(rifle -> {
                    log.debug("Found rifle: {}", rifle);
                    return ok(rifle);
                })
                .defaultIfEmpty(notFound().build());
    }

    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('rifles:edit')")
    public Mono<ResponseEntity<Rifle>> createRifle(@CurrentUser String userId, @Valid @RequestBody Rifle rifle) {
        return Mono.just(new Rifle(
                rifle.id(),
                userId,
                rifle.name(),
                rifle.description(),
                rifle.caliber(),
                rifle.barrelLength(),
                rifle.barrelContour(),
                rifle.twistRate(),
                rifle.freeBore(),
                rifle.rifling()))
                .flatMap(rifleRepository::save)
                .map(savedRifle -> {
                    log.info("Created new rifle with id: {}", savedRifle.id());
                    return status(CREATED).body(savedRifle);
                });
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('rifles:edit')")
    public Mono<ResponseEntity<Rifle>> updateRifle(@PathVariable Long id, @Valid @RequestBody Rifle rifle) {
        return rifleRepository.findById(id)
                .flatMap(existingRifle -> {
                    Rifle updatedRifle = new Rifle(
                            existingRifle.id(),
                            existingRifle.ownerId(),
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
                .map(updatedRifle -> {
                    log.info("Updated rifle with id: {}", updatedRifle.id());
                    return ok(updatedRifle);
                })
                .defaultIfEmpty(notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('rifles:delete')")
    public Mono<ResponseEntity<Void>> deleteRifle(@CurrentUser String userId, @PathVariable Long id) {
        return rifleRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingRifle -> rifleRepository.delete(existingRifle)
                        .then(Mono.just(new ResponseEntity<Void>(NO_CONTENT)))
                        .doOnSuccess(result -> log.info("Deleted rifle with id: {}", id)))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(BAD_REQUEST)
    public Mono<String> handleValidationException(WebExchangeBindException ex) {
        log.error("Validation error: {}", ex.getMessage());
        return Mono.just(ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(CONFLICT)
    public Mono<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());
        return Mono.just("Database error: " + ex.getMessage());
    }
}