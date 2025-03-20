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
                .map(rifle -> ok(rifle))
                .defaultIfEmpty(notFound().build());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(BAD_REQUEST)
    public Mono<String> handleValidationException(WebExchangeBindException ex) {
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
        return Mono.just("Database error: " + ex.getMessage());
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public Mono<ResponseEntity<Rifle>> createRifle(@Valid @RequestBody Rifle rifle) {
        return rifleRepository.save(rifle)
                .map(savedRifle -> status(CREATED).body(savedRifle))
                .onErrorMap(DataIntegrityViolationException.class, 
                    e -> new DataIntegrityViolationException("Rifle creation failed: " + e.getMessage()));
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
                .map(updatedRifle -> ok(updatedRifle))
                .defaultIfEmpty(notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteRifle(@PathVariable Long id) {
        return rifleRepository.findById(id)
                .flatMap(existingRifle -> rifleRepository.delete(existingRifle)
                        .then(Mono.just(new ResponseEntity<Void>(NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }
}