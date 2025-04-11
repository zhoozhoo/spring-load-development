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

import java.util.ArrayList;
import java.util.List;

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

import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.model.Load;
import ca.zhoozhoo.loaddev.loads.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/loads")
@Log4j2
public class LoadsController {

    private static final String ERROR_RETRIEVING_LOAD = "Error retrieving load with id: %d";
    private static final String ERROR_CREATING_LOAD = "Error creating load";
    private static final String ERROR_UPDATING_LOAD = "Error updating load with id: %d";
    private static final String ERROR_DELETING_LOAD = "Error deleting load with id: %d";

    private final LoadRepository loadRepository;
    private final SecurityUtils securityUtils;

    public LoadsController(LoadRepository loadRepository, SecurityUtils securityUtils) {
        this.loadRepository = loadRepository;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('loads:view')")
    public Flux<Load> getAllLoads() {
        return loadRepository.findAll()
                .onErrorResume(e -> {
                    log.error("Error retrieving all loads", e);
                    return Flux.error(new RuntimeException("Failed to retrieve loads"));
                });
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('loads:view') and isLoadOwner(#id)")
    public Mono<ResponseEntity<Load>> getLoadById(@PathVariable Long id) {
        return loadRepository.findById(id)
                .map(load -> ok(load))
                .defaultIfEmpty(notFound().build())
                .onErrorResume(e -> {
                    log.error(String.format(ERROR_RETRIEVING_LOAD, id), e);
                    return just(status(INTERNAL_SERVER_ERROR).build());
                });
    }

    @PostMapping
    @PreAuthorize("hasAuthority('loads:edit')")
    public Mono<ResponseEntity<Load>> createLoad(@Valid @RequestBody Load load) {
        return validateLoad(load)
                .flatMap(validLoad -> securityUtils.getCurrentUserId()
                        .flatMap(userId -> {
                            Load newLoad = new Load(
                                    null,
                                    userId,
                                    validLoad.name(),
                                    validLoad.description(),
                                    validLoad.powderManufacturer(),
                                    validLoad.powderType(),
                                    validLoad.powderCharge(),
                                    validLoad.bulletManufacturer(),
                                    validLoad.bulletType(),
                                    validLoad.bulletWeight(),
                                    validLoad.primerManufacturer(),
                                    validLoad.primerType(),
                                    validLoad.distanceFromLands(),
                                    validLoad.rifleId());
                            return loadRepository.save(newLoad);
                        }))
                .map(savedLoad -> ResponseEntity.status(CREATED).body(savedLoad))
                .onErrorResume(e -> handleError(e, ERROR_CREATING_LOAD));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('loads:edit') and isLoadOwner(#id)")
    public Mono<ResponseEntity<Load>> updateLoad(@PathVariable Long id, @Valid @RequestBody Load load) {
        return loadRepository.findById(id)
                .flatMap(existingLoad -> loadRepository.save(updateExistingLoad(existingLoad, load)))
                .map(updatedLoad -> ok(updatedLoad))
                .defaultIfEmpty(notFound().build())
                .onErrorResume(e -> handleError(e, String.format(ERROR_UPDATING_LOAD, id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('loads:delete') and isLoadOwner(#id)")
    public Mono<ResponseEntity<Void>> deleteLoad(@PathVariable Long id) {
        return loadRepository.findById(id)
                .flatMap(existingLoad -> loadRepository.delete(existingLoad)
                        .then(just(new ResponseEntity<Void>(NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND))
                .onErrorResume(e -> {
                    log.error(String.format(ERROR_DELETING_LOAD, id), e);
                    return just(new ResponseEntity<>(INTERNAL_SERVER_ERROR));
                });
    }

    private Load updateExistingLoad(Load existingLoad, Load load) {
        return new Load(
                existingLoad.id(),
                existingLoad.ownerId(),
                load.name(),
                load.description(),
                load.powderManufacturer(),
                load.powderType(),
                load.powderCharge(),
                load.bulletManufacturer(),
                load.bulletType(),
                load.bulletWeight(),
                load.primerManufacturer(),
                load.primerType(),
                load.distanceFromLands(),
                load.rifleId());
    }

    private Mono<ResponseEntity<Load>> handleError(Throwable e, String errorMessage) {
        log.error(errorMessage, e);
        if (e instanceof jakarta.validation.ConstraintViolationException) {
            return just(ResponseEntity.status(BAD_REQUEST).build());
        } else if (e instanceof IllegalArgumentException) {
            return just(ResponseEntity.status(BAD_REQUEST).body(null));
        }
        return just(ResponseEntity.status(INTERNAL_SERVER_ERROR).build());
    }

    private Mono<Load> validateLoad(Load load) {
        List<String> errors = new ArrayList<>();

        if (load.name() == null || load.name().trim().isEmpty()) {
            errors.add("Load name is required");
        }
        if (load.rifleId() == null) {
            errors.add("Rifle ID is required");
        }
        if (load.powderCharge() != null && load.powderCharge() <= 0) {
            errors.add("Powder charge must be positive");
        }
        if (load.bulletWeight() != null && load.bulletWeight() <= 0) {
            errors.add("Bullet weight must be positive");
        }

        return errors.isEmpty() ? just(load)
                : error(new IllegalArgumentException(String.join(", ", errors)));
    }
}
