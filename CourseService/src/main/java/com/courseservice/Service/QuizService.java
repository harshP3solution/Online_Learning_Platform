package com.courseservice.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persistence.DTO.*;
import com.persistence.Entity.*;
import com.persistence.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserRepo userRepo;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final ObjectMapper objectMapper;

    private static final int DEFAULT_QUIZ_SIZE = 10;

    @Transactional
    public EntityModel<QuizDTO> addQuizQuestions(QuizDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new NoSuchElementException("Course not found: " + dto.getCourseId()));

        Quiz quiz = Quiz.builder()
                .course(course)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .totalMarks(dto.getTotalMarks())
                .createdAt(LocalDateTime.now())
                .build();
        quizRepository.save(quiz);

        for (QuizQuestionDTO q : dto.getQuestions()) {
            QuizQuestion question = QuizQuestion.builder()
                    .quiz(quiz)
                    .course(course)
                    .questionText(q.getQuestionText())
                    .optionsJson(q.getOptionsJson())
                    //.correctAnswer(q.getCorrectAnswer())
                    .marks(q.getMarks() == null ? 1 : q.getMarks())
                    .build();
            quizQuestionRepository.save(question);
        }

        QuizDTO responseDto = QuizDTO.builder()
                .id(quiz.getId())
                .courseId(course.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .totalMarks(quiz.getTotalMarks())
                .build();

        EntityModel<QuizDTO> model = EntityModel.of(responseDto,
                linkTo(methodOn(com.courseservice.Controller.QuizController.class)
                        .getQuizById(quiz.getId())).withSelfRel(),
                linkTo(methodOn(com.courseservice.Controller.QuizController.class)
                        .getAllQuizzesByCourse(course.getId())).withRel("course-quizzes")
        );

        return model;
    }

    @Transactional
    public EntityModel<QuizDTO> generateFinalQuizAfterCourseCompletion(Long studentId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found: " + courseId));

        List<Lesson> lessons = lessonRepository.findByCourseId(courseId);
        if (lessons.isEmpty()) {
            throw new IllegalArgumentException("No lessons found for course " + courseId);
        }

        List<LessonProgress> completed = lessonProgressRepository
                .findByEnrollment_Student_IdAndLesson_Course_IdAndIsCompleteTrue(studentId, courseId);

        if (completed.size() < lessons.size()) {
            throw new IllegalStateException("Course not yet fully completed. Lessons completed: " +
                    completed.size() + "/" + lessons.size());
        }

        List<QuizQuestion> questions = quizQuestionRepository.findByCourseId(courseId);
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("No quiz questions available for this course");
        }

        Collections.shuffle(questions);
        List<QuizQuestion> selected = questions.stream().limit(10).collect(Collectors.toList());

        int totalMarks = selected.stream()
                .mapToInt(q -> q.getMarks() == null ? 1 : q.getMarks())
                .sum();

        Quiz quiz = Quiz.builder()
                .course(course)
                .title("Final Quiz for " + course.getTitle())
                .description("Auto-generated after course completion")
                .totalMarks(totalMarks)
                .createdAt(LocalDateTime.now())
                .build();
        quizRepository.save(quiz);

        for (QuizQuestion q : selected) {
            QuizQuestion copy = QuizQuestion.builder()
                    .quiz(quiz)
                    .course(course)
                    .questionText(q.getQuestionText())
                    .optionsJson(q.getOptionsJson())
                    .correctAnswer(q.getCorrectAnswer())
                    .marks(q.getMarks())
                    .build();
            quizQuestionRepository.save(copy);
        }

        List<QuizQuestionDTO> questionDTOs = selected.stream()
                .map(q -> QuizQuestionDTO.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .optionsJson(q.getOptionsJson())
                        .marks(q.getMarks())
                        .build())
                .collect(Collectors.toList());

        QuizDTO dto = QuizDTO.builder()
                .id(quiz.getId())
                .courseId(course.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .totalMarks(totalMarks)
                .questions(questionDTOs)
                .build();

        EntityModel<QuizDTO> model = EntityModel.of(dto,
                linkTo(methodOn(com.courseservice.Controller.QuizController.class)
                        .getQuizById(quiz.getId())).withSelfRel(),
                linkTo(methodOn(com.courseservice.Controller.QuizController.class)
                        .submitQuiz(null)).withRel("submit-quiz"),
                linkTo(methodOn(com.courseservice.Controller.QuizController.class)
                        .getAllQuizzesByCourse(courseId)).withRel("course-quizzes")
        );

        return model;
    }

    @Transactional
    public EntityModel<SubmitResponseDTO> submitQuiz(SubmitRequestDTO req) {
        Quiz quiz = quizRepository.findById(req.getQuizId())
                .orElseThrow(() -> new NoSuchElementException("Quiz not found: " + req.getQuizId()));

        User student = userRepo.findById(req.getStudentId())
                .orElseThrow(() -> new NoSuchElementException("Student not found: " + req.getStudentId()));

        Map<Long, String> answers = Optional.ofNullable(req.getAnswers()).orElse(Map.of());
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quiz.getId());

        int totalMarks = 0;
        int score = 0;

        for (QuizQuestion q : questions) {
            int marks = Optional.ofNullable(q.getMarks()).orElse(1);
            totalMarks += marks;

            String correct = q.getCorrectAnswer();
            String given = answers.get(q.getId());
            if (given != null && correct != null && given.trim().equalsIgnoreCase(correct.trim())) {
                score += marks;
            }
        }

        double percentage = totalMarks == 0 ? 0 : (score * 100.0 / totalMarks);
        boolean passed = percentage >= 50.0;

        QuizSubmission submission = QuizSubmission.builder()
                .quiz(quiz)
                .student(student)
                .answersJson(convertMapToJson(answers))
                .score(score)
                .submittedAt(LocalDateTime.now())
                .build();

        submission = quizSubmissionRepository.save(submission);

        SubmitResponseDTO response = SubmitResponseDTO.builder()
                .submissionId(submission.getId())
                .score(score)
                .totalMarks(totalMarks)
                .percentage(percentage)
                .passed(passed)
                .message("Quiz submitted successfully")
                .build();

        EntityModel<SubmitResponseDTO> model = EntityModel.of(response,
                linkTo(methodOn(com.courseservice.Controller.QuizController.class)
                        .getSubmissionById(submission.getId())).withSelfRel(),
                linkTo(methodOn(com.courseservice.Controller.QuizController.class)
                        .getQuizById(quiz.getId())).withRel("quiz-details")
        );

        return model;
    }

    private String convertMapToJson(Map<Long, String> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }
}
