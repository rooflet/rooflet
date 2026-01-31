package io.rooflet.controller;

import io.rooflet.model.entity.ExpectedRent;
import io.rooflet.model.response.ExpectedRentResponse;
import io.rooflet.service.ExpectedRentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/expected-rent")
@RequiredArgsConstructor
@Validated
@Tag(name = "Expected Rent", description = "API for querying expected rent standards by zip code and bedroom count")
public class ExpectedRentController {

    private final ExpectedRentService expectedRentService;
    @Operation(summary = "Get expected rent by zip code and bedrooms",
            description = "Retrieves the most recent expected rent for a specific zip code and bedroom count")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expected rent found successfully",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ExpectedRentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Expected rent not found for the given criteria",
                    content = @Content)
    })
    @GetMapping("/lookup")
    public ResponseEntity<ExpectedRentResponse> getExpectedRentByZipAndBedrooms(
            @Parameter(description = "ZIP code", example = "02101")
            @RequestParam String zipCode,
            @Parameter(description = "Number of bedrooms", example = "2")
            @RequestParam Integer bedrooms,
            @Parameter(description = "Effective date (optional, defaults to most recent)", example = "2025-07-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDate) {

        Optional<ExpectedRent> expectedRent;
        if (effectiveDate != null) {
            expectedRent = expectedRentService.getExpectedRentByZipAndBedroomsAndDate(
                    zipCode, bedrooms, effectiveDate);
        } else {
            expectedRent = expectedRentService.getExpectedRentByZipAndBedrooms(zipCode, bedrooms);
        }

        return expectedRent
                .map(rent -> ResponseEntity.ok(ExpectedRentResponse.from(rent)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get expected rents by zip code",
            description = "Retrieves all expected rent records for a specific zip code (all bedroom counts)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expected rents retrieved successfully",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ExpectedRentResponse.class)))
    })
    @GetMapping("/zip/{zipCode}")
    public ResponseEntity<List<ExpectedRentResponse>> getExpectedRentsByZipCode(
            @Parameter(description = "ZIP code", example = "02101")
            @PathVariable String zipCode,
            @Parameter(description = "Return only the latest effective date for each bedroom count", example = "true")
            @RequestParam(required = false, defaultValue = "false") Boolean latestOnly) {

        List<ExpectedRent> expectedRents;
        if (latestOnly) {
            expectedRents = expectedRentService.getLatestExpectedRentsByZipCode(zipCode);
        } else {
            expectedRents = expectedRentService.getExpectedRentsByZipCode(zipCode);
        }

        List<ExpectedRentResponse> responses = expectedRents.stream()
                .map(ExpectedRentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}

