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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.model.Load;
import ca.zhoozhoo.loaddev.loads.security.CurrentUser;
import ca.zhoozhoo.loaddev.loads.service.LoadsService;
import ca.zhoozhoo.loaddev.loads.model.GroupStatistics;
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

    @Autowired
    private LoadsService loadsService;

    @GetMapping
    @PreAuthorize("hasAuthority('loads:view')")
    public Flux<Load> getAllLoads(@CurrentUser String userId) {
        return loadRepository.findAllByOwnerId(userId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('loads:view')")
    public Mono<ResponseEntity<Load>> getLoadById(@CurrentUser String userId, @PathVariable Long id) {
        return loadRepository.findByIdAndOwnerId(id, userId)
                .map(load -> {
                    log.debug("Found load: {}", load);
                    return ok(load);
                })
                .defaultIfEmpty(notFound().build());
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAuthority('loads:view')")
    public Flux<GroupStatistics> getLoadStatistics(@CurrentUser String userId, @PathVariable Long id) {
        return loadsService.getGroupStatisticsForLoad(id, userId);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('loads:edit')")
    public Mono<ResponseEntity<Load>> createLoad(@CurrentUser String userId, @Valid @RequestBody Load load) {
        return Mono.just(new Load(
                null,
                userId,
                load.name(),
                load.description(),
                load.measurementUnits(),
                load.powderManufacturer(),
                load.powderType(),
                load.bulletManufacturer(),
                load.bulletType(),
                load.bulletWeight(),
                load.primerManufacturer(),
                load.primerType(),
                load.distanceFromLands(),
                load.caseOverallLength(),
                load.neckTension(),
                load.rifleId()))
                .flatMap(loadRepository::save)
                .map(savedLoad -> {
                    log.info("Created new load with id: {}", savedLoad.id());
                    return status(CREATED).body(savedLoad);
                });
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('loads:edit')")
    public Mono<ResponseEntity<Load>> updateLoad(@CurrentUser String userId, @PathVariable Long id,
            @Valid @RequestBody Load load) {
        return loadRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingLoad -> {
                    Load updatedLoad = new Load(
                            existingLoad.id(),
                            existingLoad.ownerId(),
                            load.name(),
                            load.description(),
                            load.measurementUnits(),
                            load.powderManufacturer(),
                            load.powderType(),
                            load.bulletManufacturer(),
                            load.bulletType(),
                            load.bulletWeight(),
                            load.primerManufacturer(),
                            load.primerType(),
                            load.distanceFromLands(),
                            load.caseOverallLength(),
                            load.neckTension(),
                            load.rifleId());
                    return loadRepository.save(updatedLoad);
                })
                .map(updatedLoad -> {
                    log.info("Updated load with id: {}", updatedLoad.id());
                    return ok(updatedLoad);
                })
                .defaultIfEmpty(notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('loads:delete')")
    public Mono<ResponseEntity<Void>> deleteLoad(@CurrentUser String userId, @PathVariable Long id) {
        return loadRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingLoad -> loadRepository.delete(existingLoad)
                        .then(Mono.just(new ResponseEntity<Void>(NO_CONTENT)))
                        .doOnSuccess(result -> log.info("Deleted load with id: {}", id)))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }
}
