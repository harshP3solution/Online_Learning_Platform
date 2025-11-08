package com.enrollment.Controller;

import com.enrollment.Service.EnrollmentService;
import com.persistence.DTO.ApiResponse;
import com.persistence.DTO.EnrollmentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // 🔍 SEARCH ENROLLMENTS (HATEOAS)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<EnrollmentDTO>>>> searchEnrollments(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String courseTitle) {

        CollectionModel<EntityModel<EnrollmentDTO>> enrollments =
                enrollmentService.searchEnrollments(studentId, courseId, completed, studentName, courseTitle);

        if (enrollments.getContent().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok("No enrollments found matching criteria", enrollments));
        }

        return ResponseEntity.ok(ApiResponse.ok("Enrollments fetched successfully", enrollments));
    }

    // 🧑‍🎓 ENROLL STUDENT (HATEOAS)
    @PostMapping("/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<EntityModel<EnrollmentDTO>>> enrollStudent(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {

        EntityModel<EnrollmentDTO> enrollment = enrollmentService.enrollStudent(studentId, courseId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Student enrolled successfully", enrollment));
    }

    // 🎓 GET ENROLLMENTS BY STUDENT (HATEOAS)
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<EnrollmentDTO>>>> getEnrollmentsByStudent(
            @PathVariable Long studentId) {

        CollectionModel<EntityModel<EnrollmentDTO>> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);

        if (enrollments.getContent().isEmpty()) {
            throw new NoSuchElementException("No enrollments found for student ID " + studentId);
        }

        return ResponseEntity.ok(ApiResponse.ok("Enrollments fetched successfully", enrollments));
    }

    // 📘 GET ENROLLMENTS BY COURSE (HATEOAS)
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<EnrollmentDTO>>>> getEnrollmentsByCourse(
            @PathVariable Long courseId) {

        CollectionModel<EntityModel<EnrollmentDTO>> enrollments = enrollmentService.getEnrollmentsByCourse(courseId);

        if (enrollments.getContent().isEmpty()) {
            throw new NoSuchElementException("No enrollments found for course ID " + courseId);
        }

        return ResponseEntity.ok(ApiResponse.ok("Enrollments fetched successfully", enrollments));
    }

    // 👁️ GET SINGLE ENROLLMENT BY ID (for self link in service)
    @GetMapping("/{enrollmentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<EnrollmentDTO>>> getEnrollmentById(@PathVariable Long enrollmentId) {
        // ⚠️ Only for HATEOAS self-links; simple finder
        var allEnrollments = enrollmentService.searchEnrollments(null, null, null, null, null)
                .getContent().stream()
                .filter(e -> e.getContent().getId().equals(enrollmentId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Enrollment not found with ID " + enrollmentId));

        return ResponseEntity.ok(ApiResponse.ok("Enrollment fetched successfully", allEnrollments));
    }
}
