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
@Table(name = "market_listings")
@Schema(description = "Market listing entity representing a property listing from various sources")
public class MarketListing {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Market listing ID", example = "fe1bdb55-f73e-48f1-859d-25b60d169050")
    private UUID id;

    // Source information
    @NotBlank
    @Column(length = 50, nullable = false)
    @Schema(description = "Listing source", example = "REDFIN")
    private String source;

    @Column(length = 255)
    @Schema(description = "External listing ID from the source", example = "73463500")
    private String sourceListingId;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "URL to the listing on the source website")
    private String sourceUrl;

    @Column(length = 100)
    @Schema(description = "MLS listing number if available", example = "73463500")
    private String sourceMlsNumber;

    // Sale information
    @Column(length = 50)
    @Schema(description = "Sale type", example = "MLS Listing")
    private String saleType;

    @NotBlank
    @Column(length = 50, nullable = false)
    @Schema(description = "Current listing status", example = "Active")
    private String listingStatus;

    @Schema(description = "Date property was sold", example = "2025-12-27")
    private LocalDate soldDate;

    @Schema(description = "Date property was listed", example = "2025-12-27")
    private LocalDate listDate;

    @Schema(description = "Number of days on market", example = "5")
    private Integer daysOnMarket;

    // Address information
    @NotBlank
    @Column(length = 500, nullable = false)
    @Schema(description = "Full address as provided by source", example = "23 Myrtle St")
    private String address;

    @Column(length = 255)
    @Schema(description = "Parsed primary address", example = "23 Myrtle St")
    private String address1;

    @Column(length = 255)
    @Schema(description = "Parsed secondary address (unit, apt, etc.)", example = "#204")
    private String address2;

    @NotBlank
    @Column(length = 100, nullable = false)
    @Schema(description = "City", example = "Quincy")
    private String city;

    @NotBlank
    @Column(length = 2, nullable = false)
    @Schema(description = "State abbreviation", example = "MA")
    private String state;

    @NotBlank
    @Column(length = 10, nullable = false)
    @Schema(description = "ZIP code", example = "02171")
    private String zipCode;

    @Column(length = 100)
    @Schema(description = "Neighborhood or area name", example = "North Quincy")
    private String location;

    @Column(precision = 10, scale = 8)
    @Schema(description = "Geographic latitude", example = "42.2796206")
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    @Schema(description = "Geographic longitude", example = "-71.0347665")
    private BigDecimal longitude;

    // Property details
    @NotBlank
    @Column(length = 100, nullable = false)
    @Schema(description = "Type of property", example = "Multi-Family (2-4 Unit)")
    private String propertyType;

    @Schema(description = "Number of bedrooms", example = "6")
    private Integer bedrooms;

    @Column(precision = 4, scale = 1)
    @Schema(description = "Number of bathrooms", example = "2.0")
    private BigDecimal bathrooms;

    @Schema(description = "Interior square footage", example = "2016")
    private Integer squareFeet;

    @Schema(description = "Lot size in square feet", example = "4785")
    private Integer lotSize;

    @Schema(description = "Year property was built", example = "1890")
    private Integer yearBuilt;

    // Pricing information
    @Column(precision = 15, scale = 2)
    @Schema(description = "Current listing price or sold price", example = "1060000.00")
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    @Schema(description = "Price per square foot", example = "526.00")
    private BigDecimal pricePerSqft;

    @Column(precision = 15, scale = 2)
    @Schema(description = "Original listing price", example = "1060000.00")
    private BigDecimal originalListPrice;

    // HOA and fees
    @Column(precision = 10, scale = 2)
    @Schema(description = "Monthly HOA fee", example = "493.00")
    private BigDecimal hoaFee;

    // Open house information
    @Schema(description = "Next open house start time")
    private LocalDateTime nextOpenHouseStart;

    @Schema(description = "Next open house end time")
    private LocalDateTime nextOpenHouseEnd;


    @Column(columnDefinition = "TEXT")
    @Schema(description = "JSON/text storage for additional source-specific fields")
    private String rawData;

    // Audit fields
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Schema(description = "Last scraped timestamp")
    private LocalDateTime lastScrapedAt;
}

