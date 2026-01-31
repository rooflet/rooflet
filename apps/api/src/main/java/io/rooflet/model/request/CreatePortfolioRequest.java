package io.rooflet.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new portfolio")
public class CreatePortfolioRequest {

    @NotBlank(message = "Portfolio name is required")
    @Schema(description = "Portfolio name", example = "Mom's Properties", required = true)
    private String name;

    @Schema(description = "Portfolio description", example = "Properties managed on behalf of my mother")
    private String description;
}

