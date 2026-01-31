package io.rooflet.repository;

import io.rooflet.model.entity.UserZipCodePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserZipCodePreferenceRepository extends JpaRepository<UserZipCodePreference, UserZipCodePreference.UserZipCodePreferenceId> {

    /**
     * Find all zip code preferences for a specific user
     */
    List<UserZipCodePreference> findByUserId(UUID userId);

    /**
     * Count how many zip codes a user is watching
     */
    long countByUserId(UUID userId);

    /**
     * Check if a user is already watching a specific zip code
     */
    boolean existsByUserIdAndZipCode(UUID userId, String zipCode);

    /**
     * Find all valid zip codes that have both expected rent data AND market listings
     * Returns distinct zip codes with counts
     */
    @Query(value = """
            SELECT DISTINCT ml.zip_code as zipCode,
                   COUNT(DISTINCT ml.id) as listingCount,
                   COUNT(DISTINCT er.id) as expectedRentCount
            FROM market_listings ml
            INNER JOIN expected_rent er ON ml.zip_code = er.zip_code
            GROUP BY ml.zip_code
            ORDER BY ml.zip_code
            """, nativeQuery = true)
    List<ValidZipCodeProjection> findValidZipCodes();

    /**
     * Check if a zip code is valid (has both expected rent data and market listings)
     * Returns the count of matching records
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM market_listings ml
            INNER JOIN expected_rent er ON ml.zip_code = er.zip_code
            WHERE ml.zip_code = :zipCode
            """, nativeQuery = true)
    Long countValidZipCode(@Param("zipCode") String zipCode);

    /**
     * Projection interface for valid zip code data
     */
    interface ValidZipCodeProjection {
        String getZipCode();
        Long getListingCount();
        Long getExpectedRentCount();
    }
}

