package io.rooflet.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request object for updating user password")
public class UpdatePasswordRequest {
    @NotBlank(message = "Current password is required")
    @Schema(description = "Current password for verification", example = "currentPassword123")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    @Schema(description = "New password", example = "newSecurePassword123", minLength = 8)
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "Confirmation of the new password", example = "newSecurePassword123")
    private String confirmPassword;
}
