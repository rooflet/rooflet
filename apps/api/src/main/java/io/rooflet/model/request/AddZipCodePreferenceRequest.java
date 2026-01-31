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
@Schema(description = "Request to add a zip code to user's watch list")
public class AddZipCodePreferenceRequest {

    @NotBlank(message = "ZIP code is required")
    @Schema(description = "ZIP code to watch", example = "02171")
    private String zipCode;
}

