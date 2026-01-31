package io.rooflet.controller;

import io.rooflet.model.entity.Portfolio;
import io.rooflet.model.entity.User;
import io.rooflet.model.request.CreateUserRequest;
import io.rooflet.model.request.LoginRequest;
import io.rooflet.model.request.UpdatePasswordRequest;
import io.rooflet.model.request.UpdateUserRequest;
import io.rooflet.model.response.LoginResponse;
import io.rooflet.model.response.UserResponse;
import io.rooflet.repository.PortfolioRepository;
import io.rooflet.service.AuthenticationService;
import io.rooflet.service.PortfolioService;
import io.rooflet.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "User Management", description = "API for managing users in the landlord system")
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final PortfolioService portfolioService;
    private final PortfolioRepository portfolioRepository;

    @Operation(summary = "Login user", description = "Authenticates a user with username/email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            User user = authenticationService.authenticateUser(loginRequest);
            return ResponseEntity.ok(LoginResponse.from(user));
        } catch (BadCredentialsException e) {
            log.info("Failed login attempt for username/email: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.info("Error during login for username/email: {}", loginRequest.getEmail());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Create a new user", description = "Creates a new user account with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or username/email already exists",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Validation error",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(request);
            log.info("User created successfully with email: {}, username: {}, id: {}",
                    user.getEmail(), user.getUsername(), user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
        } catch (IllegalArgumentException e) {
            String errorMessage = e.getMessage();

            if ("Email already exists".equals(errorMessage)) {
                log.warn("Failed to create user - email already exists: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "User with this email already exists",
                                "email", request.getEmail()));
            } else if (errorMessage.contains("password must be at least")) {
                log.warn("Failed to create user - password validation failed for email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", errorMessage));
            } else {
                log.warn("Failed to create user with email: {} - {}", request.getEmail(), errorMessage);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", errorMessage));
            }
        } catch (Exception e) {
            log.error("Unexpected error creating user with email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred while creating the user"));
        }
    }

    @Operation(summary = "Get current user", description = "Retrieves the currently authenticated user's profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current user retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        try {
            User user = authenticationService.getCurrentUser();

            // Ensure user has an active portfolio
            UUID activePortfolioId = portfolioService.ensureActivePortfolio();

            // Get portfolio name
            String portfolioName = null;
            if (activePortfolioId != null) {
                portfolioName = portfolioRepository.findById(activePortfolioId)
                        .map(Portfolio::getName)
                        .orElse(null);
            }

            return ResponseEntity.ok(UserResponse.from(user, activePortfolioId, portfolioName));
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "Logout user", description = "Logs out the current user and invalidates their session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged out"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        try {
            // Clear the security context
            SecurityContextHolder.clearContext();

            // Invalidate the HTTP session if it exists
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "Update current user", description = "Updates the currently authenticated user's profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or email already exists",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Validation error",
                    content = @Content)
    })
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(@Valid @RequestBody UpdateUserRequest request) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            User updatedUser = userService.updateUser(currentUser.getId(), request);
            return ResponseEntity.ok(UserResponse.from(updatedUser));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating user", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "Update current user password", description = "Updates the currently authenticated user's password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid current password or passwords don't match",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Validation error",
                    content = @Content)
    })
    @PutMapping("/me/password")
    public ResponseEntity<UserResponse> updateCurrentUserPassword(@Valid @RequestBody UpdatePasswordRequest request) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            User updatedUser = userService.updatePassword(currentUser.getId(), request);
            return ResponseEntity.ok(UserResponse.from(updatedUser));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update password: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating password", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
