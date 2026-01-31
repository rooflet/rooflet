package io.rooflet.controller;

import io.rooflet.model.RentCollection;
import io.rooflet.model.request.BulkCreateRentCollectionsRequest;
import io.rooflet.model.request.CreateRentCollectionRequest;
import io.rooflet.model.request.UpdateRentCollectionRequest;
import io.rooflet.model.response.ErrorResponse;
import io.rooflet.model.response.RentCollectionResponse;
import io.rooflet.service.RentCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rent-collections")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Rent Collection Management", description = "API for managing rent payments and collections")
public class RentCollectionController {

    private final RentCollectionService rentCollectionService;

    @Operation(summary = "Create a new rent collection record", description = "Creates a new rent collection record for tracking payments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rent collection created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RentCollectionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or duplicate record",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Validation error",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<RentCollectionResponse> createRentCollection(@Valid @RequestBody CreateRentCollectionRequest request) {
        try {
            RentCollection rentCollection = rentCollectionService.createRentCollection(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(RentCollectionResponse.from(rentCollection));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Bulk create rent collections",
            description = "Creates multiple rent collection records for a tenant with specific amounts, dates, and notes for each period")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rent collections created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RentCollectionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data, tenant not associated with property, or empty items list",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tenant not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/bulk")
    public ResponseEntity<?> bulkCreateRentCollections(
            @Valid @RequestBody BulkCreateRentCollectionsRequest request,
            HttpServletRequest httpRequest) {
        try {
            log.info("Bulk rent collection creation request received for tenantId={}, itemCount={}",
                    request.getTenantId(),
                    request.getItems() != null ? request.getItems().size() : 0);

            List<RentCollection> rentCollections = rentCollectionService.createBulkRentCollections(request);

            log.info("Successfully created {} rent collections for tenantId={}",
                    rentCollections.size(),
                    request.getTenantId());

            List<RentCollectionResponse> responses = rentCollections.stream()
                    .map(RentCollectionResponse::from)
                    .toList();
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (IllegalArgumentException e) {
            log.error("Bad request for bulk rent collection creation - tenantId={}, error={}",
                    request.getTenantId(),
                    e.getMessage(),
                    e);

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Validation failed")
                    .details(e.getMessage())
                    .path(httpRequest.getRequestURI())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error during bulk rent collection creation - tenantId={}",
                    request.getTenantId(),
                    e);

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Internal server error")
                    .details("An unexpected error occurred: " + e.getMessage())
                    .path(httpRequest.getRequestURI())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Get all rent collections", description = "Retrieves rent collections with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rent collections retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RentCollectionResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<RentCollectionResponse>> getAllRentCollections(
            @Parameter(description = "Filter by property ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam(required = false) UUID propertyId,
            @Parameter(description = "Filter by tenant ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam(required = false) UUID tenantId,
            @Parameter(description = "Start period for date range filter", example = "2024-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startPeriod,
            @Parameter(description = "End period for date range filter", example = "2024-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endPeriod) {

        List<RentCollection> rentCollections;

        if (propertyId != null && tenantId != null) {
            rentCollections = rentCollectionService.getRentCollectionsByPropertyAndTenant(propertyId, tenantId);
        } else if (propertyId != null) {
            rentCollections = rentCollectionService.getRentCollectionsByProperty(propertyId);
        } else if (tenantId != null) {
            rentCollections = rentCollectionService.getRentCollectionsByTenant(tenantId);
        } else if (startPeriod != null && endPeriod != null) {
            rentCollections = rentCollectionService.getRentCollectionsByPeriod(startPeriod, endPeriod);
        } else {
            rentCollections = rentCollectionService.getAllRentCollections();
        }

        List<RentCollectionResponse> responses = rentCollections.stream()
                .map(RentCollectionResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Add payment to rent collection", description = "Adds a payment amount to an existing rent collection record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment added successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RentCollectionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Rent collection not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid payment amount",
                    content = @Content)
    })
    @PostMapping("/{id}/payments")
    public ResponseEntity<RentCollectionResponse> addPayment(
            @Parameter(description = "Rent collection ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @RequestBody Map<String, Object> paymentRequest) {
        try {
            BigDecimal paymentAmount = new BigDecimal(paymentRequest.get("amount").toString());
            LocalDate paymentDate = paymentRequest.get("paymentDate") != null ?
                    LocalDate.parse(paymentRequest.get("paymentDate").toString()) : LocalDate.now();
            String notes = paymentRequest.get("notes") != null ?
                    paymentRequest.get("notes").toString() : null;

            RentCollection rentCollection = rentCollectionService.addPayment(id, paymentAmount, paymentDate, notes);
            return ResponseEntity.ok(RentCollectionResponse.from(rentCollection));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update rent collection", description = "Updates an existing rent collection record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rent collection updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RentCollectionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Rent collection not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<RentCollectionResponse> updateRentCollection(
            @Parameter(description = "Rent collection ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRentCollectionRequest request) {
        try {
            RentCollection rentCollection = rentCollectionService.updateRentCollection(id, request);
            return ResponseEntity.ok(RentCollectionResponse.from(rentCollection));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Permanently delete rent collection", description = "Permanently deletes a rent collection from the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rent collection deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Rent collection not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRentCollection(
            @Parameter(description = "Rent collection ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        try {
            rentCollectionService.hardDeleteRentCollection(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handle validation errors for @Valid annotated request bodies
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .rejectedValue(error.getRejectedValue())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        log.error("Validation error for request to {}: {} field errors",
                request.getRequestURI(),
                fieldErrors.size());

        fieldErrors.forEach(error ->
                log.error("  - Field '{}' with value '{}': {}",
                        error.getField(),
                        error.getRejectedValue(),
                        error.getMessage())
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .details(String.format("Request has %d validation error(s)", fieldErrors.size()))
                .fieldErrors(fieldErrors)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }
}

