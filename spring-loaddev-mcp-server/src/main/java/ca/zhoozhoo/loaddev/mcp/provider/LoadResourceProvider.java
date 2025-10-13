package ca.zhoozhoo.loaddev.mcp.provider;

import java.util.List;

import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.mcp.dto.LoadDto;
import ca.zhoozhoo.loaddev.mcp.service.LoadsService;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import reactor.core.publisher.Mono;

@Service
public class LoadResourceProvider {

    @Autowired
    private LoadsService loadsService;

    @McpResource(uri = "load://{id}", name = "Load", description = "Provides load information for a given ID")
    public Mono<ReadResourceResult> getLoadById(ReadResourceRequest request, String id) {
        return loadsService.getLoadById(Long.parseLong(id))
                .map(load -> {
                    String loadInfo = formatLoadInfo(load);
                    return new ReadResourceResult(
                            List.of(new TextResourceContents(request.uri(), "text/plain", loadInfo)));
                });
    }

    private String formatLoadInfo(LoadDto load) {
        if (load == null) {
            return "Load not found";
        }

        // Determine units based on measurement system
        boolean isMetric = "Metric".equalsIgnoreCase(load.measurementUnits());
        String weightUnit = isMetric ? "grams" : "grains";
        String lengthUnit = isMetric ? "mm" : "inches";

        StringBuilder sb = new StringBuilder();

        sb.append("ID: ").append(load.id()).append("\n");
        sb.append("Name: ").append(load.name()).append("\n");
        if (load.description() != null && !load.description().isEmpty()) {
            sb.append("Description: ").append(load.description()).append("\n");
        }
        sb.append("Units: ").append(load.measurementUnits()).append("\n\n");

        sb.append("Powder Manufacturer: ").append(load.powderManufacturer()).append("\n");
        sb.append("Powder Type: ").append(load.powderType()).append("\n\n");

        sb.append("Bullet Manufacturer: ").append(load.bulletManufacturer()).append("\n");
        sb.append("Bullet Type: ").append(load.bulletType()).append("\n");
        sb.append("Bullet Weight: ").append(load.bulletWeight()).append(" ").append(weightUnit).append("\n\n");

        sb.append("Primer Manufacturer: ").append(load.primerManufacturer()).append("\n");
        sb.append("Primer Type: ").append(load.primerType()).append("\n\n");

        if (load.distanceFromLands() != null) {
            sb.append("Distance from Lands: ").append(load.distanceFromLands()).append(" ").append(lengthUnit)
                    .append("\n");
        }
        if (load.caseOverallLength() != null) {
            sb.append("Case Overall Length: ").append(load.caseOverallLength()).append(" ").append(lengthUnit)
                    .append("\n");
        }
        if (load.neckTension() != null) {
            sb.append("Neck Tension: ").append(load.neckTension()).append(" ").append(lengthUnit).append("\n");
        }

        if (load.rifleId() != null) {
            sb.append("\nAssociated Rifle ID: ").append(load.rifleId()).append("\n");
        }

        return sb.toString();
    }
}
