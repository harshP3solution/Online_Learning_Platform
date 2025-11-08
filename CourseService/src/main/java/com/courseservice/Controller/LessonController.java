package com.courseservice.Controller;

import com.courseservice.Service.LessonService;
import com.persistence.DTO.ApiResponse;
import com.persistence.DTO.LessonRequestDTO;
import com.persistence.Entity.Lesson;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<EntityModel<Lesson>>> addLesson(@PathVariable Long courseId, @RequestBody @Valid Lesson lesson) {
        Lesson created = lessonService.addLessonToCourse(courseId, lesson).getContent();

        EntityModel<Lesson> model = EntityModel.of(created,
                linkTo(methodOn(LessonController.class).getLessonById(created.getId())).withSelfRel(),
                linkTo(methodOn(LessonController.class).getLessonsByCourse(courseId)).withRel("course-lessons"),
                linkTo(methodOn(LessonController.class).updateLesson(created.getId(), null)).withRel("update"),
                linkTo(methodOn(LessonController.class).deleteLesson(created.getId())).withRel("delete")
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Lesson added successfully", model));
    }
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<Lesson>>>> getLessonsByCourse(@PathVariable Long courseId) {

        // Fetch lessons from service (now returns CollectionModel)
        CollectionModel<EntityModel<Lesson>> lessons = lessonService.getLessonsByCourse(courseId);

        if (lessons.getContent().isEmpty()) {
            throw new NoSuchElementException("No lessons found for course " + courseId);
        }

        // Return wrapped HATEOAS response
        return ResponseEntity.ok(ApiResponse.ok("Lessons fetched successfully", lessons));
    }



    @GetMapping("/{lessonId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<EntityModel<Lesson>>> getLessonById(@PathVariable Long lessonId) {
        Lesson lesson = lessonService.getLessonById(lessonId).getContent();
        if (lesson == null) throw new NoSuchElementException("Lesson not found with ID " + lessonId);

        EntityModel<Lesson> model = EntityModel.of(lesson,
                linkTo(methodOn(LessonController.class).getLessonById(lessonId)).withSelfRel(),
                linkTo(methodOn(LessonController.class).updateLesson(lessonId, null)).withRel("update"),
                linkTo(methodOn(LessonController.class).deleteLesson(lessonId)).withRel("delete")
        );

        return ResponseEntity.ok(ApiResponse.ok("Lesson fetched successfully", model));
    }

    @PutMapping("/{lessonId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<Lesson>>> updateLesson(
            @PathVariable Long lessonId,
            @RequestBody @Valid LessonRequestDTO dto) {

        Lesson updatedLesson = lessonService.updateLesson(lessonId, dto).getContent();

        EntityModel<Lesson> model = EntityModel.of(updatedLesson,
                linkTo(methodOn(LessonController.class).getLessonById(lessonId)).withSelfRel(),
                linkTo(methodOn(LessonController.class).getLessonsByCourse(updatedLesson.getCourse().getId())).withRel("course-lessons"),
                linkTo(methodOn(LessonController.class).deleteLesson(lessonId)).withRel("delete")
        );

        return ResponseEntity.ok(ApiResponse.ok("Lesson updated successfully", model));
    }

    @DeleteMapping("/{lessonId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable Long lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok(ApiResponse.ok("Lesson deleted successfully", null));
    }
}
