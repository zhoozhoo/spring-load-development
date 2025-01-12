package ca.zhoozhoo.loaddev.load_development.web;

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

import ca.zhoozhoo.loaddev.load_development.dao.LoadRepository;
import ca.zhoozhoo.loaddev.load_development.model.Load;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/loads")
public class LoadsController {

    @Autowired
    private LoadRepository loadRepository;

    @GetMapping
    public Flux<Load> getAllLoads() {
        return loadRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Load>> getLoadById(@PathVariable Long id) {
        return loadRepository.findById(id)
                .map(load -> ResponseEntity.ok(load))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Load> createLoad(@Valid @RequestBody Load load) {
        return loadRepository.save(load);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Load>> updateLoad(@PathVariable Long id, @Valid @RequestBody Load load) {
        return loadRepository.findById(id)
                .flatMap(existingLoad -> {
                    Load updatedLoad = new Load(
                            existingLoad.id(),
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
                    return loadRepository.save(updatedLoad);
                })
                .map(updatedLoad -> ResponseEntity.ok(updatedLoad))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteLoad(@PathVariable Long id) {
        return loadRepository.findById(id)
                .flatMap(existingLoad -> loadRepository.delete(existingLoad)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
