package io.rooflet.model.request;

import io.rooflet.model.enums.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(description = "Request object for creating a new property")
public class CreatePropertyRequest {

    @NotNull(message = "Owner ID is required")
    @Schema(description = "ID of the property owner", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID ownerId;

    // Address fields
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    @Schema(description = "Primary address line", example = "123 Main Street")
    private String address1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    @Schema(description = "Secondary address line", example = "Unit 4B")
    private String address2;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "City", example = "San Francisco")
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 2, message = "State must be 2 characters")
    @Pattern(regexp = "^(AL|AK|AZ|AR|CA|CO|CT|DE|FL|GA|HI|ID|IL|IN|IA|KS|KY|LA|ME|MD|MA|MI|MN|MS|MO|MT|NE|NV|NH|NJ|NM|NY|NC|ND|OH|OK|OR|PA|RI|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI|WY|DC)$",
            message = "State must be a valid US state abbreviation")
    @Schema(description = "State abbreviation", example = "CA")
    private String state;

    @NotBlank(message = "ZIP code is required")
    @Size(max = 10, message = "ZIP code must not exceed 10 characters")
    @Schema(description = "ZIP code", example = "94102")
    private String zipCode;


    // Property details
    @NotNull(message = "Property type is required")
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
}
