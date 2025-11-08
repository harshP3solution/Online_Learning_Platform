package com.courseservice.Service;

import com.courseservice.Controller.LessonController;
import com.persistence.DTO.LessonRequestDTO;
import com.persistence.Entity.Lesson;
import com.persistence.Entity.Course;
import com.persistence.Entity.User;
import com.persistence.Repository.LessonRepository;
import com.persistence.Repository.CourseRepository;
import com.persistence.Repository.UserRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserRepo userRepo;

    public LessonService(LessonRepository lessonRepository, CourseRepository courseRepository, UserRepo userRepo) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.userRepo = userRepo;
    }

    // 🟢 Add Lesson (HATEOAS)
    public EntityModel<Lesson> addLessonToCourse(Long courseId, Lesson lesson) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id " + courseId));
        lesson.setCourse(course);
        Lesson saved = lessonRepository.save(lesson);

        return toModel(saved);
    }

    // 🔵 Get Lessons by Course (HATEOAS)
    public CollectionModel<EntityModel<Lesson>> getLessonsByCourse(Long courseId) {
        List<EntityModel<Lesson>> lessonModels = lessonRepository.findByCourseId(courseId)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(
                lessonModels,
                linkTo(methodOn(LessonController.class).getLessonsByCourse(courseId)).withSelfRel()
        );
    }




    // 🟠 Update Lesson (HATEOAS)
    @Transactional
    public EntityModel<Lesson> updateLesson(Long lessonId, @Valid LessonRequestDTO updatedLesson) {
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id " + lessonId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        boolean isAdmin = currentUser.getRole().equalsIgnoreCase("ADMIN");
        boolean isInstructor = currentUser.getRole().equalsIgnoreCase("INSTRUCTOR");

        if (isInstructor && !existingLesson.getInstructor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to update this lesson.");
        }

        existingLesson.setTitle(updatedLesson.getTitle());
        existingLesson.setContent(updatedLesson.getContent());

        if (isAdmin) {
            if (updatedLesson.getCourseId() != null) {
                Course course = courseRepository.findById(updatedLesson.getCourseId())
                        .orElseThrow(() -> new RuntimeException("Course not found with id " + updatedLesson.getCourseId()));
                existingLesson.setCourse(course);
            }

            if (updatedLesson.getInstructorId() != null) {
                User instructor = userRepo.findById(updatedLesson.getInstructorId())
                        .orElseThrow(() -> new RuntimeException("Instructor not found with id " + updatedLesson.getInstructorId()));
                existingLesson.setInstructor(instructor);
            }
        }

        Lesson updated = lessonRepository.save(existingLesson);
        return toModel(updated);
    }

    // 🔴 Delete Lesson (HATEOAS)
    public void deleteLesson(Long lessonId) {
        lessonRepository.deleteById(lessonId);
    }

    // 🟣 Get Lesson by ID (HATEOAS)
    public EntityModel<Lesson> getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id " + lessonId));
        return toModel(lesson);
    }

    // 🔗 HATEOAS Link Builder
    private EntityModel<Lesson> toModel(Lesson lesson) {
        return EntityModel.of(lesson,
                linkTo(methodOn(LessonController.class).getLessonById(lesson.getId())).withSelfRel(),
                linkTo(methodOn(LessonController.class).getLessonsByCourse(
                        lesson.getCourse() != null ? lesson.getCourse().getId() : null)).withRel("course-lessons"),
                linkTo(methodOn(LessonController.class).updateLesson(lesson.getId(), null)).withRel("update"),
                linkTo(methodOn(LessonController.class).deleteLesson(lesson.getId())).withRel("delete"));
    }
}
