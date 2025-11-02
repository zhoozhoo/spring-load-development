package ca.zhoozhoo.loaddev.loads.web;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
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
import ca.zhoozhoo.loaddev.loads.dto.GroupStatisticsDto;
import ca.zhoozhoo.loaddev.loads.model.Group;
import ca.zhoozhoo.loaddev.loads.security.CurrentUser;
import ca.zhoozhoo.loaddev.loads.security.SecurityUtils;
import ca.zhoozhoo.loaddev.loads.service.LoadsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing shooting groups.
 * <p>
 * This controller provides endpoints for CRUD operations on shooting group data,
 * including retrieval of ballistic statistics for each group. Groups represent
 * collections of shots fired with specific load configurations. All endpoints are
 * secured with OAuth2 authentication and enforce user-based access control.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Tag(name = "Groups", description = "Operations on groups belonging to the authenticated user")
@SecurityScheme(
    name = "Oauth2Security", 
    type = SecuritySchemeType.OAUTH2,
    description = "OAuth2 authentication for Load Development API",
    flows = @OAuthFlows(
        authorizationCode = @OAuthFlow(
            authorizationUrl = "${api.security.oauth2.authorization-url}",
            tokenUrl = "${api.security.oauth2.token-url}",
            scopes = {
                @OAuthScope(name = "groups:view", description = "View groups"),
                @OAuthScope(name = "groups:edit", description = "Edit groups"),
                @OAuthScope(name = "groups:delete", description = "Delete groups")
            }
        )
    )
)
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

    @Operation(summary = "Get all groups by load id", description = "Retrieves all groups associated with a specific load for the authenticated user.")
    @SecurityRequirement(name = "Oauth2Security", scopes = "view")
    @ApiResponse(responseCode = "200", description = "Found groups", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Group.class))))
    @GetMapping("/load/{loadId}")
    @PreAuthorize("hasAuthority('groups:view')")
    public Flux<Group> getAllGroupsByLoadId(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(in = PATH, description = "Id of load", required = true) @PathVariable Long loadId) {
        return groupRepository.findAllByLoadIdAndOwnerId(loadId, userId);
    }

    @Operation(summary = "Get a group by its id", description = "Retrieves detailed information about a specific group by its identifier.")
    @SecurityRequirement(name = "Oauth2Security", scopes = "view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group retrieved", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Group.class))),
            @ApiResponse(responseCode = "404", description = "Group not found", content = @Content) })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('groups:view')")
    public Mono<ResponseEntity<Group>> getGroupById(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(in = PATH, description = "Id of group", required = true) @PathVariable Long id) {
        return groupRepository.findById(id).map(group -> ok(group)).defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Get statistics for a group", description = "Retrieves statistical information about a specific group's performance.")
    @SecurityRequirement(name = "Oauth2Security", scopes = "view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupStatisticsDto.class))),
            @ApiResponse(responseCode = "404", description = "Group not found", content = @Content) })
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAuthority('groups:view')")
    public Mono<ResponseEntity<GroupStatisticsDto>> getGroupStatistics(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(in = PATH, description = "Id of group", required = true) @PathVariable Long id) {
        return loadsService.getGroupStatistics(id, userId).map(stats -> ok(stats)).defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Create a new group", description = "Creates a new group for the authenticated user.")
    @SecurityRequirement(name = "Oauth2Security", scopes = "edit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Group created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Group.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasAuthority('groups:edit')")
    public Mono<ResponseEntity<Group>> createGroup(@Parameter(hidden = true) @CurrentUser String userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Group to create") @Valid @RequestBody Group group) {
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

    @Operation(summary = "Update an existing group", description = "Updates the details of a group by its id.")
    @SecurityRequirement(name = "Oauth2Security", scopes = "edit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Group.class))),
            @ApiResponse(responseCode = "404", description = "Group not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('groups:edit')")
    public Mono<ResponseEntity<Group>> updateGroup(@Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(in = PATH, description = "Id of group", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Group to create") @Valid @RequestBody Group group) {
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

    @Operation(summary = "Delete a group", description = "Deletes a group by its id.")
    @SecurityRequirement(name = "Oauth2Security", scopes = "delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Group deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Group not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('groups:delete')")
    public Mono<ResponseEntity<Void>> deleteGroup(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(in = PATH, description = "Id of group", required = true) @PathVariable Long id) {
        return groupRepository.findById(id)
                .flatMap(existingGroup -> groupRepository.delete(existingGroup)
                        .then(Mono.just(new ResponseEntity<Void>(NO_CONTENT)))
                        .doOnSuccess(_ -> log.info("Deleted group with id: {}", id)))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }
}
