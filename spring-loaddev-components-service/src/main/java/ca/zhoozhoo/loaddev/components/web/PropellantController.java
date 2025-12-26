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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ca.zhoozhoo.loaddev.components.dao.PropellantRepository;
import ca.zhoozhoo.loaddev.components.model.Propellant;
import ca.zhoozhoo.loaddev.security.CurrentUser;
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
 * REST controller for propellant components with JSR-385/JSR-354.
 * <p>
 * OAuth2-secured CRUD operations with full-text search and multi-tenant isolation.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Tag(name = "Propellants", description = "Operations on propellants belonging to the authenticated user")
@SecurityScheme(name = "Oauth2Security", type = OAUTH2, flows = @OAuthFlows(authorizationCode = @OAuthFlow(authorizationUrl = "${springdoc.oauth2.authorization-url}", tokenUrl = "${springdoc.oauth2.token-url}", scopes = {
        @OAuthScope(name = "components:view", description = "View access"),
        @OAuthScope(name = "components:edit", description = "Edit access"),
        @OAuthScope(name = "components:delete", description = "Delete access")
})))
@RestController
@RequestMapping("/propellants")
@Log4j2
@PreAuthorize("hasRole('RELOADER')")
public class PropellantController {

    @Autowired
    private PropellantRepository propellantRepository;

    @Operation(summary = "Get all propellants", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponse(responseCode = "200", description = "Found all propellants", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Propellant.class))) })
    @GetMapping
    @PreAuthorize("hasAuthority('components:view')")
    public Flux<Propellant> getAllPropellants(@Parameter(hidden = true) @CurrentUser String userId) {
        return propellantRepository.findAllByOwnerId(userId);
    }

    @Operation(summary = "Full-text search propellants", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponse(responseCode = "200", description = "Search results", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Propellant.class))) })
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('components:view')")
    public Flux<Propellant> searchPropellants(@Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Full text search query") @RequestParam("query") String query) {
        return propellantRepository.searchByOwnerIdAndQuery(userId, query);
    }

    @Operation(summary = "Get a propellant by its id", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propellant found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Propellant.class)) }),
            @ApiResponse(responseCode = "404", description = "Propellant not found", content = @Content) })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('components:view')")
    public Mono<ResponseEntity<Propellant>> getPropellant(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of propellant") @PathVariable Long id) {
        return propellantRepository.findByIdAndOwnerId(id, userId)
                .doOnNext(propellant -> log.debug("Found propellant: {}", propellant))
                .map(propellant -> ok(propellant))
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Create a new propellant", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Propellant created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Propellant.class)) }) })
    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('components:edit')")
    public Mono<ResponseEntity<Propellant>> createPropellant(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Valid @RequestBody Propellant propellant) {
        return just(propellant)
                .map(p -> new Propellant(
                        null,
                        userId,
                        p.manufacturer(),
                        p.type(),
                        p.cost(),
                        p.weightPerContainer()))
                .flatMap(propellantRepository::save)
                .doOnNext(savedPropellant -> log.debug("Created new propellant with id: {}", savedPropellant.id()))
                .map(savedPropellant -> status(CREATED).body(savedPropellant));
    }

    @Operation(summary = "Update an existing propellant", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propellant updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Propellant.class)) }),
            @ApiResponse(responseCode = "404", description = "Propellant not found", content = @Content) })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('components:edit')")
    public Mono<ResponseEntity<Propellant>> updatePropellant(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of propellant") @PathVariable Long id,
            @Valid @RequestBody Propellant propellant) {
        return propellantRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingPropellant -> 
                     propellantRepository.save(new Propellant(
                            existingPropellant.id(),
                            existingPropellant.ownerId(),
                            propellant.manufacturer(),
                            propellant.type(),
                            propellant.cost(),
                            propellant.weightPerContainer()))                )
                .doOnNext(updatedPropellant -> log.debug("Updated propellant with id: {}", updatedPropellant.id()))
                .map(updatedPropellant -> ok(updatedPropellant))
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Delete a propellant", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:delete") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Propellant deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Propellant not found", content = @Content) })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('components:delete')")
    public Mono<ResponseEntity<Void>> deletePropellant(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of propellant") @PathVariable Long id) {
        return propellantRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingPropellant -> propellantRepository.delete(existingPropellant)
                        .thenReturn(ResponseEntity.noContent().<Void>build())
                        .doOnSuccess(_ -> log.debug("Deleted propellant with id: {}", id)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
