package io.rooflet.service;

import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    // NIST recommends BCrypt with cost factor 12 or higher for 2023+
    @Getter
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public String hashPassword(String plainTextPassword) {
        return passwordEncoder.encode(plainTextPassword);
    }

    public boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        return passwordEncoder.matches(plainTextPassword, hashedPassword);
    }
}
