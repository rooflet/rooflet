package io.rooflet.model.response;

import io.rooflet.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "User response containing user information")
public class UserResponse {
    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Account creation timestamp", example = "2023-10-12T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2023-10-12T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Active portfolio ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID activePortfolioId;

    @Schema(description = "Active portfolio name", example = "Mom's Properties")
    private String activePortfolioName;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static UserResponse from(User user, UUID activePortfolioId, String activePortfolioName) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .activePortfolioId(activePortfolioId)
                .activePortfolioName(activePortfolioName)
                .build();
    }
}
