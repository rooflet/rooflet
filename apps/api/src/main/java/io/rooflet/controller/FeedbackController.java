package io.rooflet.controller;

import io.rooflet.model.entity.User;
import io.rooflet.model.request.FeedbackRequest;
import io.rooflet.model.response.MessageResponse;
import io.rooflet.service.AuthenticationService;
import io.rooflet.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<MessageResponse> submitFeedback(@RequestBody FeedbackRequest feedbackRequest) {
        User user = authenticationService.getCurrentUser();
        feedbackService.saveFeedback(feedbackRequest, user);
        return ResponseEntity.ok(new MessageResponse("Feedback submitted successfully"));
    }
}
