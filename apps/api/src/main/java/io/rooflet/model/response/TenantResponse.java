package io.rooflet.model.response;

import io.rooflet.model.entity.Tenant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Tenant response containing tenant information")
public class TenantResponse {

    @Schema(description = "Tenant ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Tenant full name", example = "Jane Smith")
    private String name;

    @Schema(description = "Tenant email address", example = "jane.smith@email.com")
    private String email;

    @Schema(description = "Tenant phone number", example = "+1-555-123-4567")
    private String phoneNumber;

    @Schema(description = "Associated property ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID propertyId;

    @Schema(description = "Property address for display", example = "123 Main St, Unit 4B")
    private String propertyAddress;

    @Schema(description = "Lease start date")
    private LocalDate leaseStartDate;

    @Schema(description = "Lease end date")
    private LocalDate leaseEndDate;

    @Schema(description = "Whether the tenant is archived", example = "false")
    private Boolean archived;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Monthly rent for the tenant", example = "1200.00")
    private BigDecimal monthlyRent;

    public static TenantResponse from(Tenant tenant) {
        String propertyAddress = null;
        if (tenant.getProperty() != null) {
            propertyAddress = tenant.getProperty().getAddress1();
            if (tenant.getProperty().getAddress2() != null && !tenant.getProperty().getAddress2().isEmpty()) {
                propertyAddress += ", " + tenant.getProperty().getAddress2();
            }
        }

        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .email(tenant.getEmail())
                .phoneNumber(tenant.getPhoneNumber())
                .propertyId(tenant.getProperty() != null ? tenant.getProperty().getId() : null)
                .propertyAddress(propertyAddress)
                .leaseStartDate(tenant.getLeaseStartDate())
                .leaseEndDate(tenant.getLeaseEndDate())
                .monthlyRent(tenant.getMonthlyRent())
                .archived(tenant.getArchived())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
