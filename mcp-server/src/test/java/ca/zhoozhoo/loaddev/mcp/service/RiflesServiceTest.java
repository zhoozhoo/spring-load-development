package ca.zhoozhoo.loaddev.mcp.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.function.client.WebClient;

import io.modelcontextprotocol.spec.McpError;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RiflesServiceTest {

    private MockWebServer mockWebServer;
    private RiflesService riflesService;

    @Mock
    private DiscoveryClient discoveryClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder().build();
        riflesService = new RiflesService(webClient, discoveryClient, "rifles-service");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getRifles_ShouldReturnRifles() {
        ServiceInstance serviceInstance = mock(ServiceInstance.class);
        when(serviceInstance.getUri()).thenReturn(mockWebServer.url("/").uri());
        when(discoveryClient.getInstances("rifles-service")).thenReturn(List.of(serviceInstance));

        mockWebServer.enqueue(new MockResponse()
                .setBody("[{\"id\":1,\"name\":\"Rifle 1\"}]")
                .addHeader("Content-Type", "application/json"));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("token");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn(jwt);
        SecurityContext securityContext = new SecurityContextImpl(authentication);

        StepVerifier.create(riflesService.getRifles()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .expectNextMatches(rifle -> rifle.id() == 1L && "Rifle 1".equals(rifle.name()))
                .verifyComplete();
    }

    @Test
    void getRifles_ShouldReturnError_WhenServiceNotFound() {
        when(discoveryClient.getInstances("rifles-service")).thenReturn(Collections.emptyList());

        StepVerifier.create(riflesService.getRifles())
                .expectError(McpError.class)
                .verify();
    }

    @Test
    void getRifleById_ShouldReturnRifle() {
        ServiceInstance serviceInstance = mock(ServiceInstance.class);
        when(serviceInstance.getUri()).thenReturn(mockWebServer.url("/").uri());
        when(discoveryClient.getInstances("rifles-service")).thenReturn(List.of(serviceInstance));

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"id\":1,\"name\":\"Rifle 1\"}")
                .addHeader("Content-Type", "application/json"));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("token");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn(jwt);
        SecurityContext securityContext = new SecurityContextImpl(authentication);

        StepVerifier.create(riflesService.getRifleById(1L)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .expectNextMatches(rifle -> rifle.id() == 1L && "Rifle 1".equals(rifle.name()))
                .verifyComplete();
    }
}
