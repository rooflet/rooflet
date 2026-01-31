package io.rooflet.repository;

import io.rooflet.model.entity.PortfolioMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioMemberRepository extends JpaRepository<PortfolioMember, UUID> {

    List<PortfolioMember> findByPortfolioId(UUID portfolioId);

    List<PortfolioMember> findByUserId(UUID userId);

    Optional<PortfolioMember> findByPortfolioIdAndUserId(UUID portfolioId, UUID userId);

    long countByPortfolioIdAndRole(UUID portfolioId, String role);

    void deleteByPortfolioIdAndUserId(UUID portfolioId, UUID userId);
}

