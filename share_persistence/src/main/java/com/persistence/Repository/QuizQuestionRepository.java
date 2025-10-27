package com.persistence.Repository;

// package com.persistence.Repository;

import com.persistence.Entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByQuizId(Long quizId);

    List<QuizQuestion> findByCourseId(Long courseId);
}

