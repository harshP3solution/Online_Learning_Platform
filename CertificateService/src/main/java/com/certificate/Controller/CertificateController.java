package com.certificate.Controller;

import com.certificate.Service.CertificateService;
import com.persistence.DTO.ApiResponse;
import com.persistence.DTO.CertificateDTO;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping("/generate/{enrollmentId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CertificateDTO>> generateCertificate(@PathVariable Long enrollmentId) {
        CertificateDTO certificate = certificateService.generateCertificateByEnrollmentId(enrollmentId);
        if (certificate == null) {
            throw new NoSuchElementException("No enrollment found with ID: " + enrollmentId);
        }
        return ResponseEntity.ok(ApiResponse.ok("Certificate generated successfully", certificate));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<CertificateDTO>>> getCertificatesByStudent(@PathVariable Long studentId) {
        List<CertificateDTO> list = certificateService.getCertificatesByStudent(studentId);
        if (list.isEmpty()) {
            throw new NoSuchElementException("No certificates found for student ID: " + studentId);
        }
        return ResponseEntity.ok(ApiResponse.ok("Certificates fetched successfully", list));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<CertificateDTO>>> getCertificatesByCourse(@PathVariable Long courseId) {
        List<CertificateDTO> list = certificateService.getCertificatesByCourse(courseId);
        if (list.isEmpty()) {
            throw new NoSuchElementException("No certificates found for course ID: " + courseId);
        }
        return ResponseEntity.ok(ApiResponse.ok("Certificates fetched successfully", list));
    }
}
