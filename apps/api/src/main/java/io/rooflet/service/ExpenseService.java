package io.rooflet.service;

import io.rooflet.model.entity.Expense;
import io.rooflet.model.entity.Property;
import io.rooflet.model.request.CreateExpenseRequest;
import io.rooflet.model.request.UpdateExpenseRequest;
import io.rooflet.repository.ExpenseRepository;
import io.rooflet.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final PropertyRepository propertyRepository;
    private final AuthenticationService authenticationService;
    private final PortfolioPermissionService permissionService;

    public Expense createExpense(CreateExpenseRequest request) {
        Property property = null;
        if (request.getPropertyId() != null) {
            property = getPropertyOwnedByCurrentUser(request.getPropertyId());
        }

        Expense expense = Expense.builder()
                .property(property)
                .owner(authenticationService.getCurrentUser())
                .expenseDate(request.getExpenseDate())
                .category(request.getCategory())
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        return expenseRepository.save(expense);
    }

    public Expense updateExpense(UUID id, UpdateExpenseRequest request) {
        Expense expense = findById(id);

        // Update only non-null fields
        if (request.getExpenseDate() != null) expense.setExpenseDate(request.getExpenseDate());
        if (request.getCategory() != null) expense.setCategory(request.getCategory());
        if (request.getAmount() != null) expense.setAmount(request.getAmount());
        if (request.getDescription() != null) expense.setDescription(request.getDescription());

        // Handle property association
        if (request.getPropertyId() != null) {
            Property property = getPropertyOwnedByCurrentUser(request.getPropertyId());
            expense.setProperty(property);
        }

        return expenseRepository.save(expense);
    }

    @Transactional(readOnly = true)
    public List<Expense> getAllExpenses() {
        UUID currentUserId = authenticationService.getCurrentUserId();
        UUID activePortfolioId = authenticationService.requireActivePortfolioId();

        permissionService.requireViewPermission(currentUserId, activePortfolioId);

        return expenseRepository.findByPortfolio(activePortfolioId);
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpensesByProperty(UUID propertyId) {
        verifyPropertyAccess(propertyId);
        return expenseRepository.findByProperty_Id(propertyId);
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        UUID activePortfolioId = authenticationService.requireActivePortfolioId();

        permissionService.requireViewPermission(currentUserId, activePortfolioId);

        return expenseRepository.findExpensesByPortfolioAndDateRange(activePortfolioId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpensesByPropertyAndDateRange(UUID propertyId, LocalDate startDate, LocalDate endDate) {
        verifyPropertyAccess(propertyId);
        return expenseRepository.findByProperty_IdAndExpenseDateBetween(propertyId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpensesByProperty(UUID propertyId, LocalDate startDate, LocalDate endDate) {
        verifyPropertyAccess(propertyId);
        return expenseRepository.getTotalExpensesByPropertyIdAndDateRange(propertyId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Expense findById(UUID id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found with ID: " + id));

        // Verify expense belongs to an accessible portfolio
        UUID currentUserId = authenticationService.getCurrentUserId();
        if (expense.getProperty() != null && expense.getProperty().getPortfolio() != null) {
            permissionService.requireViewPermission(currentUserId, expense.getProperty().getPortfolio().getId());
        } else if (expense.getProperty() != null) {
            // Fallback for properties without portfolio
            if (!authenticationService.isCurrentUserOwner(expense.getProperty().getOwner().getId())) {
                throw new IllegalArgumentException("Expense not found with ID: " + id);
            }
        } else if (expense.getOwner() != null) {
            // For unassigned expenses, check owner field
            if (!authenticationService.isCurrentUserOwner(expense.getOwner().getId())) {
                throw new IllegalArgumentException("Expense not found with ID: " + id);
            }
        }

        return expense;
    }

    public void deleteExpense(UUID id) {
        Expense expense = findById(id);

        // Verify manage permission
        UUID currentUserId = authenticationService.getCurrentUserId();
        if (expense.getProperty() != null && expense.getProperty().getPortfolio() != null) {
            permissionService.requireManagePermission(currentUserId, expense.getProperty().getPortfolio().getId());
        }

        expenseRepository.deleteById(id);
    }

    public void unassignExpenseFromProperty(UUID expenseId) {
        Expense expense = findById(expenseId);

        // Verify manage permission
        UUID currentUserId = authenticationService.getCurrentUserId();
        if (expense.getProperty() != null && expense.getProperty().getPortfolio() != null) {
            permissionService.requireManagePermission(currentUserId, expense.getProperty().getPortfolio().getId());
        }

        expense.setProperty(null);
        expenseRepository.save(expense);
    }

    private void verifyPropertyAccess(UUID propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID: " + propertyId));

        UUID currentUserId = authenticationService.getCurrentUserId();
        if (property.getPortfolio() != null) {
            permissionService.requireViewPermission(currentUserId, property.getPortfolio().getId());
        } else {
            // Fallback for properties without portfolio
            if (!authenticationService.isCurrentUserOwner(property.getOwner().getId())) {
                throw new IllegalArgumentException("Property not found with ID: " + propertyId);
            }
        }
    }

    private Property getPropertyOwnedByCurrentUser(UUID propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID: " + propertyId));

        UUID currentUserId = authenticationService.getCurrentUserId();
        if (property.getPortfolio() != null) {
            permissionService.requireManagePermission(currentUserId, property.getPortfolio().getId());
        } else {
            // Fallback for properties without portfolio
            if (!authenticationService.isCurrentUserOwner(property.getOwner().getId())) {
                throw new IllegalArgumentException("Property not found with ID: " + propertyId);
            }
        }

        return property;
    }
}
