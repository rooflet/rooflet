package io.rooflet.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_market_listing_lists")
@Schema(description = "Custom lists for organizing market listings by user")
public class UserMarketListingList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "List ID", example = "ffffffff-1111-1111-1111-111111111111")
    private UUID id;

    @NotNull
    @Column(nullable = false, columnDefinition = "CHAR(36)")
    @Schema(description = "User ID who owns this list", example = "11111111-1111-1111-1111-111111111111")
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    @NotBlank
    @Column(nullable = false, length = 255)
    @Schema(description = "List name", example = "Multi-Family Targets")
    private String name;

    @Column(length = 1000)
    @Schema(description = "List description", example = "Properties with multiple units for rental income")
    private String description;

    @Column(length = 20)
    @Schema(description = "Display color (hex code or named color)", example = "#4CAF50")
    private String color;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

