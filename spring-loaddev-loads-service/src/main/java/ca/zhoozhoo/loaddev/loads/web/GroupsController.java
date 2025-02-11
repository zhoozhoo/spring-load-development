package ca.zhoozhoo.loaddev.loads.web;

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

import ca.zhoozhoo.loaddev.loads.dao.GroupRepository;
import ca.zhoozhoo.loaddev.loads.model.Group;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/groups")
public class GroupsController {

    @Autowired
    private GroupRepository groupRepository;

    @GetMapping
    public Flux<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Group>> getGroupById(@PathVariable Long id) {
        return groupRepository.findById(id)
                .map(group -> ResponseEntity.ok(group))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Group> createGroup(@Valid @RequestBody Group group) {
        return groupRepository.save(group);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Group>> updateGroup(@PathVariable Long id, @Valid @RequestBody Group group) {
        return groupRepository.findById(id)
                .flatMap(existingGroup -> {
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
                })
                .map(updatedGroup -> ResponseEntity.ok(updatedGroup))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteGroup(@PathVariable Long id) {
        return groupRepository.findById(id)
                .flatMap(existingGroup -> groupRepository.delete(existingGroup)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
