package io.rooflet.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(description = "Request object for creating a new rent collection record")
public class CreateRentCollectionRequest {

    @NotNull(message = "Property ID is required")
    @Schema(description = "Associated property ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID propertyId;

    @NotNull(message = "Tenant ID is required")
    @Schema(description = "Associated tenant ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID tenantId;

    @NotNull(message = "Expected amount is required")
    @Positive(message = "Expected amount must be positive")
    @Schema(description = "Expected rent amount for the period", example = "2500.00")
    private BigDecimal expectedAmount;

    @Schema(description = "Amount paid (defaults to 0)", example = "1000.00")
    private BigDecimal paidAmount;

    @Schema(description = "Date when payment was received")
    private LocalDate paymentDate;

    @Schema(description = "Notes about the payment period", example = "Paid $1,000 upfront, will pay remaining $1,500 by Jan 15th")
    private String notes;
}
