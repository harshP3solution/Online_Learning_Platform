package com.certificate.Service;

import com.certificate.event.CertificateGeneratedEvent;
import com.persistence.DTO.CertificateDTO;
import com.persistence.Entity.Certificate;
import com.persistence.Entity.Enrollment;
import com.persistence.Entity.User;
import com.persistence.Entity.Course;
import com.persistence.Repository.CertificateRepository;
import com.persistence.Repository.LessonProgressRepository;
import com.persistence.Repository.EnrollmentRepository;
import com.persistence.Repository.UserRepo;
import com.persistence.Repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserRepo userRepo;
    private final CourseRepository courseRepository;

    public CertificateDTO generateCertificateByEnrollmentId(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + enrollmentId));

        // Check lesson completion
        long totalLessons = lessonProgressRepository.countByEnrollmentId(enrollmentId);
        long completedLessons = lessonProgressRepository.countByEnrollmentIdAndIsCompleteTrue(enrollmentId);

        if (totalLessons == 0 || totalLessons != completedLessons) {
            throw new RuntimeException("Cannot generate certificate. Not all lessons completed!");
        }


        List<Certificate> existing = certificateRepository.findByEnrollment_Id(enrollmentId);
        Certificate certificate;
        if (!existing.isEmpty()) {
            certificate = existing.get(0);
        } else {
            // Create and save new certificate
            certificate = Certificate.builder()
                    .enrollment(enrollment)
                    .student(enrollment.getStudent())
                    .course(enrollment.getCourse())
                    .createdAt(LocalDateTime.now())
                    .completionDate(LocalDateTime.now())
                    .build();
            certificate = certificateRepository.save(certificate);

            // Send Kafka event
            CertificateGeneratedEvent event = CertificateGeneratedEvent.builder()
                    .certificateId(certificate.getId())
                    .studentId(enrollment.getStudent().getId())
                    .courseId(enrollment.getCourse().getId())
                    .courseTitle(enrollment.getCourse().getTitle())
                    .studentEmail(enrollment.getStudent().getEmail())
                    .build();
            kafkaTemplate.send("certificate-generated-topic", event);
        }

        // Convert entity to DTO for response
        return CertificateDTO.builder()
                .id(certificate.getId())
                .createdAt(certificate.getCreatedAt())
                .completionDate(certificate.getCompletionDate())
                .enrollmentId(certificate.getEnrollment().getId())
                .studentId(certificate.getStudent().getId())
                .studentEmail(certificate.getStudent().getEmail())
                .courseId(certificate.getCourse().getId())
                .courseTitle(certificate.getCourse().getTitle())
                .build();
    }

    /**
     * Get all certificates by student
     */
    public List<CertificateDTO> getCertificatesByStudent(Long studentId) {
        User student = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        return certificateRepository.findByStudent(student)
                .stream()
                .map(c -> CertificateDTO.builder()
                        .id(c.getId())
                        .createdAt(c.getCreatedAt())
                        .completionDate(c.getCompletionDate())
                        .enrollmentId(c.getEnrollment().getId())
                        .studentId(c.getStudent().getId())
                        .studentEmail(c.getStudent().getEmail())
                        .courseId(c.getCourse().getId())
                        .courseTitle(c.getCourse().getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get all certificates by course
     */
    public List<CertificateDTO> getCertificatesByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        return certificateRepository.findByCourse(course)
                .stream()
                .map(c -> CertificateDTO.builder()
                        .id(c.getId())
                        .createdAt(c.getCreatedAt())
                        .completionDate(c.getCompletionDate())
                        .enrollmentId(c.getEnrollment().getId())
                        .studentId(c.getStudent().getId())
                        .studentEmail(c.getStudent().getEmail())
                        .courseId(c.getCourse().getId())
                        .courseTitle(c.getCourse().getTitle())
                        .build())
                .collect(Collectors.toList());
    }
}
