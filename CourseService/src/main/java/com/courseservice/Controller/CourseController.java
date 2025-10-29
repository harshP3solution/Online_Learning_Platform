package com.courseservice.Controller;

import com.courseservice.Service.CourseService;
import com.persistence.DTO.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> createCourse(@RequestBody @Valid CourseRequestDTO dto) {
        CourseResponseDTO created = courseService.createCourse(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Course created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> updateCourse(@PathVariable Long id,
                                                                       @RequestBody @Valid CourseRequestDTO dto) {
        CourseResponseDTO updated = courseService.updateCourse(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Course updated successfully", updated));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> getCourseById(@PathVariable Long id) {
        CourseResponseDTO course = courseService.getCourseById(id);
        if (course == null) throw new NoSuchElementException("Course not found with ID " + id);
        return ResponseEntity.ok(ApiResponse.ok("Course fetched successfully", course));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getAllCourses() {
        return ResponseEntity.ok(ApiResponse.ok("All courses fetched", courseService.getAllCourses()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.ok("Course deleted successfully", null));
    }
}
