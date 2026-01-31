package io.rooflet.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "Expense category enumeration")
@Getter
@RequiredArgsConstructor
public enum ExpenseCategory {
    REPAIRS_MAINTENANCE("Repairs & Maintenance"),
    UTILITIES("Utilities"),
    PROPERTY_MANAGEMENT("Property Management"),
    INSURANCE("Insurance"),
    PROPERTY_TAX("Property Tax"),
    HOA_FEES("HOA Fees"),
    LEGAL("Legal"),
    PROFESSIONAL_SERVICES("Professional Services"),
    LANDSCAPING("Landscaping"),
    CLEANING("Cleaning"),
    CAPITAL_IMPROVEMENTS("Capital Improvements"),
    MARKETING("Marketing"),
    OTHER("Other");

    private final String displayName;
}
