package com.userservices.Service;

import com.persistence.DTO.UserResponseDTO;
import com.persistence.Entity.Course;
import com.persistence.Entity.User;
import com.persistence.Repository.CourseRepository;
import com.persistence.Repository.UserRepo;
import com.userservices.FeignClient.CourseClient;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServices {

    private final UserRepo userRepo;
    private final CourseRepository courseRepo;
    private final EmailService emailService;

    @Getter
    private final CourseClient courseClient;

    public UserServices(UserRepo userRepo,
                        CourseRepository courseRepo,
                        CourseClient courseClient,
                        EmailService emailService) {
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.courseClient = courseClient;
        this.emailService = emailService;
    }

    // ✅ Return all users as DTOs
    public List<UserResponseDTO> getAll() {
        return userRepo.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    // ✅ Return single user as DTO
    public UserResponseDTO getById(Long id) {
        return userRepo.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    // ✅ Convert Entity → DTO to avoid recursion
    private UserResponseDTO convertToDTO(User user) {
        if (user == null) return null;

        return UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .coursesTaught(
                        user.getCoursesTaught() == null ? List.of() :
                                user.getCoursesTaught().stream()
                                        .map(course -> UserResponseDTO.CourseDTO.builder()
                                                .id(course.getId())
                                                .title(course.getTitle())
                                                .category(course.getCategory())
                                                .build())
                                        .toList()
                )
                .build();
    }

    // Create user
    public User create(User user) {
        return userRepo.save(user);
    }

    //  Update user
    public User update(Long id, User updatedUser) {
        return userRepo.findById(id).map(u -> {
            u.setFullName(updatedUser.getFullName());
            u.setEmail(updatedUser.getEmail());
            u.setPasswordHash(updatedUser.getPasswordHash());
            u.setRole(updatedUser.getRole());
            return userRepo.save(u);
        }).orElse(null);
    }

    // Delete user
    public void delete(Long id) {
        userRepo.deleteById(id);
    }

    // ✅ Get all courses for a user (from Course microservice)
    public List<Course> getAllCoursesForUser(Long userId) {
        return courseClient.getAllCourses();
    }

    // ✅ Register user to a course and send email
    public void registerUserToCourse(Long userId, Long courseId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Add course to user's taught list
        user.getCoursesTaught().add(course);
        userRepo.save(user);

        // Send email notification
        emailService.sendRegistrationEmail(user, course);
    }
}
