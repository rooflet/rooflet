package io.rooflet.controller;

import io.rooflet.model.entity.UserZipCodePreference;
import io.rooflet.model.request.AddZipCodePreferenceRequest;
import io.rooflet.model.response.AvailableZipCodeResponse;
import io.rooflet.model.response.MessageResponse;
import io.rooflet.model.response.ZipCodePreferenceResponse;
import io.rooflet.service.AuthenticationService;
import io.rooflet.service.UserZipCodePreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-preferences/zip-codes")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Zip Code Preferences", description = "API for managing user's watched zip codes for market listings")
public class UserZipCodePreferenceController {

    private final UserZipCodePreferenceService preferenceService;
    private final AuthenticationService authenticationService;

    @Operation(summary = "Add zip code to watch list",
            description = "Add a zip code to the current user's watch list. Maximum limit applies (default 10). " +
                    "ZIP code must have both expected rent data and active market listings.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "ZIP code added successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ZipCodePreferenceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - max limit reached, already watching, or invalid zip code",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ZipCodePreferenceResponse> addZipCode(
            @Valid @RequestBody AddZipCodePreferenceRequest request) {
        try {
            UUID userId = authenticationService.getCurrentUserId();
            UserZipCodePreference preference = preferenceService.addZipCode(userId, request.getZipCode());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ZipCodePreferenceResponse.from(preference));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Remove zip code from watch list",
            description = "Remove a zip code from the current user's watch list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ZIP code removed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "ZIP code not in watch list",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{zipCode}")
    public ResponseEntity<MessageResponse> removeZipCode(
            @Parameter(description = "ZIP code to remove", example = "02171")
            @PathVariable String zipCode) {
        try {
            UUID userId = authenticationService.getCurrentUserId();
            preferenceService.removeZipCode(userId, zipCode);
            return ResponseEntity.ok(new MessageResponse("ZIP code " + zipCode + " removed from watch list"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get user's watched zip codes",
            description = "Retrieve all zip codes the current user is watching")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Watched zip codes retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ZipCodePreferenceResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<ZipCodePreferenceResponse>> getUserZipCodes() {
        UUID userId = authenticationService.getCurrentUserId();
        List<UserZipCodePreference> preferences = preferenceService.getUserZipCodes(userId);
        List<ZipCodePreferenceResponse> responses = preferences.stream()
                .map(ZipCodePreferenceResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get available zip codes",
            description = "Retrieve all valid zip codes (with both expected rent data and market listings) " +
                    "along with metadata including listing counts and watch status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available zip codes retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AvailableZipCodeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/available")
    public ResponseEntity<List<AvailableZipCodeResponse>> getAvailableZipCodes() {
        UUID userId = authenticationService.getCurrentUserId();
        List<AvailableZipCodeResponse> availableZipCodes = preferenceService.getAvailableZipCodes(userId);
        return ResponseEntity.ok(availableZipCodes);
    }
}

