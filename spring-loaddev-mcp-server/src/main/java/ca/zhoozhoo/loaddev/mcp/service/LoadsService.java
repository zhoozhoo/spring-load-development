package ca.zhoozhoo.loaddev.mcp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.zhoozhoo.loaddev.mcp.dto.LoadDto;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Service
@Log4j2
public class LoadsService {

    @Autowired
    private WebClient webClient;

    @Autowired
    private DiscoveryClient discoveryClient;

    // Name of the loads service as registered in Eureka
    private final String loadsServiceName = "loads-service";

    public Flux<LoadDto> getAllLoads() {
        var instances = discoveryClient.getInstances(loadsServiceName);
        if (instances == null || instances.isEmpty()) {
            return Flux.error(new IllegalStateException("loads-service not found in discovery"));
        }
        String baseUrl = instances.get(0).getUri().toString();
        log.info("Calling loads service at {}", baseUrl);
        // Use full URI to ensure WebClient sends the request correctly
        return webClient
            .get()
            .uri(baseUrl + "/loads")
            .retrieve()
            .bodyToFlux(LoadDto.class)
            .doOnNext(load -> log.info("Received load: {}", load))
            .doOnError(error -> log.error("Error retrieving loads: {}", error.getMessage()));
    }
}
