package io.rooflet.model.response;

import io.rooflet.model.entity.PortfolioMember;
import io.rooflet.model.enums.PortfolioRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Portfolio member response containing member information")
public class PortfolioMemberResponse {

    @Schema(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Portfolio ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID portfolioId;

    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @Schema(description = "User email", example = "john@example.com")
    private String userEmail;

    @Schema(description = "User full name", example = "John Doe")
    private String userFullName;

    @Schema(description = "User's role in the portfolio", example = "MANAGER")
    private PortfolioRole role;

    @Schema(description = "Membership creation timestamp")
    private LocalDateTime createdAt;

    public static PortfolioMemberResponse from(PortfolioMember member) {
        return PortfolioMemberResponse.builder()
                .id(member.getId())
                .portfolioId(member.getPortfolio().getId())
                .userId(member.getUser().getId())
                .userEmail(member.getUser().getEmail())
                .userFullName(member.getUser().getFullName())
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .build();
    }
}

