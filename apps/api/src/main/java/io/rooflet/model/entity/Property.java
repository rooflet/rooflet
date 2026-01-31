package io.rooflet.model.entity;

import io.rooflet.model.enums.PropertyType;
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
@Table(name = "properties")
@Schema(description = "Property entity representing a real estate property")
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Property ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Property owner (deprecated, use portfolio)")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    @Schema(description = "Portfolio this property belongs to")
    private Portfolio portfolio;

    // Address fields
    @NotBlank
    @Schema(description = "Primary address line", example = "123 Main Street")
    private String address1;

    @Schema(description = "Secondary address line", example = "Apt 4B")
    private String address2;

    @NotBlank
    @Schema(description = "City", example = "San Francisco")
    private String city;

    @NotBlank
    @Column(length = 2)
    @Schema(description = "State abbreviation", example = "CA")
    private String state;

    @NotBlank
    @Column(length = 10)
    @Schema(description = "ZIP code", example = "94102")
    private String zipCode;


    // Property details
    @Enumerated(EnumType.STRING)
    @NotNull
    @Schema(description = "Type of property")
    private PropertyType propertyType;

    @Schema(description = "Number of bedrooms", example = "3")
    private Integer bedrooms;

    @Schema(description = "Number of bathrooms", example = "2.5")
    private BigDecimal bathrooms;

    @Schema(description = "Square footage", example = "1500")
    private Integer squareFeet;


    // Financial information
    @Schema(description = "Current market value", example = "750000.00")
    private BigDecimal marketValue;

    @Schema(description = "Original purchase price", example = "600000.00")
    private BigDecimal purchasePrice;

    @Column(name = "purchase_date")
    @Schema(description = "Date when the property was purchased", example = "2020-06-15")
    private LocalDate purchaseDate;

    @Schema(description = "Outstanding debt on property", example = "450000.00")
    private BigDecimal debt;

    @Schema(description = "Interest rate percentage", example = "4.25")
    private BigDecimal interestRate;

    // Monthly costs
    @Schema(description = "Monthly HOA fees", example = "250.00")
    private BigDecimal monthlyHoa;

    @Schema(description = "Monthly property tax", example = "625.00")
    private BigDecimal monthlyPropertyTax;

    @Schema(description = "Monthly insurance cost", example = "150.00")
    private BigDecimal monthlyInsurance;

    // Additional fields
    @Column(columnDefinition = "TEXT")
    @Schema(description = "Additional notes about the property")
    private String notes;

    @Builder.Default
    @Schema(description = "Whether the property is archived", example = "false")
    private Boolean archived = false;

    // Audit fields
    @CreationTimestamp
    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;
}
