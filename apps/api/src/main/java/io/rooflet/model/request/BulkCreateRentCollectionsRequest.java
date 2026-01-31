package io.rooflet.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Request object for bulk creating rent collection records with specific amounts and dates")
public class BulkCreateRentCollectionsRequest {

    @NotNull(message = "Tenant ID is required")
    @Schema(description = "Tenant ID for whom to create rent collections", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID tenantId;

    @NotNull(message = "Items list is required")
    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    @Schema(description = "List of individual rent collection items with specific amounts and dates")
    private List<RentCollectionItemRequest> items;
}

