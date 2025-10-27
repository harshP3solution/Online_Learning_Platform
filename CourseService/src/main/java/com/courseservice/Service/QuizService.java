package com.courseservice.Service;

import com.persistence.DTO.QuizDTO;
import com.persistence.DTO.QuizQuestionDTO;
import com.persistence.DTO.SubmitRequestDTO;
import com.persistence.DTO.SubmitResponseDTO;
import com.persistence.Entity.*;
import com.persistence.Repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public void addQuizQuestions(QuizDTO dto) {
        // Fetch the course (since we now add quiz per course)
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new NoSuchElementException("Course not found: " + dto.getCourseId()));

        // Create quiz
        Quiz quiz = Quiz.builder()
                .course(course)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .totalMarks(dto.getTotalMarks())
                .createdAt(LocalDateTime.now())
                .build();
        quizRepository.save(quiz);

        // Save each question and link to quiz + course
        for (QuizQuestionDTO q : dto.getQuestions()) {
            QuizQuestion question = QuizQuestion.builder()
                    .quiz(quiz)
                    .course(course)
                    .questionText(q.getQuestionText())
                    .optionsJson(q.getOptionsJson())
                   // .correctAnswer(q.getCorrectAnswer())
                    .marks(q.getMarks() == null ? 1 : q.getMarks())
                    .build();
            quizQuestionRepository.save(question);
        }
    }

 //GENERATE FINAL QUIZ AFTER COURSE COMPLETION

    @Transactional
    public QuizDTO generateFinalQuizAfterCourseCompletion(Long studentId, Long courseId) {
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

        // ✅ FIX: clone questions properly
        for (QuizQuestion q : selected) {
            QuizQuestion copy = QuizQuestion.builder()
                    .quiz(quiz)
                    .course(course)
                    .questionText(q.getQuestionText())
                    .optionsJson(q.getOptionsJson())
                    .correctAnswer(q.getCorrectAnswer()) // ✅ carry correct answer
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

        return QuizDTO.builder()
                .id(quiz.getId())
                .courseId(course.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .totalMarks(totalMarks)
                .questions(questionDTOs)
                .build();
    }


    // --------------------------------------------------------------------------------------------
    // ✅ SUBMIT QUIZ ANSWERS
    // --------------------------------------------------------------------------------------------
    @Transactional
    public SubmitResponseDTO submitQuiz(SubmitRequestDTO req) {
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

        return SubmitResponseDTO.builder()
                .submissionId(submission.getId())
                .score(score)
                .totalMarks(totalMarks)
                .percentage(percentage)
                .passed(passed)
                .message("Quiz submitted successfully")
                .build();
    }
//    @Transactional
//    public SubmitResponseDTO submitQuiz(SubmitRequestDTO req) {
//        Quiz quiz = quizRepository.findById(req.getQuizId())
//                .orElseThrow(() -> new NoSuchElementException("Quiz not found: " + req.getQuizId()));
//
//        User student = userRepo.findById(req.getStudentId())
//                .orElseThrow(() -> new NoSuchElementException("Student not found: " + req.getStudentId()));
//
//        Map<Long, String> answers = Optional.ofNullable(req.getAnswers()).orElse(Map.of());
//        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quiz.getId());
//
//        int totalMarks = 0;
//        int score = 0;
//
//        System.out.println("\n=== QUIZ SUBMISSION DEBUG START ===");
//        System.out.println("Quiz ID: " + quiz.getId() + " | Student ID: " + student.getId());
//        System.out.println("------------------------------------");
//
//        for (QuizQuestion q : questions) {
//            int marks = Optional.ofNullable(q.getMarks()).orElse(1);
//            totalMarks += marks;
//
//            String correct = q.getCorrectAnswer();
//            String given = answers.get(q.getId());
//
//            System.out.println("QID: " + q.getId() +
//                    " | Given: [" + given + "] | Correct: [" + correct + "]");
//
//            if (answersMatch(given, correct)) {
//                score += marks;
//                System.out.println("✅ Matched QID " + q.getId() + " | +" + marks + " marks");
//            } else {
//                System.out.println("❌ Wrong QID " + q.getId());
//            }
//        }
//
//        System.out.println("------------------------------------");
//        System.out.println("Final Score: " + score + "/" + totalMarks);
//        System.out.println("=== QUIZ SUBMISSION DEBUG END ===\n");
//
//        double percentage = totalMarks == 0 ? 0 : (score * 100.0 / totalMarks);
//        boolean passed = percentage >= 50.0;
//
//        QuizSubmission submission = QuizSubmission.builder()
//                .quiz(quiz)
//                .student(student)
//                .answersJson(convertMapToJson(answers))
//                .score(score)
//                .submittedAt(LocalDateTime.now())
//                .build();
//
//        submission = quizSubmissionRepository.save(submission);
//
//        return SubmitResponseDTO.builder()
//                .submissionId(submission.getId())
//                .score(score)
//                .totalMarks(totalMarks)
//                .percentage(percentage)
//                .passed(passed)
//                .message("Quiz submitted successfully")
//                .build();
//    }
//    private boolean answersMatch(String given, String correct) {
//        if (given == null || correct == null) return false;
//
//        // Normalize text: remove quotes, spaces, HTML-like symbols
//        String normalizedGiven = given
//                .replaceAll("[\"\\s]+", "")
//                .replace("&lt;", "<")
//                .replace("&gt;", ">")
//                .trim();
//
//        String normalizedCorrect = correct
//                .replaceAll("[\"\\s]+", "")
//                .replace("&lt;", "<")
//                .replace("&gt;", ">")
//                .trim();
//
//        return normalizedGiven.equalsIgnoreCase(normalizedCorrect);
//    }


    private String convertMapToJson(Map<Long, String> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }
}
