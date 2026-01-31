package io.rooflet.repository;

import io.rooflet.model.entity.UserMarketListingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserMarketListingListRepository extends JpaRepository<UserMarketListingList, UUID> {

    // Find all lists for a user
    List<UserMarketListingList> findByUserId(UUID userId);
}

