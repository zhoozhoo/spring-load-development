package ca.zhoozhoo.loaddev.mcp.service;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import ca.zhoozhoo.loaddev.mcp.dto.GroupStatisticsDto;
import ca.zhoozhoo.loaddev.mcp.dto.LoadDto;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Service for managing load operations through service discovery.
 */
@Service
@Log4j2
public class LoadsService {

    @Autowired
    private WebClient webClient;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${service.loads.name:loads-service}")
    private String loadsServiceName;

    /**
     * Retrieves all loads from the loads service.
     * 
     * @return A Flux of LoadDto objects
     */
    public Flux<LoadDto> getLoads() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMapMany(auth -> {
                    var instances = discoveryClient.getInstances(loadsServiceName);
                    if (instances == null || instances.isEmpty()) {
                        return Flux.error(new IllegalStateException(
                                String.format("Service %s not found in discovery", loadsServiceName)));
                    }

                    return webClient
                            .get()
                            .uri(instances.get(0).getUri().toString() + "/loads")
                            .headers(h -> h.setBearerAuth(((Jwt) auth.getCredentials()).getTokenValue()))
                            .retrieve()
                            .bodyToFlux(LoadDto.class)
                            .onErrorMap(WebClientResponseException.class,
                                    e -> e.getStatusCode() == UNAUTHORIZED
                                            ? new SecurityException("Authentication failed", e)
                                            : e)
                            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                                    .filter(throwable -> !(throwable instanceof SecurityException)));
                });
    }

    /**
     * Retrieves a single load by its ID from the loads service.
     * 
     * @param id The ID of the load to retrieve
     * @return A Mono of LoadDto object
     */
    public Mono<LoadDto> getLoadById(Long id) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    var instances = discoveryClient.getInstances(loadsServiceName);
                    if (instances == null || instances.isEmpty()) {
                        return Mono.error(new IllegalStateException(
                                String.format("Service %s not found in discovery", loadsServiceName)));
                    }

                    return webClient
                            .get()
                            .uri(instances.get(0).getUri().toString() + "/loads/" + id)
                            .headers(h -> h.setBearerAuth(((Jwt) auth.getCredentials()).getTokenValue()))
                            .retrieve()
                            .bodyToMono(LoadDto.class)
                            .onErrorMap(WebClientResponseException.class,
                                    e -> e.getStatusCode() == UNAUTHORIZED
                                            ? new SecurityException("Authentication failed", e)
                                            : e.getStatusCode() == NOT_FOUND
                                                    ? new IllegalArgumentException("Load not found with ID: " + id)
                                                    : e)
                            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                                    .filter(throwable -> !(throwable instanceof SecurityException)));
                });
    }

    /**
     * Retrieves statistics for a single load by its ID from the loads service.
     * 
     * @param id The ID of the load to retrieve statistics for
     * @return A Flux of GroupStatisticsDto objects
     */
    public Flux<GroupStatisticsDto> getLoadStatistics(Long id) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMapMany(auth -> {
                    var instances = discoveryClient.getInstances(loadsServiceName);
                    if (instances == null || instances.isEmpty()) {
                        return Flux.error(new IllegalStateException(
                                String.format("Service %s not found in discovery", loadsServiceName)));
                    }

                    return webClient
                            .get()
                            .uri(instances.get(0).getUri().toString() + "/loads/" + id + "/statistics")
                            .headers(h -> h.setBearerAuth(((Jwt) auth.getCredentials()).getTokenValue()))
                            .retrieve()
                            .bodyToFlux(GroupStatisticsDto.class)
                            .onErrorMap(WebClientResponseException.class,
                                    e -> e.getStatusCode() == UNAUTHORIZED
                                            ? new SecurityException("Authentication failed", e)
                                            : e.getStatusCode() == NOT_FOUND
                                                    ? new IllegalArgumentException("Load not found with ID: " + id)
                                                    : e)
                            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                                    .filter(throwable -> !(throwable instanceof SecurityException)));
                });
    }
}
