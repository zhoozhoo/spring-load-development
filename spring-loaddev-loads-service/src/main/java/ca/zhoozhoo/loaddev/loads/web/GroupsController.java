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

import ca.zhoozhoo.loaddev.loads.dao.GroupRepository;
import ca.zhoozhoo.loaddev.loads.model.Group;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/groups")
@Log4j2
public class GroupsController {

    @Autowired
    private GroupRepository groupRepository;

    @GetMapping
    public Flux<Group> getAllGroups() {
        return groupRepository.findAll()
                .onErrorResume(e -> {
                    log.error("Error retrieving all groups", e);
                    return Flux.error(new RuntimeException("Failed to retrieve groups"));
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Group>> getGroupById(@PathVariable Long id) {
        return groupRepository.findById(id)
                .map(group -> ok(group))
                .defaultIfEmpty(notFound().build())
                .onErrorResume(e -> {
                    log.error("Error retrieving group with id: " + id, e);
                    return just(status(INTERNAL_SERVER_ERROR).build());
                });
    }

    @PostMapping
    public Mono<ResponseEntity<Group>> createGroup(@Valid @RequestBody Group group) {
        return groupRepository.save(group)
                .map(savedGroup -> status(CREATED).body(savedGroup))
                .onErrorResume(e -> {
                    log.error("Error creating group", e);
                    if (e instanceof jakarta.validation.ConstraintViolationException) {
                        return just(status(BAD_REQUEST).build());
                    }
                    return just(status(INTERNAL_SERVER_ERROR).build());
                });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Group>> updateGroup(@PathVariable Long id, @Valid @RequestBody Group group) {
        return groupRepository.findById(id)
                .flatMap(existingGroup -> {
                    try {
                        Group updatedGroup = new Group(
                                existingGroup.id(),
                                group.numberOfShots(),
                                group.targetRange(),
                                group.groupSize(),
                                group.mean(),
                                group.median(),
                                group.min(),
                                group.max(),
                                group.standardDeviation(),
                                group.extremeSpread());
                        return groupRepository.save(updatedGroup);
                    } catch (Exception e) {
                        log.error("Error updating group with id: " + id, e);
                        return error(e);
                    }
                })
                .map(updatedGroup -> ResponseEntity.ok(updatedGroup))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("Error in update operation for group with id: " + id, e);
                    return just(status(INTERNAL_SERVER_ERROR).build());
                });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteGroup(@PathVariable Long id) {
        return groupRepository.findById(id)
                .flatMap(existingGroup -> groupRepository.delete(existingGroup)
                        .then(just(new ResponseEntity<Void>(NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND))
                .onErrorResume(e -> {
                    log.error("Error deleting group with id: " + id, e);
                    return just(new ResponseEntity<>(INTERNAL_SERVER_ERROR));
                });
    }
}
