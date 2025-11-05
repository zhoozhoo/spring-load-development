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

import ca.zhoozhoo.loaddev.loads.dao.ShotJsr385Repository;
import ca.zhoozhoo.loaddev.loads.model.ShotJsr385;
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

/**
 * REST controller for managing individual shot data using JSR-385 Quantity API.
 * <p>
 * This controller provides endpoints for CRUD operations on shot velocity measurements
 * within shooting groups. Each shot represents a single round fired and recorded during
 * load testing. Velocity measurements use type-safe units via javax.measure. All
 * endpoints are secured with OAuth2 authentication and enforce user-based access control.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Tag(name = "Shots JSR-385", description = "Operations on shots with JSR-385 unit support belonging to the authenticated user")
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
@RequestMapping("/jsr385/shots")
@Log4j2
@PreAuthorize("hasRole('RELOADER')")
public class ShotsJsr385Controller {

    @Autowired
    private ShotJsr385Repository shotRepository;

    @Operation(summary = "Get all JSR-385 shots by group id", description = "Retrieves all shots associated with a specific group for the authenticated user.", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "shots:view") })
    @ApiResponse(responseCode = "200", description = "Found the shots", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ShotJsr385.class))) })
    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasAuthority('shots:view')")
    public Flux<ShotJsr385> getShotsByGroupId(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of group") @PathVariable Long groupId) {
        return shotRepository.findByGroupIdAndOwnerId(groupId, userId);
    }

    @Operation(summary = "Get a JSR-385 shot by its id", description = "Retrieves detailed information about a specific shot by its identifier.", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "shots:view") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shot retrieved", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ShotJsr385.class)) }),
            @ApiResponse(responseCode = "404", description = "Shot not found", content = @Content) })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('shots:view')")
    public Mono<ResponseEntity<ShotJsr385>> getShotById(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of shot") @PathVariable Long id) {
        return shotRepository.findByIdAndOwnerId(id, userId)
                .map(shot -> ok(shot))
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Create a new JSR-385 shot", description = "Creates a new shot for the authenticated user.", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "shots:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Shot created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShotJsr385.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('shots:edit')")
    public Mono<ResponseEntity<ShotJsr385>> createShot(
            @Parameter(hidden = true) @CurrentUser String userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Shot to create") @Valid @RequestBody ShotJsr385 shot) {
        return Mono.just(new ShotJsr385(
                null,
                userId,
                shot.groupId(),
                shot.velocity()))
                .flatMap(shotRepository::save)
                .map(savedShot -> {
                    log.info("Created new JSR-385 shot with id: {}", savedShot.id());
                    return status(CREATED).body(savedShot);
                });
    }

    @Operation(summary = "Update an existing JSR-385 shot", description = "Updates the details of a shot by its id.", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "shots:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shot updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShotJsr385.class))),
            @ApiResponse(responseCode = "404", description = "Shot not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('shots:edit')")
    public Mono<ResponseEntity<ShotJsr385>> updateShot(
            @Parameter(hidden = true) @CurrentUser String userId, 
            @Parameter(description = "Id of shot") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Shot data to update") @Valid @RequestBody ShotJsr385 shot) {
        return shotRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingShot -> {
                    ShotJsr385 updatedShot = new ShotJsr385(
                            existingShot.id(),
                            existingShot.ownerId(),
                            shot.groupId(),
                            shot.velocity());
                    return shotRepository.save(updatedShot);
                })
                .map(updatedShot -> {
                    log.info("Updated JSR-385 shot with id: {}", updatedShot.id());
                    return ok(updatedShot);
                })
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Delete a JSR-385 shot", description = "Deletes a shot by its id.", security = {
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
        return shotRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingShot -> shotRepository.delete(existingShot)
                        .then(Mono.just(new ResponseEntity<Void>(NO_CONTENT)))
                        .doOnSuccess(_ -> log.info("Deleted JSR-385 shot with id: {}", id)))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }
}
