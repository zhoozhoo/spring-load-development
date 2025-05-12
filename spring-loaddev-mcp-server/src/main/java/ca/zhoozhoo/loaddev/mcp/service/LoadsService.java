package ca.zhoozhoo.loaddev.mcp.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

@Service
public class LoadsService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${loads.service.name:loads-service}")
    private String loadsServiceName;

    public Flux<Object> getLoads(String accessToken, String userId) {
        Optional<String> baseUrl = discoveryClient.getInstances(loadsServiceName)
                .stream()
                .findFirst()
                .map(si -> si.getUri().toString());

        if (baseUrl.isEmpty()) {
            return Flux.error(new IllegalStateException("loads-service not found in discovery"));
        }

        return webClientBuilder
                .baseUrl(baseUrl.get())
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/loads").build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("X-User-Id", userId)
                .retrieve()
                .bodyToFlux(Object.class);
    }
}
