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
class LoadsServiceTest {

    private MockWebServer mockWebServer;
    private LoadsService loadsService;

    @Mock
    private DiscoveryClient discoveryClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder().build();
        loadsService = new LoadsService(webClient, discoveryClient, "loads-service");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getLoads_ShouldReturnLoads() {
        ServiceInstance serviceInstance = mock(ServiceInstance.class);
        when(serviceInstance.getUri()).thenReturn(mockWebServer.url("/").uri());
        when(discoveryClient.getInstances("loads-service")).thenReturn(List.of(serviceInstance));

        mockWebServer.enqueue(new MockResponse()
                .setBody("[{\"id\":1,\"name\":\"Load 1\"}]")
                .addHeader("Content-Type", "application/json"));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("token");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn(jwt);
        SecurityContext securityContext = new SecurityContextImpl(authentication);

        StepVerifier.create(loadsService.getLoads()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .expectNextMatches(load -> load.id() == 1L && "Load 1".equals(load.name()))
                .verifyComplete();
    }

    @Test
    void getLoads_ShouldReturnError_WhenServiceNotFound() {
        when(discoveryClient.getInstances("loads-service")).thenReturn(Collections.emptyList());

        StepVerifier.create(loadsService.getLoads())
                .expectError(McpError.class)
                .verify();
    }

    @Test
    void getLoadById_ShouldReturnLoad() {
        ServiceInstance serviceInstance = mock(ServiceInstance.class);
        when(serviceInstance.getUri()).thenReturn(mockWebServer.url("/").uri());
        when(discoveryClient.getInstances("loads-service")).thenReturn(List.of(serviceInstance));

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"id\":1,\"name\":\"Load 1\"}")
                .addHeader("Content-Type", "application/json"));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("token");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn(jwt);
        SecurityContext securityContext = new SecurityContextImpl(authentication);

        StepVerifier.create(loadsService.getLoadById(1L)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .expectNextMatches(load -> load.id() == 1L && "Load 1".equals(load.name()))
                .verifyComplete();
    }
    
    @Test
    void getLoadById_ShouldReturnError_WhenNotFound() {
        ServiceInstance serviceInstance = mock(ServiceInstance.class);
        when(serviceInstance.getUri()).thenReturn(mockWebServer.url("/").uri());
        when(discoveryClient.getInstances("loads-service")).thenReturn(List.of(serviceInstance));

        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("token");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn(jwt);
        SecurityContext securityContext = new SecurityContextImpl(authentication);

        StepVerifier.create(loadsService.getLoadById(1L)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .expectError(McpError.class)
                .verify();
    }
}
