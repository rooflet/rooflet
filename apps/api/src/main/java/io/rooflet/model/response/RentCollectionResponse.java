package io.rooflet.model.response;

import io.rooflet.model.RentCollection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Rent collection response containing payment information")
public class RentCollectionResponse {

    @Schema(description = "Rent collection ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Associated property ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID propertyId;

    @Schema(description = "Property address for display", example = "123 Main St, Unit 4B")
    private String propertyAddress;

    @Schema(description = "Associated tenant ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID tenantId;

    @Schema(description = "Tenant name", example = "John Smith")
    private String tenantName;

    @Schema(description = "Expected rent amount for the period", example = "2500.00")
    private BigDecimal expectedAmount;

    @Schema(description = "Amount paid so far", example = "1000.00")
    private BigDecimal paidAmount;

    @Schema(description = "Date when payment was received")
    private LocalDate paymentDate;

    @Schema(description = "Notes about the payment period")
    private String notes;


    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;

    public static RentCollectionResponse from(RentCollection rentCollection) {
        String propertyAddress = null;
        if (rentCollection.getProperty() != null) {
            propertyAddress = rentCollection.getProperty().getAddress1();
            if (rentCollection.getProperty().getAddress2() != null &&
                    !rentCollection.getProperty().getAddress2().isEmpty()) {
                propertyAddress += ", " + rentCollection.getProperty().getAddress2();
            }
        }

        return RentCollectionResponse.builder()
                .id(rentCollection.getId())
                .propertyId(rentCollection.getProperty().getId())
                .propertyAddress(propertyAddress)
                .tenantId(rentCollection.getTenant().getId())
                .tenantName(rentCollection.getTenant().getName())
                .expectedAmount(rentCollection.getExpectedAmount())
                .paidAmount(rentCollection.getPaidAmount())
                .paymentDate(rentCollection.getPaymentDate())
                .notes(rentCollection.getNotes())
                .createdAt(rentCollection.getCreatedAt())
                .updatedAt(rentCollection.getUpdatedAt())
                .build();
    }
}
