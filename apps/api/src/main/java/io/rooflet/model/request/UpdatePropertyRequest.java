package io.rooflet.model.request;

import io.rooflet.model.enums.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Request object for updating an existing property")
public class UpdatePropertyRequest {

    // Address fields
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    @Schema(description = "Primary address line", example = "123 Main Street")
    private String address1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    @Schema(description = "Secondary address line", example = "Unit 4B")
    private String address2;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "City", example = "San Francisco")
    private String city;

    @Size(min = 2, max = 2, message = "State must be 2 characters")
    @Schema(description = "State abbreviation", example = "CA")
    private String state;

    @Size(max = 10, message = "ZIP code must not exceed 10 characters")
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
}
