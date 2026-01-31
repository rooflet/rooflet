package io.rooflet.model.response;

import io.rooflet.model.entity.Property;
import io.rooflet.model.enums.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Property response containing property information")
public class PropertyResponse {

    @Schema(description = "Property ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Owner ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID ownerId;

    @Schema(description = "Owner full name", example = "John Smith")
    private String ownerName;

    // Address fields
    @Schema(description = "Primary address line", example = "123 Main Street")
    private String address1;

    @Schema(description = "Secondary address line", example = "Unit 4B")
    private String address2;

    @Schema(description = "City", example = "San Francisco")
    private String city;

    @Schema(description = "State abbreviation", example = "CA")
    private String state;

    @Schema(description = "ZIP code", example = "94102")
    private String zipCode;


    // Property details
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
    @Schema(description = "Additional notes about the property")
    private String notes;

    @Schema(description = "Whether the property is archived", example = "false")
    private Boolean archived;

    // Audit fields
    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;

    public static PropertyResponse from(Property property) {
        return PropertyResponse.builder()
                .id(property.getId())
                .ownerId(property.getOwner().getId())
                .ownerName(property.getOwner().getFullName())
                .address1(property.getAddress1())
                .address2(property.getAddress2())
                .city(property.getCity())
                .state(property.getState())
                .zipCode(property.getZipCode())
                .propertyType(property.getPropertyType())
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .squareFeet(property.getSquareFeet())
                .marketValue(property.getMarketValue())
                .purchasePrice(property.getPurchasePrice())
                .purchaseDate(property.getPurchaseDate())
                .debt(property.getDebt())
                .interestRate(property.getInterestRate())
                .monthlyHoa(property.getMonthlyHoa())
                .monthlyPropertyTax(property.getMonthlyPropertyTax())
                .monthlyInsurance(property.getMonthlyInsurance())
                .notes(property.getNotes())
                .archived(property.getArchived())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .build();
    }
}
