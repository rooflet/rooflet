package io.rooflet.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error response with detailed information")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error message", example = "Validation failed")
    private String message;

    @Schema(description = "Detailed error information", example = "Tenant must be associated with a property")
    private String details;

    @Schema(description = "List of validation errors")
    private List<FieldError> fieldErrors;

    @Schema(description = "Timestamp when the error occurred")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Request path that caused the error", example = "/api/rent-collections/bulk")
    private String path;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        @Schema(description = "Field name that failed validation", example = "items[0].paymentDate")
        private String field;

        @Schema(description = "Rejected value")
        private Object rejectedValue;

        @Schema(description = "Validation error message", example = "Payment date is required")
        private String message;
    }
}

