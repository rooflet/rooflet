package io.rooflet.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "Property type enumeration")
@Getter
@RequiredArgsConstructor
public enum PropertyType {
    SINGLE_FAMILY("Single Family"),
    CONDO("Condo"),
    TOWN_HOUSE("Town House"),
    MULTI_FAMILY("Multi Family");

    private final String displayName;
}
