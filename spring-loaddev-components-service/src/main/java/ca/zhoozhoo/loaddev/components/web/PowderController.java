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

import ca.zhoozhoo.loaddev.components.dao.PowderRepository;
import ca.zhoozhoo.loaddev.components.model.Powder;
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

@Tag(name = "Powders", description = "Operations on powders belonging to the authenticated user")
@SecurityScheme(name = "Oauth2Security", type = OAUTH2, flows = @OAuthFlows(authorizationCode = @OAuthFlow(authorizationUrl = "${springdoc.oauth2.authorization-url}", tokenUrl = "${springdoc.oauth2.token-url}", scopes = {
        @OAuthScope(name = "components:view", description = "View access"),
        @OAuthScope(name = "components:edit", description = "Edit access"),
        @OAuthScope(name = "components:delete", description = "Delete access")
})))
@RestController
@RequestMapping("/powders")
@Log4j2
@PreAuthorize("hasRole('RELOADER')")
public class PowderController {

    @Autowired
    private PowderRepository powderRepository;

    @Operation(summary = "Get all powders", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponse(responseCode = "200", description = "Found all powders", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Powder.class))) })
    @GetMapping
    @PreAuthorize("hasAuthority('components:view')")
    public Flux<Powder> getAllPowders(@Parameter(hidden = true) @CurrentUser String userId) {
        return powderRepository.findAllByOwnerId(userId);
    }

    @Operation(summary = "Get a powder by its id", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Powder found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Powder.class)) }),
            @ApiResponse(responseCode = "404", description = "Powder not found", content = @Content) })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('components:view')")
    public Mono<ResponseEntity<Powder>> getPowder(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of powder") @PathVariable Long id) {
        return powderRepository.findByIdAndOwnerId(id, userId)
                .map(powder -> {
                    log.debug("Found powder: {}", powder);
                    return ok(powder);
                })
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Create a new powder", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Powder created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Powder.class)) }) })
    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('components:edit')")
    public Mono<ResponseEntity<Powder>> createPowder(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Valid @RequestBody Powder powder) {
        return just(powder)
                .map(p -> new Powder(
                        null,
                        userId,
                        p.manufacturer(),
                        p.type(),
                        p.measurementUnits(),
                        p.cost(),
                        p.currency(),
                        p.weightPerContainer()))
                .flatMap(powderRepository::save)
                .map(savedPowder -> {
                    log.info("Created new powder with id: {}", savedPowder.id());
                    return status(CREATED).body(savedPowder);
                });
    }

    @Operation(summary = "Update an existing powder", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Powder updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Powder.class)) }),
            @ApiResponse(responseCode = "404", description = "Powder not found", content = @Content) })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('components:edit')")
    public Mono<ResponseEntity<Powder>> updatePowder(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of powder") @PathVariable Long id,
            @Valid @RequestBody Powder powder) {
        return powderRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingPowder -> {
                    Powder updatedPowder = new Powder(
                            existingPowder.id(),
                            existingPowder.ownerId(),
                            powder.manufacturer(),
                            powder.type(),
                            powder.measurementUnits(),
                            powder.cost(),
                            powder.currency(),
                            powder.weightPerContainer());
                    return powderRepository.save(updatedPowder);
                })
                .map(updatedPowder -> {
                    log.info("Updated powder with id: {}", updatedPowder.id());
                    return ok(updatedPowder);
                })
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Delete a powder", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:delete") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Powder deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Powder not found", content = @Content) })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('components:delete')")
    public Mono<ResponseEntity<Void>> deletePowder(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of powder") @PathVariable Long id) {
        return powderRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingPowder -> powderRepository.delete(existingPowder)
                        .then(just(new ResponseEntity<Void>(NO_CONTENT)))
                        .doOnSuccess(result -> log.info("Deleted powder with id: {}", id)))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }
}
