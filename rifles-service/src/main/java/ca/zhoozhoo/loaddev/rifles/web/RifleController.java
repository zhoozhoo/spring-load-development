package ca.zhoozhoo.loaddev.rifles.web;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import ca.zhoozhoo.loaddev.rifles.dao.RifleRepository;
import ca.zhoozhoo.loaddev.rifles.model.Rifle;
import ca.zhoozhoo.loaddev.security.CurrentUser;
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
 * REST controller for managing rifle firearm specifications.
 * <p>
 * Provides CRUD operations for rifles with JSR-385 Quantity types for measurements.
 * All endpoints are OAuth2 secured with user-based access control for multi-tenant isolation.
 *
 * @author Zhubin Salehi
 */
@Tag(name = "Rifles", description = "Operations on rifles belonging to the authenticated user")
@SecurityScheme(
    name = "Oauth2Security", 
    type = SecuritySchemeType.OAUTH2,
    description = "OAuth2 authentication for Rifles API",
    flows = @OAuthFlows(
        authorizationCode = @OAuthFlow(
            authorizationUrl = "${api.security.oauth2.authorization-url}",
            tokenUrl = "${api.security.oauth2.token-url}",
            scopes = {
                @OAuthScope(name = "rifles:view", description = "View rifles"),
                @OAuthScope(name = "rifles:edit", description = "Edit rifles"),
                @OAuthScope(name = "rifles:delete", description = "Delete rifles")
            }
        )
    )
)
@RestController
@RequestMapping("/rifles")
@Log4j2
@PreAuthorize("hasRole('RELOADER')")
public class RifleController {

    @Autowired
    private RifleRepository rifleRepository;

    @Operation(summary = "Get all rifles", description = "Retrieves all rifles belonging to the authenticated user")
    @SecurityRequirement(name = "Oauth2Security", scopes = "view")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of rifles", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Rifle.class))))
    @GetMapping
    @PreAuthorize("hasAuthority('rifles:view')")
    public Flux<Rifle> getAllRifles(@Parameter(hidden = true) @CurrentUser String userId) {
        return rifleRepository.findAllByOwnerId(userId);
    }

    @Operation(summary = "Get rifle by ID", description = "Retrieves a specific rifle by its ID for the authenticated user")
    @SecurityRequirement(name = "Oauth2Security", scopes = "view")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rifle", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Rifle.class))),
            @ApiResponse(responseCode = "404", description = "Rifle not found", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('rifles:view')")
    public Mono<ResponseEntity<Rifle>> getRifleById(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Rifle ID", required = true) @PathVariable Long id) {
        return rifleRepository.findByIdAndOwnerId(id, userId)
                .doOnNext(rifle -> log.debug("Found rifle: {}", rifle))
                .map(rifle -> ok(rifle))
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Create a new rifle", description = "Creates a new rifle for the authenticated user")
    @SecurityRequirement(name = "Oauth2Security", scopes = "edit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rifle successfully created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Rifle.class))),
            @ApiResponse(responseCode = "400", description = "Invalid rifle data", content = @Content)
    })
    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('rifles:edit')")
    public Mono<ResponseEntity<Rifle>> createRifle(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Rifle to create", required = true) @Valid @RequestBody Rifle rifle) {
                if (userId == null || userId.isBlank()) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).<Rifle>build());
                }
        return Mono.just(new Rifle(
                rifle.id(),
                userId,
                rifle.name(),
                rifle.description(),
                rifle.caliber(),
                rifle.barrelLength(),
                rifle.barrelContour(),
                rifle.rifling(),
                rifle.zeroing()))
                .flatMap(rifleRepository::save)
                .doOnNext(savedRifle -> log.info("Created new rifle with id: {}", savedRifle.id()))
                .map(savedRifle -> status(CREATED).body(savedRifle));
    }

    @Operation(summary = "Update an existing rifle", description = "Updates an existing rifle by its ID")
    @SecurityRequirement(name = "Oauth2Security", scopes = "edit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rifle successfully updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Rifle.class))),
            @ApiResponse(responseCode = "400", description = "Invalid rifle data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Rifle not found", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('rifles:edit')")
    public Mono<ResponseEntity<Rifle>> updateRifle(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Rifle ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated rifle data", required = true) @Valid @RequestBody Rifle rifle) {
        return rifleRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingRifle -> rifleRepository.save(new Rifle(
                        existingRifle.id(),
                        existingRifle.ownerId(),
                        rifle.name(),
                        rifle.description(),
                        rifle.caliber(),
                        rifle.barrelLength(),
                        rifle.barrelContour(),
                        rifle.rifling(),
                        rifle.zeroing())))
                .doOnNext(updatedRifle -> log.info("Updated rifle with id: {}", updatedRifle.id()))
                .map(updatedRifle -> ok(updatedRifle))
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Delete a rifle", description = "Deletes a rifle by its ID for the authenticated user")
    @SecurityRequirement(name = "Oauth2Security", scopes = "delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rifle successfully deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Rifle not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('rifles:delete')")
    public Mono<ResponseEntity<Void>> deleteRifle(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Rifle ID", required = true) @PathVariable Long id) {
        return rifleRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingRifle -> rifleRepository.delete(existingRifle)
                        .thenReturn(ResponseEntity.noContent().<Void>build())
                        .doOnSuccess(_ -> log.info("Deleted rifle with id: {}", id)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}