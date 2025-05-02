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

import ca.zhoozhoo.loaddev.loads.dao.GroupRepository;
import ca.zhoozhoo.loaddev.loads.model.Group;
import ca.zhoozhoo.loaddev.loads.model.GroupStatistics;
import ca.zhoozhoo.loaddev.loads.security.CurrentUser;
import ca.zhoozhoo.loaddev.loads.security.SecurityUtils;
import ca.zhoozhoo.loaddev.loads.service.LoadsService;
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

    @Autowired
    private LoadsService loadsService;

    @GetMapping("/load/{loadId}")
    @PreAuthorize("hasAuthority('groups:view')")
    public Flux<Group> getAllGroupsByLoadId(@CurrentUser String userId, @PathVariable Long loadId) {
        return groupRepository.findAllByLoadIdAndOwnerId(loadId, userId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('groups:view')")
    public Mono<ResponseEntity<Group>> getGroupById(@CurrentUser String userId, @PathVariable Long id) {
        return groupRepository.findById(id)
                .map(group -> ok(group))
                .defaultIfEmpty(notFound().build());
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAuthority('groups:view')")
    public Mono<ResponseEntity<GroupStatistics>> getGroupStatistics(@CurrentUser String userId, @PathVariable Long id) {
        return loadsService.getGroupStatistics(id, userId)
                .map(stats -> ok(stats))
                .defaultIfEmpty(notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('groups:edit')")
    public Mono<ResponseEntity<Group>> createGroup(@CurrentUser String userId, @Valid @RequestBody Group group) {
        return securityUtils.getCurrentUserId()
                .flatMap(ownerid -> {
                    var newGroup = new Group(
                            group.id(),
                            ownerid,
                            group.loadId(),
                            group.date(),
                            group.powderCharge(),
                            group.targetRange(),
                            group.groupSize());
                    return groupRepository.save(newGroup);
                })
                .map(savedGroup -> status(CREATED).body(savedGroup));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('groups:edit')")
    public Mono<ResponseEntity<Group>> updateGroup(@CurrentUser String userId, @PathVariable Long id,
            @Valid @RequestBody Group group) {
        return groupRepository.findById(id)
                .flatMap(existingGroup -> {
                    var updatedGroup = new Group(
                            existingGroup.id(),
                            existingGroup.ownerId(),
                            existingGroup.loadId(),
                            group.date(),
                            group.powderCharge(),
                            group.targetRange(),
                            group.groupSize());
                    return groupRepository.save(updatedGroup);
                })
                .map(updatedGroup -> ok(updatedGroup))
                .defaultIfEmpty(notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('groups:delete')")
    public Mono<ResponseEntity<Void>> deleteGroup(@CurrentUser String userId, @PathVariable Long id) {
        return groupRepository.findById(id)
                .flatMap(existingGroup -> groupRepository.delete(existingGroup)
                        .then(Mono.just(new ResponseEntity<Void>(NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }
}
