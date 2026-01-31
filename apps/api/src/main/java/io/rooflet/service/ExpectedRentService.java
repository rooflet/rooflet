package io.rooflet.service;

import io.rooflet.model.entity.ExpectedRent;
import io.rooflet.repository.ExpectedRentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ExpectedRentService {

    private final ExpectedRentRepository expectedRentRepository;

    /**
     * Get expected rent by ID
     */
    public ExpectedRent findById(UUID id) {
        log.debug("Finding expected rent by ID: {}", id);
        return expectedRentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expected rent not found with ID: " + id));
    }

    /**
     * Get expected rent by zip code and bedrooms (latest effective date)
     */
    public Optional<ExpectedRent> getExpectedRentByZipAndBedrooms(String zipCode, Integer bedrooms) {
        log.debug("Finding expected rent for zip code: {} and bedrooms: {}", zipCode, bedrooms);
        return expectedRentRepository.findByZipCodeAndBedrooms(zipCode, bedrooms);
    }

    /**
     * Get expected rent by zip code and bedrooms for a specific effective date
     */
    public Optional<ExpectedRent> getExpectedRentByZipAndBedroomsAndDate(
            String zipCode, Integer bedrooms, LocalDate effectiveDate) {
        log.debug("Finding expected rent for zip code: {}, bedrooms: {}, effective date: {}",
                zipCode, bedrooms, effectiveDate);
        return expectedRentRepository.findByZipCodeAndBedroomsAndEffectiveDate(zipCode, bedrooms, effectiveDate);
    }

    /**
     * Get all expected rents for a specific zip code (all bedroom counts)
     */
    public List<ExpectedRent> getExpectedRentsByZipCode(String zipCode) {
        log.debug("Finding all expected rents for zip code: {}", zipCode);
        return expectedRentRepository.findByZipCode(zipCode);
    }

    /**
     * Get the latest expected rents for a specific zip code (most recent effective date for each bedroom count)
     */
    public List<ExpectedRent> getLatestExpectedRentsByZipCode(String zipCode) {
        log.debug("Finding latest expected rents for zip code: {}", zipCode);
        return expectedRentRepository.findLatestByZipCode(zipCode);
    }
}

