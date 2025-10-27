package com.courseservice.Service;


import com.persistence.Entity.Lesson;
import com.persistence.Entity.Course;
import com.persistence.Repository.LessonRepository;
import com.persistence.Repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    public LessonService(LessonRepository lessonRepository, CourseRepository courseRepository) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
    }

    public Lesson addLessonToCourse(Long courseId, Lesson lesson) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id " + courseId));
        lesson.setCourse(course);
        return lessonRepository.save(lesson);
    }

    public List<Lesson> getLessonsByCourse(Long courseId) {
        return lessonRepository.findByCourseId(courseId);
    }

    public Lesson updateLesson(Long lessonId, Lesson updatedLesson) {
        return lessonRepository.findById(lessonId).map(lesson -> {
            lesson.setTitle(updatedLesson.getTitle());
            lesson.setContent(updatedLesson.getContent());


            return lessonRepository.save(lesson);
        }).orElseThrow(() -> new RuntimeException("Lesson not found with id " + lessonId));
    }

    public void deleteLesson(Long lessonId) {
        lessonRepository.deleteById(lessonId);
    }

    public Lesson getLessonById(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id " + lessonId));
    }
}

