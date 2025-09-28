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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ca.zhoozhoo.loaddev.components.dao.PrimerRepository;
import ca.zhoozhoo.loaddev.components.model.Primer;
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

@Tag(name = "Primers", description = "Operations on primers belonging to the authenticated user")
@SecurityScheme(name = "Oauth2Security", type = OAUTH2, flows = @OAuthFlows(authorizationCode = @OAuthFlow(authorizationUrl = "${springdoc.oauth2.authorization-url}", tokenUrl = "${springdoc.oauth2.token-url}", scopes = {
        @OAuthScope(name = "components:view", description = "View access"),
        @OAuthScope(name = "components:edit", description = "Edit access"),
        @OAuthScope(name = "components:delete", description = "Delete access")
})))
@RestController
@RequestMapping("/primers")
@Log4j2
@PreAuthorize("hasRole('RELOADER')")
public class PrimerController {

    @Autowired
    private PrimerRepository primerRepository;

    @Operation(summary = "Get all primers", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponse(responseCode = "200", description = "Found all primers", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Primer.class))) })
    @GetMapping
    @PreAuthorize("hasAuthority('components:view')")
    public Flux<Primer> getAllPrimers(@Parameter(hidden = true) @CurrentUser String userId) {
        return primerRepository.findAllByOwnerId(userId);
    }

    @Operation(summary = "Full-text search primers", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponse(responseCode = "200", description = "Search results", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Primer.class))) })
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('components:view')")
        public Flux<Primer> searchPrimers(@Parameter(hidden = true) @CurrentUser String userId,
                                                                          @Parameter(description = "Full text search query") @RequestParam("query") String query) {
                return primerRepository.searchByOwnerIdAndQuery(userId, query);
        }

    @Operation(summary = "Get a primer by its id", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Primer found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Primer.class)) }),
            @ApiResponse(responseCode = "404", description = "Primer not found", content = @Content) })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('components:view')")
    public Mono<ResponseEntity<Primer>> getPrimer(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of primer") @PathVariable Long id) {
        return primerRepository.findByIdAndOwnerId(id, userId)
                .map(primer -> {
                    log.debug("Found primer: {}", primer);
                    return ok(primer);
                })
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Create a new primer", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Primer created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Primer.class)) }) })
    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('components:edit')")
    public Mono<ResponseEntity<Primer>> createPrimer(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Valid @RequestBody Primer primer) {
        return just(primer)
                .map(p -> new Primer(
                        null,
                        userId,
                        p.manufacturer(),
                        p.type(),
                        p.primerSize(),
                        p.cost(),
                        p.currency(),
                        p.quantityPerBox()))
                .flatMap(primerRepository::save)
                .map(savedPrimer -> {
                    log.info("Created new primer with id: {}", savedPrimer.id());
                    return status(CREATED).body(savedPrimer);
                });
    }

    @Operation(summary = "Update an existing primer", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Primer updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Primer.class)) }),
            @ApiResponse(responseCode = "404", description = "Primer not found", content = @Content) })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('components:edit')")
    public Mono<ResponseEntity<Primer>> updatePrimer(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of primer") @PathVariable Long id,
            @Valid @RequestBody Primer primer) {
        return primerRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingPrimer -> {
                    Primer updatedPrimer = new Primer(
                            existingPrimer.id(),
                            existingPrimer.ownerId(),
                            primer.manufacturer(),
                            primer.type(),
                            primer.primerSize(),
                            primer.cost(),
                            primer.currency(),
                            primer.quantityPerBox());
                    return primerRepository.save(updatedPrimer);
                })
                .map(updatedPrimer -> {
                    log.info("Updated primer with id: {}", updatedPrimer.id());
                    return ok(updatedPrimer);
                })
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Delete a primer", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:delete") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Primer deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Primer not found", content = @Content) })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('components:delete')")
    public Mono<ResponseEntity<Void>> deletePrimer(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of primer") @PathVariable Long id) {
        return primerRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingPrimer -> primerRepository.delete(existingPrimer)
                        .then(just(new ResponseEntity<Void>(NO_CONTENT)))
                        .doOnSuccess(_ -> log.info("Deleted primer with id: {}", id)))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }
}
