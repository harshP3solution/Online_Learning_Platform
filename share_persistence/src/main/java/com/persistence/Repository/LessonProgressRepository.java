package com.persistence.Repository;

import com.persistence.Entity.LessonProgress;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    List<LessonProgress> findByEnrollmentId(Long enrollmentId);

    Optional<LessonProgress> findByEnrollmentIdAndLessonId(Long enrollmentId, Long lessonId);

    long countByEnrollmentIdAndIsCompleteTrue(Long enrollmentId);

    long countByEnrollmentId(Long enrollmentId);

    boolean existsByEnrollmentStudentIdAndLessonIdAndIsCompleteTrue(Long studentId, Long lessonId);
    List<LessonProgress> findByEnrollment_Student_IdAndLesson_Course_IdAndIsCompleteTrue(Long studentId, Long courseId);


}
