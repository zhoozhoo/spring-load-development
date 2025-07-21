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

import ca.zhoozhoo.loaddev.components.dao.CaseRepository;
import ca.zhoozhoo.loaddev.components.model.Case;
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

@Tag(name = "Cases", description = "Operations on cases belonging to the authenticated user")
@SecurityScheme(name = "Oauth2Security", type = OAUTH2, flows = @OAuthFlows(authorizationCode = @OAuthFlow(authorizationUrl = "${springdoc.oauth2.authorization-url}", tokenUrl = "${springdoc.oauth2.token-url}", scopes = {
        @OAuthScope(name = "components:view", description = "View access"),
        @OAuthScope(name = "components:edit", description = "Edit access"),
        @OAuthScope(name = "components:delete", description = "Delete access")
})))
@RestController
@RequestMapping("/cases")
@Log4j2
@PreAuthorize("hasRole('RELOADER')")
public class CaseController {

    @Autowired
    private CaseRepository caseRepository;

    @Operation(summary = "Get all cases", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponse(responseCode = "200", description = "Found all cases", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Case.class))) })
    @GetMapping
    @PreAuthorize("hasAuthority('components:view')")
    public Flux<Case> getAllCases(@Parameter(hidden = true) @CurrentUser String userId) {
        return caseRepository.findAllByOwnerId(userId);
    }

    @Operation(summary = "Get a case by its id", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:view") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Case found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Case.class)) }),
            @ApiResponse(responseCode = "404", description = "Case not found", content = @Content) })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('components:view')")
    public Mono<ResponseEntity<Case>> getCase(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of case") @PathVariable Long id) {
        return caseRepository.findByIdAndOwnerId(id, userId)
                .map(casing -> {
                    log.debug("Found case: {}", casing);
                    return ok(casing);
                })
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Create a new case", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Case created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Case.class)) }) })
    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('components:edit')")
    public Mono<ResponseEntity<Case>> createCase(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Valid @RequestBody Case casing) {
        return just(casing)
                .map(c -> new Case(
                        null,
                        userId,
                        c.manufacturer(),
                        c.caliber(),
                        c.primerSize(),
                        c.cost(),
                        c.currency(),
                        c.quantityPerBox()))
                .flatMap(caseRepository::save)
                .map(savedCase -> {
                    log.info("Created new case with id: {}", savedCase.id());
                    return status(CREATED).body(savedCase);
                });
    }

    @Operation(summary = "Update an existing case", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Case updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Case.class)) }),
            @ApiResponse(responseCode = "404", description = "Case not found", content = @Content) })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('components:edit')")
    public Mono<ResponseEntity<Case>> updateCase(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of case") @PathVariable Long id,
            @Valid @RequestBody Case casing) {
        return caseRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingCase -> {
                    Case updatedCase = new Case(
                            existingCase.id(),
                            existingCase.ownerId(),
                            casing.manufacturer(),
                            casing.caliber(),
                            casing.primerSize(),
                            casing.cost(),
                            casing.currency(),
                            casing.quantityPerBox());
                    return caseRepository.save(updatedCase);
                })
                .map(updatedCase -> {
                    log.info("Updated case with id: {}", updatedCase.id());
                    return ok(updatedCase);
                })
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Delete a case", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "components:delete") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Case deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Case not found", content = @Content) })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('components:delete')")
    public Mono<ResponseEntity<Void>> deleteCase(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of case") @PathVariable Long id) {
        return caseRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingCase -> caseRepository.delete(existingCase)
                        .then(just(new ResponseEntity<Void>(NO_CONTENT)))
                        .doOnSuccess(result -> log.info("Deleted case with id: {}", id)))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }
}
