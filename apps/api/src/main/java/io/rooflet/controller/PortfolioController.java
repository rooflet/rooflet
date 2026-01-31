package io.rooflet.controller;

import io.rooflet.model.entity.Portfolio;
import io.rooflet.model.entity.PortfolioMember;
import io.rooflet.model.entity.User;
import io.rooflet.model.request.AddPortfolioMemberRequest;
import io.rooflet.model.request.CreatePortfolioRequest;
import io.rooflet.model.request.UpdatePortfolioMemberRoleRequest;
import io.rooflet.model.request.UpdatePortfolioRequest;
import io.rooflet.model.response.MessageResponse;
import io.rooflet.model.response.PortfolioMemberResponse;
import io.rooflet.model.response.PortfolioResponse;
import io.rooflet.repository.UserRepository;
import io.rooflet.service.PortfolioService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
@Validated
@Tag(name = "Portfolio Management", description = "API for managing property portfolios and members")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserRepository userRepository;

    @Operation(summary = "Get all portfolios", description = "Retrieves all portfolios accessible to the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolios retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PortfolioResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> getAllPortfolios(
            @Parameter(description = "Show only active (non-archived) portfolios", example = "true")
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        List<Portfolio> portfolios = activeOnly
                ? portfolioService.getActivePortfolios()
                : portfolioService.getAccessiblePortfolios();

        List<PortfolioResponse> responses = portfolios.stream()
                .map(PortfolioResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Create a new portfolio", description = "Creates a new portfolio. The creator becomes the owner.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Portfolio created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<PortfolioResponse> createPortfolio(@Valid @RequestBody CreatePortfolioRequest request) {
        Portfolio portfolio = portfolioService.createPortfolio(request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(PortfolioResponse.from(portfolio));
    }

    @Operation(summary = "Get a portfolio by ID", description = "Retrieves a specific portfolio by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio not found",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable UUID id) {
        Portfolio portfolio = portfolioService.getPortfolio(id);
        return ResponseEntity.ok(PortfolioResponse.from(portfolio));
    }

    @Operation(summary = "Update portfolio details", description = "Updates portfolio name and/or description. Requires OWNER permission.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio not found",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<PortfolioResponse> updatePortfolio(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePortfolioRequest request) {
        Portfolio portfolio = portfolioService.updatePortfolio(id, request.getName(), request.getDescription());
        return ResponseEntity.ok(PortfolioResponse.from(portfolio));
    }

    @Operation(summary = "Archive a portfolio", description = "Archives a portfolio. Requires OWNER permission.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio archived successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio not found",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content)
    })
    @PutMapping("/{id}/archive")
    public ResponseEntity<PortfolioResponse> archivePortfolio(@PathVariable UUID id) {
        Portfolio portfolio = portfolioService.archivePortfolio(id);
        return ResponseEntity.ok(PortfolioResponse.from(portfolio));
    }

    @Operation(summary = "Unarchive a portfolio", description = "Unarchives a portfolio. Requires OWNER permission.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio unarchived successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio not found",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content)
    })
    @PutMapping("/{id}/unarchive")
    public ResponseEntity<PortfolioResponse> unarchivePortfolio(@PathVariable UUID id) {
        Portfolio portfolio = portfolioService.unarchivePortfolio(id);
        return ResponseEntity.ok(PortfolioResponse.from(portfolio));
    }

    @Operation(summary = "Switch active portfolio", description = "Sets the specified portfolio as the active portfolio in the session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio switched successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio not found",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Cannot switch to archived portfolio",
                    content = @Content)
    })
    @PostMapping("/{id}/switch")
    public ResponseEntity<MessageResponse> switchPortfolio(@PathVariable UUID id) {
        portfolioService.switchPortfolio(id);
        return ResponseEntity.ok(new MessageResponse("Successfully switched to portfolio " + id));
    }

    @Operation(summary = "Get portfolio members", description = "Retrieves all members of a portfolio. Requires VIEW permission.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Members retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PortfolioMemberResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio not found",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content)
    })
    @GetMapping("/{id}/members")
    public ResponseEntity<List<PortfolioMemberResponse>> getPortfolioMembers(@PathVariable UUID id) {
        List<PortfolioMember> members = portfolioService.getPortfolioMembers(id);
        List<PortfolioMemberResponse> responses = members.stream()
                .map(PortfolioMemberResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Add a member to portfolio", description = "Adds a user to the portfolio with the specified role. Requires OWNER permission.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member added successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PortfolioMemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or user already a member",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Portfolio or user not found",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content)
    })
    @PostMapping("/{id}/members")
    public ResponseEntity<PortfolioMemberResponse> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddPortfolioMemberRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + request.getEmail()));

        PortfolioMember member = portfolioService.addMember(id, user, request.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(PortfolioMemberResponse.from(member));
    }

    @Operation(summary = "Update member role", description = "Updates a member's role in the portfolio. Requires OWNER permission.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member role updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PortfolioMemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or cannot change last owner's role",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Portfolio or member not found",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content)
    })
    @PutMapping("/{portfolioId}/members/{userId}")
    public ResponseEntity<PortfolioMemberResponse> updateMemberRole(
            @PathVariable UUID portfolioId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdatePortfolioMemberRoleRequest request) {
        PortfolioMember member = portfolioService.updateMemberRole(portfolioId, userId, request.getRole());
        return ResponseEntity.ok(PortfolioMemberResponse.from(member));
    }

    @Operation(summary = "Remove a member from portfolio", description = "Removes a user from the portfolio. Requires OWNER permission.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member removed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cannot remove last owner",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Portfolio or member not found",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content)
    })
    @DeleteMapping("/{portfolioId}/members/{userId}")
    public ResponseEntity<MessageResponse> removeMember(
            @Parameter(description = "Portfolio ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID portfolioId,
            @Parameter(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID userId) {
        portfolioService.removeMember(portfolioId, userId);
        return ResponseEntity.ok(new MessageResponse("Member removed successfully"));
    }
}
