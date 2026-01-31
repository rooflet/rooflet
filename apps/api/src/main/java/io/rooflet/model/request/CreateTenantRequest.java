package io.rooflet.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Getter
@Schema(description = "Request object for creating a new tenant")
public class CreateTenantRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "Tenant full name", example = "Jane Smith")
    private String name;

    @Email(message = "Email must be valid")
    @Schema(description = "Tenant email address", example = "jane.smith@email.com")
    private String email;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only numbers")
    @Schema(description = "Tenant phone number", example = "15551234567")
    private String phoneNumber;

    @Schema(description = "Associated property ID (optional)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID propertyId;

    @Schema(description = "Lease start date")
    private LocalDate leaseStartDate;

    @Schema(description = "Lease end date")
    private LocalDate leaseEndDate;

    @NotNull(message = "Monthly rent is required")
    @DecimalMin(value = "0.01", message = "Monthly rent must be greater than 0")
    @Schema(description = "Monthly rent for the tenant", example = "1200.00")
    private BigDecimal monthlyRent;
}
