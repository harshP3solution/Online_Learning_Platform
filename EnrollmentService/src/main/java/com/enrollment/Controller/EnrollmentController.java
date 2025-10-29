package com.enrollment.Controller;

import com.enrollment.Service.EnrollmentService;
import com.persistence.DTO.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<EnrollmentResponseDTO>> enrollStudent(
            @RequestParam Long studentId, @RequestParam Long courseId) {
        EnrollmentResponseDTO enrollment = enrollmentService.enrollStudent(studentId, courseId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Student enrolled successfully", enrollment));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponseDTO>>> getEnrollmentsByStudent(@PathVariable Long studentId) {
        List<EnrollmentResponseDTO> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
        if (enrollments.isEmpty()) throw new NoSuchElementException("No enrollments found for student ID " + studentId);
        return ResponseEntity.ok(ApiResponse.ok("Enrollments fetched successfully", enrollments));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponseDTO>>> getEnrollmentsByCourse(@PathVariable Long courseId) {
        List<EnrollmentResponseDTO> enrollments = enrollmentService.getEnrollmentsByCourse(courseId);
        if (enrollments.isEmpty()) throw new NoSuchElementException("No enrollments found for course ID " + courseId);
        return ResponseEntity.ok(ApiResponse.ok("Enrollments fetched successfully", enrollments));
    }
}
