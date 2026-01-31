package io.rooflet.service;

import io.rooflet.model.entity.Portfolio;
import io.rooflet.model.entity.Property;
import io.rooflet.model.entity.User;
import io.rooflet.model.request.CreatePropertyRequest;
import io.rooflet.model.request.UpdatePropertyRequest;
import io.rooflet.repository.PortfolioRepository;
import io.rooflet.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PortfolioRepository portfolioRepository;
    private final AuthenticationService authenticationService;
    private final PortfolioPermissionService permissionService;

    public Property createProperty(CreatePropertyRequest request) {
        User currentUser = authenticationService.getCurrentUser();
        UUID activePortfolioId = authenticationService.requireActivePortfolioId();

        // Verify user has manage permission on the portfolio
        permissionService.requireManagePermission(currentUser.getId(), activePortfolioId);

        Portfolio portfolio = portfolioRepository.findById(activePortfolioId)
                .orElseThrow(() -> new IllegalStateException("Active portfolio not found: " + activePortfolioId));

        // Build property with both owner (for backward compatibility) and portfolio
        Property property = Property.builder()
                .owner(currentUser)
                .portfolio(portfolio)
                .address1(request.getAddress1())
                .address2(request.getAddress2())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .propertyType(request.getPropertyType())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .squareFeet(request.getSquareFeet())
                .marketValue(request.getMarketValue())
                .purchasePrice(request.getPurchasePrice())
                .purchaseDate(request.getPurchaseDate())
                .debt(request.getDebt())
                .interestRate(request.getInterestRate())
                .monthlyHoa(request.getMonthlyHoa())
                .monthlyPropertyTax(request.getMonthlyPropertyTax())
                .monthlyInsurance(request.getMonthlyInsurance())
                .notes(request.getNotes())
                .archived(false)
                .build();

        return propertyRepository.save(property);
    }

    public Property updateProperty(UUID id, UpdatePropertyRequest request) {
        Property property = findById(id);
        // Permission already verified in findById

        // Update only non-null fields
        if (request.getAddress1() != null) property.setAddress1(request.getAddress1());
        if (request.getAddress2() != null) property.setAddress2(request.getAddress2());
        if (request.getCity() != null) property.setCity(request.getCity());
        if (request.getState() != null) property.setState(request.getState());
        if (request.getZipCode() != null) property.setZipCode(request.getZipCode());
        if (request.getBedrooms() != null) property.setBedrooms(request.getBedrooms());
        if (request.getBathrooms() != null) property.setBathrooms(request.getBathrooms());
        if (request.getSquareFeet() != null) property.setSquareFeet(request.getSquareFeet());
        if (request.getMarketValue() != null) property.setMarketValue(request.getMarketValue());
        if (request.getPurchasePrice() != null) property.setPurchasePrice(request.getPurchasePrice());
        if (request.getPurchaseDate() != null) property.setPurchaseDate(request.getPurchaseDate());
        if (request.getDebt() != null) property.setDebt(request.getDebt());
        if (request.getInterestRate() != null) property.setInterestRate(request.getInterestRate());
        if (request.getMonthlyHoa() != null) property.setMonthlyHoa(request.getMonthlyHoa());
        if (request.getMonthlyPropertyTax() != null) property.setMonthlyPropertyTax(request.getMonthlyPropertyTax());
        if (request.getMonthlyInsurance() != null) property.setMonthlyInsurance(request.getMonthlyInsurance());
        if (request.getNotes() != null) property.setNotes(request.getNotes());
        if (request.getArchived() != null) property.setArchived(request.getArchived());

        return propertyRepository.save(property);
    }

    @Transactional(readOnly = true)
    public List<Property> getAllProperties() {
        UUID currentUserId = authenticationService.getCurrentUserId();
        UUID activePortfolioId = authenticationService.requireActivePortfolioId();

        // Verify user has view permission
        permissionService.requireViewPermission(currentUserId, activePortfolioId);

        return propertyRepository.findByPortfolioId(activePortfolioId);
    }

    @Transactional(readOnly = true)
    public List<Property> getActiveProperties() {
        UUID currentUserId = authenticationService.getCurrentUserId();
        UUID activePortfolioId = authenticationService.requireActivePortfolioId();

        // Verify user has view permission
        permissionService.requireViewPermission(currentUserId, activePortfolioId);

        return propertyRepository.findByPortfolioIdAndArchivedFalse(activePortfolioId);
    }

    @Transactional(readOnly = true)
    public Property findById(UUID id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID: " + id));

        // Verify user has permission to view this property's portfolio
        UUID currentUserId = authenticationService.getCurrentUserId();
        if (property.getPortfolio() != null) {
            permissionService.requireViewPermission(currentUserId, property.getPortfolio().getId());
        } else {
            // Fallback to old owner check for properties without portfolio
            if (!authenticationService.isCurrentUserOwner(property.getOwner().getId())) {
                throw new IllegalArgumentException("Property not found with ID: " + id);
            }
        }

        return property;
    }

    public void deleteProperty(UUID id) {
        Property property = findById(id);

        // Verify user has manage permission
        UUID currentUserId = authenticationService.getCurrentUserId();
        if (property.getPortfolio() != null) {
            permissionService.requireManagePermission(currentUserId, property.getPortfolio().getId());
        }

        property.setArchived(true);
        propertyRepository.save(property);
    }

    public void hardDeleteProperty(UUID id) {
        Property property = findById(id);

        // Verify user has manage permission
        UUID currentUserId = authenticationService.getCurrentUserId();
        if (property.getPortfolio() != null) {
            permissionService.requireManagePermission(currentUserId, property.getPortfolio().getId());
        }

        propertyRepository.deleteById(id);
    }
}
