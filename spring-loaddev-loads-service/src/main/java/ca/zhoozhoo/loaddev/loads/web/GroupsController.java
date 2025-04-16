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
import ca.zhoozhoo.loaddev.loads.security.CurrentUser;
import ca.zhoozhoo.loaddev.loads.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/groups")
@Log4j2
@PreAuthorize("hasRole('RELOADER')")
public class GroupsController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize("hasAuthority('groups:view')")
    public Flux<Group> getAllGroups(@CurrentUser String userId) {
        return groupRepository.findAll()
                .onErrorResume(e -> {
                    log.error("Error retrieving all groups", e);
                    return Flux.error(new RuntimeException("Failed to retrieve groups"));
                });
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('groups:view')")
    public Mono<ResponseEntity<Group>> getGroupById(@CurrentUser String userId, @PathVariable Long id) {
        return groupRepository.findById(id)
                .map(group -> ok(group))
                .defaultIfEmpty(notFound().build())
                .onErrorResume(e -> {
                    log.error("Error retrieving group with id: " + id, e);
                    return just(status(INTERNAL_SERVER_ERROR).build());
                });
    }

    @PostMapping
    @PreAuthorize("hasAuthority('groups:edit')")
    public Mono<ResponseEntity<Group>> createGroup(@CurrentUser String userId, @Valid @RequestBody Group group) {
        return securityUtils.getCurrentUserId()
                .flatMap(ownerid -> {
                    Group newGroup = new Group(
                            group.id(),
                            ownerid, // Set the current user as owner
                            group.numberOfShots(),
                            group.targetRange(),
                            group.groupSize(),
                            group.mean(),
                            group.median(),
                            group.min(),
                            group.max(),
                            group.standardDeviation(),
                            group.extremeSpread());
                    return groupRepository.save(newGroup);
                })
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
    @PreAuthorize("hasAuthority('groups:edit')")
    public Mono<ResponseEntity<Group>> updateGroup(@CurrentUser String userId, @PathVariable Long id, @Valid @RequestBody Group group) {
        return groupRepository.findById(id)
                .flatMap(existingGroup -> {
                    try {
                        Group updatedGroup = new Group(
                                existingGroup.id(),
                                existingGroup.ownerId(), // Preserve the original owner
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
    @PreAuthorize("hasAuthority('groups:delete')")
    public Mono<ResponseEntity<Void>> deleteGroup(@CurrentUser String userId, @PathVariable Long id) {
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
