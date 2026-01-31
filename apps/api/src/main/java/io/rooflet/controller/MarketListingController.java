package io.rooflet.controller;

import io.rooflet.model.entity.MarketListing;
import io.rooflet.model.entity.UserMarketListingPreference;
import io.rooflet.model.response.MarketListingResponse;
import io.rooflet.model.response.MarketListingWithPreferenceResponse;
import io.rooflet.service.AuthenticationService;
import io.rooflet.service.MarketListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/market-listings")
@RequiredArgsConstructor
@Validated
@Tag(name = "Market Listings", description = "API for managing property market listings from various sources")
public class MarketListingController {

    private final MarketListingService marketListingService;
    private final AuthenticationService authenticationService;

    @Operation(summary = "Get all market listings",
            description = "Retrieves market listings from the current user's watched zip codes. " +
                    "Returns empty list if user has no watched zip codes configured.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Market listings retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MarketListingResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<MarketListingResponse>> getAllMarketListings() {
        UUID userId = authenticationService.getCurrentUserId();
        List<MarketListing> listings = marketListingService.getMarketListingsForUser(userId);
        List<MarketListingResponse> responses = listings.stream()
                .map(MarketListingResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get market listing by ID", description = "Retrieves a specific market listing by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Market listing found successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MarketListingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Market listing not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<MarketListingResponse> getMarketListingById(
            @Parameter(description = "Market listing ID", example = "fe1bdb55-f73e-48f1-859d-25b60d169050")
            @PathVariable UUID id) {
        try {
            MarketListing listing = marketListingService.findById(id);
            return ResponseEntity.ok(MarketListingResponse.from(listing));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


    // Interested operations
    @Operation(summary = "Toggle interested status", description = "Toggles the interested flag for a market listing for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interested status toggled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MarketListingWithPreferenceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Market listing not found")
    })
    @PostMapping("/{id}/interested/toggle")
    public ResponseEntity<MarketListingWithPreferenceResponse> toggleInterested(
            @Parameter(description = "Market listing ID", example = "fe1bdb55-f73e-48f1-859d-25b60d169050")
            @PathVariable UUID id) {
        try {
            UUID userId = authenticationService.getCurrentUserId();
            MarketListing listing = marketListingService.findById(id);
            UserMarketListingPreference preference = marketListingService.toggleInterested(userId, id);
            return ResponseEntity.ok(MarketListingWithPreferenceResponse.from(listing, preference));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Set interested status", description = "Sets the interested flag for a market listing to a specific value for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interested status set successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MarketListingWithPreferenceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Market listing not found")
    })
    @PutMapping("/{id}/interested")
    public ResponseEntity<MarketListingWithPreferenceResponse> setInterested(
            @Parameter(description = "Market listing ID", example = "fe1bdb55-f73e-48f1-859d-25b60d169050")
            @PathVariable UUID id,
            @Parameter(description = "Interested status", example = "true")
            @RequestParam boolean isInterested) {
        try {
            UUID userId = authenticationService.getCurrentUserId();
            MarketListing listing = marketListingService.findById(id);
            UserMarketListingPreference preference = marketListingService.setInterested(userId, id, isInterested);
            return ResponseEntity.ok(MarketListingWithPreferenceResponse.from(listing, preference));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get all interested listings", description = "Retrieves all market listings marked as interested by the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interested listings retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MarketListingWithPreferenceResponse.class)))
    })
    @GetMapping("/interested")
    public ResponseEntity<List<MarketListingWithPreferenceResponse>> getInterested() {
        UUID userId = authenticationService.getCurrentUserId();
        List<UserMarketListingPreference> preferences = marketListingService.getInterested(userId);
        List<MarketListingWithPreferenceResponse> responses = preferences.stream()
                .map(pref -> {
                    MarketListing listing = marketListingService.findById(pref.getListingId());
                    return MarketListingWithPreferenceResponse.from(listing, pref);
                })
                .toList();
        return ResponseEntity.ok(responses);
    }

    // Filter operations
    @Operation(summary = "Get listings by status", description = "Retrieves all market listings with a specific status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listings retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MarketListingResponse.class)))
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MarketListingResponse>> getListingsByStatus(
            @Parameter(description = "Listing status", example = "Active")
            @PathVariable String status) {
        List<MarketListing> listings = marketListingService.findByStatus(status);
        List<MarketListingResponse> responses = listings.stream()
                .map(MarketListingResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get listings by location", description = "Retrieves all market listings in a specific city and state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listings retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MarketListingResponse.class)))
    })
    @GetMapping("/location")
    public ResponseEntity<List<MarketListingResponse>> getListingsByLocation(
            @Parameter(description = "City", example = "Quincy")
            @RequestParam String city,
            @Parameter(description = "State abbreviation", example = "MA")
            @RequestParam String state) {
        List<MarketListing> listings = marketListingService.findByCityAndState(city, state);
        List<MarketListingResponse> responses = listings.stream()
                .map(MarketListingResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get listings by source", description = "Retrieves all market listings from a specific source")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listings retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MarketListingResponse.class)))
    })
    @GetMapping("/source/{source}")
    public ResponseEntity<List<MarketListingResponse>> getListingsBySource(
            @Parameter(description = "Listing source", example = "REDFIN")
            @PathVariable String source) {
        List<MarketListing> listings = marketListingService.findBySource(source);
        List<MarketListingResponse> responses = listings.stream()
                .map(MarketListingResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get listings by price range", description = "Retrieves all market listings within a specific price range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listings retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MarketListingResponse.class)))
    })
    @GetMapping("/price-range")
    public ResponseEntity<List<MarketListingResponse>> getListingsByPriceRange(
            @Parameter(description = "Minimum price", example = "200000")
            @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price", example = "500000")
            @RequestParam BigDecimal maxPrice) {
        List<MarketListing> listings = marketListingService.findByPriceRange(minPrice, maxPrice);
        List<MarketListingResponse> responses = listings.stream()
                .map(MarketListingResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get listings by bedrooms and bathrooms", description = "Retrieves all market listings with minimum bedrooms and bathrooms")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listings retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MarketListingResponse.class)))
    })
    @GetMapping("/beds-baths")
    public ResponseEntity<List<MarketListingResponse>> getListingsByBedsAndBaths(
            @Parameter(description = "Minimum bedrooms", example = "2")
            @RequestParam Integer minBedrooms,
            @Parameter(description = "Minimum bathrooms", example = "1.0")
            @RequestParam BigDecimal minBathrooms) {
        List<MarketListing> listings = marketListingService.findByBedroomsAndBathrooms(minBedrooms, minBathrooms);
        List<MarketListingResponse> responses = listings.stream()
                .map(MarketListingResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get listings by property type", description = "Retrieves all market listings of a specific property type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listings retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MarketListingResponse.class)))
    })
    @GetMapping("/property-type/{propertyType}")
    public ResponseEntity<List<MarketListingResponse>> getListingsByPropertyType(
            @Parameter(description = "Property type", example = "Condo/Co-op")
            @PathVariable String propertyType) {
        List<MarketListing> listings = marketListingService.findByPropertyType(propertyType);
        List<MarketListingResponse> responses = listings.stream()
                .map(MarketListingResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}

