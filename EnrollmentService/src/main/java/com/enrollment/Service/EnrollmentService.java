package com.enrollment.Service;

import com.enrollment.event.EnrollmentCreatedEvent;
import com.persistence.DTO.EnrollmentDTO;
import com.persistence.Entity.Course;
import com.persistence.Entity.Enrollment;
import com.persistence.Entity.User;
import com.persistence.Repository.CourseRepository;
import com.persistence.Repository.EnrollmentRepository;
import com.persistence.Repository.UserRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepo userRepo;
    private final CourseRepository courseRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    // 🔍 SEARCH ENROLLMENTS
    public CollectionModel<EntityModel<EnrollmentDTO>> searchEnrollments(Long studentId, Long courseId, Boolean completed, String studentName, String courseTitle) {

        String studentNameKey = (studentName == null ? "" : studentName.trim().toLowerCase(Locale.ROOT));
        String courseTitleKey = (courseTitle == null ? "" : courseTitle.trim().toLowerCase(Locale.ROOT));

        String studentPattern = "%" + studentNameKey + "%";
        String coursePattern = "%" + courseTitleKey + "%";

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Enrollment> cq = cb.createQuery(Enrollment.class);
        Root<Enrollment> root = cq.from(Enrollment.class);

        List<Predicate> predicates = new ArrayList<>();

        if (studentId != null) {
            predicates.add(cb.equal(root.get("student").get("id"), studentId));
        }

        if (courseId != null) {
            predicates.add(cb.equal(root.get("course").get("id"), courseId));
        }

        if (completed != null) {
            predicates.add(cb.equal(root.get("completed"), completed));
        }

        if (!studentNameKey.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("student").get("fullName")), studentPattern));
        }

        if (!courseTitleKey.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("course").get("title")), coursePattern));
        }

        cq.select(root).where(cb.and(predicates.toArray(new Predicate[0])));
        List<Enrollment> results = entityManager.createQuery(cq).getResultList();

        List<EntityModel<EnrollmentDTO>> enrollments = results.stream()
                .map(this::mapToDTO)
                .map(this::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(enrollments,
                linkTo(methodOn(com.enrollment.Controller.EnrollmentController.class)
                        .searchEnrollments(studentId, courseId, completed, studentName, courseTitle))
                        .withSelfRel());
    }

    // 🧑‍🎓 ENROLL A STUDENT IN A COURSE
    public EntityModel<EnrollmentDTO> enrollStudent(Long studentId, Long courseId) {
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

        // ✅ Kafka event (unchanged)
        EnrollmentCreatedEvent event = EnrollmentCreatedEvent.builder()
                .enrollmentId(saved.getId())
                .studentId(student.getId())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .studentEmail(student.getEmail())
                .build();
        kafkaTemplate.send("enrollment-created-topic", event);
        log.info("✅ Enrollment event sent: {}", event.getMessage());

        return toModel(mapToDTO(saved));
    }

    // 🧩 PRIVATE DTO MAPPER (UNCHANGED)
    private EnrollmentDTO mapToDTO(Enrollment enrollment) {
        return EnrollmentDTO.builder()
                .id(enrollment.getId())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .progress(enrollment.getProgress())
                .completed(enrollment.getCompleted())
                .student(EnrollmentDTO.StudentDTO.builder()
                        .id(enrollment.getStudent().getId())
                        .fullName(enrollment.getStudent().getFullName())
                        .email(enrollment.getStudent().getEmail())
                        .build())
                .course(EnrollmentDTO.CourseDTO.builder()
                        .id(enrollment.getCourse().getId())
                        .title(enrollment.getCourse().getTitle())
                        .category(enrollment.getCourse().getCategory())
                        .build())
                .build();
    }

    // 🧭 HATEOAS Link Wrapper
    private EntityModel<EnrollmentDTO> toModel(EnrollmentDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(com.enrollment.Controller.EnrollmentController.class)
                        .getEnrollmentById(dto.getId())).withSelfRel(),
                linkTo(methodOn(com.enrollment.Controller.EnrollmentController.class)
                        .getEnrollmentsByStudent(dto.getStudent().getId())).withRel("student-enrollments"),
                linkTo(methodOn(com.enrollment.Controller.EnrollmentController.class)
                        .getEnrollmentsByCourse(dto.getCourse().getId())).withRel("course-enrollments"));
    }

    // 📚 GET ENROLLMENTS BY STUDENT
    public CollectionModel<EntityModel<EnrollmentDTO>> getEnrollmentsByStudent(Long studentId) {
        User student = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        List<EntityModel<EnrollmentDTO>> list = enrollmentRepository.findByStudent(student)
                .stream()
                .map(this::mapToDTO)
                .map(this::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(list,
                linkTo(methodOn(com.enrollment.Controller.EnrollmentController.class)
                        .getEnrollmentsByStudent(studentId)).withSelfRel());
    }

    // 📘 GET ENROLLMENTS BY COURSE
    public CollectionModel<EntityModel<EnrollmentDTO>> getEnrollmentsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        List<EntityModel<EnrollmentDTO>> list = enrollmentRepository.findByCourse(course)
                .stream()
                .map(this::mapToDTO)
                .map(this::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(list,
                linkTo(methodOn(com.enrollment.Controller.EnrollmentController.class)
                        .getEnrollmentsByCourse(courseId)).withSelfRel());
    }
}
