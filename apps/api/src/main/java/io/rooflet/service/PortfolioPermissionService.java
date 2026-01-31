package io.rooflet.service;

import io.rooflet.model.entity.PortfolioMember;
import io.rooflet.model.enums.PortfolioRole;
import io.rooflet.repository.PortfolioMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PortfolioPermissionService {

    private final PortfolioMemberRepository portfolioMemberRepository;

    /**
     * Check if user has view permission for a portfolio
     * All roles (OWNER, MANAGER, VIEWER) have view permission
     */
    public boolean hasViewPermission(UUID userId, UUID portfolioId) {
        Optional<PortfolioMember> member = portfolioMemberRepository
                .findByPortfolioIdAndUserId(portfolioId, userId);
        return member.isPresent();
    }

    /**
     * Check if user has manage permission for a portfolio
     * OWNER and MANAGER roles have manage permission
     */
    public boolean hasManagePermission(UUID userId, UUID portfolioId) {
        Optional<PortfolioMember> member = portfolioMemberRepository
                .findByPortfolioIdAndUserId(portfolioId, userId);

        if (member.isEmpty()) {
            return false;
        }

        PortfolioRole role = member.get().getRole();
        return role == PortfolioRole.OWNER || role == PortfolioRole.MANAGER;
    }

    /**
     * Check if user has owner permission for a portfolio
     * Only OWNER role has owner permission
     */
    public boolean hasOwnerPermission(UUID userId, UUID portfolioId) {
        Optional<PortfolioMember> member = portfolioMemberRepository
                .findByPortfolioIdAndUserId(portfolioId, userId);

        if (member.isEmpty()) {
            return false;
        }

        return member.get().getRole() == PortfolioRole.OWNER;
    }

    /**
     * Get user's role in a portfolio
     */
    public Optional<PortfolioRole> getUserRole(UUID userId, UUID portfolioId) {
        return portfolioMemberRepository
                .findByPortfolioIdAndUserId(portfolioId, userId)
                .map(PortfolioMember::getRole);
    }

    /**
     * Verify user has view permission or throw exception
     */
    public void requireViewPermission(UUID userId, UUID portfolioId) {
        if (!hasViewPermission(userId, portfolioId)) {
            throw new SecurityException("User does not have view permission for portfolio " + portfolioId);
        }
    }

    /**
     * Verify user has manage permission or throw exception
     */
    public void requireManagePermission(UUID userId, UUID portfolioId) {
        if (!hasManagePermission(userId, portfolioId)) {
            throw new SecurityException("User does not have manage permission for portfolio " + portfolioId);
        }
    }

    /**
     * Verify user has owner permission or throw exception
     */
    public void requireOwnerPermission(UUID userId, UUID portfolioId) {
        if (!hasOwnerPermission(userId, portfolioId)) {
            throw new SecurityException("User does not have owner permission for portfolio " + portfolioId);
        }
    }
}

