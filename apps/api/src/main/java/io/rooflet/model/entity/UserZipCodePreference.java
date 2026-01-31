package io.rooflet.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_zip_code_preferences")
@IdClass(UserZipCodePreference.UserZipCodePreferenceId.class)
@Schema(description = "User-specific zip code preferences for watching market listings")
public class UserZipCodePreference {

    @Id
    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    @Schema(description = "User ID", example = "11111111-1111-1111-1111-111111111111")
    private UUID userId;

    @Id
    @Column(name = "zip_code", length = 10)
    @Schema(description = "ZIP code", example = "02171")
    private String zipCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    // Composite primary key class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserZipCodePreferenceId implements Serializable {
        private UUID userId;
        private String zipCode;
    }
}

