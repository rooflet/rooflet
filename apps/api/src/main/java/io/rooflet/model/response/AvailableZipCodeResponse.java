package io.rooflet.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Available zip code with metadata about listings and expected rent")
public class AvailableZipCodeResponse {

    @Schema(description = "ZIP code", example = "02171")
    private String zipCode;

    @Schema(description = "Number of active market listings in this zip code", example = "25")
    private Long listingCount;

    @Schema(description = "Number of expected rent records for this zip code", example = "5")
    private Long expectedRentCount;

    @Schema(description = "Whether user is currently watching this zip code", example = "true")
    private Boolean isWatched;
}

