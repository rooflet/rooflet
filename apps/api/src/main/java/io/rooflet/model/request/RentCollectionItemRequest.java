package io.rooflet.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Individual rent collection item for bulk creation")
public class RentCollectionItemRequest {

    @NotNull(message = "Payment date is required")
    @Schema(description = "Date for this rent collection period (typically first of the month)", example = "2024-01-01")
    private LocalDate paymentDate;

    @Schema(description = "Expected rent amount for this period (defaults to tenant's monthly rent)", example = "2500.00")
    private BigDecimal expectedAmount;

    @Schema(description = "Amount paid for this period (defaults to 0)", example = "2500.00")
    private BigDecimal paidAmount;

    @Schema(description = "Optional notes for this specific period", example = "Payment received via bank transfer")
    private String notes;
}

