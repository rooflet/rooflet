package io.rooflet.service;

import io.rooflet.model.RentCollection;
import io.rooflet.model.entity.Property;
import io.rooflet.model.entity.Tenant;
import io.rooflet.model.request.BulkCreateRentCollectionsRequest;
import io.rooflet.model.request.CreateRentCollectionRequest;
import io.rooflet.model.request.RentCollectionItemRequest;
import io.rooflet.model.request.UpdateRentCollectionRequest;
import io.rooflet.repository.PropertyRepository;
import io.rooflet.repository.RentCollectionRepository;
import io.rooflet.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RentCollectionService {

    private final RentCollectionRepository rentCollectionRepository;
    private final PropertyRepository propertyRepository;
    private final TenantRepository tenantRepository;
    private final AuthenticationService authenticationService;
    private final PortfolioPermissionService permissionService;

    public RentCollection createRentCollection(CreateRentCollectionRequest request) {
        Property property = getPropertyOwnedByCurrentUser(request.getPropertyId());
        Tenant tenant = getTenantAccessibleByCurrentUser(request.getTenantId());

        // Validate tenant is not archived
        validateTenantNotArchived(tenant);

        // Adjust rent collection date to be within lease period
        LocalDate adjustedPaymentDate = adjustRentCollectionDateWithinLease(tenant, request.getPaymentDate());

        RentCollection rentCollection = RentCollection.builder()
                .property(property)
                .tenant(tenant)
                .expectedAmount(request.getExpectedAmount())
                .paidAmount(request.getPaidAmount() != null ? request.getPaidAmount() : BigDecimal.ZERO)
                .paymentDate(adjustedPaymentDate)
                .notes(request.getNotes())
                .build();

        return rentCollectionRepository.save(rentCollection);
    }

    public RentCollection updateRentCollection(UUID id, UpdateRentCollectionRequest request) {
        RentCollection rentCollection = findById(id);

        // Validate tenant is not archived
        validateTenantNotArchived(rentCollection.getTenant());

        // Adjust new payment date to be within lease period if it's being updated
        LocalDate adjustedPaymentDate = null;
        if (request.getPaymentDate() != null) {
            adjustedPaymentDate = adjustRentCollectionDateWithinLease(rentCollection.getTenant(), request.getPaymentDate());
        }

        // Update only non-null fields
        if (request.getExpectedAmount() != null) rentCollection.setExpectedAmount(request.getExpectedAmount());
        if (request.getPaidAmount() != null) rentCollection.setPaidAmount(request.getPaidAmount());
        if (adjustedPaymentDate != null) rentCollection.setPaymentDate(adjustedPaymentDate);
        if (request.getNotes() != null) rentCollection.setNotes(request.getNotes());

        return rentCollectionRepository.save(rentCollection);
    }

    public RentCollection addPayment(UUID id, BigDecimal paymentAmount, LocalDate paymentDate, String notes) {
        RentCollection rentCollection = findById(id);

        // Validate tenant is not archived
        validateTenantNotArchived(rentCollection.getTenant());

        // Adjust payment date to be within lease period if it's being set
        LocalDate adjustedPaymentDate = null;
        if (paymentDate != null) {
            adjustedPaymentDate = adjustRentCollectionDateWithinLease(rentCollection.getTenant(), paymentDate);
        }

        BigDecimal newPaidAmount = rentCollection.getPaidAmount().add(paymentAmount);
        rentCollection.setPaidAmount(newPaidAmount);

        if (adjustedPaymentDate != null) {
            rentCollection.setPaymentDate(adjustedPaymentDate);
        }

        if (notes != null && !notes.trim().isEmpty()) {
            String existingNotes = rentCollection.getNotes();
            if (existingNotes != null && !existingNotes.trim().isEmpty()) {
                rentCollection.setNotes(existingNotes + "\n" + notes);
            } else {
                rentCollection.setNotes(notes);
            }
        }


        return rentCollectionRepository.save(rentCollection);
    }

    @Transactional(readOnly = true)
    public List<RentCollection> getAllRentCollections() {
        UUID currentUserId = authenticationService.getCurrentUserId();
        UUID activePortfolioId = authenticationService.requireActivePortfolioId();

        // Verify user has view permission
        permissionService.requireViewPermission(currentUserId, activePortfolioId);

        return rentCollectionRepository.findByPortfolioId(activePortfolioId);
    }

    @Transactional(readOnly = true)
    public List<RentCollection> getRentCollectionsByProperty(UUID propertyId) {
        // Verify property ownership first
        getPropertyOwnedByCurrentUser(propertyId);
        return rentCollectionRepository.findByProperty_Id(propertyId);
    }

    @Transactional(readOnly = true)
    public List<RentCollection> getRentCollectionsByTenant(UUID tenantId) {
        // Verify tenant access first
        getTenantAccessibleByCurrentUser(tenantId);
        return rentCollectionRepository.findByTenant_Id(tenantId);
    }

    @Transactional(readOnly = true)
    public List<RentCollection> getRentCollectionsByPropertyAndTenant(UUID propertyId, UUID tenantId) {
        // Verify both property and tenant access
        getPropertyOwnedByCurrentUser(propertyId);
        getTenantAccessibleByCurrentUser(tenantId);
        return rentCollectionRepository.findByProperty_IdAndTenant_Id(propertyId, tenantId);
    }

    @Transactional(readOnly = true)
    public List<RentCollection> getRentCollectionsByPeriod(LocalDate startPeriod, LocalDate endPeriod) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        UUID activePortfolioId = authenticationService.requireActivePortfolioId();

        // Verify user has view permission
        permissionService.requireViewPermission(currentUserId, activePortfolioId);

        return rentCollectionRepository.findRentCollectionsByPortfolioAndPeriod(activePortfolioId, startPeriod, endPeriod);
    }

    @Transactional(readOnly = true)
    public RentCollection findById(UUID id) {
        RentCollection rentCollection = rentCollectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rent collection not found with ID: " + id));

        // Verify user has permission to view this rent collection's property portfolio
        UUID currentUserId = authenticationService.getCurrentUserId();
        Property property = rentCollection.getProperty();
        if (property.getPortfolio() != null) {
            permissionService.requireViewPermission(currentUserId, property.getPortfolio().getId());
        } else {
            // Fallback to old owner check for properties without portfolio
            if (!authenticationService.isCurrentUserOwner(property.getOwner().getId())) {
                throw new IllegalArgumentException("Rent collection not found with ID: " + id);
            }
        }

        return rentCollection;
    }

    public void hardDeleteRentCollection(UUID id) {
        rentCollectionRepository.deleteById(id);
    }

    public List<RentCollection> createBulkRentCollections(BulkCreateRentCollectionsRequest request) {
        log.debug("Starting bulk rent collection creation for tenantId={}, itemCount={}",
                request.getTenantId(),
                request.getItems() != null ? request.getItems().size() : 0);

        Tenant tenant = getTenantAccessibleByCurrentUser(request.getTenantId());
        log.debug("Found tenant: id={}, name={}, propertyId={}",
                tenant.getId(),
                tenant.getName(),
                tenant.getProperty() != null ? tenant.getProperty().getId() : "null");

        // Validate tenant is not archived
        validateTenantNotArchived(tenant);

        if (tenant.getProperty() == null) {
            String errorMsg = String.format(
                    "Tenant must be associated with a property. Tenant ID: %s, Name: %s",
                    tenant.getId(),
                    tenant.getName());
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // Perform bulk validations before creating any rent collections
        validateBulkRentCollectionDates(tenant, request.getItems());

        List<RentCollection> rentCollections = new ArrayList<>();

        // Create rent collections from provided items
        int itemIndex = 0;
        for (var item : request.getItems()) {
            itemIndex++;
            log.debug("Processing item {}/{}: paymentDate={}, expectedAmount={}, paidAmount={}",
                    itemIndex,
                    request.getItems().size(),
                    item.getPaymentDate(),
                    item.getExpectedAmount(),
                    item.getPaidAmount());

            // Use tenant's monthly rent as default if not provided
            BigDecimal expectedAmount = item.getExpectedAmount() != null
                    ? item.getExpectedAmount()
                    : (tenant.getMonthlyRent() != null ? tenant.getMonthlyRent() : BigDecimal.ZERO);

            if (expectedAmount.compareTo(BigDecimal.ZERO) < 0) {
                String errorMsg = String.format(
                        "Item #%d has invalid expected amount: %s. Expected amount cannot be negative.",
                        itemIndex,
                        expectedAmount);
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            BigDecimal paidAmount = item.getPaidAmount() != null
                    ? item.getPaidAmount()
                    : BigDecimal.ZERO;

            if (paidAmount.compareTo(BigDecimal.ZERO) < 0) {
                String errorMsg = String.format(
                        "Item #%d has invalid paid amount: %s. Paid amount cannot be negative.",
                        itemIndex,
                        paidAmount);
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            if (paidAmount.compareTo(expectedAmount) > 0) {
                log.warn("Item #{}: paidAmount ({}) exceeds expectedAmount ({})",
                        itemIndex,
                        paidAmount,
                        expectedAmount);
            }

            RentCollection rentCollection = RentCollection.builder()
                    .property(tenant.getProperty())
                    .tenant(tenant)
                    .expectedAmount(expectedAmount)
                    .paidAmount(paidAmount)
                    .paymentDate(item.getPaymentDate())
                    .notes(item.getNotes())
                    .build();

            rentCollections.add(rentCollection);
            log.debug("Successfully created rent collection object for item #{}", itemIndex);
        }

        log.info("Saving {} rent collections to database for tenantId={}",
                rentCollections.size(),
                request.getTenantId());

        List<RentCollection> savedCollections = rentCollectionRepository.saveAll(rentCollections);

        log.info("Successfully saved {} rent collections for tenantId={}",
                savedCollections.size(),
                request.getTenantId());

        return savedCollections;
    }

    private Property getPropertyOwnedByCurrentUser(UUID propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID: " + propertyId));

        // Verify user has permission to view this property's portfolio
        UUID currentUserId = authenticationService.getCurrentUserId();
        if (property.getPortfolio() != null) {
            permissionService.requireViewPermission(currentUserId, property.getPortfolio().getId());
        } else {
            // Fallback to old owner check for properties without portfolio
            if (!authenticationService.isCurrentUserOwner(property.getOwner().getId())) {
                throw new IllegalArgumentException("Property not found with ID: " + propertyId);
            }
        }

        return property;
    }

    private Tenant getTenantAccessibleByCurrentUser(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + tenantId));

        // Verify tenant belongs to a property accessible by the current user
        if (tenant.getProperty() != null) {
            UUID currentUserId = authenticationService.getCurrentUserId();
            if (tenant.getProperty().getPortfolio() != null) {
                permissionService.requireViewPermission(currentUserId, tenant.getProperty().getPortfolio().getId());
            } else {
                // Fallback to old owner check for properties without portfolio
                if (!authenticationService.isCurrentUserOwner(tenant.getProperty().getOwner().getId())) {
                    throw new IllegalArgumentException("Tenant not found with ID: " + tenantId);
                }
            }
        }

        return tenant;
    }

    private LocalDate adjustRentCollectionDateWithinLease(Tenant tenant, LocalDate rentCollectionDate) {
        if (rentCollectionDate == null) {
            return null; // No adjustment needed if date is null
        }

        LocalDate leaseStartDate = tenant.getLeaseStartDate();
        LocalDate leaseEndDate = tenant.getLeaseEndDate();
        LocalDate adjustedDate = rentCollectionDate;

        // If lease start date is set and rent collection date is before it, adjust to lease start date
        if (leaseStartDate != null && rentCollectionDate.isBefore(leaseStartDate)) {
            log.info("Adjusting rent collection date from {} to lease start date {} for tenant {} (ID: {})",
                    rentCollectionDate, leaseStartDate, tenant.getName(), tenant.getId());
            adjustedDate = leaseStartDate;
        }

        // Check if lease end date is set and if rent collection date is after it
        if (leaseEndDate != null && adjustedDate.isAfter(leaseEndDate)) {
            throw new IllegalArgumentException(
                    String.format("Rent collection date %s is after the tenant's lease end date %s",
                            rentCollectionDate, leaseEndDate));
        }

        return adjustedDate;
    }

    private void validateTenantNotArchived(Tenant tenant) {
        if (tenant.getArchived() != null && tenant.getArchived()) {
            throw new IllegalArgumentException(
                    String.format("Cannot create or update rent collections for archived tenant (ID: %s, Name: %s)",
                            tenant.getId(), tenant.getName()));
        }
    }

    private void validateBulkRentCollectionDates(Tenant tenant, List<RentCollectionItemRequest> items) {
        LocalDate leaseStartDate = tenant.getLeaseStartDate();
        LocalDate leaseEndDate = tenant.getLeaseEndDate();

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be null or empty");
        }

        // Track all payment dates to check for duplicates/overlaps
        List<LocalDate> paymentDates = new ArrayList<>();

        // Validate each item
        int itemIndex = 0;
        for (var item : items) {
            itemIndex++;
            LocalDate paymentDate = item.getPaymentDate();

            if (paymentDate == null) {
                throw new IllegalArgumentException(
                        String.format("Item #%d: Payment date cannot be null", itemIndex));
            }

            // Date Boundary Validation 1: Period start date cannot be before tenant's lease start date
            if (leaseStartDate != null && paymentDate.isBefore(leaseStartDate)) {
                throw new IllegalArgumentException(
                        String.format("Item #%d: Payment date %s cannot be before tenant's lease start date %s",
                                itemIndex, paymentDate, leaseStartDate));
            }

            // Date Boundary Validation 2: Period end date cannot be after tenant's lease end date
            if (leaseEndDate != null && paymentDate.isAfter(leaseEndDate)) {
                throw new IllegalArgumentException(
                        String.format("Item #%d: Payment date %s cannot be after tenant's lease end date %s",
                                itemIndex, paymentDate, leaseEndDate));
            }

            paymentDates.add(paymentDate);
        }

        // Sort payment dates for easier validation
        paymentDates.sort(LocalDate::compareTo);

        // Cross-Period Validation: Check for duplicate/overlapping dates
        for (int i = 0; i < paymentDates.size() - 1; i++) {
            if (paymentDates.get(i).equals(paymentDates.get(i + 1))) {
                throw new IllegalArgumentException(
                        String.format("Duplicate payment date found: %s. Each payment date must be unique.",
                                paymentDates.get(i)));
            }
        }

        // Duration Constraint: Individual period duration cannot exceed 10 years (3,650 days)
        // Check the span from earliest to latest date
        if (paymentDates.size() > 1) {
            LocalDate earliestDate = paymentDates.get(0);
            LocalDate latestDate = paymentDates.get(paymentDates.size() - 1);
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(earliestDate, latestDate);

            if (daysBetween > 3650) {
                throw new IllegalArgumentException(
                        String.format("The duration between the earliest (%s) and latest (%s) payment dates " +
                                        "exceeds 10 years (3,650 days). Duration: %d days",
                                earliestDate, latestDate, daysBetween));
            }
        }

        log.debug("Bulk rent collection date validation passed for {} items", items.size());
    }
}
