package io.rooflet.repository;

import io.rooflet.model.entity.MarketListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MarketListingRepository extends JpaRepository<MarketListing, UUID> {

    // Find by status
    List<MarketListing> findByListingStatus(String listingStatus);

    // Find by location
    @Query("SELECT m FROM MarketListing m WHERE m.city = :city AND m.state = :state")
    List<MarketListing> findByCityAndState(@Param("city") String city, @Param("state") String state);

    // Find by source
    List<MarketListing> findBySource(String source);


    // Find by price range
    @Query("SELECT m FROM MarketListing m WHERE m.price BETWEEN :minPrice AND :maxPrice")
    List<MarketListing> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice,
                                          @Param("maxPrice") java.math.BigDecimal maxPrice);

    // Find by bedrooms and bathrooms
    @Query("SELECT m FROM MarketListing m WHERE m.bedrooms >= :minBedrooms AND m.bathrooms >= :minBathrooms")
    List<MarketListing> findByBedroomsAndBathrooms(@Param("minBedrooms") Integer minBedrooms,
                                                     @Param("minBathrooms") java.math.BigDecimal minBathrooms);

    // Find by property type
    List<MarketListing> findByPropertyType(String propertyType);

    // Find by zip codes (for user preferences)
    List<MarketListing> findByZipCodeIn(List<String> zipCodes);
}

