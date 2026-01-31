package io.rooflet.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_market_listing_preferences")
@IdClass(UserMarketListingPreference.UserMarketListingPreferenceId.class)
@Schema(description = "User-specific preferences for market listings (interested flags)")
public class UserMarketListingPreference {

    @Id
    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    @Schema(description = "User ID", example = "11111111-1111-1111-1111-111111111111")
    private UUID userId;

    @Id
    @Column(name = "listing_id", columnDefinition = "CHAR(36)")
    @Schema(description = "Listing ID", example = "fe1bdb55-f73e-48f1-859d-25b60d169050")
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private MarketListing listing;

    @Column(nullable = false)
    @Builder.Default
    @Schema(description = "User marked as interested in this listing", example = "false")
    private Boolean isInterested = false;

    @Column(length = 2000)
    @Schema(description = "User notes about this listing", example = "Great location near subway")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    // Composite key class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserMarketListingPreferenceId implements Serializable {
        private UUID userId;
        private UUID listingId;
    }
}

