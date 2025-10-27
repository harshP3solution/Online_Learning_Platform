package com.courseservice.Controller;

import com.courseservice.Service.QuizService;
import com.persistence.DTO.QuizDTO;
import com.persistence.DTO.SubmitRequestDTO;
import com.persistence.DTO.SubmitResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/add-questions")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<String> addCourseQuizQuestions(@RequestBody @Valid QuizDTO dto) {
        quizService.addQuizQuestions(dto);
        return ResponseEntity.ok("Quiz questions added successfully for course");
    }
    @PostMapping("/generate/final")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizDTO> generateFinalQuizAfterCourseCompletion(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        QuizDTO dto = quizService.generateFinalQuizAfterCourseCompletion(studentId, courseId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmitResponseDTO> submitQuiz(@RequestBody @Valid SubmitRequestDTO req) {
        SubmitResponseDTO response = quizService.submitQuiz(req);
        return ResponseEntity.ok(response);
    }
}
