package io.rooflet.service;

import io.rooflet.config.MarketListingConfigProperties;
import io.rooflet.model.entity.UserZipCodePreference;
import io.rooflet.model.response.AvailableZipCodeResponse;
import io.rooflet.repository.UserZipCodePreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserZipCodePreferenceService {

    private final UserZipCodePreferenceRepository preferenceRepository;
    private final MarketListingConfigProperties configProperties;

    /**
     * Add a zip code to user's watch list
     * Validates that:
     * - User hasn't exceeded the maximum allowed zip codes
     * - Zip code exists in both expected_rent and market_listings tables
     * - User isn't already watching this zip code
     */
    public UserZipCodePreference addZipCode(UUID userId, String zipCode) {
        log.debug("Adding zip code {} for user {}", zipCode, userId);

        // Check if user is already watching this zip code
        if (preferenceRepository.existsByUserIdAndZipCode(userId, zipCode)) {
            throw new IllegalArgumentException("You are already watching zip code: " + zipCode);
        }

        // Check if user has reached the maximum number of watched zip codes
        long currentCount = preferenceRepository.countByUserId(userId);
        if (currentCount >= configProperties.getMaxWatchedZipCodes()) {
            throw new IllegalArgumentException(
                    String.format("Maximum number of watched zip codes (%d) reached. Please remove a zip code before adding a new one.",
                            configProperties.getMaxWatchedZipCodes())
            );
        }

        // Validate that the zip code has both expected rent data and market listings
        Long validZipCodeCount = preferenceRepository.countValidZipCode(zipCode);
        if (validZipCodeCount == null || validZipCodeCount == 0) {
            throw new IllegalArgumentException(
                    "ZIP code '" + zipCode + "' is not available. It must have both expected rent data and active market listings."
            );
        }

        // Create and save the preference
        UserZipCodePreference preference = UserZipCodePreference.builder()
                .userId(userId)
                .zipCode(zipCode)
                .build();

        UserZipCodePreference saved = preferenceRepository.save(preference);
        log.info("User {} added zip code {} to watch list", userId, zipCode);
        return saved;
    }

    /**
     * Remove a zip code from user's watch list
     */
    public void removeZipCode(UUID userId, String zipCode) {
        log.debug("Removing zip code {} for user {}", zipCode, userId);

        if (!preferenceRepository.existsByUserIdAndZipCode(userId, zipCode)) {
            throw new IllegalArgumentException("You are not watching zip code: " + zipCode);
        }

        UserZipCodePreference.UserZipCodePreferenceId id =
                new UserZipCodePreference.UserZipCodePreferenceId(userId, zipCode);
        preferenceRepository.deleteById(id);
        log.info("User {} removed zip code {} from watch list", userId, zipCode);
    }

    /**
     * Get all zip codes a user is watching
     */
    @Transactional(readOnly = true)
    public List<UserZipCodePreference> getUserZipCodes(UUID userId) {
        log.debug("Fetching watched zip codes for user {}", userId);
        return preferenceRepository.findByUserId(userId);
    }

    /**
     * Get all valid zip codes with metadata (listing counts, expected rent availability, and watch status)
     */
    @Transactional(readOnly = true)
    public List<AvailableZipCodeResponse> getAvailableZipCodes(UUID userId) {
        log.debug("Fetching available zip codes for user {}", userId);

        // Get user's watched zip codes
        Set<String> watchedZipCodes = preferenceRepository.findByUserId(userId)
                .stream()
                .map(UserZipCodePreference::getZipCode)
                .collect(Collectors.toSet());

        // Get all valid zip codes with counts
        List<UserZipCodePreferenceRepository.ValidZipCodeProjection> validZipCodes =
                preferenceRepository.findValidZipCodes();

        return validZipCodes.stream()
                .map(projection -> AvailableZipCodeResponse.builder()
                        .zipCode(projection.getZipCode())
                        .listingCount(projection.getListingCount())
                        .expectedRentCount(projection.getExpectedRentCount())
                        .isWatched(watchedZipCodes.contains(projection.getZipCode()))
                        .build())
                .toList();
    }

    /**
     * Get the list of zip codes a user is watching (just the strings)
     */
    @Transactional(readOnly = true)
    public List<String> getUserWatchedZipCodesList(UUID userId) {
        log.debug("Fetching watched zip code list for user {}", userId);
        return preferenceRepository.findByUserId(userId)
                .stream()
                .map(UserZipCodePreference::getZipCode)
                .toList();
    }
}

