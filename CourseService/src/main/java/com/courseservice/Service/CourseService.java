package com.courseservice.Service;

import com.courseservice.event.CourseCreateEvent;
import com.persistence.DTO.CourseRequestDTO;
import com.persistence.DTO.CourseResponseDTO;
import com.persistence.Entity.Course;
import com.persistence.Entity.User;
import com.persistence.Repository.CourseRepository;

import com.persistence.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepo userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Create a course
    public CourseResponseDTO createCourse(CourseRequestDTO dto) {
        // Validate instructor
        User instructor = userRepository.findById(dto.getInstructorId())
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        // Build course entity
        Course course = Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .instructor(instructor)
                .build();

        // Save to database
        Course saved = courseRepository.save(course);

        // Send Kafka event
        CourseCreateEvent event = CourseCreateEvent.builder()
                .courseId(saved.getId())
                .title(saved.getTitle())
                .category(saved.getCategory())
                .instructorId(saved.getInstructor().getId())
                .build();
        kafkaTemplate.send("course-created-topic", event);

        // Return DTO
        return mapToDTO(saved);
    }

    // Update a course
    public CourseResponseDTO updateCourse(Long id, CourseRequestDTO dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User instructor = userRepository.findById(dto.getInstructorId())
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setCategory(dto.getCategory());
        course.setInstructor(instructor);

        Course updated = courseRepository.save(course);

        return mapToDTO(updated);
    }

    // Delete a course
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found with id " + id);
        }
        courseRepository.deleteById(id);
    }

    // Get a course by ID
    public CourseResponseDTO getCourseById(Long id) {
        return courseRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    // Get all courses
    public List<CourseResponseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Mapper: Entity -> DTO
    private CourseResponseDTO mapToDTO(Course course) {
        return CourseResponseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .instructor(CourseResponseDTO.InstructorDTO.builder()
                        .id(course.getInstructor().getId())
                        .fullName(course.getInstructor().getFullName())
                        .email(course.getInstructor().getEmail())
                        .role(String.valueOf(course.getInstructor().getRole()))
                        .build())
                .build();
    }
}
