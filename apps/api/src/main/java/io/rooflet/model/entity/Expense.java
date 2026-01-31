package io.rooflet.model.entity;

import io.rooflet.model.enums.ExpenseCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "expenses")
@Schema(description = "Expense entity representing property-related expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Expense ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    @Schema(description = "Associated property (optional)")
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Schema(description = "Owner of the expense")
    private User owner;

    @NotNull
    @Schema(description = "Date when the expense was incurred")
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Schema(description = "Category of the expense")
    private ExpenseCategory category;

    @NotNull
    @Positive
    @Column(precision = 10, scale = 2)
    @Schema(description = "Expense amount", example = "150.50")
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Description of the expense", example = "Plumbing repair for kitchen sink")
    private String description;

    // Audit fields
    @CreationTimestamp
    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;
}
