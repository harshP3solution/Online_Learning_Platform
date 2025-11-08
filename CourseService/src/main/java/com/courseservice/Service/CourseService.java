package com.courseservice.Service;

import com.courseservice.Controller.CourseController;
import com.courseservice.event.CourseCreateEvent;
import com.persistence.DTO.CourseRequestDTO;
import com.persistence.DTO.CourseDTO;
import com.persistence.Entity.Course;
import com.persistence.Entity.User;
import com.persistence.Repository.CourseRepository;
import com.persistence.Repository.UserRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepo userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    // 🔍 SEARCH COURSES (CriteriaBuilder + HATEOAS)
    public List<EntityModel<CourseDTO>> searchCourses(String title, String category, Long instructorId) {
        String titleKey = (title == null ? "" : title.trim().toLowerCase(Locale.ROOT));
        String categoryKey = (category == null ? "" : category.trim().toLowerCase(Locale.ROOT));

        String titlePattern = "%" + titleKey + "%";
        String categoryPattern = "%" + categoryKey + "%";

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Course> cq = cb.createQuery(Course.class);
        Root<Course> root = cq.from(Course.class);

        List<Predicate> predicates = new ArrayList<>();

        if (!titleKey.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("title")), titlePattern));
        }
        if (!categoryKey.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("category")), categoryPattern));
        }
        if (instructorId != null) {
            predicates.add(cb.equal(root.get("instructor").get("id"), instructorId));
        }

        cq.select(root).where(cb.and(predicates.toArray(new Predicate[0])));
        List<Course> results = entityManager.createQuery(cq).getResultList();

        return results.stream()
                .map(this::mapToDTO)
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    // 🟢 CREATE COURSE (Kafka + HATEOAS)
    public EntityModel<CourseDTO> createCourse(CourseRequestDTO dto) {
        User instructor = userRepository.findById(dto.getInstructorId())
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        Course course = Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .instructor(instructor)
                .build();

        Course saved = courseRepository.save(course);

        // Send Kafka event
        CourseCreateEvent event = CourseCreateEvent.builder()
                .courseId(saved.getId())
                .title(saved.getTitle())
                .category(saved.getCategory())
                .instructorId(saved.getInstructor().getId())
                .build();

        kafkaTemplate.send("course-created-topic", event);

        return toModel(mapToDTO(saved));
    }

    // 🟠 UPDATE COURSE (Role-based + HATEOAS)
    @Transactional
    public EntityModel<CourseDTO> updateCourse(Long id, CourseRequestDTO dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        boolean isAdmin = currentUser.getRole().equalsIgnoreCase("ADMIN");
        boolean isInstructor = currentUser.getRole().equalsIgnoreCase("INSTRUCTOR");

        if (isInstructor && !course.getInstructor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to update this course.");
        }

        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setCategory(dto.getCategory());

        if (isAdmin && dto.getInstructorId() != null) {
            User instructor = userRepository.findById(dto.getInstructorId())
                    .orElseThrow(() -> new RuntimeException("Instructor not found with id " + dto.getInstructorId()));
            course.setInstructor(instructor);
        }

        Course updated = courseRepository.save(course);
        return toModel(mapToDTO(updated));
    }

    // 🔵 GET COURSE BY ID
    public EntityModel<CourseDTO> getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id " + id));
        return toModel(mapToDTO(course));
    }

    // 🟣 GET ALL COURSES
    public CollectionModel<EntityModel<CourseDTO>> getAllCourses() {
        List<EntityModel<CourseDTO>> courseModels = courseRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .map(this::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(courseModels,
                linkTo(methodOn(CourseController.class).getAllCourses()).withSelfRel());
    }

    // 🔴 DELETE COURSE
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found with id " + id);
        }
        courseRepository.deleteById(id);
    }

    // 🧩 Map Entity → DTO
    private CourseDTO mapToDTO(Course course) {
        if (course == null) return null;

        return CourseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .instructor(course.getInstructor() != null
                        ? CourseDTO.InstructorDTO.builder()
                        .id(course.getInstructor().getId())
                        .fullName(course.getInstructor().getFullName())
                        .email(course.getInstructor().getEmail())
                        .role(String.valueOf(course.getInstructor().getRole()))
                        .build()
                        : null)
                .build();
    }

    // 🔗 Convert DTO → HATEOAS Model (✅ Unidirectional)
    private EntityModel<CourseDTO> toModel(CourseDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(CourseController.class).getCourseById(dto.getId())).withSelfRel(),
                linkTo(methodOn(CourseController.class).updateCourse(dto.getId(), null)).withRel("update"),
                linkTo(methodOn(CourseController.class).deleteCourse(dto.getId())).withRel("delete"),
                linkTo(methodOn(CourseController.class).getAllCourses()).withRel("all-courses")
        );
    }
}
