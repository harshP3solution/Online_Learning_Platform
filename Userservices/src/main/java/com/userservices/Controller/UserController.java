package com.userservices.Controller;

import com.persistence.DTO.ApiResponse;
import com.persistence.DTO.UserResponseDTO;
import com.persistence.Entity.Course;
import com.persistence.Entity.User;
import com.userservices.Service.UserServices;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServices userServices;
    public UserController(UserServices userServices) { this.userServices = userServices; }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("All users fetched", userServices.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getById(@PathVariable Long id) {
        UserResponseDTO dto = userServices.getById(id);
        if (dto == null) throw new NoSuchElementException("User not found with ID " + id);
        return ResponseEntity.ok(ApiResponse.ok("User fetched successfully", dto));
    }

    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody @Valid User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", userServices.create(user)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<User>> update(@PathVariable Long id, @RequestBody @Valid User user) {
        return ResponseEntity.ok(ApiResponse.ok("User updated successfully", userServices.update(id, user)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userServices.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted successfully", null));
    }

    @GetMapping("/{id}/courses")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Course>>> getAllCoursesForUser(@PathVariable Long id) {
        List<Course> list = userServices.getAllCoursesForUser(id);
        return ResponseEntity.ok(ApiResponse.ok("Courses for user fetched successfully", list));
    }

    @PostMapping("/{userId}/courses/{courseId}/register")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<String>> registerUserToCourse(@PathVariable Long userId, @PathVariable Long courseId) {
        userServices.registerUserToCourse(userId, courseId);
        return ResponseEntity.ok(ApiResponse.ok("User registered to course and email sent successfully", null));
    }
}
