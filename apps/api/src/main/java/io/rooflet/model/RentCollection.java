package io.rooflet.model;

import io.rooflet.model.entity.Property;
import io.rooflet.model.entity.Tenant;
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
@Table(name = "rent_collections")
@Schema(description = "Rent collection entity representing rent payments")
public class RentCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Rent collection ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @NotNull
    @Schema(description = "Associated property")
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull
    @Schema(description = "Associated tenant")
    private Tenant tenant;

    @NotNull
    @Positive
    @Column(precision = 10, scale = 2)
    @Schema(description = "Expected rent amount for the period", example = "2500.00")
    private BigDecimal expectedAmount;

    @Builder.Default
    @Column(precision = 10, scale = 2)
    @Schema(description = "Amount paid so far", example = "1000.00")
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Schema(description = "Date when payment was received")
    private LocalDate paymentDate;

    @Column(columnDefinition = "CLOB")
    @Schema(description = "Notes about the payment period", example = "Paid $1,000 upfront, will pay remaining $1,200 by Jan 15th")
    private String notes;


    // Audit fields
    @CreationTimestamp
    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;
}
