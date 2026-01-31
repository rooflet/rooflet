package io.rooflet.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for user authentication")
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Schema(description = "Email for authentication", example = "john_doe@test.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User's password", example = "securePassword123")
    private String password;
}
