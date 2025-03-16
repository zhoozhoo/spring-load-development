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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.model.Load;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/loads")
@Log4j2
public class LoadsController {

    @Autowired
    private LoadRepository loadRepository;

    @GetMapping
    public Flux<Load> getAllLoads() {
        return loadRepository.findAll()
                .onErrorResume(e -> {
                    log.error("Error retrieving all loads", e);
                    return Flux.error(new RuntimeException("Failed to retrieve loads"));
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Load>> getLoadById(@PathVariable Long id) {
        return loadRepository.findById(id)
                .map(load -> ok(load))
                .defaultIfEmpty(notFound().build())
                .onErrorResume(e -> {
                    log.error("Error retrieving load with id: " + id, e);
                    return just(status(INTERNAL_SERVER_ERROR).build());
                });
    }

    @PostMapping
    public Mono<ResponseEntity<Load>> createLoad(@Valid @RequestBody Load load) {
        return loadRepository.save(load)
                .map(savedLoad -> status(CREATED).body(savedLoad))
                .onErrorResume(e -> {
                    log.error("Error creating load", e);
                    if (e instanceof jakarta.validation.ConstraintViolationException) {
                        return just(status(BAD_REQUEST).build());
                    }
                    return just(status(INTERNAL_SERVER_ERROR).build());
                });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Load>> updateLoad(@PathVariable Long id, @Valid @RequestBody Load load) {
        return loadRepository.findById(id)
                .flatMap(existingLoad -> {
                    try {
                        Load updatedLoad = new Load(existingLoad.id(), load.name(), load.description(),
                                load.powderManufacturer(), load.powderType(), load.powderCharge(),
                                load.bulletManufacturer(), load.bulletType(), load.bulletWeight(),
                                load.primerManufacturer(), load.primerType(), load.distanceFromLands(),
                                load.rifleId());
                        return loadRepository.save(updatedLoad);
                    } catch (Exception e) {
                        log.error("Error updating load with id: " + id, e);
                        return error(e);
                    }
                })
                .map(updatedLoad -> ok(updatedLoad))
                .defaultIfEmpty(notFound().build())
                .onErrorResume(e -> {
                    log.error("Error in update operation for load with id: " + id, e);
                    return just(status(INTERNAL_SERVER_ERROR).build());
                });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteLoad(@PathVariable Long id) {
        return loadRepository.findById(id)
                .flatMap(existingLoad -> loadRepository.delete(existingLoad)
                        .then(just(new ResponseEntity<Void>(NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND))
                .onErrorResume(e -> {
                    log.error("Error deleting load with id: " + id, e);
                    return just(new ResponseEntity<>(INTERNAL_SERVER_ERROR));
                });
    }
}
