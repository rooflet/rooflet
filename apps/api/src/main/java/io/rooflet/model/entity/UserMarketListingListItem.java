package io.rooflet.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_market_listing_list_items")
@IdClass(UserMarketListingListItem.UserMarketListingListItemId.class)
@Schema(description = "Join table associating market listings with user custom lists")
public class UserMarketListingListItem {

    @Id
    @Column(name = "list_id", columnDefinition = "CHAR(36)")
    @Schema(description = "List ID", example = "ffffffff-1111-1111-1111-111111111111")
    private UUID listId;

    @Id
    @Column(name = "listing_id", columnDefinition = "CHAR(36)")
    @Schema(description = "Listing ID", example = "fe1bdb55-f73e-48f1-859d-25b60d169050")
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", insertable = false, updatable = false)
    private UserMarketListingList list;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private MarketListing listing;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp when listing was added to the list")
    private LocalDateTime addedAt;

    // Composite key class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserMarketListingListItemId implements Serializable {
        private UUID listId;
        private UUID listingId;
    }
}

