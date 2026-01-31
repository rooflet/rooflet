package io.rooflet.service;

import io.rooflet.model.entity.Property;
import io.rooflet.model.entity.Tenant;
import io.rooflet.model.request.CreateTenantRequest;
import io.rooflet.model.request.UpdateTenantRequest;
import io.rooflet.repository.PropertyRepository;
import io.rooflet.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TenantService {

    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;
    private final AuthenticationService authenticationService;
    private final PortfolioPermissionService permissionService;

    public Tenant createTenant(CreateTenantRequest request) {
        Property property = null;
        if (request.getPropertyId() != null) {
            property = getPropertyOwnedByCurrentUser(request.getPropertyId());
        }

        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .property(property)
                .leaseStartDate(request.getLeaseStartDate())
                .leaseEndDate(request.getLeaseEndDate())
                .monthlyRent(request.getMonthlyRent())
                .archived(false)
                .build();

        return tenantRepository.save(tenant);
    }

    public Tenant updateTenant(UUID id, UpdateTenantRequest request) {
        Tenant tenant = findById(id);

        // Update only non-null fields
        if (request.getName() != null) tenant.setName(request.getName());
        if (request.getEmail() != null) tenant.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) tenant.setPhoneNumber(request.getPhoneNumber());
        if (request.getLeaseStartDate() != null) tenant.setLeaseStartDate(request.getLeaseStartDate());
        if (request.getLeaseEndDate() != null) tenant.setLeaseEndDate(request.getLeaseEndDate());
        if (request.getMonthlyRent() != null) tenant.setMonthlyRent(request.getMonthlyRent());
        if (request.getArchived() != null) tenant.setArchived(request.getArchived());

        // Handle property association
        if (request.getPropertyId() != null) {
            Property property = getPropertyOwnedByCurrentUser(request.getPropertyId());
            tenant.setProperty(property);
        }

        return tenantRepository.save(tenant);
    }

    @Transactional(readOnly = true)
    public List<Tenant> getAllTenants() {
        UUID currentUserId = authenticationService.getCurrentUserId();
        UUID activePortfolioId = authenticationService.requireActivePortfolioId();

        permissionService.requireViewPermission(currentUserId, activePortfolioId);

        return tenantRepository.findTenantsForPortfolio(activePortfolioId);
    }

    @Transactional(readOnly = true)
    public List<Tenant> getTenantsByProperty(UUID propertyId) {
        // Verify property access
        verifyPropertyAccess(propertyId);
        return tenantRepository.findByProperty_Id(propertyId);
    }

    @Transactional(readOnly = true)
    public List<Tenant> getActiveTenantsByProperty(UUID propertyId) {
        // Verify property access
        verifyPropertyAccess(propertyId);
        return tenantRepository.findActiveByPropertyId(propertyId);
    }

    @Transactional(readOnly = true)
    public List<Tenant> getActiveTenants() {
        UUID currentUserId = authenticationService.getCurrentUserId();
        UUID activePortfolioId = authenticationService.requireActivePortfolioId();

        permissionService.requireViewPermission(currentUserId, activePortfolioId);

        return tenantRepository.findActiveTenantsForPortfolio(activePortfolioId);
    }

    @Transactional(readOnly = true)
    public List<Tenant> getUnassignedTenants() {
        UUID currentUserId = authenticationService.getCurrentUserId();
        UUID activePortfolioId = authenticationService.requireActivePortfolioId();

        permissionService.requireViewPermission(currentUserId, activePortfolioId);

        return tenantRepository.findByPropertyIsNull();
    }

    @Transactional(readOnly = true)
    public Tenant findById(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + id));

        // Verify tenant belongs to a property in an accessible portfolio
        if (tenant.getProperty() != null) {
            UUID currentUserId = authenticationService.getCurrentUserId();
            if (tenant.getProperty().getPortfolio() != null) {
                permissionService.requireViewPermission(currentUserId, tenant.getProperty().getPortfolio().getId());
            } else {
                // Fallback for properties without portfolio
                if (!authenticationService.isCurrentUserOwner(tenant.getProperty().getOwner().getId())) {
                    throw new IllegalArgumentException("Tenant not found with ID: " + id);
                }
            }
        }

        return tenant;
    }

    public void deleteTenant(UUID id) {
        Tenant tenant = findById(id);

        // Verify manage permission
        if (tenant.getProperty() != null && tenant.getProperty().getPortfolio() != null) {
            UUID currentUserId = authenticationService.getCurrentUserId();
            permissionService.requireManagePermission(currentUserId, tenant.getProperty().getPortfolio().getId());
        }

        tenant.setArchived(true);
        tenantRepository.save(tenant);
    }

    public void hardDeleteTenant(UUID id) {
        Tenant tenant = findById(id);

        // Verify manage permission
        if (tenant.getProperty() != null && tenant.getProperty().getPortfolio() != null) {
            UUID currentUserId = authenticationService.getCurrentUserId();
            permissionService.requireManagePermission(currentUserId, tenant.getProperty().getPortfolio().getId());
        }

        tenantRepository.deleteById(id);
    }

    public void unassignTenantFromProperty(UUID tenantId) {
        Tenant tenant = findById(tenantId);

        // Verify manage permission
        if (tenant.getProperty() != null && tenant.getProperty().getPortfolio() != null) {
            UUID currentUserId = authenticationService.getCurrentUserId();
            permissionService.requireManagePermission(currentUserId, tenant.getProperty().getPortfolio().getId());
        }

        tenant.setProperty(null);
        tenantRepository.save(tenant);
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
