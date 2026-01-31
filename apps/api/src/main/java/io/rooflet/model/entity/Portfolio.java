package io.rooflet.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "portfolios")
@Schema(description = "Portfolio entity representing a collection of properties under single ownership")
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Portfolio ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    @Schema(description = "Portfolio name", example = "Mom's Properties")
    private String name;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Portfolio description", example = "Properties managed on behalf of my mother")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    @Schema(description = "Whether the portfolio is archived", example = "false")
    private Boolean archived = false;

    @CreationTimestamp
    @Schema(description = "Portfolio creation timestamp")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Schema(description = "Portfolio last update timestamp")
    private LocalDateTime updatedAt;
}

