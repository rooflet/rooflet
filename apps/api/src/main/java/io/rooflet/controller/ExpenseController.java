package io.rooflet.controller;

import io.rooflet.model.entity.Expense;
import io.rooflet.model.request.CreateExpenseRequest;
import io.rooflet.model.request.UpdateExpenseRequest;
import io.rooflet.model.response.ExpenseResponse;
import io.rooflet.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Validated
@Tag(name = "Expense Management", description = "API for managing property expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Create a new expense", description = "Creates a new expense with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Expense created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Validation error",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        try {
            Expense expense = expenseService.createExpense(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ExpenseResponse.from(expense));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get all expenses", description = "Retrieves expenses with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExpenseResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses(
            @Parameter(description = "Filter by property ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam(required = false) UUID propertyId,
            @Parameter(description = "Start date for date range filter", example = "2024-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for date range filter", example = "2024-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Expense> expenses;

        if (propertyId != null && startDate != null && endDate != null) {
            expenses = expenseService.getExpensesByPropertyAndDateRange(propertyId, startDate, endDate);
        } else if (propertyId != null) {
            expenses = expenseService.getExpensesByProperty(propertyId);
        } else if (startDate != null && endDate != null) {
            expenses = expenseService.getExpensesByDateRange(startDate, endDate);
        } else {
            expenses = expenseService.getAllExpenses();
        }

        List<ExpenseResponse> expenseResponses = expenses.stream()
                .map(ExpenseResponse::from)
                .toList();
        return ResponseEntity.ok(expenseResponses);
    }

    @Operation(summary = "Get expense by ID", description = "Retrieves a specific expense by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense found successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Expense not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @Parameter(description = "Expense ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        try {
            Expense expense = expenseService.findById(id);
            return ResponseEntity.ok(ExpenseResponse.from(expense));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get total expenses", description = "Calculates total expenses for a property within a date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalExpenses(
            @Parameter(description = "Property ID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @RequestParam UUID propertyId,
            @Parameter(description = "Start date", example = "2024-01-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date", example = "2024-12-31", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            BigDecimal total = expenseService.getTotalExpensesByProperty(propertyId, startDate, endDate);
            return ResponseEntity.ok(total != null ? total : BigDecimal.ZERO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update expense", description = "Updates an existing expense with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Expense not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @Parameter(description = "Expense ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExpenseRequest request) {
        try {
            Expense expense = expenseService.updateExpense(id, request);
            return ResponseEntity.ok(ExpenseResponse.from(expense));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Unassign expense from property", description = "Removes the property association from an expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense unassigned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Expense not found",
                    content = @Content)
    })
    @PutMapping("/{id}/unassign")
    public ResponseEntity<ExpenseResponse> unassignExpense(
            @Parameter(description = "Expense ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        try {
            expenseService.unassignExpenseFromProperty(id);
            Expense expense = expenseService.findById(id);
            return ResponseEntity.ok(ExpenseResponse.from(expense));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete expense", description = "Permanently deletes an expense from the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @Parameter(description = "Expense ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
