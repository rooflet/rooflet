package io.rooflet.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "tenants")
@Schema(description = "Tenant entity representing a property renter")
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Tenant ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @NotBlank
    @Schema(description = "Tenant full name", example = "Jane Smith")
    private String name;

    @Email
    @Schema(description = "Tenant email address", example = "jane.smith@email.com")
    private String email;

    @Schema(description = "Tenant phone number", example = "+1-555-123-4567")
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    @Schema(description = "Associated property (optional)")
    private Property property;

    @Column(name = "lease_start_date")
    private LocalDate leaseStartDate;

    @Column(name = "lease_end_date")
    private LocalDate leaseEndDate;

    @Column(name = "monthly_rent", nullable = false)
    @Schema(description = "Monthly rent for the tenant", example = "1200.00")
    private BigDecimal monthlyRent;

    @Builder.Default
    @Schema(description = "Whether the tenant record is archived", example = "false")
    private Boolean archived = false;

    // Audit fields
    @CreationTimestamp
    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;
}
