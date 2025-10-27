package com.userservices.Controller;

import com.persistence.DTO.UserResponseDTO;
import com.persistence.Entity.Course;
import com.persistence.Entity.User;
import com.userservices.Service.UserServices;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServices userServices;

    public UserController(UserServices userServices) {
        this.userServices = userServices;
    }

    // ✅ Get all users (as DTOs)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDTO> getAll() {
        return userServices.getAll();
    }

    // ✅ Get user by ID (as DTO)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public UserResponseDTO getById(@PathVariable Long id) {
        return userServices.getById(id);
    }

    // ✅ Register a new user (public)
    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public User register(@RequestBody @Valid User user) {
        return userServices.create(user);
    }

    // ✅ Update user info (still returns entity, since it’s admin/student only)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public User update(@PathVariable Long id, @RequestBody @Valid User user) {
        return userServices.update(id, user);
    }

    // ✅ Delete user (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        userServices.delete(id);
    }

    // ✅ Get all courses for a specific user
    @GetMapping("/{id}/courses")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public List<Course> getAllCoursesForUser(@PathVariable Long id) {
        return userServices.getAllCoursesForUser(id);
    }

    // ✅ Register a user to a course
    @PostMapping("/{userId}/courses/{courseId}/register")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> registerUserToCourse(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        userServices.registerUserToCourse(userId, courseId);
        return ResponseEntity.ok("User registered to course and email sent successfully.");
    }
}
