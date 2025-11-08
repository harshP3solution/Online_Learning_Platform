package com.userservices.Service;

import com.persistence.Entity.User;
import com.persistence.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;// your existing email sender

    public String forgotPassword(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found with email: " + email));

        // Generate random reset token
        String token = UUID.randomUUID().toString();

        // Set token & expiry (valid for 15 minutes)
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepo.save(user);

        // Create reset link (frontend or backend endpoint)
        String resetLink = "http://localhost:8080/auth/reset-password?token=" + token;

        // Send email
        String subject = "Password Reset Request";
        String body = String.format("""
                Hi %s,
                
                You requested to reset your password. Click the link below:
                %s
                
                This link is valid for 15 minutes.
                
                If you didn't request this, ignore this email.
                """, user.getFullName(), resetLink);

        emailService.sendEmail(user.getEmail(), subject, body);

        return "Password reset link sent to " + email;
    }
    public String resetPassword(String token, String newPassword) {
        User user = userRepo.findByResetToken(token)
                .orElseThrow(() -> new NoSuchElementException("Invalid or expired token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        // Encrypt password before saving
        user.setPasswordHash(passwordEncoder.encode(newPassword));

        // Clear token after reset
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepo.save(user);

        return "Password reset successful";
    }

}
