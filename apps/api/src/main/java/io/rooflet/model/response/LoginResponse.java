package io.rooflet.model.response;

import io.rooflet.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Response object for successful authentication")
public class LoginResponse {
    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "User's email", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Authentication timestamp", example = "2023-10-12T10:30:00")
    private LocalDateTime loginTime;

    @Schema(description = "Authentication success message", example = "Login successful")
    private String message;

    public static LoginResponse from(User user) {
        return LoginResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .loginTime(LocalDateTime.now())
                .message("Login successful")
                .build();
    }
}
