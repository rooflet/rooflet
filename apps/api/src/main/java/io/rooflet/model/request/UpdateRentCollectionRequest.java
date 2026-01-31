package io.rooflet.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Request object for updating an existing rent collection record")
public class UpdateRentCollectionRequest {

    @Positive(message = "Expected amount must be positive")
    @Schema(description = "Expected rent amount for the period", example = "2500.00")
    private BigDecimal expectedAmount;

    @Schema(description = "Amount paid", example = "2500.00")
    private BigDecimal paidAmount;

    @Schema(description = "Date when payment was received")
    private LocalDate paymentDate;

    @Schema(description = "Notes about the payment period", example = "Payment completed in full")
    private String notes;
}
