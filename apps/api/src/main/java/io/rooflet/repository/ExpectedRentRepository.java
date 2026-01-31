package io.rooflet.repository;

import io.rooflet.model.entity.ExpectedRent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpectedRentRepository extends JpaRepository<ExpectedRent, UUID> {

    /**
     * Find expected rent by zip code and bedrooms for the most recent effective date
     */
    @Query(value = "SELECT * FROM expected_rent WHERE zip_code = :zipCode AND bedrooms = :bedrooms " +
            "ORDER BY effective_date DESC LIMIT 1", nativeQuery = true)
    Optional<ExpectedRent> findByZipCodeAndBedrooms(
            @Param("zipCode") String zipCode,
            @Param("bedrooms") Integer bedrooms);

    /**
     * Find expected rent by zip code and bedrooms for a specific effective date
     */
    @Query(value = "SELECT * FROM expected_rent WHERE zip_code = :zipCode AND bedrooms = :bedrooms " +
            "AND effective_date = :effectiveDate", nativeQuery = true)
    Optional<ExpectedRent> findByZipCodeAndBedroomsAndEffectiveDate(
            @Param("zipCode") String zipCode,
            @Param("bedrooms") Integer bedrooms,
            @Param("effectiveDate") LocalDate effectiveDate);

    /**
     * Find all expected rents for a specific zip code (all bedroom counts)
     */
    @Query(value = "SELECT * FROM expected_rent WHERE zip_code = :zipCode " +
            "ORDER BY bedrooms ASC, effective_date DESC", nativeQuery = true)
    List<ExpectedRent> findByZipCode(@Param("zipCode") String zipCode);

    /**
     * Find all expected rents for a specific zip code for the most recent effective date
     */
    @Query(value = "SELECT DISTINCT ON (bedrooms) * FROM expected_rent WHERE zip_code = :zipCode " +
            "ORDER BY bedrooms ASC, effective_date DESC", nativeQuery = true)
    List<ExpectedRent> findLatestByZipCode(@Param("zipCode") String zipCode);
}

