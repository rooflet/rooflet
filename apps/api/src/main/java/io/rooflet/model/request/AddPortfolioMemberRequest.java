package io.rooflet.model.request;

import io.rooflet.model.enums.PortfolioRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to add a member to a portfolio")
public class AddPortfolioMemberRequest {

    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email of the user to add", example = "john@example.com", required = true)
    private String email;

    @NotNull(message = "Role is required")
    @Schema(description = "Role to assign to the user", example = "MANAGER", required = true)
    private PortfolioRole role;
}

