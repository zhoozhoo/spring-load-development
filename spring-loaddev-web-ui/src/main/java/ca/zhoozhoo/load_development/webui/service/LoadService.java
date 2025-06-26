package ca.zhoozhoo.load_development.webui.service;

import ca.zhoozhoo.load_development.webui.dto.LoadDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LoadService {

    private static final Logger logger = LoggerFactory.getLogger(LoadService.class);
    
    private final WebClient webClient;

    @Autowired
    public LoadService(WebClient webClient) {
        this.webClient = webClient;
    }

    @PreAuthorize("hasRole('USER')")
    public Page<LoadDto> getAllLoads(Pageable pageable) {
        try {
            // Call API Gateway loads service endpoint
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/loads")
                            .queryParam("page", pageable.getPageNumber())
                            .queryParam("size", pageable.getPageSize())
                            .queryParam("sort", getSortParam(pageable))
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            return parsePageResponse(response, pageable);
            
        } catch (WebClientResponseException e) {
            logger.error("Error fetching loads from API Gateway: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch loads", e);
        }
    }

    @PreAuthorize("hasRole('USER')")
    public Page<LoadDto> searchLoads(String searchTerm, Pageable pageable) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/loads")
                            .queryParam("search", searchTerm)
                            .queryParam("page", pageable.getPageNumber())
                            .queryParam("size", pageable.getPageSize())
                            .queryParam("sort", getSortParam(pageable))
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            return parsePageResponse(response, pageable);
            
        } catch (WebClientResponseException e) {
            logger.error("Error searching loads from API Gateway: {}", e.getMessage());
            throw new RuntimeException("Failed to search loads", e);
        }
    }

    @PreAuthorize("hasRole('USER')")
    public Optional<LoadDto> getLoadById(Long id) {
        try {
            LoadDto load = webClient.get()
                    .uri("/api/loads/{id}", id)
                    .retrieve()
                    .bodyToMono(LoadDto.class)
                    .block();
            
            return Optional.ofNullable(load);
            
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            logger.error("Error fetching load {} from API Gateway: {}", id, e.getMessage());
            throw new RuntimeException("Failed to fetch load", e);
        }
    }

    @PreAuthorize("hasRole('USER')")
    public LoadDto createLoad(LoadDto loadDto) {
        try {
            return webClient.post()
                    .uri("/api/loads")
                    .bodyValue(loadDto)
                    .retrieve()
                    .bodyToMono(LoadDto.class)
                    .block();
                    
        } catch (WebClientResponseException e) {
            logger.error("Error creating load via API Gateway: {}", e.getMessage());
            throw new RuntimeException("Failed to create load", e);
        }
    }

    @PreAuthorize("hasRole('USER')")
    public Optional<LoadDto> updateLoad(Long id, LoadDto loadDto) {
        try {
            LoadDto updatedLoad = webClient.put()
                    .uri("/api/loads/{id}", id)
                    .bodyValue(loadDto)
                    .retrieve()
                    .bodyToMono(LoadDto.class)
                    .block();
                    
            return Optional.ofNullable(updatedLoad);
            
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            logger.error("Error updating load {} via API Gateway: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update load", e);
        }
    }

    @PreAuthorize("hasRole('USER')")
    public boolean deleteLoad(Long id) {
        try {
            webClient.delete()
                    .uri("/api/loads/{id}", id)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
                    
            return true;
            
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            logger.error("Error deleting load {} via API Gateway: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete load", e);
        }
    }

    @PreAuthorize("hasRole('USER')")
    public List<LoadDto> getLoadsByCartridge(String cartridge) {
        try {
            return webClient.get()
                    .uri("/api/loads/by-cartridge/{cartridge}", cartridge)
                    .retrieve()
                    .bodyToFlux(LoadDto.class)
                    .collectList()
                    .block();
                    
        } catch (WebClientResponseException e) {
            logger.error("Error fetching loads by cartridge {} from API Gateway: {}", cartridge, e.getMessage());
            throw new RuntimeException("Failed to fetch loads by cartridge", e);
        }
    }

    @PreAuthorize("hasRole('USER')")
    public List<LoadDto> getLoadsByBullet(String bullet) {
        try {
            return webClient.get()
                    .uri("/api/loads/by-bullet/{bullet}", bullet)
                    .retrieve()
                    .bodyToFlux(LoadDto.class)
                    .collectList()
                    .block();
                    
        } catch (WebClientResponseException e) {
            logger.error("Error fetching loads by bullet {} from API Gateway: {}", bullet, e.getMessage());
            throw new RuntimeException("Failed to fetch loads by bullet", e);
        }
    }

    @PreAuthorize("hasRole('USER')")
    public List<LoadDto> getLoadsByPowder(String powder) {
        try {
            return webClient.get()
                    .uri("/api/loads/by-powder/{powder}", powder)
                    .retrieve()
                    .bodyToFlux(LoadDto.class)
                    .collectList()
                    .block();
                    
        } catch (WebClientResponseException e) {
            logger.error("Error fetching loads by powder {} from API Gateway: {}", powder, e.getMessage());
            throw new RuntimeException("Failed to fetch loads by powder", e);
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new IllegalStateException("No authenticated user found");
    }
    
    private String getSortParam(Pageable pageable) {
        if (pageable.getSort().isEmpty()) {
            return "createdAt,desc";
        }
        
        return pageable.getSort().stream()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .reduce((a, b) -> a + ";" + b)
                .orElse("createdAt,desc");
    }
    
    @SuppressWarnings("unchecked")
    private Page<LoadDto> parsePageResponse(Map<String, Object> response, Pageable pageable) {
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) response.get("content");
        List<LoadDto> loads = contentList.stream()
                .map(this::mapToLoadDto)
                .toList();
                
        Integer totalElements = (Integer) response.get("totalElements");
        
        return new PageImpl<>(loads, pageable, totalElements != null ? totalElements : loads.size());
    }
    
    private LoadDto mapToLoadDto(Map<String, Object> map) {
        LoadDto dto = new LoadDto();
        dto.setId(map.get("id") != null ? Long.valueOf(map.get("id").toString()) : null);
        dto.setCartridge((String) map.get("cartridge"));
        dto.setBullet((String) map.get("bullet"));
        dto.setPowder((String) map.get("powder"));
        dto.setPowderCharge(map.get("powderCharge") != null ? Double.valueOf(map.get("powderCharge").toString()) : null);
        dto.setPrimer((String) map.get("primer"));
        dto.setCaseName((String) map.get("caseName"));
        dto.setOverallLength(map.get("overallLength") != null ? Double.valueOf(map.get("overallLength").toString()) : null);
        dto.setVelocity(map.get("velocity") != null ? Integer.valueOf(map.get("velocity").toString()) : null);
        dto.setNotes((String) map.get("notes"));
        dto.setCreatedBy((String) map.get("createdBy"));
        // Note: Date parsing would need additional logic for proper LocalDateTime conversion
        return dto;
    }
}
