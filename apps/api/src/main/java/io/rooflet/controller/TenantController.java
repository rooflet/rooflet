package io.rooflet.controller;

import io.rooflet.model.entity.Tenant;
import io.rooflet.model.request.CreateTenantRequest;
import io.rooflet.model.request.UpdateTenantRequest;
import io.rooflet.model.response.TenantResponse;
import io.rooflet.service.TenantService;
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
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Validated
@Tag(name = "Tenant Management", description = "API for managing tenants")
public class TenantController {

    private final TenantService tenantService;

    @Operation(summary = "Create a new tenant", description = "Creates a new tenant with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tenant created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TenantResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Validation error",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        try {
            Tenant tenant = tenantService.createTenant(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(TenantResponse.from(tenant));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get all tenants", description = "Retrieves a list of tenants with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tenants retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TenantResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<TenantResponse>> getAllTenants(
            @Parameter(description = "Filter by property ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam(required = false) UUID propertyId,
            @Parameter(description = "Show only active (non-archived) tenants", example = "true")
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly,
            @Parameter(description = "Show only unassigned tenants", example = "true")
            @RequestParam(required = false, defaultValue = "false") Boolean unassignedOnly) {

        List<Tenant> tenants;
        if (unassignedOnly) {
            tenants = tenantService.getUnassignedTenants();
        } else if (propertyId != null && activeOnly) {
            tenants = tenantService.getActiveTenantsByProperty(propertyId);
        } else if (propertyId != null) {
            tenants = tenantService.getTenantsByProperty(propertyId);
        } else if (activeOnly) {
            tenants = tenantService.getActiveTenants();
        } else {
            tenants = tenantService.getAllTenants();
        }

        List<TenantResponse> tenantResponses = tenants.stream()
                .map(TenantResponse::from)
                .toList();
        return ResponseEntity.ok(tenantResponses);
    }

    @Operation(summary = "Get tenant by ID", description = "Retrieves a specific tenant by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tenant found successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TenantResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tenant not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenantById(
            @Parameter(description = "Tenant ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        try {
            Tenant tenant = tenantService.findById(id);
            return ResponseEntity.ok(TenantResponse.from(tenant));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update tenant", description = "Updates an existing tenant with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tenant updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TenantResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tenant not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> updateTenant(
            @Parameter(description = "Tenant ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantRequest request) {
        try {
            Tenant tenant = tenantService.updateTenant(id, request);
            return ResponseEntity.ok(TenantResponse.from(tenant));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Unassign tenant from property", description = "Removes the property association from a tenant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tenant unassigned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TenantResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tenant not found",
                    content = @Content)
    })
    @PutMapping("/{id}/unassign")
    public ResponseEntity<TenantResponse> unassignTenantFromProperty(
            @Parameter(description = "Tenant ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        try {
            tenantService.unassignTenantFromProperty(id);
            Tenant tenant = tenantService.findById(id);
            return ResponseEntity.ok(TenantResponse.from(tenant));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Archive tenant", description = "Soft deletes a tenant by marking them as archived")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tenant archived successfully"),
            @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveTenant(
            @Parameter(description = "Tenant ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        try {
            tenantService.deleteTenant(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Permanently delete tenant", description = "Permanently deletes a tenant from the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tenant deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> deleteTenant(
            @Parameter(description = "Tenant ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        try {
            tenantService.hardDeleteTenant(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
