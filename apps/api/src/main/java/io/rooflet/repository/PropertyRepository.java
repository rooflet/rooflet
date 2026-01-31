package io.rooflet.repository;

import io.rooflet.model.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {
    // Using owner_id navigation for JPA query derivation
    List<Property> findByOwner_Id(UUID ownerId);

    @Query(value = "SELECT * FROM properties WHERE user_id = :userId AND (archived IS NULL OR archived = FALSE)", nativeQuery = true)
    List<Property> findByUserIdAndArchivedFalse(@Param("userId") UUID userId);

    List<Property> findByPortfolioId(UUID portfolioId);

    @Query(value = "SELECT * FROM properties WHERE portfolio_id = :portfolioId AND (archived IS NULL OR archived = FALSE)", nativeQuery = true)
    List<Property> findByPortfolioIdAndArchivedFalse(@Param("portfolioId") UUID portfolioId);
}
