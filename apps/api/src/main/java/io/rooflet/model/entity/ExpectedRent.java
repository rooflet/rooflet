package io.rooflet.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "expected_rent")
@Schema(description = "Expected rent information by zip code and bedroom count")
public class ExpectedRent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Expected rent ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @NotBlank
    @Column(name = "zip_code", length = 10, nullable = false)
    @Schema(description = "ZIP code", example = "02101")
    private String zipCode;

    @NotNull
    @Column(name = "bedrooms", nullable = false)
    @Schema(description = "Number of bedrooms", example = "2")
    private Integer bedrooms;

    @NotNull
    @Column(name = "expected_rent", precision = 10, scale = 2, nullable = false)
    @Schema(description = "Expected rent amount", example = "2500.00")
    private BigDecimal expectedRent;

    @NotBlank
    @Column(name = "source", length = 100, nullable = false)
    @Schema(description = "Data source", example = "Boston Housing Authority Payment Standard")
    private String source;

    @NotNull
    @Column(name = "effective_date", nullable = false)
    @Schema(description = "Effective date of the rent standard", example = "2025-07-01")
    private LocalDate effectiveDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

