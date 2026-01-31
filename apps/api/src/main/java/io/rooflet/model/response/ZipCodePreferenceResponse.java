package io.rooflet.model.response;

import io.rooflet.model.entity.UserZipCodePreference;
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
@Schema(description = "User's zip code preference response")
public class ZipCodePreferenceResponse {

    @Schema(description = "User ID", example = "11111111-1111-1111-1111-111111111111")
    private UUID userId;

    @Schema(description = "ZIP code", example = "02171")
    private String zipCode;

    @Schema(description = "Creation timestamp", example = "2026-01-02T10:30:00")
    private LocalDateTime createdAt;

    public static ZipCodePreferenceResponse from(UserZipCodePreference preference) {
        return ZipCodePreferenceResponse.builder()
                .userId(preference.getUserId())
                .zipCode(preference.getZipCode())
                .createdAt(preference.getCreatedAt())
                .build();
    }
}

