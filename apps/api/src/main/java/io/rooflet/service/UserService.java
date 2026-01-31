package io.rooflet.service;

import io.rooflet.model.entity.User;
import io.rooflet.model.request.CreateUserRequest;
import io.rooflet.model.request.UpdatePasswordRequest;
import io.rooflet.model.request.UpdateUserRequest;
import io.rooflet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Verify new password meets minimum length requirement
        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordService.hashPassword(request.getPassword()))
                .build();

        return userRepository.save(user);
    }

    public User updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if email is being changed to one that already exists (but not the current user's email)
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        return userRepository.save(user);
    }

    public User updatePassword(UUID userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify current password
        if (!passwordService.verifyPassword(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Verify new password and confirmation match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        // Verify new password meets minimum length requirement
        if (request.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        // Update password
        user.setPassword(passwordService.hashPassword(request.getNewPassword()));

        return userRepository.save(user);
    }
}
