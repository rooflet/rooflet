package io.rooflet.repository;

import io.rooflet.model.RentCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RentCollectionRepository extends JpaRepository<RentCollection, UUID> {

    List<RentCollection> findByProperty_Id(UUID propertyId);

    List<RentCollection> findByTenant_Id(UUID tenantId);

    List<RentCollection> findByProperty_IdAndTenant_Id(UUID propertyId, UUID tenantId);

    @Query(value = "SELECT rc.* FROM rent_collections rc JOIN properties p ON rc.property_id = p.id WHERE p.portfolio_id = :portfolioUuid", nativeQuery = true)
    List<RentCollection> findByPortfolioId(@Param("portfolioUuid") UUID portfolioUuid);

    @Query(value = "SELECT rc.* FROM rent_collections rc JOIN properties p ON rc.property_id = p.id WHERE p.portfolio_id = :portfolioUuid AND rc.payment_date BETWEEN :startPeriod AND :endPeriod", nativeQuery = true)
    List<RentCollection> findRentCollectionsByPortfolioAndPeriod(@Param("portfolioUuid") UUID portfolioUuid,
                                                                 @Param("startPeriod") LocalDate startPeriod,
                                                                 @Param("endPeriod") LocalDate endPeriod);
}
