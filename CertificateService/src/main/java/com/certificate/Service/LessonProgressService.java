package com.certificate.Service;

import com.persistence.Entity.Lesson;
import com.persistence.Entity.LessonProgress;
import com.persistence.Entity.Enrollment;
import com.persistence.Repository.LessonProgressRepository;
import com.persistence.Repository.LessonRepository;
import com.persistence.Repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class LessonProgressService {

    public LessonProgressService(LessonProgressRepository lessonProgressRepository, LessonRepository lessonRepository, EnrollmentRepository enrollmentRepository, CertificateService certificateService) {
        this.lessonProgressRepository = lessonProgressRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.certificateService = certificateService;
    }

    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CertificateService certificateService;


    public LessonProgress markLessonAsComplete(Long enrollmentId, Long lessonId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found: " + enrollmentId));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found: " + lessonId));

        LessonProgress progress = lessonProgressRepository
                .findByEnrollmentIdAndLessonId(enrollmentId, lessonId)
                .orElse(LessonProgress.builder()
                        .enrollment(enrollment)
                        .lesson(lesson)
                        .isComplete(false)
                        .build());

        progress.setIsComplete(true);
        progress.setCompletedAt(LocalDateTime.now());
        LessonProgress savedProgress = lessonProgressRepository.save(progress);

        // Trigger certificate generation (DRY: use CertificateService)
        try {
            certificateService.generateCertificateByEnrollmentId(enrollmentId);
        } catch (RuntimeException ignored) {
            // Not all lessons completed yet — safe to ignore
        }

        return savedProgress;
    }

    public List<LessonProgress> getProgressByEnrollment(Long enrollmentId) {
        return lessonProgressRepository.findByEnrollmentId(enrollmentId);
    }
}
