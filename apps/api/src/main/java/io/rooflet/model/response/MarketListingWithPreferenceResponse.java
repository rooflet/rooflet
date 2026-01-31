package io.rooflet.model.response;

import io.rooflet.model.entity.MarketListing;
import io.rooflet.model.entity.UserMarketListingPreference;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing market listing details with user-specific preferences")
public class MarketListingWithPreferenceResponse {

    @Schema(description = "Market listing ID", example = "fe1bdb55-f73e-48f1-859d-25b60d169050")
    private UUID id;

    @Schema(description = "Listing source", example = "REDFIN")
    private String source;

    @Schema(description = "External listing ID from the source", example = "73463500")
    private String sourceListingId;

    @Schema(description = "URL to the listing on the source website")
    private String sourceUrl;

    @Schema(description = "MLS listing number if available", example = "73463500")
    private String sourceMlsNumber;

    @Schema(description = "Sale type", example = "MLS Listing")
    private String saleType;

    @Schema(description = "Current listing status", example = "Active")
    private String listingStatus;

    @Schema(description = "Date property was sold", example = "2025-12-27")
    private LocalDate soldDate;

    @Schema(description = "Date property was listed", example = "2025-12-27")
    private LocalDate listDate;

    @Schema(description = "Number of days on market", example = "5")
    private Integer daysOnMarket;

    @Schema(description = "Full address as provided by source", example = "23 Myrtle St")
    private String address;

    @Schema(description = "Parsed primary address", example = "23 Myrtle St")
    private String address1;

    @Schema(description = "Parsed secondary address (unit, apt, etc.)", example = "#204")
    private String address2;

    @Schema(description = "City", example = "Quincy")
    private String city;

    @Schema(description = "State abbreviation", example = "MA")
    private String state;

    @Schema(description = "ZIP code", example = "02171")
    private String zipCode;

    @Schema(description = "Neighborhood or area name", example = "North Quincy")
    private String location;

    @Schema(description = "Geographic latitude", example = "42.2796206")
    private BigDecimal latitude;

    @Schema(description = "Geographic longitude", example = "-71.0347665")
    private BigDecimal longitude;

    @Schema(description = "Type of property", example = "Multi-Family (2-4 Unit)")
    private String propertyType;

    @Schema(description = "Number of bedrooms", example = "6")
    private Integer bedrooms;

    @Schema(description = "Number of bathrooms", example = "2.0")
    private BigDecimal bathrooms;

    @Schema(description = "Interior square footage", example = "2016")
    private Integer squareFeet;

    @Schema(description = "Lot size in square feet", example = "4785")
    private Integer lotSize;

    @Schema(description = "Year property was built", example = "1890")
    private Integer yearBuilt;

    @Schema(description = "Current listing price or sold price", example = "1060000.00")
    private BigDecimal price;

    @Schema(description = "Price per square foot", example = "526.00")
    private BigDecimal pricePerSqft;

    @Schema(description = "Original listing price", example = "1060000.00")
    private BigDecimal originalListPrice;

    @Schema(description = "Monthly HOA fee", example = "493.00")
    private BigDecimal hoaFee;

    @Schema(description = "Next open house start time")
    private LocalDateTime nextOpenHouseStart;

    @Schema(description = "Next open house end time")
    private LocalDateTime nextOpenHouseEnd;

    @Schema(description = "JSON/text storage for additional source-specific fields")
    private String rawData;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Last scraped timestamp")
    private LocalDateTime lastScrapedAt;

    // User preference fields

    @Schema(description = "User marked as interested in this listing", example = "false")
    private Boolean isInterested;

    @Schema(description = "User notes about this listing")
    private String notes;

    /**
     * Convert MarketListing entity to response DTO with user preference
     */
    public static MarketListingWithPreferenceResponse from(MarketListing listing, UserMarketListingPreference preference) {
        return MarketListingWithPreferenceResponse.builder()
                .id(listing.getId())
                .source(listing.getSource())
                .sourceListingId(listing.getSourceListingId())
                .sourceUrl(listing.getSourceUrl())
                .sourceMlsNumber(listing.getSourceMlsNumber())
                .saleType(listing.getSaleType())
                .listingStatus(listing.getListingStatus())
                .soldDate(listing.getSoldDate())
                .listDate(listing.getListDate())
                .daysOnMarket(listing.getDaysOnMarket())
                .address(listing.getAddress())
                .address1(listing.getAddress1())
                .address2(listing.getAddress2())
                .city(listing.getCity())
                .state(listing.getState())
                .zipCode(listing.getZipCode())
                .location(listing.getLocation())
                .latitude(listing.getLatitude())
                .longitude(listing.getLongitude())
                .propertyType(listing.getPropertyType())
                .bedrooms(listing.getBedrooms())
                .bathrooms(listing.getBathrooms())
                .squareFeet(listing.getSquareFeet())
                .lotSize(listing.getLotSize())
                .yearBuilt(listing.getYearBuilt())
                .price(listing.getPrice())
                .pricePerSqft(listing.getPricePerSqft())
                .originalListPrice(listing.getOriginalListPrice())
                .hoaFee(listing.getHoaFee())
                .nextOpenHouseStart(listing.getNextOpenHouseStart())
                .nextOpenHouseEnd(listing.getNextOpenHouseEnd())
                .rawData(listing.getRawData())
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .lastScrapedAt(listing.getLastScrapedAt())
                .isInterested(preference != null ? preference.getIsInterested() : false)
                .notes(preference != null ? preference.getNotes() : null)
                .build();
    }
}

