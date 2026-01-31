package io.rooflet.service;

import io.rooflet.model.entity.User;
import io.rooflet.model.request.LoginRequest;
import io.rooflet.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private static final String ACTIVE_PORTFOLIO_ID_KEY = "activePortfolioId";

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    /**
     * Authenticates a user with username and password
     *
     * @param loginRequest the login credentials
     * @return the authenticated User entity
     * @throws BadCredentialsException if credentials are invalid
     */
    public User authenticateUser(LoginRequest loginRequest) {
        try {
            // Create authentication token
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    );

            // Authenticate using Spring Security's AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Create security context and set authentication
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Save the security context to the session
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            HttpServletResponse response = attr.getResponse();
            securityContextRepository.saveContext(context, request, response);

            log.info("User '{}' authenticated successfully", loginRequest.getEmail());

            // Return the user entity
            return userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User email not found: " + loginRequest.getEmail()));

        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username or password", e);
        }
    }

    /**
     * Gets the currently authenticated user from the security context
     *
     * @return the authenticated User entity
     * @throws UsernameNotFoundException if user is not found or not authenticated
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UsernameNotFoundException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Gets the ID of the currently authenticated user
     *
     * @return the user ID
     */
    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Checks if the current user owns the specified property
     *
     * @param propertyOwnerId the owner ID to check
     * @return true if the current user owns the property
     */
    public boolean isCurrentUserOwner(UUID propertyOwnerId) {
        return getCurrentUserId().equals(propertyOwnerId);
    }

    /**
     * Gets the active portfolio ID from the session
     *
     * @return the active portfolio ID, or null if not set
     */
    public UUID getActivePortfolioId() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(false);
            if (session != null) {
                return (UUID) session.getAttribute(ACTIVE_PORTFOLIO_ID_KEY);
            }
            return null;
        } catch (IllegalStateException e) {
            log.warn("Unable to get session for active portfolio", e);
            return null;
        }
    }

    /**
     * Sets the active portfolio ID in the session
     *
     * @param portfolioId the portfolio ID to set as active
     */
    public void setActivePortfolioId(UUID portfolioId) {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);
            session.setAttribute(ACTIVE_PORTFOLIO_ID_KEY, portfolioId);
            log.info("Set active portfolio to {} for user {}", portfolioId, getCurrentUserId());
        } catch (IllegalStateException e) {
            log.error("Unable to set active portfolio in session", e);
            throw new RuntimeException("Unable to set active portfolio", e);
        }
    }

    /**
     * Gets the active portfolio ID, throwing an exception if not set
     *
     * @return the active portfolio ID
     * @throws IllegalStateException if no active portfolio is set
     */
    public UUID requireActivePortfolioId() {
        UUID portfolioId = getActivePortfolioId();
        if (portfolioId == null) {
            throw new IllegalStateException("No active portfolio set in session");
        }
        return portfolioId;
    }

    /**
     * Clears the active portfolio from the session
     */
    public void clearActivePortfolio() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(false);
            if (session != null) {
                session.removeAttribute(ACTIVE_PORTFOLIO_ID_KEY);
                log.info("Cleared active portfolio for user {}", getCurrentUserId());
            }
        } catch (IllegalStateException e) {
            log.warn("Unable to clear active portfolio from session", e);
        }
    }
}
