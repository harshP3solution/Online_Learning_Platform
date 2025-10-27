package com.userservices.Controller;

import com.persistence.Entity.EmailLog;
import com.persistence.Repository.EmailLogRepository;
import com.userservices.Service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailService emailService;
    private final EmailLogRepository emailLogRepository;

    public EmailController(EmailService emailService, EmailLogRepository emailLogRepository) {
        this.emailService = emailService;
        this.emailLogRepository = emailLogRepository;
    }

    // Get all email logs — ADMIN only
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmailLog>> getAllEmailLogs() {
        return ResponseEntity.ok(emailLogRepository.findAll());
    }

    // Get email logs by user — ADMIN or STUDENT or INSTRUCTOR
    @GetMapping("/logs/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    public ResponseEntity<List<EmailLog>> getEmailLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(emailLogRepository.findByUserId(userId));
    }

    // Get email logs by course — ADMIN or INSTRUCTOR
    @GetMapping("/logs/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<EmailLog>> getEmailLogsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(emailLogRepository.findByCourseId(courseId));
    }
}
