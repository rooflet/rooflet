package io.rooflet.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update portfolio details")
public class UpdatePortfolioRequest {

    @Schema(description = "Portfolio name", example = "Mom's Properties")
    private String name;

    @Schema(description = "Portfolio description", example = "Properties managed on behalf of my mother")
    private String description;
}

