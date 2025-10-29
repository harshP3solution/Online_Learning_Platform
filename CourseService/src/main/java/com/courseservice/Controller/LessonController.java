package com.courseservice.Controller;

import com.courseservice.Service.LessonService;
import com.persistence.DTO.ApiResponse;
import com.persistence.Entity.Lesson;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Lesson>> addLesson(@PathVariable Long courseId, @RequestBody @Valid Lesson lesson) {
        Lesson created = lessonService.addLessonToCourse(courseId, lesson);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Lesson added successfully", created));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<Lesson>>> getLessonsByCourse(@PathVariable Long courseId) {
        List<Lesson> lessons = lessonService.getLessonsByCourse(courseId);
        if (lessons.isEmpty()) throw new NoSuchElementException("No lessons found for course " + courseId);
        return ResponseEntity.ok(ApiResponse.ok("Lessons fetched successfully", lessons));
    }

    @GetMapping("/{lessonId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<Lesson>> getLessonById(@PathVariable Long lessonId) {
        Lesson lesson = lessonService.getLessonById(lessonId);
        if (lesson == null) throw new NoSuchElementException("Lesson not found with ID " + lessonId);
        return ResponseEntity.ok(ApiResponse.ok("Lesson fetched successfully", lesson));
    }

    @PutMapping("/{lessonId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Lesson>> updateLesson(@PathVariable Long lessonId, @RequestBody @Valid Lesson lesson) {
        Lesson updated = lessonService.updateLesson(lessonId, lesson);
        return ResponseEntity.ok(ApiResponse.ok("Lesson updated successfully", updated));
    }

    @DeleteMapping("/{lessonId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable Long lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok(ApiResponse.ok("Lesson deleted successfully", null));
    }
}
