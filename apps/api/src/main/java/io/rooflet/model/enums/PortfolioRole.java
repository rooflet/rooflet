package io.rooflet.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "Portfolio role enumeration")
@Getter
@RequiredArgsConstructor
public enum PortfolioRole {
    OWNER("Owner"),
    MANAGER("Manager"),
    VIEWER("Viewer");

    private final String displayName;
}

