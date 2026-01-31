package io.rooflet.model.entity;

import io.rooflet.model.enums.PortfolioRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "portfolio_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "user_id"}))
@Schema(description = "Portfolio member entity representing user access to a portfolio")
public class PortfolioMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Portfolio member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @NotNull
    @Schema(description = "Associated portfolio")
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    @Schema(description = "Associated user")
    private User user;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 20)
    @Schema(description = "User's role in the portfolio")
    private PortfolioRole role;

    @CreationTimestamp
    @Schema(description = "Membership creation timestamp")
    private LocalDateTime createdAt;
}

