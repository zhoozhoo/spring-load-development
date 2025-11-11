package ca.zhoozhoo.loaddev.mcp.provider;

import java.util.List;

import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.mcp.dto.LoadDto;
import ca.zhoozhoo.loaddev.mcp.service.LoadsService;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import reactor.core.publisher.Mono;

/**
 * MCP resource provider for load information.
 * <p>
 * Exposes load data as MCP resources accessible via URI patterns. Resources provide
 * formatted, human-readable representations of load data for consumption by AI
 * assistants and other MCP clients.
 * <p>
 * Resources are automatically discovered and registered by the MCP framework through
 * the {@code @McpResource} annotation. Authentication is handled transparently using
 * JWT tokens from the security context.
 * 
 * @author Zhubin Salehi
 * @see org.springaicommunity.mcp.annotation.McpResource
 */
@Service
public class LoadResourceProvider {

    @Autowired
    private LoadsService loadsService;

    /**
     * Retrieves and formats load information as an MCP resource.
     * <p>
     * Returns a human-readable text representation of the load including all
     * component specifications with appropriate unit labels (metric vs imperial).
     * Authentication is automatically propagated from the security context.
     *
     * @param request the MCP read resource request containing the URI
     * @param id the unique identifier of the load to retrieve
     * @return Mono emitting ReadResourceResult with formatted load information
     * @throws McpError with INTERNAL_ERROR if service discovery fails
     * @throws McpError with INVALID_REQUEST if authentication fails
     * @throws NumberFormatException if id is not a valid number
     */
    @McpResource(uri = "load://{id}", name = "Load", description = "Provides load information for a given ID")
    public Mono<ReadResourceResult> getLoadById(ReadResourceRequest request, String id) {
        return loadsService.getLoadById(Long.parseLong(id))
                .map(load -> {
                    String loadInfo = formatLoadInfo(load);
                    return new ReadResourceResult(
                            List.of(new TextResourceContents(request.uri(), "text/plain", loadInfo)));
                });
    }

    /**
     * Formats load information into a human-readable text representation.
     * <p>
     * Automatically adjusts unit labels (grains/grams, inches/mm) based on the
     * load's measurement system. Includes all available load specifications
     * with clear formatting and proper line breaks.
     *
     * @param load the LoadDto to format
     * @return formatted multi-line string representation of the load, or "Load not found" if null
     */
    private String formatLoadInfo(LoadDto load) {
        if (load == null) {
            return "Load not found";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("ID: ").append(load.id()).append("\n");
        sb.append("Name: ").append(load.name()).append("\n");
        if (load.description() != null && !load.description().isEmpty()) {
            sb.append("Description: ").append(load.description()).append("\n");
        }
        sb.append("\n");

        sb.append("Powder Manufacturer: ").append(load.powderManufacturer()).append("\n");
        sb.append("Powder Type: ").append(load.powderType()).append("\n\n");

                sb.append("Bullet Manufacturer: ").append(load.bulletManufacturer()).append("\n");
        sb.append("Bullet Type: ").append(load.bulletType()).append("\n");
                if (load.bulletWeight() != null) {
                        sb.append("Bullet Weight: ").append(load.bulletWeight().getValue())
                            .append(" ").append(load.bulletWeight().getUnit()).append("\n\n");
                }

        sb.append("Primer Manufacturer: ").append(load.primerManufacturer()).append("\n");
        sb.append("Primer Type: ").append(load.primerType()).append("\n\n");

        if (load.distanceFromLands() != null) {
            sb.append("Distance from Lands: ").append(load.distanceFromLands().getValue())
              .append(" ").append(load.distanceFromLands().getUnit()).append("\n");
        }
        if (load.caseOverallLength() != null) {
            sb.append("Case Overall Length: ").append(load.caseOverallLength().getValue())
              .append(" ").append(load.caseOverallLength().getUnit()).append("\n");
        }
        if (load.neckTension() != null) {
            sb.append("Neck Tension: ").append(load.neckTension().getValue())
              .append(" ").append(load.neckTension().getUnit()).append("\n");
        }

        if (load.rifleId() != null) {
            sb.append("\nAssociated Rifle ID: ").append(load.rifleId()).append("\n");
        }

        return sb.toString();
    }
}
