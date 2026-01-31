package io.rooflet.repository;

import io.rooflet.model.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {

    @Query(value = "SELECT DISTINCT p.* FROM portfolios p " +
            "JOIN portfolio_members pm ON p.id = pm.portfolio_id " +
            "WHERE pm.user_id = :userUuid", nativeQuery = true)
    List<Portfolio> findByUserId(@Param("userUuid") UUID userUuid);

    @Query(value = "SELECT DISTINCT p.* FROM portfolios p " +
            "JOIN portfolio_members pm ON p.id = pm.portfolio_id " +
            "WHERE pm.user_id = :userUuid AND (p.archived IS NULL OR p.archived = FALSE)",
            nativeQuery = true)
    List<Portfolio> findActiveByUserId(@Param("userUuid") UUID userUuid);
}

