package io.rooflet.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {
    @NotBlank(message = "Feedback type is required")
    private String feedbackType;

    @NotBlank(message = "Feedback is required")
    private String message;
    
    @NotBlank(message = "Current page is required")
    private String currentPage;
}

