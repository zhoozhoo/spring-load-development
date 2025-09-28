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

import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.dto.GroupStatisticsDto;
import ca.zhoozhoo.loaddev.loads.model.Load;
import ca.zhoozhoo.loaddev.loads.security.CurrentUser;
import ca.zhoozhoo.loaddev.loads.service.LoadsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Loads", description = "Operations on loads belonging to the authenticated user")
@SecurityScheme(
    name = "Oauth2Security", 
    type = SecuritySchemeType.OAUTH2,
    flows = @OAuthFlows(
        authorizationCode = @OAuthFlow(
            authorizationUrl = "${springdoc.oauth2.authorization-url}",
            tokenUrl = "${springdoc.oauth2.token-url}",
            scopes = {
                @OAuthScope(name = "loads:view", description = "View access"),
                @OAuthScope(name = "loads:edit", description = "Edit access"),
                @OAuthScope(name = "loads:delete", description = "Delete access")
            }
        )
    )
)
@RestController
@RequestMapping("/loads")
@Log4j2
@PreAuthorize("hasRole('RELOADER')")
public class LoadsController {

    @Autowired
    private LoadRepository loadRepository;

    @Autowired
    private LoadsService loadsService;

    @Operation(summary = "Get all loads", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "loads:view") })
    @ApiResponse(responseCode = "200", description = "Found the load", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Load.class))) })
    @GetMapping
    @PreAuthorize("hasAuthority('loads:view')")
    public Flux<Load> getAllLoads(@Parameter(hidden = true) @CurrentUser String userId) {
        return loadRepository.findAllByOwnerId(userId);
    }

    @Operation(summary = "Get a load by its id", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "loads:view") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found retrieved", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Load.class)) }),
            @ApiResponse(responseCode = "404", description = "Load not found", content = @Content) })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('loads:view')")
    public Mono<ResponseEntity<Load>> getLoadById(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of load") @PathVariable Long id) {
        return loadRepository.findByIdAndOwnerId(id, userId)
                .map(load -> {
                    log.debug("Found load: {}", load);
                    return ok(load);
                })
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Get group statistics for a load", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "loads:view") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GroupStatisticsDto.class)))),
            @ApiResponse(responseCode = "404", description = "Load not found", content = @Content)
    })
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAuthority('loads:view')")
    public Flux<GroupStatisticsDto> getLoadStatistics(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of load") @PathVariable Long id) {
        return loadsService.getGroupStatisticsForLoad(id, userId);
    }

    @Operation(summary = "Create a new load", security = { @SecurityRequirement(name = "Oauth2Security", scopes = "loads:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Load created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Load.class)))
    })
    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('loads:edit')")
    public Mono<ResponseEntity<Load>> createLoad(
            @Parameter(hidden = true) @CurrentUser String userId, 
            @Valid @RequestBody Load load) {
        return Mono.just(new Load(
                null,
                userId,
                load.name(),
                load.description(),
                load.measurementUnits(),
                load.powderManufacturer(),
                load.powderType(),
                load.bulletManufacturer(),
                load.bulletType(),
                load.bulletWeight(),
                load.primerManufacturer(),
                load.primerType(),
                load.distanceFromLands(),
                load.caseOverallLength(),
                load.neckTension(),
                load.rifleId()))
                .flatMap(loadRepository::save)
                .map(savedLoad -> {
                    log.info("Created new load with id: {}", savedLoad.id());
                    return status(CREATED).body(savedLoad);
                });
    }

    @Operation(summary = "Update an existing load", security = {
            @SecurityRequirement(name = "Oauth2Security", scopes = "loads:edit") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Load updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Load.class))),
            @ApiResponse(responseCode = "404", description = "Load not found", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('loads:edit')")
    public Mono<ResponseEntity<Load>> updateLoad(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of load") @PathVariable Long id,
            @Valid @RequestBody Load load) {
        return loadRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingLoad -> {
                    Load updatedLoad = new Load(
                            existingLoad.id(),
                            existingLoad.ownerId(),
                            load.name(),
                            load.description(),
                            load.measurementUnits(),
                            load.powderManufacturer(),
                            load.powderType(),
                            load.bulletManufacturer(),
                            load.bulletType(),
                            load.bulletWeight(),
                            load.primerManufacturer(),
                            load.primerType(),
                            load.distanceFromLands(),
                            load.caseOverallLength(),
                            load.neckTension(),
                            load.rifleId());
                    return loadRepository.save(updatedLoad);
                })
                .map(updatedLoad -> {
                    log.info("Updated load with id: {}", updatedLoad.id());
                    return ok(updatedLoad);
                })
                .defaultIfEmpty(notFound().build());
    }

    @Operation(summary = "Delete a load", security = { @SecurityRequirement(name = "Oauth2Security", scopes = "loads:delete") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Load deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Load not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('loads:delete')")
    public Mono<ResponseEntity<Void>> deleteLoad(
            @Parameter(hidden = true) @CurrentUser String userId,
            @Parameter(description = "Id of load") @PathVariable Long id) {
        return loadRepository.findByIdAndOwnerId(id, userId)
                .flatMap(existingLoad -> loadRepository.delete(existingLoad)
                        .then(Mono.just(new ResponseEntity<Void>(NO_CONTENT)))
                        .doOnSuccess(_ -> log.info("Deleted load with id: {}", id)))
                .defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
    }
}
