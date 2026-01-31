package io.rooflet.model.response;

import io.rooflet.model.entity.Expense;
import io.rooflet.model.enums.ExpenseCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Expense response containing expense information")
public class ExpenseResponse {

    @Schema(description = "Expense ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Associated property ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID propertyId;

    @Schema(description = "Property address for display", example = "123 Main St, Unit 4B")
    private String propertyAddress;

    @Schema(description = "Date when the expense was incurred")
    private LocalDate expenseDate;

    @Schema(description = "Category of the expense")
    private ExpenseCategory category;

    @Schema(description = "Category display name", example = "Repairs & Maintenance")
    private String categoryDisplayName;

    @Schema(description = "Expense amount", example = "150.50")
    private BigDecimal amount;

    @Schema(description = "Description of the expense", example = "Plumbing repair for kitchen sink")
    private String description;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;

    public static ExpenseResponse from(Expense expense) {
        String propertyAddress = null;
        if (expense.getProperty() != null) {
            propertyAddress = expense.getProperty().getAddress1();
            if (expense.getProperty().getAddress2() != null && !expense.getProperty().getAddress2().isEmpty()) {
                propertyAddress += ", " + expense.getProperty().getAddress2();
            }
        }

        return ExpenseResponse.builder()
                .id(expense.getId())
                .propertyId(expense.getProperty() != null ? expense.getProperty().getId() : null)
                .propertyAddress(propertyAddress)
                .expenseDate(expense.getExpenseDate())
                .category(expense.getCategory())
                .categoryDisplayName(expense.getCategory().getDisplayName())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
