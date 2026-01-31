package io.rooflet.service;

import io.rooflet.model.Feedback;
import io.rooflet.model.entity.User;
import io.rooflet.model.request.FeedbackRequest;
import io.rooflet.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    public void saveFeedback(FeedbackRequest feedbackRequest, User user) {
        Feedback feedback = new Feedback();
        feedback.setFeedbackType(feedbackRequest.getFeedbackType());
        feedback.setMessage(feedbackRequest.getMessage());
        feedback.setCurrentPage(feedbackRequest.getCurrentPage());
        feedback.setUser(user);
        feedbackRepository.save(feedback);
    }
}

