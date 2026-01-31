package io.rooflet.model.request;

import io.rooflet.model.enums.PortfolioRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a member's role in a portfolio")
public class UpdatePortfolioMemberRoleRequest {

    @NotNull(message = "Role is required")
    @Schema(description = "New role to assign", example = "VIEWER", required = true)
    private PortfolioRole role;
}

