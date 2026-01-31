package io.rooflet.repository;

import io.rooflet.model.entity.UserMarketListingPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserMarketListingPreferenceRepository extends JpaRepository<UserMarketListingPreference, UserMarketListingPreference.UserMarketListingPreferenceId> {

    // Find preference by user and listing
    Optional<UserMarketListingPreference> findByUserIdAndListingId(UUID userId, UUID listingId);

    // Find all interested listings for a user
    @Query("SELECT p FROM UserMarketListingPreference p WHERE p.userId = :userId AND p.isInterested = true")
    List<UserMarketListingPreference> findInterestedByUserId(@Param("userId") UUID userId);
}

