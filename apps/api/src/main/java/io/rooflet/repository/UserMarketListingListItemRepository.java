package io.rooflet.repository;

import io.rooflet.model.entity.UserMarketListingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserMarketListingListItemRepository extends JpaRepository<UserMarketListingListItem, UserMarketListingListItem.UserMarketListingListItemId> {

    // Find all items in a list
    List<UserMarketListingListItem> findByListId(UUID listId);

    // Find specific item in a list
    Optional<UserMarketListingListItem> findByListIdAndListingId(UUID listId, UUID listingId);

    // Find all lists containing a specific listing
    List<UserMarketListingListItem> findByListingId(UUID listingId);

    // Delete item from list
    void deleteByListIdAndListingId(UUID listId, UUID listingId);

    // Count items in a list
    @Query("SELECT COUNT(i) FROM UserMarketListingListItem i WHERE i.listId = :listId")
    long countByListId(@Param("listId") UUID listId);
}

