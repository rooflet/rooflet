package io.rooflet.controller;

import io.rooflet.model.entity.Property;
import io.rooflet.model.request.CreatePropertyRequest;
import io.rooflet.model.request.UpdatePropertyRequest;
import io.rooflet.model.response.PropertyResponse;
import io.rooflet.service.PropertyService;
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
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Validated
@Tag(name = "Property Management", description = "API for managing real estate properties")
public class PropertyController {

    private final PropertyService propertyService;

    @Operation(summary = "Create a new property", description = "Creates a new property with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Property created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PropertyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Validation error",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<PropertyResponse> createProperty(@Valid @RequestBody CreatePropertyRequest request) {
        try {
            Property property = propertyService.createProperty(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(PropertyResponse.from(property));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get all properties", description = "Retrieves a list of all properties")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Properties retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PropertyResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getAllProperties(
            @Parameter(description = "Show only active (non-archived) properties", example = "true")
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {

        List<Property> properties;
        if (activeOnly) {
            properties = propertyService.getActiveProperties();
        } else {
            properties = propertyService.getAllProperties();
        }

        List<PropertyResponse> propertyResponses = properties.stream()
                .map(PropertyResponse::from)
                .toList();
        return ResponseEntity.ok(propertyResponses);
    }

    @Operation(summary = "Get property by ID", description = "Retrieves a specific property by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Property found successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PropertyResponse.class))),
            @ApiResponse(responseCode = "404", description = "Property not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getPropertyById(
            @Parameter(description = "Property ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        try {
            Property property = propertyService.findById(id);
            return ResponseEntity.ok(PropertyResponse.from(property));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update property", description = "Updates an existing property with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Property updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PropertyResponse.class))),
            @ApiResponse(responseCode = "404", description = "Property not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> updateProperty(
            @Parameter(description = "Property ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePropertyRequest request) {
        try {
            Property property = propertyService.updateProperty(id, request);
            return ResponseEntity.ok(PropertyResponse.from(property));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Archive property", description = "Soft deletes a property by marking it as archived")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Property archived successfully"),
            @ApiResponse(responseCode = "404", description = "Property not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveProperty(
            @Parameter(description = "Property ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        try {
            propertyService.deleteProperty(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Permanently delete property", description = "Permanently deletes a property from the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Property deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Property not found")
    })
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> deleteProperty(
            @Parameter(description = "Property ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        try {
            propertyService.hardDeleteProperty(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
