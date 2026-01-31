package io.rooflet.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Getter
@Schema(description = "Request object for updating an existing tenant")
public class UpdateTenantRequest {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "Tenant full name", example = "Jane Smith")
    private String name;

    @Email(message = "Email must be valid")
    @Schema(description = "Tenant email address", example = "jane.smith@email.com")
    private String email;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Schema(description = "Tenant phone number", example = "+1-555-123-4567")
    private String phoneNumber;

    @Schema(description = "Associated property ID (optional)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID propertyId;

    @Schema(description = "Lease start date")
    private LocalDate leaseStartDate;

    @Schema(description = "Lease end date")
    private LocalDate leaseEndDate;

    @Schema(description = "Whether the tenant is archived", example = "false")
    private Boolean archived;

    @Schema(description = "Monthly rent for the tenant", example = "1200.00")
    private BigDecimal monthlyRent;
}
