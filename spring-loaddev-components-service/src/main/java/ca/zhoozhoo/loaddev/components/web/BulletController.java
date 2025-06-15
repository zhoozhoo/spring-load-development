package ca.zhoozhoo.loaddev.components.web;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.OAUTH2;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ca.zhoozhoo.loaddev.components.dao.BulletRepository;
import ca.zhoozhoo.loaddev.components.model.Bullet;
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

@Tag(name = "Bullets", description = "Operations on bullets belonging to the authenticated user")
@SecurityScheme(name = "Oauth2Security", type = OAUTH2, flows = @OAuthFlows(authorizationCode = @OAuthFlow(authorizationUrl = "${springdoc.oauth2.authorization-url}", tokenUrl = "${springdoc.oauth2.token-url}", scopes = {
        @OAuthScope(name = "components:view", description = "View access"),
        @OAuthScope(name = "components:edit", description = "Edit access"),
        @OAuthScope(name = "components:delete", description = "Delete access")
})))
@RestController
@RequestMapping("/bullets")
@Log4j2
@PreAuthorize("hasRole('RELOADER')")
public class BulletController {

    @Autowired
    private BulletRepository bulletRepository;

    @Operation(summary = "Get all bullets", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponse(responseCode = "200", description = "Found all bullets", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Bullet.class))) })
    @GetMapping
    @PreAuthorize("hasAuthority('components:view')")
    public Flux<Bullet> getAllBullets(@Parameter(hidden = true) @CurrentUser String userId) {
        return bulletRepository.findAllByOwnerId(userId);
    }

    @Operation(summary = "Get a bullet by its id", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bullet found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Bullet.class)) }),
            @ApiResponse(responseCode = "404", description = "Bullet not found", content = @Content) })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('components:view')")
    public Mono<ResponseEntity<Bullet>> getBullet(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of bullet") @PathVariable Long id) {
        return bulletRepository.findByIdAndOwnerId(id, userId)
                .map(bullet -> {
                    log.debug("Found bullet: {}", bullet);
                    return ok(bullet);
                })
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Create a new bullet", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bullet created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Bullet.class)) }) })
    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('components:edit')")
    public Mono<ResponseEntity<Bullet>> createBullet(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Valid @RequestBody Bullet bullet) {
        return just(bullet)
                .map(b -> new Bullet(
                        null,
                        userId,
                        b.manufacturer(),
                        b.weight(),
                        b.type(),
                        b.measurementUnits(),
                        b.cost(),
                        b.currency(),
                        b.quantityPerBox()))
                .flatMap(bulletRepository::save)
                .map(savedBullet -> {
                    log.info("Created new bullet with id: {}", savedBullet.id());
                    return status(CREATED).body(savedBullet);
                });
    }

    @Operation(summary = "Update an existing bullet", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bullet updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Bullet.class)) }),
            @ApiResponse(responseCode = "404", description = "Bullet not found", content = @Content) })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('components:edit')")
    public Mono<ResponseEntity<Bullet>> updateBullet(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of bullet") @PathVariable Long id,
            @Valid @RequestBody Bullet bullet) {
        return bulletRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingBullet -> {
                    Bullet updatedBullet = new Bullet(
                            existingBullet.id(),
                            existingBullet.ownerId(),
                            bullet.manufacturer(),
                            bullet.weight(),
                            bullet.type(),
                            bullet.measurementUnits(),
                            bullet.cost(),
                            bullet.currency(),
                            bullet.quantityPerBox());
                    return bulletRepository.save(updatedBullet);
                })
                .map(updatedBullet -> {
                    log.info("Updated bullet with id: {}", updatedBullet.id());
                    return ok(updatedBullet);
                })
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Delete a bullet", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:delete") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bullet deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bullet not found", content = @Content) })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('components:delete')")
    public Mono<ResponseEntity<Void>> deleteBullet(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of bullet") @PathVariable Long id) {
        return bulletRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingBullet -> bulletRepository.delete(existingBullet)
                        .then(just(new ResponseEntity<Void>(NO_CONTENT)))
                        .doOnSuccess(result -> log.info("Deleted bullet with id: {}", id)))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }
}
