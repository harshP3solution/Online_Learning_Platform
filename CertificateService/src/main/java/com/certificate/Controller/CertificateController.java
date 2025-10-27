package com.certificate.Controller;

import com.persistence.DTO.CertificateDTO;
import com.persistence.Entity.Certificate;
import com.certificate.Service.CertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    // Manually trigger certificate generation
    @PostMapping("/generate/{enrollmentId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CertificateDTO> generateCertificate(@PathVariable Long enrollmentId) {
        CertificateDTO certificate = certificateService.generateCertificateByEnrollmentId(enrollmentId);
        return ResponseEntity.ok(certificate);
    }

    // Get all certificates for a student
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<List<CertificateDTO>> getCertificatesByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(certificateService.getCertificatesByStudent(studentId));
    }

    // Get all certificates for a course
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<List<CertificateDTO>> getCertificatesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(certificateService.getCertificatesByCourse(courseId));
    }
}
