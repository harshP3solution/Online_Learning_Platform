package com.certificate.Controller;

import com.certificate.Service.LessonProgressService;
import com.persistence.DTO.ApiResponse;
import com.persistence.Entity.LessonProgress;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/lesson-progress")
public class LessonProgressController {

    private final LessonProgressService lessonProgressService;

    public LessonProgressController(LessonProgressService lessonProgressService) {
        this.lessonProgressService = lessonProgressService;
    }

    @PostMapping("/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<LessonProgress>> markLessonAsComplete(
            @RequestParam Long enrollmentId,
            @RequestParam Long lessonId) {

        LessonProgress progress = lessonProgressService.markLessonAsComplete(enrollmentId, lessonId);
        if (progress == null) {
            throw new NoSuchElementException("Invalid enrollment or lesson ID");
        }
        return ResponseEntity.ok(ApiResponse.ok("Lesson marked as complete", progress));
    }

    @GetMapping("/{enrollmentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<LessonProgress>>> getProgress(@PathVariable Long enrollmentId) {
        List<LessonProgress> progressList = lessonProgressService.getProgressByEnrollment(enrollmentId);
        if (progressList.isEmpty()) {
            throw new NoSuchElementException("No progress found for enrollment ID: " + enrollmentId);
        }
        return ResponseEntity.ok(ApiResponse.ok("Lesson progress fetched successfully", progressList));
    }
}
