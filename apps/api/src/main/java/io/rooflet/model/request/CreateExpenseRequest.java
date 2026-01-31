package io.rooflet.model.request;

import io.rooflet.model.enums.ExpenseCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(description = "Request object for creating a new expense")
public class CreateExpenseRequest {

    @Schema(description = "Associated property ID (optional)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID propertyId;

    @NotNull(message = "Expense date is required")
    @Schema(description = "Date when the expense was incurred")
    private LocalDate expenseDate;

    @NotNull(message = "Category is required")
    @Schema(description = "Category of the expense")
    private ExpenseCategory category;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Expense amount", example = "150.50")
    private BigDecimal amount;

    @Schema(description = "Description of the expense", example = "Plumbing repair for kitchen sink")
    private String description;
}
