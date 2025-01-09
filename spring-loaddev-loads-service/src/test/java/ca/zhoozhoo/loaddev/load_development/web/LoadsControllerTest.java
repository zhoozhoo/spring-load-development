package ca.zhoozhoo.loaddev.load_development.web;

import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.zhoozhoo.loaddev.load_development.dao.LoadRepository;
import ca.zhoozhoo.loaddev.load_development.model.Load;
import reactor.core.publisher.Flux;

@WebFluxTest(LoadsController.class)
class LoadsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private LoadRepository loadRepository;

    @Test
    void shouldGetAllLoads() {
        Load load1 = new Load(1L, "Load1", "Description1", "Manufacturer1", "Type1", 10.0,
                "BulletManufacturer1", "BulletType1", 100.0, "PrimerManufacturer1", "PrimerType1", 0.020, 1L);
        Load load2 = new Load(2L, "Load2", "Description2", "Manufacturer2", "Type2", 20.0,
                "BulletManufacturer2", "BulletType2", 200.0, "PrimerManufacturer2", "PrimerType2", 0.020, 2L);

        given(loadRepository.findAll()).willReturn(Flux.just(load1, load2));

        webTestClient.get().uri("/loads")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1L)
                .jsonPath("$[0].name").isEqualTo("Load1")
                .jsonPath("$[1].id").isEqualTo(2L)
                .jsonPath("$[1].name").isEqualTo("Load2");
    }
}