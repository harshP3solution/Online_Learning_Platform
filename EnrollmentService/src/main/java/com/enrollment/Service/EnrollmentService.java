package com.enrollment.Service;

import com.enrollment.event.EnrollmentCreatedEvent;
import com.persistence.DTO.EnrollmentResponseDTO;
import com.persistence.Entity.Course;
import com.persistence.Entity.Enrollment;
import com.persistence.Entity.User;
import com.persistence.Repository.CourseRepository;
import com.persistence.Repository.EnrollmentRepository;
import com.persistence.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepo userRepo;
    private final CourseRepository courseRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Enroll a student in a course
    public EnrollmentResponseDTO enrollStudent(Long studentId, Long courseId) {
        User student = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        enrollmentRepository.findByStudentAndCourse(student, course).ifPresent(e -> {
            throw new RuntimeException("Student is already enrolled in this course!");
        });

        Enrollment saved = enrollmentRepository.save(
                Enrollment.builder()
                        .student(student)
                        .course(course)
                        .enrollmentDate(LocalDateTime.now())
                        .progress(0f)
                        .completed(false)
                        .build()
        );

        // Kafka event
        EnrollmentCreatedEvent event = EnrollmentCreatedEvent.builder()
                .enrollmentId(saved.getId())
                .studentId(student.getId())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .studentEmail(student.getEmail())
                .build();
        kafkaTemplate.send("enrollment-created-topic", event);

        return mapToDTO(saved);
    }

    private EnrollmentResponseDTO mapToDTO(Enrollment enrollment) {
        return EnrollmentResponseDTO.builder()
                .id(enrollment.getId())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .progress(enrollment.getProgress())
                .completed(enrollment.getCompleted())
                .student(EnrollmentResponseDTO.StudentDTO.builder()
                        .id(enrollment.getStudent().getId())
                        .fullName(enrollment.getStudent().getFullName())
                        .email(enrollment.getStudent().getEmail())
                        .build())
                .course(EnrollmentResponseDTO.CourseDTO.builder()
                        .id(enrollment.getCourse().getId())
                        .title(enrollment.getCourse().getTitle())
                        .category(enrollment.getCourse().getCategory())
                        .build())
                .build();
    }


    // Get all enrollments of a student
    public List<EnrollmentResponseDTO> getEnrollmentsByStudent(Long studentId) {
        User student = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        return enrollmentRepository.findByStudent(student)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get all enrollments of a course
    public List<EnrollmentResponseDTO> getEnrollmentsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        return enrollmentRepository.findByCourse(course)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}
