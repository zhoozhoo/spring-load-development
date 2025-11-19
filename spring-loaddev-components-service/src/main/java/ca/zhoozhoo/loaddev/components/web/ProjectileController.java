package ca.zhoozhoo.loaddev.components.web;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.OAUTH2;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ca.zhoozhoo.loaddev.components.dao.ProjectileRepository;
import ca.zhoozhoo.loaddev.components.model.Projectile;
import ca.zhoozhoo.loaddev.components.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * REST controller for projectile components with JSR-385/JSR-354.
 * <p>
 * OAuth2-secured CRUD operations with full-text search and multi-tenant isolation.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Tag(name = "Projectiles", description = "Operations on projectiles belonging to the authenticated user")
@SecurityScheme(name = "Oauth2Security", type = OAUTH2, flows = @OAuthFlows(authorizationCode = @OAuthFlow(authorizationUrl = "${springdoc.oauth2.authorization-url}", tokenUrl = "${springdoc.oauth2.token-url}", scopes = {
        @OAuthScope(name = "components:view", description = "View access"),
        @OAuthScope(name = "components:edit", description = "Edit access"),
        @OAuthScope(name = "components:delete", description = "Delete access")
})))
@RestController
@RequestMapping("/projectiles")
@Log4j2
@PreAuthorize("hasRole('RELOADER')")
public class ProjectileController {

    @Autowired
    private ProjectileRepository projectileRepository;

    @Operation(summary = "Get all projectiles", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponse(responseCode = "200", description = "Found all projectiles", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Projectile.class))) })
    @GetMapping
    @PreAuthorize("hasAuthority('components:view')")
    public Flux<Projectile> getAllProjectiles(@Parameter(hidden = true) @CurrentUser String userId) {
        return projectileRepository.findAllByOwnerId(userId);
    }

    @Operation(summary = "Full-text search projectiles", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponse(responseCode = "200", description = "Search results", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Projectile.class))) })
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('components:view')")
    public Flux<Projectile> searchProjectiles(@Parameter(hidden = true) @CurrentUser String userId,
                                               @Parameter(description = "Full text search query") @RequestParam("query") String query) {
        return projectileRepository.searchByOwnerIdAndQuery(userId, query);
    }

    @Operation(summary = "Get a projectile by its id", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projectile found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Projectile.class)) }),
            @ApiResponse(responseCode = "404", description = "Projectile not found", content = @Content) })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('components:view')")
    public Mono<ResponseEntity<Projectile>> getProjectile(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of projectile") @PathVariable Long id) {
        return projectileRepository.findByIdAndOwnerId(id, userId)
                .doOnNext(projectile -> log.debug("Found projectile: {}", projectile))
                .map(projectile -> ok(projectile))
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Create a new projectile", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Projectile created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Projectile.class)) }) })
    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('components:edit')")
    public Mono<ResponseEntity<Projectile>> createProjectile(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Valid @RequestBody Projectile projectile) {
        return just(projectile)
                .map(p -> new Projectile(
                        null,
                        userId,
                        p.manufacturer(),
                        p.weight(),
                        p.type(),
                        p.cost(),
                        p.quantityPerBox()))
                .flatMap(projectileRepository::save)
                .doOnNext(savedProjectile -> log.debug("Created new projectile with id: {}", savedProjectile.id()))
                .map(savedProjectile -> status(CREATED).body(savedProjectile));
    }

    @Operation(summary = "Update an existing projectile", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projectile updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Projectile.class)) }),
            @ApiResponse(responseCode = "404", description = "Projectile not found", content = @Content) })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('components:edit')")
    public Mono<ResponseEntity<Projectile>> updateProjectile(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of projectile") @PathVariable Long id,
            @Valid @RequestBody Projectile projectile) {
        return projectileRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingProjectile -> projectileRepository.save(new Projectile(
                        existingProjectile.id(),
                        existingProjectile.ownerId(),
                        projectile.manufacturer(),
                        projectile.weight(),
                        projectile.type(),
                        projectile.cost(),
                        projectile.quantityPerBox())))
                .doOnNext(updatedProjectile -> log.debug("Updated projectile with id: {}", updatedProjectile.id()))
                .map(updatedProjectile -> ok(updatedProjectile))
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Delete a projectile", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:delete") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Projectile deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Projectile not found", content = @Content) })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('components:delete')")
    public Mono<ResponseEntity<Void>> deleteProjectile(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of projectile") @PathVariable Long id) {
        return projectileRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingProjectile -> projectileRepository.delete(existingProjectile)
                        .thenReturn(ResponseEntity.noContent().<Void>build())
                        .doOnSuccess(_ -> log.debug("Deleted projectile with id: {}", id)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
