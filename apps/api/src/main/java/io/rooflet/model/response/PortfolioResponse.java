package io.rooflet.model.response;

import io.rooflet.model.entity.Portfolio;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Portfolio response containing portfolio information")
public class PortfolioResponse {

    @Schema(description = "Portfolio ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Portfolio name", example = "Mom's Properties")
    private String name;

    @Schema(description = "Portfolio description", example = "Properties managed on behalf of my mother")
    private String description;

    @Schema(description = "Whether the portfolio is archived", example = "false")
    private Boolean archived;

    @Schema(description = "Portfolio creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Portfolio last update timestamp")
    private LocalDateTime updatedAt;

    public static PortfolioResponse from(Portfolio portfolio) {
        return PortfolioResponse.builder()
                .id(portfolio.getId())
                .name(portfolio.getName())
                .description(portfolio.getDescription())
                .archived(portfolio.getArchived())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }
}

