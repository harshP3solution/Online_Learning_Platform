package com.userservices.Controller;

import com.persistence.DTO.ApiResponse;
import com.persistence.Entity.EmailLog;
import com.persistence.Repository.EmailLogRepository;
import com.userservices.Service.EmailService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailService emailService;
    private final EmailLogRepository emailLogRepository;

    public EmailController(EmailService emailService, EmailLogRepository emailLogRepository) {
        this.emailService = emailService;
        this.emailLogRepository = emailLogRepository;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EmailLog>>> getAllEmailLogs() {
        return ResponseEntity.ok(ApiResponse.ok("All email logs fetched", emailLogRepository.findAll()));
    }

    @GetMapping("/logs/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<EmailLog>>> getEmailLogsByUser(@PathVariable Long userId) {
        List<EmailLog> logs = emailLogRepository.findByUserId(userId);
        if (logs.isEmpty()) throw new NoSuchElementException("No email logs found for user " + userId);
        return ResponseEntity.ok(ApiResponse.ok("Email logs fetched successfully", logs));
    }

    @GetMapping("/logs/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<EmailLog>>> getEmailLogsByCourse(@PathVariable Long courseId) {
        List<EmailLog> logs = emailLogRepository.findByCourseId(courseId);
        if (logs.isEmpty()) throw new NoSuchElementException("No email logs found for course " + courseId);
        return ResponseEntity.ok(ApiResponse.ok("Email logs fetched successfully", logs));
    }
}
