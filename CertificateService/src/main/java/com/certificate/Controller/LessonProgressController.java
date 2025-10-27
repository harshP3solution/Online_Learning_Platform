package com.certificate.Controller;

import com.certificate.Service.LessonProgressService;
import com.persistence.Entity.LessonProgress;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/lesson-progress")
public class LessonProgressController {

    private final LessonProgressService lessonProgressService;

    public LessonProgressController(LessonProgressService lessonProgressService) {
        this.lessonProgressService = lessonProgressService;
    }

    @PostMapping("/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LessonProgress> markLessonAsComplete(
            @RequestParam Long enrollmentId,
            @RequestParam Long lessonId) {

        LessonProgress progress = lessonProgressService.markLessonAsComplete(enrollmentId, lessonId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{enrollmentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR')")
    public ResponseEntity<List<LessonProgress>> getProgress(@PathVariable Long enrollmentId) {
        List<LessonProgress> progressList = lessonProgressService.getProgressByEnrollment(enrollmentId);
        return ResponseEntity.ok(progressList);
    }
}
