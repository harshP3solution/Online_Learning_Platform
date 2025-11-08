package com.courseservice.Controller;

import com.courseservice.Service.CourseService;
import com.persistence.DTO.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // 🔍 SEARCH COURSES
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<EntityModel<CourseDTO>>>> searchCourses(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long instructorId) {

        List<EntityModel<CourseDTO>> courses = courseService.searchCourses(title, category, instructorId);

        if (courses == null || courses.isEmpty()) {
            String message = "No courses found matching the given criteria.";

            if (title != null && !title.trim().isEmpty()) {
                message = "No course found with the title: '" + title + "'";
            } else if (category != null && !category.trim().isEmpty()) {
                message = "No course found under the category: '" + category + "'";
            } else if (instructorId != null) {
                message = "No courses found for instructor with ID: " + instructorId;
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
        }

        return ResponseEntity.ok(ApiResponse.ok("Courses fetched successfully", courses));
    }

    // 🟢 CREATE COURSE (HATEOAS)
    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<CourseDTO>>> createCourse(@RequestBody @Valid CourseRequestDTO dto) {
        EntityModel<CourseDTO> created = courseService.createCourse(dto);

        // Self link for location header
        Link selfLink = linkTo(methodOn(CourseController.class).getCourseById(created.getContent().getId()))
                .withSelfRel();

        return ResponseEntity.created(selfLink.toUri())
                .body(ApiResponse.ok("Course created successfully", created));
    }

    // 🟠 UPDATE COURSE (HATEOAS)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<EntityModel<CourseDTO>>> updateCourse(@PathVariable Long id,
                                                                            @RequestBody @Valid CourseRequestDTO dto) {
        try {
            EntityModel<CourseDTO> updated = courseService.updateCourse(id, dto);
            return ResponseEntity.ok(ApiResponse.ok("Course updated successfully", updated));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // 🔵 GET COURSE BY ID (HATEOAS)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<EntityModel<CourseDTO>>> getCourseById(@PathVariable Long id) {
        try {
            EntityModel<CourseDTO> courseModel = courseService.getCourseById(id);
            return ResponseEntity.ok(ApiResponse.ok("Course fetched successfully", courseModel));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // 🟣 GET ALL COURSES (HATEOAS)
    @GetMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<CourseDTO>>>> getAllCourses() {
        CollectionModel<EntityModel<CourseDTO>> allCourses = courseService.getAllCourses();

        if (allCourses.getContent().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No courses available"));
        }

        return ResponseEntity.ok(ApiResponse.ok("All courses fetched successfully", allCourses));
    }

    // 🔴 DELETE COURSE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.ok(ApiResponse.ok("Course deleted successfully", null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
