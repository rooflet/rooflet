package io.rooflet.service;

import io.rooflet.model.entity.Portfolio;
import io.rooflet.model.entity.PortfolioMember;
import io.rooflet.model.entity.User;
import io.rooflet.model.enums.PortfolioRole;
import io.rooflet.repository.PortfolioMemberRepository;
import io.rooflet.repository.PortfolioRepository;
import io.rooflet.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioMemberRepository portfolioMemberRepository;
    private final PropertyRepository propertyRepository;
    private final AuthenticationService authenticationService;
    private final PortfolioPermissionService permissionService;

    /**
     * Create a new portfolio
     * The creating user automatically becomes OWNER
     */
    public Portfolio createPortfolio(String name, String description) {
        User currentUser = authenticationService.getCurrentUser();

        Portfolio portfolio = Portfolio.builder()
                .name(name)
                .description(description)
                .archived(false)
                .build();

        portfolio = portfolioRepository.save(portfolio);

        // Add creator as OWNER
        PortfolioMember member = PortfolioMember.builder()
                .portfolio(portfolio)
                .user(currentUser)
                .role(PortfolioRole.OWNER)
                .build();

        portfolioMemberRepository.save(member);

        log.info("Created portfolio {} for user {}", portfolio.getId(), currentUser.getId());

        return portfolio;
    }

    /**
     * Get all portfolios accessible to the current user
     */
    @Transactional(readOnly = true)
    public List<Portfolio> getAccessiblePortfolios() {
        UUID userId = authenticationService.getCurrentUserId();
        return portfolioRepository.findByUserId(userId);
    }

    /**
     * Get all active (non-archived) portfolios accessible to the current user
     */
    @Transactional(readOnly = true)
    public List<Portfolio> getActivePortfolios() {
        UUID userId = authenticationService.getCurrentUserId();
        return portfolioRepository.findActiveByUserId(userId);
    }

    /**
     * Get a specific portfolio by ID
     * Requires view permission
     */
    @Transactional(readOnly = true)
    public Portfolio getPortfolio(UUID portfolioId) {
        UUID userId = authenticationService.getCurrentUserId();
        permissionService.requireViewPermission(userId, portfolioId);

        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));
    }

    /**
     * Update portfolio details
     * Requires owner permission
     */
    public Portfolio updatePortfolio(UUID portfolioId, String name, String description) {
        UUID userId = authenticationService.getCurrentUserId();
        permissionService.requireOwnerPermission(userId, portfolioId);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));

        if (name != null) {
            portfolio.setName(name);
        }
        if (description != null) {
            portfolio.setDescription(description);
        }

        return portfolioRepository.save(portfolio);
    }

    /**
     * Archive a portfolio
     * Requires owner permission
     */
    public Portfolio archivePortfolio(UUID portfolioId) {
        UUID userId = authenticationService.getCurrentUserId();
        permissionService.requireOwnerPermission(userId, portfolioId);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));

        // Validate that the user has more than one portfolio
        List<Portfolio> allUserPortfolios = portfolioRepository.findByUserId(userId);
        if (allUserPortfolios.size() <= 1) {
            throw new IllegalStateException("Cannot archive the only portfolio. Users must have at least one portfolio.");
        }

        portfolio.setArchived(true);
        portfolio = portfolioRepository.save(portfolio);

        // Clear active portfolio if this was the active one
        UUID activePortfolioId = authenticationService.getActivePortfolioId();
        if (portfolioId.equals(activePortfolioId)) {
            authenticationService.clearActivePortfolio();
        }

        log.info("Archived portfolio {} by user {}", portfolioId, userId);

        return portfolio;
    }

    /**
     * Unarchive a portfolio
     * Requires owner permission
     */
    public Portfolio unarchivePortfolio(UUID portfolioId) {
        UUID userId = authenticationService.getCurrentUserId();
        permissionService.requireOwnerPermission(userId, portfolioId);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));

        portfolio.setArchived(false);
        portfolio = portfolioRepository.save(portfolio);

        log.info("Unarchived portfolio {} by user {}", portfolioId, userId);

        return portfolio;
    }

    /**
     * Switch to a different portfolio
     * Requires view permission on target portfolio
     */
    public void switchPortfolio(UUID portfolioId) {
        UUID userId = authenticationService.getCurrentUserId();

        // Verify user has access to this portfolio
        permissionService.requireViewPermission(userId, portfolioId);

        // Verify portfolio exists and is not archived
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));

        if (Boolean.TRUE.equals(portfolio.getArchived())) {
            throw new IllegalArgumentException("Cannot switch to archived portfolio");
        }

        authenticationService.setActivePortfolioId(portfolioId);
        log.info("User {} switched to portfolio {}", userId, portfolioId);
    }

    /**
     * Ensure user has an active portfolio set
     * Auto-selects first available active portfolio if none is set
     */
    public UUID ensureActivePortfolio() {
        UUID activePortfolioId = authenticationService.getActivePortfolioId();

        if (activePortfolioId != null) {
            // Verify it still exists and user has access
            try {
                UUID userId = authenticationService.getCurrentUserId();
                if (permissionService.hasViewPermission(userId, activePortfolioId)) {
                    Portfolio portfolio = portfolioRepository.findById(activePortfolioId).orElse(null);
                    if (portfolio != null && !Boolean.TRUE.equals(portfolio.getArchived())) {
                        return activePortfolioId;
                    }
                }
            } catch (Exception e) {
                log.warn("Active portfolio validation failed", e);
            }
        }

        // No active portfolio or invalid - select first available
        List<Portfolio> activePortfolios = getActivePortfolios();
        if (activePortfolios.isEmpty()) {
            // No portfolios exist - create a default one
            log.info("User {} has no portfolios, creating default portfolio", authenticationService.getCurrentUserId());
            Portfolio defaultPortfolio = createPortfolio("My Portfolio", "Default portfolio");
            authenticationService.setActivePortfolioId(defaultPortfolio.getId());
            return defaultPortfolio.getId();
        }

        Portfolio firstPortfolio = activePortfolios.getFirst();
        authenticationService.setActivePortfolioId(firstPortfolio.getId());
        log.info("Auto-selected portfolio {} for user {}",
                firstPortfolio.getId(), authenticationService.getCurrentUserId());

        return firstPortfolio.getId();
    }

    /**
     * Add a member to a portfolio
     * Requires owner permission
     */
    public PortfolioMember addMember(UUID portfolioId, User user, PortfolioRole role) {
        UUID userId = authenticationService.getCurrentUserId();
        permissionService.requireOwnerPermission(userId, portfolioId);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));

        // Check if member already exists
        if (portfolioMemberRepository.findByPortfolioIdAndUserId(portfolioId, user.getId()).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this portfolio");
        }

        PortfolioMember member = PortfolioMember.builder()
                .portfolio(portfolio)
                .user(user)
                .role(role)
                .build();

        member = portfolioMemberRepository.save(member);

        log.info("Added user {} as {} to portfolio {}", user.getId(), role, portfolioId);

        return member;
    }

    /**
     * Update a member's role in a portfolio
     * Requires owner permission
     */
    public PortfolioMember updateMemberRole(UUID portfolioId, UUID memberUserId, PortfolioRole newRole) {
        UUID userId = authenticationService.getCurrentUserId();
        permissionService.requireOwnerPermission(userId, portfolioId);

        PortfolioMember member = portfolioMemberRepository.findByPortfolioIdAndUserId(portfolioId, memberUserId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found in portfolio"));

        // Prevent removing the last owner
        if (member.getRole() == PortfolioRole.OWNER && newRole != PortfolioRole.OWNER) {
            long ownerCount = portfolioMemberRepository.countByPortfolioIdAndRole(portfolioId, "OWNER");
            if (ownerCount <= 1) {
                throw new IllegalStateException("Cannot change role of last owner");
            }
        }

        member.setRole(newRole);
        member = portfolioMemberRepository.save(member);

        log.info("Updated user {} role to {} in portfolio {}", memberUserId, newRole, portfolioId);

        return member;
    }

    /**
     * Remove a member from a portfolio
     * Requires owner permission
     */
    @Transactional
    public void removeMember(UUID portfolioId, UUID memberUserId) {
        UUID userId = authenticationService.getCurrentUserId();
        permissionService.requireOwnerPermission(userId, portfolioId);

        PortfolioMember member = portfolioMemberRepository.findByPortfolioIdAndUserId(portfolioId, memberUserId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found in portfolio"));

        // Prevent removing the last owner
        if (member.getRole() == PortfolioRole.OWNER) {
            long ownerCount = portfolioMemberRepository.countByPortfolioIdAndRole(portfolioId, "OWNER");
            if (ownerCount <= 1) {
                throw new IllegalStateException("Cannot remove the last owner");
            }
        }

        portfolioMemberRepository.delete(member);

        log.info("Removed user {} from portfolio {}", memberUserId, portfolioId);
    }

    /**
     * Get all members of a portfolio
     * Requires view permission
     */
    @Transactional(readOnly = true)
    public List<PortfolioMember> getPortfolioMembers(UUID portfolioId) {
        UUID userId = authenticationService.getCurrentUserId();
        permissionService.requireViewPermission(userId, portfolioId);

        return portfolioMemberRepository.findByPortfolioId(portfolioId);
    }
}

