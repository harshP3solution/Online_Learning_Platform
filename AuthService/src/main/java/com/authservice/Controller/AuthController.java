package com.authservice.Controller;

import com.authservice.config.JwtService;
import com.persistence.DTO.ApiResponse;
import com.persistence.Entity.User;
import com.persistence.Repository.UserRepo;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepo userRepo, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> signup(@RequestBody User user) {
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User already exists with this email");
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        userRepo.save(user);

        Map<String, Object> result = Map.of("role", user.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User created successfully", result));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody User request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new NoSuchElementException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPasswordHash(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(ApiResponse.ok("Login successful", Map.of("token", token)));
    }
}
