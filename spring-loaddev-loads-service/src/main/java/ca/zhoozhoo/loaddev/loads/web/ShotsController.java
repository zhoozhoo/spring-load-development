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

import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import ca.zhoozhoo.loaddev.loads.model.Shot;
import ca.zhoozhoo.loaddev.loads.security.CurrentUser;
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

@Tag(name = "Shots", description = "Operations on shots belonging to the authenticated user")
@SecurityScheme(
    name = "Oauth2Security", 
    type = SecuritySchemeType.OAUTH2,
    flows = @OAuthFlows(
        authorizationCode = @OAuthFlow(
            authorizationUrl = "${springdoc.oauth2.authorization-url}",
            tokenUrl = "${springdoc.oauth2.token-url}",
            scopes = {
                @OAuthScope(name = "shots:view", description = "View access"),
                @OAuthScope(name = "shots:edit", description = "Edit access"),
                @OAuthScope(name = "shots:delete", description = "Delete access")
            }
        )
    )
)
@RestController
@RequestMapping("/shots")
@Log4j2
@PreAuthorize("hasRole('RELOADER')")
public class ShotsController {

    @Autowired
    private ShotRepository shotRepository;

    @Operation(summary = "Get all shots by group id", description = "Retrieves all shots associated with a specific group for the authenticated user.", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "shots:view") })
    @ApiResponse(responseCode = "200", description = "Found the shots", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Shot.class))) })
    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasAuthority('shots:view')")
    public Flux<Shot> getShotsByGroupId(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of group") @PathVariable Long groupId) {
        return shotRepository.findByGroupIdAndOwnerId(groupId, userId);
    }

    @Operation(summary = "Get a shot by its id", description = "Retrieves detailed information about a specific shot by its identifier.", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "shots:view") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shot retrieved", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Shot.class)) }),
            @ApiResponse(responseCode = "404", description = "Shot not found", content = @Content) })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('shots:view')")
    public Mono<ResponseEntity<Shot>> getShotById(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of shot") @PathVariable Long id) {
        return shotRepository.findById(id)
                .map(shot -> ok(shot))
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Create a new shot", description = "Creates a new shot for the authenticated user.", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "shots:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Shot created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Shot.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('shots:edit')")
    public Mono<ResponseEntity<Shot>> createShot(
            @Parameter(hidden = true) @CurrentUser String userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Shot to create") @Valid @RequestBody Shot shot) {
        return Mono.just(new Shot(
                null,
                userId,
                shot.groupId(),
                shot.velocity()))
                .flatMap(shotRepository::save)
                .map(savedShot -> status(CREATED).body(savedShot));
    }

    @Operation(summary = "Update an existing shot", description = "Updates the details of a shot by its id.", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "shots:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shot updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Shot.class))),
            @ApiResponse(responseCode = "404", description = "Shot not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('shots:edit')")
    public Mono<ResponseEntity<Shot>> updateShot(
            @Parameter(hidden = true) @CurrentUser String userId, 
            @Parameter(description = "Id of shot") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Shot data to update") @Valid @RequestBody Shot shot) {
        return shotRepository.findById(id)
                .flatMap(existingShot -> {
                    Shot updatedShot = new Shot(
                            existingShot.id(),
                            existingShot.ownerId(),
                            shot.groupId(),
                            shot.velocity());
                    return shotRepository.save(updatedShot);
                })
                .map(updatedShot -> ok(updatedShot))
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Delete a shot", description = "Deletes a shot by its id.", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "shots:delete") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Shot deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Shot not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('shots:delete')")
    public Mono<ResponseEntity<Void>> deleteShot(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of shot") @PathVariable Long id) {
        return shotRepository.findById(id)
                .flatMap(existingShot -> shotRepository.delete(existingShot)
                        .then(Mono.just(new ResponseEntity<Void>(NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }
}
