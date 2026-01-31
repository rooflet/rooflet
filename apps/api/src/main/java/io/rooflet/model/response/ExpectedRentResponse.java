package io.rooflet.model.response;

import io.rooflet.model.entity.ExpectedRent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Expected rent response containing rent standard information")
public class ExpectedRentResponse {

    @Schema(description = "Expected rent ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "ZIP code", example = "02101")
    private String zipCode;

    @Schema(description = "Number of bedrooms", example = "2")
    private Integer bedrooms;

    @Schema(description = "Expected rent amount", example = "2500.00")
    private BigDecimal expectedRent;

    @Schema(description = "Data source", example = "Boston Housing Authority Payment Standard")
    private String source;

    @Schema(description = "Effective date of the rent standard", example = "2025-07-01")
    private LocalDate effectiveDate;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    /**
     * Convert ExpectedRent entity to ExpectedRentResponse
     */
    public static ExpectedRentResponse from(ExpectedRent expectedRent) {
        if (expectedRent == null) {
            return null;
        }

        return ExpectedRentResponse.builder()
                .id(expectedRent.getId())
                .zipCode(expectedRent.getZipCode())
                .bedrooms(expectedRent.getBedrooms())
                .expectedRent(expectedRent.getExpectedRent())
                .source(expectedRent.getSource())
                .effectiveDate(expectedRent.getEffectiveDate())
                .createdAt(expectedRent.getCreatedAt())
                .updatedAt(expectedRent.getUpdatedAt())
                .build();
    }
}

